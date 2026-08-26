package io.github.nutant.fastrecipesearch;

import com.fast.recipesearch.AbstractRecipeDB;
import com.fast.recipesearch.IntLongMap;
import com.fast.recipesearch.IntMapContainer;
import com.google.common.base.Stopwatch;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class RecipeDB<C extends RecipeInput, T extends Recipe<C>> extends AbstractRecipeDB<IRecipeHolder<T>> {
    private static final Comparator<IRecipeHolder<?>> COMPARATOR = Comparator.comparing(r -> r.self().id());

    private static final ConcurrentHashMap<Class<?>, Boolean> recipeClassCache = new ConcurrentHashMap<>();

    private int maxInputAmount;
    private Reference2ReferenceMap<Item, IntSet> rawHash = new Reference2ReferenceOpenHashMap<>();
    private final Reference2ReferenceMap<Item, int[]> hash = new Reference2ReferenceOpenHashMap<>();

    private RecipeDB() {
    }

    public static <C extends RecipeInput, T extends Recipe<C>> RecipeDB<C, T> create(RecipeType<T> type, Collection<RecipeHolder<T>> rs) {
        Stopwatch watch = Stopwatch.createStarted();
        var db = AbstractRecipeDB.build(new RecipeDB<>(), (Collection<IRecipeHolder<T>>) (Object) rs);
        watch.stop();
        Config.LOGGER.info("Constructed recipe list for {} in {}. {}/{} recipes in the tree.", BuiltInRegistries.RECIPE_TYPE.getKey(type), watch, rs.size() - db.serialRecipes.size(), rs.size());
        return db;
    }

    public Optional<RecipeHolder<T>> get(C inv, Level world) {
        if (this.rootBranch != null) {
            var map = extractIntMap(inv);
            if (!map.isEmpty()) {
                var holder = findAnyMatch(map, map.toIntArray(), getPredicate(map, inv, world));
                if (holder != null) return Optional.of(holder.self());
                return Optional.empty();
            }
        }
        var holder = findInSerial(this.serialRecipes, getPredicate(inv, world));
        if (holder != null) return Optional.of(holder.self());
        return Optional.empty();
    }

    public Stream<RecipeHolder<T>> getAll(C inv, Level world) {
        if (this.rootBranch != null) {
            var map = extractIntMap(inv);
            if (!map.isEmpty()) {
                return search(map, map.toIntArray(), getPredicate(map, inv, world)).stream().map(IRecipeHolder::self);
            }
        }
        return serialRecipes.stream().filter(getPredicate(inv, world)).map(IRecipeHolder::self);
    }

    private Predicate<IRecipeHolder<T>> getPredicate(IntLongMap map, C inv, Level world) {
        if (maxInputAmount > 1) {
            return r -> {
                var c = r.getIntContainer();
                return (c == null || c.match(map)) && r.self().value().matches(inv, world);
            };
        }
        return getPredicate(inv, world);
    }

    private Predicate<IRecipeHolder<T>> getPredicate(C inv, Level world) {
        return r -> r.self().value().matches(inv, world);
    }

    private IntLongMap extractIntMap(C inv) {
        var map = new IntLongMap();
        var size = inv.size();
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
        if (serialRecipes.isEmpty()) return;
        serialRecipes.sort(COMPARATOR);
    }

    @Override
    protected boolean supportsParallel(IRecipeHolder<T> recipe) {
        return false;
    }


    @Override
    protected void setRecipeContainer(IRecipeHolder<T> recipe, IntMapContainer container) {
        recipe.setIntContainer(container);
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
    protected IntLongMap extractIntMap(IRecipeHolder<T> recipe) {
        if (recipe.self().value().isSpecial()) return IntLongMap.EMPTY;
        if (!isSafeRecipeClass(recipe.self().value().getClass(), recipe.self().value().getType())) return IntLongMap.EMPTY;
        var map = new IntLongMap();
        int inputAmount = 0;
        for (Ingredient ingredient : recipe.self().value().placementInfo().ingredients()) {
            if (ingredient.isCustom()) {
                var action = Fastrecipesearch.CUSTOM.get(ingredient.getCustomIngredient().getClass());
                if (action != null) {
                    var set = new IntOpenHashSet();
                    ObjIntConsumer<Item> consumer = (item, hash) -> {
                        set.add(hash);
                        rawHash.computeIfAbsent(item, i -> new IntOpenHashSet()).add(hash);
                    };
                    action.accept(ingredient.getCustomIngredient(), consumer);
                    set.forEach(i -> map.add(i, 1));
                    if (!set.isEmpty()) inputAmount++;
                }
            } else {
                if (ingredient.values.size() == 1) {
                    var item = ingredient.values.get(0).value();
                    if (item != Items.AIR) {
                        var hash = item.hashCode();
                        map.add(hash, 1);
                        inputAmount++;
                        rawHash.computeIfAbsent(item, i -> new IntOpenHashSet()).add(hash);
                    }
                } else {
                    var hash = ingredient.values.hashCode();
                    map.add(hash, 1);
                    inputAmount++;
                    ingredient.values.forEach(h -> rawHash.computeIfAbsent(h.value(), i -> new IntOpenHashSet()).add(hash));
                }
            }
        }
        maxInputAmount = Math.max(maxInputAmount, inputAmount);
        return map;
    }
}
