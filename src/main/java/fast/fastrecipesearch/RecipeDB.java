package fast.fastrecipesearch;

import com.fast.recipesearch.AbstractContainerRecipeDB;
import com.fast.recipesearch.AbstractRecipeDB;
import com.fast.recipesearch.IntLongMap;
import com.google.common.base.Stopwatch;
import it.unimi.dsi.fastutil.Function;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class RecipeDB<C extends Container, T extends Recipe<C>> extends AbstractContainerRecipeDB<RecipeHolder<C, T>> {

    private int maxInputAmount;
    private Reference2ReferenceMap<Item, IntSet> rawHash = new Reference2ReferenceOpenHashMap<>();
    private final Reference2ReferenceMap<Item, int[]> hash = new Reference2ReferenceOpenHashMap<>();

    private RecipeDB(List<Runnable> branchBuilder) {
        super(branchBuilder);
    }

    static <C extends Container, T extends Recipe<C>> RecipeDB<C, T> create(RecipeType<?> type, Map<ResourceLocation, T> rs) {
        Stopwatch watch = Stopwatch.createStarted();
        var db = AbstractRecipeDB.create(rs.entrySet().stream().map(e -> new RecipeHolder<>(e.getKey(), e.getValue())).toList(), RecipeDB::new);
        watch.stop();
        Fastrecipesearch.LOGGER.info("Constructed recipe list for {} in {}. {}/{} recipes in the tree.", BuiltInRegistries.RECIPE_TYPE.getKey(type), watch, rs.size() - db.serialRecipes.size(), rs.size());
        return db;
    }

    RecipeHolder<C, T> get(C inv, Level world) {
        if (this.rootBranch != null) {
            var map = extractIntMap(inv);
            if (!map.isEmpty()) {
                var holder = findAnyMatch(map.toIntArray(), getFunction(map, inv, world));
                return holder;
            }
        }
        return findInSerial(this.serialRecipes, r -> r.recipe.matches(inv, world) ? r : null);
    }

    List<T> getAll(C inv, Level world) {
        var list = new ArrayList<T>();
        if (this.rootBranch != null) {
            var map = extractIntMap(inv);
            if (!map.isEmpty()) {
                search(map.toIntArray(), getFunction(map, inv, world)).forEach(r -> list.add(r.recipe));
                return list;
            }
        }
        searchFallback(r -> r.recipe.matches(inv, world) ? r : null).forEach(r -> list.add(r.recipe));
        return list;
    }

    private Function<RecipeHolder<C, T>, RecipeHolder<C, T>> getFunction(IntLongMap map, C inv, Level world) {
        if (maxInputAmount > 1) {
            return o -> {
                var r = (RecipeHolder<C, T>) o;
                var c = getRecipeContainer(r);
                if ((c == null || c.match(map)) && r.recipe.matches(inv, world)) return r;
                return null;
            };
        }
        return o -> {
            var r = (RecipeHolder<C, T>) o;
            return r.recipe.matches(inv, world) ? r : null;
        };
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
    public void build(List<Runnable> branchBuilder) {
        super.build(branchBuilder);
        rawHash.forEach((k, v) -> hash.put(k, v.toIntArray()));
        rawHash = null;
    }

    @Override
    protected boolean supportsParallel(RecipeHolder<C, T> recipe) {
        return false;
    }

    @Override
    protected IntLongMap extractIntMap(RecipeHolder<C, T> recipe) {
        var map = new IntLongMap();
        int inputAmount = 0;
        for (Ingredient ingredient : recipe.recipe.getIngredients()) {
            if (ingredient.isVanilla() && ingredient.values.length == 1) {
                if (ingredient.values[0] instanceof Ingredient.ItemValue itemValue) {
                    var item = itemValue.item.getItem();
                    if (item != Items.AIR) {
                        var hash = item.hashCode();
                        map.add(hash, 1);
                        inputAmount++;
                        rawHash.computeIfAbsent(item, i -> new IntOpenHashSet()).add(hash);
                    }
                } else if (ingredient.values[0] instanceof Ingredient.TagValue tagValue) {
                    var o = BuiltInRegistries.ITEM.getTag(tagValue.tag).orElse(null);
                    if (o != null) {
                        var hash = tagValue.tag.hashCode();
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
