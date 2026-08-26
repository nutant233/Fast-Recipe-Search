package io.github.nutant.fastrecipesearch;

import com.fast.recipesearch.AbstractContainerRecipeDB;
import com.fast.recipesearch.IntLongMap;
import com.google.common.base.Stopwatch;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ObjIntConsumer;

class RecipeDB<C extends RecipeInput, T extends Recipe<C>> extends AbstractContainerRecipeDB<IRecipeHolder<T>> {

    private static final ConcurrentHashMap<Class<?>, Boolean> recipeClassCache = new ConcurrentHashMap<>();

    private int maxInputAmount;
    private Reference2ReferenceMap<Item, IntSet> rawHash = new Reference2ReferenceOpenHashMap<>();
    private final Reference2ReferenceMap<Item, int[]> hash = new Reference2ReferenceOpenHashMap<>();

    private RecipeDB(List<Runnable> branchBuilder) {
        super(branchBuilder);
    }

    static <C extends RecipeInput, T extends Recipe<C>> RecipeDB<C, T> create(RecipeType<T> type, Collection<RecipeEntry<T>> rs) {
        Stopwatch watch = Stopwatch.createStarted();
        var db = AbstractContainerRecipeDB.create((Collection<IRecipeHolder<T>>) (Object) rs, RecipeDB::new);
        watch.stop();
        Fastrecipesearch.LOGGER.info("Constructed recipe list for {} in {}. {}/{} recipes in the tree.", Registries.RECIPE_TYPE.getKey(type), watch, rs.size() - db.serialRecipes.size(), rs.size());
        return db;
    }

    Optional<RecipeEntry<T>> get(C inv, World world) {
        if (this.rootBranch != null) {
            var map = extractIntMap(inv);
            if (!map.isEmpty()) {
                var holder = findAnyMatch(map, map.toIntArray(), getFunction(map, inv, world));
                if (holder != null) return Optional.of(holder.self());
                return Optional.empty();
            }
        }
        var holder = findInSerial(this.serialRecipes, r -> r.self().value().matches(inv, world) ? r : null);
        if (holder != null) return Optional.of(holder.self());
        return Optional.empty();
    }

    List<RecipeEntry<T>> getAll(C inv, World world) {
        var list = new ArrayList<RecipeEntry<T>>();
        if (this.rootBranch != null) {
            var map = extractIntMap(inv);
            if (!map.isEmpty()) {
                search(map, map.toIntArray(), getFunction(map, inv, world)).forEach(r -> list.add(r.self()));
                list.sort(Comparator.comparing((entry) -> entry.value().getResult(world.getRegistryManager()).getTranslationKey()));
                return list;
            }
        }
        searchFallback(r -> r.self().value().matches(inv, world) ? r : null).forEach(r -> list.add(r.self()));
        list.sort(Comparator.comparing((entry) -> entry.value().getResult(world.getRegistryManager()).getTranslationKey()));
        return list;
    }

    private Function<IRecipeHolder<T>, IRecipeHolder<T>> getFunction(IntLongMap map, C inv, World world) {
        if (maxInputAmount > 1) {
            return o -> {
                var r = (IRecipeHolder<T>) o;
                var c = r.getIntContainer();
                if ((c == null || c.match(map)) && r.self().value().matches(inv, world)) return r;
                return null;
            };
        }
        return o -> {
            var r = (IRecipeHolder<T>) o;
            return r.self().value().matches(inv, world) ? r : null;
        };
    }

    private IntLongMap extractIntMap(C inv) {
        var map = new IntLongMap();
        var size = inv.getSize();
        for (int i = 0; i < size; i++) {
            var item = inv.getStackInSlot(i).getItem();
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
    public void build(List<Runnable> branchBuilder) {
        super.build(branchBuilder);
        rawHash.forEach((k, v) -> hash.put(k, v.toIntArray()));
        rawHash = null;
    }

    @Override
    protected boolean supportsParallel(IRecipeHolder<T> recipe) {
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
        var typeId = Registries.RECIPE_TYPE.getKey(type);
        if (allowed) {
            Config.LOGGER.info("Recipe class '{}' ({}) will be indexed (recipe_class_mode={})", name, typeId, Config.recipeClassMode.name().toLowerCase());
        } else {
            Config.LOGGER.info("Recipe class '{}' ({}) will not be indexed (recipe_class_mode={})", name, typeId, Config.recipeClassMode.name().toLowerCase());
        }
        return allowed;
    }

    private static boolean isVanillaRecipeClass(String name) {
        return name.startsWith("net.minecraft.recipe.");
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
    protected IntLongMap extractIntMap(IRecipeHolder<T> recipe) {
        var map = new IntLongMap();
        if (!isSafeRecipeClass(recipe.self().value().getClass(), recipe.self().value().getType())) return map;
        int inputAmount = 0;
        for (Ingredient ingredient : recipe.self().value().getIngredients()) {
            if (ingredient.getClass() == Ingredient.class) {
                if (ingredient.entries.length == 1) {
                    if (ingredient.entries[0] instanceof Ingredient.StackEntry(ItemStack stack)) {
                        var item = stack.getItem();
                        if (item != Items.AIR) {
                            var hash = Registries.ITEM.getId(item).hashCode();
                            map.add(hash, 1);
                            inputAmount++;
                            rawHash.computeIfAbsent(item, i -> new IntOpenHashSet()).add(hash);
                        }
                    } else if (ingredient.entries[0] instanceof Ingredient.TagEntry(TagKey<Item> tag)) {
                        var o = Registries.ITEM.getEntryList(tag).orElse(null);
                        if (o != null) {
                            var hash = tag.id().hashCode();
                            map.add(hash, 1);
                            inputAmount++;
                            o.forEach(h -> rawHash.computeIfAbsent(h.value(), i -> new IntOpenHashSet()).add(hash));
                        }
                    }
                }
            } else {
                var action = Fastrecipesearch.CUSTOM.get(ingredient.getClass());
                if (action != null) {
                    var set = new IntOpenHashSet();
                    ObjIntConsumer<Item> consumer = (item, hash) -> {
                        set.add(hash);
                        rawHash.computeIfAbsent(item, i -> new IntOpenHashSet()).add(hash);
                    };
                    action.accept(ingredient, consumer);
                    set.forEach(i -> map.add(i, 1));
                    if (!set.isEmpty()) inputAmount++;
                }
            }
        }
        maxInputAmount = Math.max(maxInputAmount, inputAmount);
        return map;
    }
}
