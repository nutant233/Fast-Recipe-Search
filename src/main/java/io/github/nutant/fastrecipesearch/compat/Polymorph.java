package io.github.nutant.fastrecipesearch.compat;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.base.IRecipeContext;
import com.illusivesoulworks.polymorph.api.common.capability.IRecipeData;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.world.World;

public class Polymorph {

    public static <I extends RecipeInput, T extends Recipe<I>> RecipeEntry<T> getBlockEntityRecipe(RecipeManager manager, RecipeType<T> type, I inventory, World level) {
        if (((IRecipeContext) manager).polymorph$getContext() instanceof BlockEntity blockEntity) {
            IRecipeData<?> data = PolymorphApi.getInstance().getBlockEntityRecipeData(blockEntity);
            if (data != null) {
                return PolymorphApi.getInstance().getRecipeManager().getBlockEntityRecipe(type, inventory, level, blockEntity).orElse(null);
            }
        }
        return null;
    }
}
