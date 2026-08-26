package io.github.nutant.fastrecipesearch.mixin;

import com.fast.recipesearch.IntMapContainer;
import io.github.nutant.fastrecipesearch.IRecipeHolder;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(RecipeHolder.class)
public class RecipeEntryMixin<T extends Recipe<?>> implements IRecipeHolder<T> {

    @Unique
    private IntMapContainer fastRecipeSearch$container;

    @Override
    public IntMapContainer getIntContainer() {
        return fastRecipeSearch$container;
    }

    @Override
    public void setIntContainer(IntMapContainer intMapContainer) {
        fastRecipeSearch$container = intMapContainer;
    }
}
