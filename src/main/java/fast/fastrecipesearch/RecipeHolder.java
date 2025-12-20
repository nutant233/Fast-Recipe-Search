package fast.fastrecipesearch;

import com.fast.recipesearch.IntContainerHolder;
import com.fast.recipesearch.IntMapContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;

class RecipeHolder<C extends Container, T extends Recipe<C>> implements IntContainerHolder {
    final ResourceLocation id;
    final T recipe;
    IntMapContainer container;

    RecipeHolder(ResourceLocation id, T recipe) {
        this.id = id;
        this.recipe = recipe;
    }

    @Override
    public IntMapContainer getIntContainer() {
        return container;
    }

    @Override
    public void setIntContainer(IntMapContainer intMapContainer) {
        container = intMapContainer;
    }
}
