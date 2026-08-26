package io.github.nutant.fastrecipesearch;

import com.google.gson.JsonElement;
import io.github.nutant.fastrecipesearch.compat.Polymorph;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RecipeManager extends net.minecraft.world.item.crafting.RecipeManager {

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

    /** Allow/deny set for the current mode, or null when every type is optimized. */
    @Nullable
    private Set<RecipeType<?>> typeFilter;
    private boolean typeFilterAllow;
    private final Set<RecipeType<?>> skippedTypes = ConcurrentHashMap.newKeySet();

    public RecipeManager(HolderLookup.Provider registries) {
        super(registries);
        switch (Config.optimizeTypeMode) {
            case ALL -> {
                typeFilter = null;
                typeFilterAllow = true;
            }
            case VANILLA -> {
                typeFilter = VANILLA_TYPES;
                typeFilterAllow = true;
            }
            case WHITELIST -> {
                typeFilter = resolve(Config.optimizeTypeWhitelist);
                typeFilterAllow = true;
            }
            case BLACKLIST -> {
                typeFilter = resolve(Config.optimizeTypeBlacklist);
                typeFilterAllow = false;
            }
        }
    }

    private boolean shouldOptimize(RecipeType<?> type) {
        if (typeFilter == null || typeFilterAllow == typeFilter.contains(type)) {
            return true;
        }
        if (skippedTypes.add(type)) {
            Config.LOGGER.info("Recipe type '{}' will not be optimized (optimize_type_mode={})", BuiltInRegistries.RECIPE_TYPE.getKey(type), Config.optimizeTypeMode.name().toLowerCase());
        }
        return false;
    }

    private static Set<RecipeType<?>> resolve(Set<String> ids) {
        var set = new ReferenceOpenHashSet<RecipeType<?>>();
        for (String id : ids) {
            var key = ResourceLocation.tryParse(id);
            if (key != null) {
                var type = BuiltInRegistries.RECIPE_TYPE.get(key);
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

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> p_44037_, ResourceManager p_44038_, ProfilerFiller p_44039_) {
        super.apply(p_44037_, p_44038_, p_44039_);
        cachedDBMap.clear();
    }

    @Override
    public void replaceRecipes(Iterable<RecipeHolder<?>> p_44025_) {
        super.replaceRecipes(p_44025_);
        cachedDBMap.clear();
    }

    @Override
    public <C extends RecipeInput, T extends Recipe<C>> @NotNull Optional<RecipeHolder<T>> getRecipeFor(RecipeType<T> type, C input, Level world, @Nullable RecipeHolder<T> lastRecipe) {
        if (!shouldOptimize(type)) {
            return super.getRecipeFor(type, input, world, lastRecipe);
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
    public <C extends RecipeInput, T extends Recipe<C>> @NotNull List<RecipeHolder<T>> getRecipesFor(RecipeType<T> type, C input, Level world) {
        if (!shouldOptimize(type)) {
            return super.getRecipesFor(type, input, world);
        }
        var cachedRecipeList = getDB(type);
        return cachedRecipeList.getAll(input, world);
    }

    @SuppressWarnings("unchecked")
    private <C extends RecipeInput, T extends Recipe<C>> RecipeDB<C, T> getDB(RecipeType<T> type) {
        return (RecipeDB<C, T>) cachedDBMap.computeIfAbsent(type, k -> {
            Config.LOGGER.info("Recipe type '{}' will be optimized (optimize_type_mode={})", BuiltInRegistries.RECIPE_TYPE.getKey(k), Config.optimizeTypeMode.name().toLowerCase());
            return RecipeDB.create(type, byType(type));
        });
    }
}
