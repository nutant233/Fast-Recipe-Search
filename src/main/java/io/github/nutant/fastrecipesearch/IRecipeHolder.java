package io.github.nutant.fastrecipesearch;

import com.fast.recipesearch.IntMapContainer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public interface IRecipeHolder<T extends Recipe<?>> {

    default RecipeHolder<T> self() {
        return (RecipeHolder) (Object) this;
    }

    IntMapContainer getIntContainer();

    void setIntContainer(IntMapContainer intMapContainer);
}
