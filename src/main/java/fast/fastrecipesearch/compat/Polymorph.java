package fast.fastrecipesearch.compat;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.base.IRecipeContext;
import com.illusivesoulworks.polymorph.api.common.capability.IRecipeData;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class Polymorph {

    public static <I extends RecipeInput, T extends Recipe<I>> RecipeHolder<T> getBlockEntityRecipe(RecipeManager manager, RecipeType<T> type, I inventory, Level level) {
        IRecipeData<?> data = (IRecipeData<?>) ((IRecipeContext) manager).polymorph$getContext();
        if (data instanceof BlockEntity blockEntity) {
            data = PolymorphApi.getInstance().getBlockEntityRecipeData(blockEntity);
            if (data != null) {
                return PolymorphApi.getInstance().getRecipeManager().getBlockEntityRecipe(type, inventory, level, blockEntity).orElse(null);
            }
        }
        return null;
    }
}
