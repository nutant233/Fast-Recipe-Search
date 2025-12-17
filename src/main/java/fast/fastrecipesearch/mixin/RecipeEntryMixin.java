package fast.fastrecipesearch.mixin;

import com.fast.recipesearch.IntMapContainer;
import fast.fastrecipesearch.IRecipeHolder;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(RecipeHolder.class)
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
