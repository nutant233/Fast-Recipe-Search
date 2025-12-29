package fast.fastrecipesearch;

import com.google.gson.JsonElement;
import fast.fastrecipesearch.compat.Polymorph;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class RecipeManager extends net.minecraft.world.item.crafting.RecipeManager {

    private final Map<RecipeType<?>, RecipeDB<?, ?>> cachedDBMap = new ConcurrentHashMap<>();

    public RecipeManager(HolderLookup.Provider registries) {
        super(registries);
    }

    public <C extends RecipeInput, T extends Recipe<C>> Optional<RecipeHolder<T>> super_getFirstMatch(RecipeType<T> type, C inv, Level world) {
        return super.getRecipeFor(type, inv, world, (RecipeHolder<T>) null);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> p_44037_, ResourceManager p_44038_, ProfilerFiller p_44039_) {
        super.apply(p_44037_, p_44038_, p_44039_);
        cachedDBMap.clear();
    }

    @Override
    public <C extends RecipeInput, T extends Recipe<C>> @NotNull Optional<RecipeHolder<T>> getRecipeFor(RecipeType<T> type, C input, Level world, @Nullable RecipeHolder<T> lastRecipe) {
        if (Fastrecipesearch.polymorph) {
            var recipe = Polymorph.getBlockEntityRecipe(this, type, input, world);
            if (recipe != null) return Optional.of(recipe);
        }
        if (lastRecipe != null && lastRecipe.value().matches(input, world)) return Optional.of(lastRecipe);
        var cachedRecipeList = getDB(type);
        return cachedRecipeList.get(input, world);
    }

    @Override
    public <C extends RecipeInput, T extends Recipe<C>> @NotNull List<RecipeHolder<T>> getRecipesFor(RecipeType<T> type, C input, Level world) {
        var cachedRecipeList = getDB(type);
        return cachedRecipeList.getAll(input, world);
    }

    @SuppressWarnings("unchecked")
    private <C extends RecipeInput, T extends Recipe<C>> RecipeDB<C, T> getDB(RecipeType<T> type) {
        return (RecipeDB<C, T>) cachedDBMap.computeIfAbsent(type, k -> RecipeDB.create(type, byType(type)));
    }
}
