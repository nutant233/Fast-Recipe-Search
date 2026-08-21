package fast.fastrecipesearch;

import com.google.gson.JsonElement;
import fast.fastrecipesearch.compat.Polymorph;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RecipeManager extends net.minecraft.recipe.RecipeManager {

    private static final ReferenceOpenHashSet<RecipeType<?>> VANILLA_TYPES = Util.make(() -> {
        var set = new ReferenceOpenHashSet<RecipeType<?>>();
        set.add(RecipeType.CRAFTING);
        set.add(RecipeType.SMELTING);
        set.add(RecipeType.BLASTING);
        set.add(RecipeType.SMOKING);
        set.add(RecipeType.CAMPFIRE_COOKING);
        set.add(RecipeType.STONECUTTING);
        set.add(RecipeType.SMITHING);
        return set;
    });

    private final Map<RecipeType<?>, RecipeDB<?, ?>> cachedDBMap = new ConcurrentHashMap<>();

    /** Recipe types that take the fast path, or null when all types are optimized. */
    @Nullable
    private final Set<RecipeType<?>> optimizedTypes;

    public RecipeManager(RegistryWrapper.WrapperLookup registries) {
        super(registries);
        optimizedTypes = switch (Config.optimizeMode) {
            case ALL -> null;
            case VANILLA -> VANILLA_TYPES;
            case WHITELIST -> resolve(Config.optimizeWhitelist);
            case BLACKLIST -> resolveComplement(Config.optimizeBlacklist);
        };
    }

    private boolean shouldOptimize(RecipeType<?> type) {
        return optimizedTypes == null || optimizedTypes.contains(type);
    }

    private static Set<RecipeType<?>> resolve(Set<String> ids) {
        var set = new ReferenceOpenHashSet<RecipeType<?>>();
        for (String id : ids) {
            var key = Identifier.tryParse(id);
            if (key != null) {
                var type = Registries.RECIPE_TYPE.get(key);
                if (type != null) {
                    set.add(type);
                } else {
                    Config.LOGGER.warn("Unknown recipe type '{}' in config, ignoring", id);
                }
            } else {
                Config.LOGGER.warn("Invalid recipe type id '{}' in config, ignoring", id);
            }
        }
        return set;
    }

    /** Blacklist mode: optimize every registered recipe type except the listed ones. */
    private static Set<RecipeType<?>> resolveComplement(Set<String> blacklist) {
        var excluded = resolve(blacklist);
        var set = new ReferenceOpenHashSet<RecipeType<?>>();
        for (var key : Registries.RECIPE_TYPE.getIds()) {
            var type = Registries.RECIPE_TYPE.get(key);
            if (type != null && !excluded.contains(type)) {
                set.add(type);
            }
        }
        return set;
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
        if (!shouldOptimize(type)) {
            return super.getFirstMatch(type, input, world, lastRecipe);
        }
        if (Fastrecipesearch.polymorph) {
            var recipe = Polymorph.getBlockEntityRecipe(this, type, input, world);
            if (recipe != null) return Optional.of(recipe);
        }
        if (lastRecipe != null && lastRecipe.value().matches(input, world)) return Optional.of(lastRecipe);
        var cachedRecipeList = getDB(type);
        return cachedRecipeList.get(input, world);
    }

    @Override
    public <C extends RecipeInput, T extends Recipe<C>> @NotNull List<RecipeEntry<T>> getAllMatches(RecipeType<T> type, C input, World world) {
        if (!shouldOptimize(type)) {
            return super.getAllMatches(type, input, world);
        }
        var cachedRecipeList = getDB(type);
        return cachedRecipeList.getAll(input, world);
    }

    @SuppressWarnings("unchecked")
    private <C extends RecipeInput, T extends Recipe<C>> RecipeDB<C, T> getDB(RecipeType<T> type) {
        return (RecipeDB<C, T>) cachedDBMap.computeIfAbsent(type, k -> RecipeDB.create(type, getAllOfType(type)));
    }
}
