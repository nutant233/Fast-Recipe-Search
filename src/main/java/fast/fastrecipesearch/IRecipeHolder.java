package fast.fastrecipesearch;

import com.fast.recipesearch.IntContainerHolder;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public interface IRecipeHolder<T extends Recipe<?>> extends IntContainerHolder {

    default RecipeHolder<T> self() {
        return (RecipeHolder) (Object) this;
    }
}
