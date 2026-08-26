package io.github.nutant.fastrecipesearch.compat;

import com.illusivesoulworks.polymorph.common.crafting.RecipeSelection;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class Polymorph {

    public static <T extends Recipe<C>, C extends Container> T getBlockEntityRecipe(RecipeType<T> type, C inventory, Level level, BlockEntity blockEntity) {
        return RecipeSelection.getBlockEntityRecipe(type, inventory, level, blockEntity).orElse(null);
    }
}
