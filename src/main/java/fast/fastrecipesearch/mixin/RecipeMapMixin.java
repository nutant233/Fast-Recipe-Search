package fast.fastrecipesearch.mixin;

import fast.fastrecipesearch.Config;
import fast.fastrecipesearch.Fastrecipesearch;
import fast.fastrecipesearch.IRecipeMap;
import fast.fastrecipesearch.RecipeDB;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Mixin(RecipeMap.class)
public abstract class RecipeMapMixin implements IRecipeMap {

    @Shadow
    public abstract <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> byType(RecipeType<T> type);

    @Unique
    private final Map<RecipeType<?>, RecipeDB<?, ?>> fastRecipeSearch$cachedDBMap = new ConcurrentHashMap<>();

    @Inject(method = "getRecipesFor", at = @At("HEAD"), cancellable = true)
    public <I extends RecipeInput, T extends Recipe<I>> void getRecipesFor(RecipeType<T> type, I container, Level level, CallbackInfoReturnable<Stream<RecipeHolder<T>>> cir) {
        if (container.isEmpty()) {
            cir.setReturnValue(Stream.empty());
        } else {
            if (Config.optimize_only_vanilla && !Fastrecipesearch.VANILLA_TYPES.contains(type)) return;
            cir.setReturnValue(container.isEmpty() ? Stream.empty() : getDB(type).getAll(container, level));
        }
    }

    @Override
    public <C extends RecipeInput, T extends Recipe<C>> RecipeDB<C, T> getDB(RecipeType<T> type) {
        return (RecipeDB<C, T>) fastRecipeSearch$cachedDBMap.computeIfAbsent(type, _ -> RecipeDB.create(type, byType(type)));
    }
}
