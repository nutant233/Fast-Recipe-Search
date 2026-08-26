package io.github.nutant.fastrecipesearch.mixin;

import io.github.nutant.fastrecipesearch.IRecipeMap;
import io.github.nutant.fastrecipesearch.RecipeDB;
import io.github.nutant.fastrecipesearch.RecipeTypeFilter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Mixin(RecipeMap.class)
public abstract class RecipeMapMixin implements IRecipeMap {

    @Shadow
    public abstract <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> byType(RecipeType<T> type);

    @Unique
    private Map<RecipeType<?>, RecipeDB<?, ?>> fastRecipeSearch$cachedDBMap;

    @Unique
    private Set<RecipeType<?>> fastRecipeSearch$optimizedTypes;

    @Unique
    private Set<RecipeType<?>> fastRecipeSearch$getOptimizedTypes() {
        var types = fastRecipeSearch$optimizedTypes;
        if (types == null) {
            types = fastRecipeSearch$optimizedTypes = RecipeTypeFilter.optimizedTypes();
        }
        return types;
    }

    @Inject(method = "getRecipesFor", at = @At("HEAD"), cancellable = true)
    public <I extends RecipeInput, T extends Recipe<I>> void getRecipesFor(RecipeType<T> type, I container, Level level, CallbackInfoReturnable<Stream<RecipeHolder<T>>> cir) {
        if (container.isEmpty()) {
            cir.setReturnValue(Stream.empty());
        } else {
            var optimizedTypes = fastRecipeSearch$getOptimizedTypes();
            if (optimizedTypes == null || optimizedTypes.contains(type)) {
                cir.setReturnValue(getDB(type).getAll(container, level));
            }
        }
    }

    @Override
    public <C extends RecipeInput, T extends Recipe<C>> RecipeDB<C, T> getDB(RecipeType<T> type) {
        var map = fastRecipeSearch$cachedDBMap;
        if (map == null) {
            synchronized (this) {
                map = fastRecipeSearch$cachedDBMap;
                if (map == null) {
                    map = fastRecipeSearch$cachedDBMap = new ConcurrentHashMap<>();
                }
            }
        }
        return (RecipeDB<C, T>) map.computeIfAbsent(type, _ -> RecipeDB.create(type, byType(type)));
    }
}
