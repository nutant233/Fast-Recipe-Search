package fast.fastrecipesearch;

import com.gto.recipesearch.IngredientTable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;

class RecipeHolder<C extends Container, T extends Recipe<C>> {
    final ResourceLocation id;
    final T recipe;
    IngredientTable container;

    RecipeHolder(ResourceLocation id, T recipe) {
        this.id = id;
        this.recipe = recipe;
    }
}
