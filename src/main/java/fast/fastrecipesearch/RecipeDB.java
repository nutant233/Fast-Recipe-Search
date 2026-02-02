package fast.fastrecipesearch;

import com.fast.recipesearch.AbstractRecipeDB;
import com.fast.recipesearch.IntLongMap;
import com.fast.recipesearch.IntMapContainer;
import com.google.common.base.Stopwatch;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class RecipeDB<C extends RecipeInput, T extends Recipe<C>> extends AbstractRecipeDB<IRecipeHolder<T>> {
    private static final Comparator<IRecipeHolder<?>> COMPARATOR = Comparator.comparing(r -> r.self().id());


    private int maxInputAmount;
    private Reference2ReferenceMap<Item, IntSet> rawHash = new Reference2ReferenceOpenHashMap<>();
    private final Reference2ReferenceMap<Item, int[]> hash = new Reference2ReferenceOpenHashMap<>();

    private RecipeDB() {
    }

    static <C extends RecipeInput, T extends Recipe<C>> RecipeDB<C, T> create(RecipeType<T> type, Collection<RecipeHolder<T>> rs) {
        Stopwatch watch = Stopwatch.createStarted();
        var db = AbstractRecipeDB.build(new RecipeDB<>(), (Collection<IRecipeHolder<T>>) (Object) rs);
        watch.stop();
        Config.LOGGER.info("Constructed recipe list for {} in {}. {}/{} recipes in the tree.", BuiltInRegistries.RECIPE_TYPE.getKey(type), watch, rs.size() - db.serialRecipes.size(), rs.size());
        return db;
    }

    Optional<RecipeHolder<T>> get(C inv, Level world) {
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

    List<RecipeHolder<T>> getAll(C inv, Level world) {
        if (this.rootBranch != null) {
            var map = extractIntMap(inv);
            if (!map.isEmpty()) {
                return search(map, map.toIntArray(), getPredicate(map, inv, world)).stream().sorted(COMPARATOR).map(IRecipeHolder::self).collect(Collectors.toList());
            }
        }
        return serialRecipes.stream().filter(getPredicate(inv, world)).map(IRecipeHolder::self).collect(Collectors.toList());
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

    @Override
    protected IntLongMap extractIntMap(IRecipeHolder<T> recipe) {
        var map = new IntLongMap();
        int inputAmount = 0;
        for (Ingredient ingredient : recipe.self().value().getIngredients()) {
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
            } else if (ingredient.values.length == 1) {
                if (ingredient.values[0] instanceof Ingredient.ItemValue(ItemStack stack)) {
                    var item = stack.getItem();
                    if (item != Items.AIR) {
                        var hash = BuiltInRegistries.ITEM.getKey(item).hashCode();
                        map.add(hash, 1);
                        inputAmount++;
                        rawHash.computeIfAbsent(item, i -> new IntOpenHashSet()).add(hash);
                    }
                } else if (ingredient.values[0] instanceof Ingredient.TagValue(TagKey<Item> tag)) {
                    var o = BuiltInRegistries.ITEM.getTag(tag).orElse(null);
                    if (o != null) {
                        var hash = tag.location().hashCode();
                        map.add(hash, 1);
                        inputAmount++;
                        o.forEach(h -> rawHash.computeIfAbsent(h.value(), i -> new IntOpenHashSet()).add(hash));
                    }
                }
            }
        }
        maxInputAmount = Math.max(maxInputAmount, inputAmount);
        return map;
    }
}
