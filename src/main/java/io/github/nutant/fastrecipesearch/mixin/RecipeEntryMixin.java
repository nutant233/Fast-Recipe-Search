package io.github.nutant.fastrecipesearch.mixin;

import com.fast.recipesearch.IntMapContainer;
import io.github.nutant.fastrecipesearch.IRecipeHolder;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(RecipeEntry.class)
public class RecipeEntryMixin<T extends Recipe<?>> implements IRecipeHolder<T> {

    @Unique
    private IntMapContainer container;

    @Override
    public IntMapContainer getIntContainer() {
        return container;
    }

    @Override
    public void setIntContainer(IntMapContainer intMapContainer) {
        container = intMapContainer;
    }
}
