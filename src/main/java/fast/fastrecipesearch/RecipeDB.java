package fast.fastrecipesearch;

import com.fast.recipesearch.AbstractContainerRecipeDB;
import com.fast.recipesearch.IntLongMap;
import com.google.common.base.Stopwatch;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

class RecipeDB<C extends RecipeInput, T extends Recipe<C>> extends AbstractContainerRecipeDB<IRecipeHolder<T>> {

    private int maxInputAmount;
    private Reference2ReferenceMap<Item, IntList> hashCode = new Reference2ReferenceOpenHashMap<>();
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

    public Optional<RecipeEntry<T>> get(C inv, World world) {
        if (this.rootBranch != null) {
            var map = extractIntMap(inv);
            if (!map.isEmpty()) {
                var holder = findAnyMatch(map.toIntArray(), getFunction(map, inv, world));
                if (holder != null) return Optional.of(holder.self());
                return Optional.empty();
            }
        }
        var holder = findInSerial(this.serialRecipes, r -> r.self().value().matches(inv, world) ? r : null);
        if (holder != null) return Optional.of(holder.self());
        return Optional.empty();
    }

    public List<RecipeEntry<T>> getAll(C inv, World world) {
        var list = new ArrayList<RecipeEntry<T>>();
        if (this.rootBranch != null) {
            var map = extractIntMap(inv);
            if (!map.isEmpty()) {
                search(map.toIntArray(), getFunction(map, inv, world)).forEach(r -> list.add(r.self()));
                return list;
            }
        }
        searchFallback(r -> r.self().value().matches(inv, world) ? r : null).forEach(r -> list.add(r.self()));
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
        hashCode.forEach((k, v) -> {
            hash.put(k, v.toIntArray());
        });
        hashCode = null;
    }

    @Override
    protected boolean supportsParallel(IRecipeHolder<T> recipe) {
        return false;
    }

    @Override
    protected IntLongMap extractIntMap(IRecipeHolder<T> recipe) {
        var map = new IntLongMap();
        int inputAmount = 0;
        for (Ingredient ingredient : recipe.self().value().getIngredients()) {
            if (ingredient.entries.length == 1) {
                if (ingredient.entries[0] instanceof Ingredient.StackEntry(ItemStack stack)) {
                    var item = stack.getItem();
                    if (item != Items.AIR) {
                        var hash = item.hashCode();
                        map.add(hash, 1);
                        inputAmount++;
                        hashCode.computeIfAbsent(item, i -> new IntArrayList()).add(hash);
                    }
                } else if (ingredient.entries[0] instanceof Ingredient.TagEntry(TagKey<Item> tag)) {
                    var o = Registries.ITEM.getEntryList(tag).orElse(null);
                    if (o != null) {
                        var hash = tag.hashCode();
                        map.add(hash, 1);
                        inputAmount++;
                        o.forEach(h -> hashCode.computeIfAbsent(h.value(), i -> new IntArrayList()).add(hash));
                    }
                }
            }
        }
        maxInputAmount = Math.max(maxInputAmount, inputAmount);
        return map;
    }
}
