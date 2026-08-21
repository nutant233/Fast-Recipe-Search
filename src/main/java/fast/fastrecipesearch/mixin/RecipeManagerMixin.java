package fast.fastrecipesearch.mixin;

import fast.fastrecipesearch.IRecipeMap;
import fast.fastrecipesearch.RecipeTypeFilter;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.Set;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

    @Shadow
    private RecipeMap recipes;

    @Unique
    private final Set<RecipeType<?>> fastRecipeSearch$optimizedTypes = RecipeTypeFilter.optimizedTypes();

    @Inject(method = "getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    public <I extends RecipeInput, T extends Recipe<I>> void getRecipeFor(RecipeType<T> type, I input, Level level, CallbackInfoReturnable<Optional<RecipeHolder<T>>> cir) {
        if (input.isEmpty()) {
            cir.setReturnValue(Optional.empty());
        } else if (fastRecipeSearch$optimizedTypes == null || fastRecipeSearch$optimizedTypes.contains(type)) {
            cir.setReturnValue(((IRecipeMap) recipes).getDB(type).get(input, level));
        } else {
            var recipes = this.recipes.byType(type);
            for (var recipe : recipes) {
                if (recipe.value().matches(input, level)) {
                    cir.setReturnValue(Optional.of(recipe));
                    return;
                }
            }
            cir.setReturnValue(Optional.empty());
        }
    }
}
