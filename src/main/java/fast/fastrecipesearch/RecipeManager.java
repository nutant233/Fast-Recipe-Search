package fast.fastrecipesearch;

import com.google.gson.JsonElement;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class RecipeManager extends net.minecraft.recipe.RecipeManager {

    private final Map<RecipeType<?>, RecipeDB<?, ?>> cachedDBMap = new ConcurrentHashMap<>();

    public RecipeManager(RegistryWrapper.WrapperLookup registries) {
        super(registries);
    }

    public <C extends RecipeInput, T extends Recipe<C>> Optional<RecipeEntry<T>> super_getFirstMatch(RecipeType<T> type, C inv, World world) {
        return super.getFirstMatch(type, inv, world, (RecipeEntry<T>) null);
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler) {
        super.apply(map, resourceManager, profiler);
        cachedDBMap.clear();
    }

    @Override
    public <C extends RecipeInput, T extends Recipe<C>> @NotNull Optional<RecipeEntry<T>> getFirstMatch(RecipeType<T> type, C input, World world, @Nullable RecipeEntry<T> lastRecipe) {
        if (lastRecipe != null && lastRecipe.value().matches(input, world)) return Optional.of(lastRecipe);
        var cachedRecipeList = getDB(type);
        return cachedRecipeList.get(input, world);
    }

    @Override
    public <C extends RecipeInput, T extends Recipe<C>> @NotNull List<RecipeEntry<T>> getAllMatches(RecipeType<T> type, C input, World world) {
        var cachedRecipeList = getDB(type);
        return cachedRecipeList.getAll(input, world);
    }

    @SuppressWarnings("unchecked")
    private <C extends RecipeInput, T extends Recipe<C>> RecipeDB<C, T> getDB(RecipeType<T> type) {
        return (RecipeDB<C, T>) cachedDBMap.computeIfAbsent(type, k -> RecipeDB.create(type, getAllOfType(type)));
    }
}
