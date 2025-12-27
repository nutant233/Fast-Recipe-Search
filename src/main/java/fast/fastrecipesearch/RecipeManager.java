package fast.fastrecipesearch;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class RecipeManager extends net.minecraft.world.item.crafting.RecipeManager {

    private final Map<RecipeType<?>, RecipeDB<?, ?>> cachedDBMap = new ConcurrentHashMap<>();

    public RecipeManager(net.minecraftforge.common.crafting.conditions.ICondition.IContext context) {
        super(context);
    }

    public <C extends Container, T extends Recipe<C>> List<T> super_getRecipeFor(RecipeType<T> type, C input, Level world) {
        return super.getRecipesFor(type, input, world);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> p_44037_, ResourceManager p_44038_, ProfilerFiller p_44039_) {
        super.apply(p_44037_, p_44038_, p_44039_);
        cachedDBMap.clear();
    }

    @Override
    public void replaceRecipes(Iterable<Recipe<?>> recipes) {
        super.replaceRecipes(recipes);
        cachedDBMap.clear();
    }

    @Override
    public <C extends Container, T extends Recipe<C>> Optional<T> getRecipeFor(RecipeType<T> type, C input, Level world) {
        var cachedRecipeList = getDB(type);
        var holder = cachedRecipeList.get(input, world);
        if (holder != null) return Optional.of(holder.recipe);
        return Optional.empty();
    }

    @Override
    public <C extends Container, T extends Recipe<C>> Optional<Pair<ResourceLocation, T>> getRecipeFor(RecipeType<T> type, C input, Level world, @Nullable ResourceLocation lastRecipe) {
        Map<ResourceLocation, T> map = this.byType(type);
        if (lastRecipe != null) {
            T t = map.get(lastRecipe);
            if (t != null && t.matches(input, world)) {
                return Optional.of(Pair.of(lastRecipe, t));
            }
        }

        var cachedRecipeList = getDB(type);
        var holder = cachedRecipeList.get(input, world);
        if (holder != null) return Optional.of(Pair.of(holder.id, holder.recipe));
        return Optional.empty();
    }

    @Override
    public <C extends Container, T extends Recipe<C>> List<T> getRecipesFor(RecipeType<T> type, C input, Level world) {
        var cachedRecipeList = getDB(type);
        return cachedRecipeList.getAll(input, world);
    }

    @SuppressWarnings("unchecked")
    private <C extends Container, T extends Recipe<C>> RecipeDB<C, T> getDB(RecipeType<T> type) {
        return (RecipeDB<C, T>) cachedDBMap.computeIfAbsent(type, k -> RecipeDB.create(type, byType(type)));
    }
}
