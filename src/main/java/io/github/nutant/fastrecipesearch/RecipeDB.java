package io.github.nutant.fastrecipesearch;

import com.gto.recipesearch.AbstractRecipeDB;
import com.gto.recipesearch.IntLongMap;
import com.gto.recipesearch.IngredientTable;
import com.google.common.base.Stopwatch;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class RecipeDB<C extends Container, T extends Recipe<C>> extends AbstractRecipeDB<RecipeHolder<C, T>> {

    private static final Comparator<RecipeHolder<?, ?>> COMPARATOR = Comparator.comparing(r -> r.id);
    private static final ConcurrentHashMap<Class<?>, Boolean> recipeClassCache = new ConcurrentHashMap<>();

    private int maxInputAmount;
    private Reference2ReferenceMap<Item, IntSet> rawHash = new Reference2ReferenceOpenHashMap<>();
    private final Reference2ReferenceMap<Item, int[]> hash = new Reference2ReferenceOpenHashMap<>();

    private RecipeDB() {
    }

    static <C extends Container, T extends Recipe<C>> RecipeDB<C, T> create(RecipeType<?> type, Map<ResourceLocation, T> rs) {
        Stopwatch watch = Stopwatch.createStarted();
        var db = AbstractRecipeDB.build(new RecipeDB<>(), rs.entrySet().stream().map(e -> new RecipeHolder<>(e.getKey(), e.getValue())).toList());
        watch.stop();
        Config.LOGGER.info("Constructed recipe list for {} in {}. {}/{} recipes in the tree.", BuiltInRegistries.RECIPE_TYPE.getKey(type), watch, rs.size() - db.unindexedSerial.size(), rs.size());
        return db;
    }

    RecipeHolder<C, T> get(C inv, Level world) {
        if (this.rootBranch != null) {
            var map = extractIntMap(inv);
            if (!map.isEmpty()) {
                return findAnyMatch(map, map.toIntArray(), getPredicate(map, inv, world));
            }
        }
        return findInSerial(this.unindexedSerial, getPredicate(inv, world));
    }

    List<T> getAll(C inv, Level world) {
        if (this.rootBranch != null) {
            var map = extractIntMap(inv);
            if (!map.isEmpty()) {
                return search(map, map.toIntArray(), getPredicate(map, inv, world)).stream().sorted(COMPARATOR).map(r -> r.recipe).collect(Collectors.toList());
            }
        }
        return unindexedSerial.stream().filter(getPredicate(inv, world)).map(r -> r.recipe).collect(Collectors.toList());
    }

    private Predicate<RecipeHolder<C, T>> getPredicate(IntLongMap map, C inv, Level world) {
        if (maxInputAmount > 1) {
            return r -> {
                var c = r.container;
                return (c == null || c.match(map)) && r.recipe.matches(inv, world);
            };
        }
        return getPredicate(inv, world);
    }

    private Predicate<RecipeHolder<C, T>> getPredicate(C inv, Level world) {
        return r -> r.recipe.matches(inv, world);
    }

    private IntLongMap extractIntMap(C inv) {
        var map = new IntLongMap();
        var size = inv.getContainerSize();
        for (int i = 0; i < size; i++) {
            var item = inv.getItem(i).getItem();
            if (item != Items.AIR) {
                var ints = hash.get(item);
                if (ints != null) {
                    for (int hash : ints) map.add(hash, 1);
                }
            }
        }
        return map;
    }

    @Override
    public void finishBuild() {
        super.finishBuild();
        rawHash.forEach((k, v) -> hash.put(k, v.toIntArray()));
        rawHash = null;
        if (unindexedSerial.isEmpty()) return;
        unindexedSerial.sort(COMPARATOR);
    }

    @Override
    protected boolean supportsParallel(RecipeHolder<C, T> recipe) {
        return false;
    }

    private static boolean isSafeRecipeClass(Class<?> clz, RecipeType<?> type) {
        return recipeClassCache.computeIfAbsent(clz, c -> computeSafeRecipeClass(c, type));
    }

    private static boolean computeSafeRecipeClass(Class<?> clz, RecipeType<?> type) {
        String name = clz.getName();
        boolean allowed;
        if (Config.recipeClassMode == Config.OptimizeMode.ALL) {
            allowed = true;
        } else {
            if (isVanillaRecipeClass(name)) {
                allowed = true;
            } else {
                allowed = switch (Config.recipeClassMode) {
                    case BLACKLIST -> !matchesClass(Config.recipeClassBlacklist, name);
                    case VANILLA -> false;
                    case WHITELIST -> matchesClass(Config.recipeClassWhitelist, name);
                    default -> throw new IllegalStateException("Unexpected value: " + Config.recipeClassMode);
                };
            }
        }
        var typeId = BuiltInRegistries.RECIPE_TYPE.getKey(type);
        if (allowed) {
            Config.LOGGER.info("Recipe class '{}' ({}) will be indexed (recipe_class_mode={})", name, typeId, Config.recipeClassMode.name().toLowerCase());
        } else {
            Config.LOGGER.info("Recipe class '{}' ({}) will not be indexed (recipe_class_mode={})", name, typeId, Config.recipeClassMode.name().toLowerCase());
        }
        return allowed;
    }

    private static boolean isVanillaRecipeClass(String name) {
        return name.startsWith("net.minecraft.world.item.crafting.");
    }

    private static boolean matchesClass(Set<String> patterns, String className) {
        for (String pattern : patterns) {
            if (pattern.endsWith(".")) {
                if (className.startsWith(pattern)) return true;
            } else if (className.equals(pattern)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected IntLongMap extractIngredientMap(RecipeHolder<C, T> recipe) {
        if (!isSafeRecipeClass(recipe.recipe.getClass(), recipe.recipe.getType())) return IntLongMap.EMPTY;
        var map = new IntLongMap();
        int inputAmount = 0;
        for (Ingredient ingredient : recipe.recipe.getIngredients()) {
            if (ingredient.isVanilla()) {
                if (ingredient.values.length == 1) {
                    if (ingredient.values[0] instanceof Ingredient.ItemValue itemValue) {
                        var item = itemValue.item.getItem();
                        if (item != Items.AIR) {
                            var hash = BuiltInRegistries.ITEM.getKey(item).hashCode();
                            map.add(hash, 1);
                            inputAmount++;
                            rawHash.computeIfAbsent(item, i -> new IntOpenHashSet()).add(hash);
                        }
                    } else if (ingredient.values[0] instanceof Ingredient.TagValue tagValue) {
                        var o = BuiltInRegistries.ITEM.getTag(tagValue.tag).orElse(null);
                        if (o != null) {
                            var hash = tagValue.tag.location().hashCode();
                            map.add(hash, 1);
                            inputAmount++;
                            o.forEach(h -> rawHash.computeIfAbsent(h.value(), i -> new IntOpenHashSet()).add(hash));
                        }
                    }
                }
            } else {
                var action = Fastrecipesearch.getCustomIngredientAction(ingredient.getClass());
                if (action != null) {
                    var set = new IntOpenHashSet();
                    action.accept(ingredient, (item, hash) -> {
                        set.add(hash);
                        rawHash.computeIfAbsent(item, i -> new IntOpenHashSet()).add(hash);
                    });
                    set.forEach(i -> map.add(i, 1));
                    if (!set.isEmpty()) inputAmount++;
                }
            }
        }
        maxInputAmount = Math.max(maxInputAmount, inputAmount);
        return map;
    }

    @Override
    protected void setIngredientTable(RecipeHolder<C, T> ctRecipeHolder, IngredientTable IngredientTable) {
        ctRecipeHolder.container = IngredientTable;
    }
}
