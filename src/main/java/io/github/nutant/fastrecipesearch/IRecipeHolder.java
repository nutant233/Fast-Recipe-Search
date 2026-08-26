package io.github.nutant.fastrecipesearch;

import com.fast.recipesearch.IntContainerHolder;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;

public interface IRecipeHolder<T extends Recipe<?>> extends IntContainerHolder {

    default RecipeEntry<T> self() {
        return (RecipeEntry) (Object) this;
    }
}
