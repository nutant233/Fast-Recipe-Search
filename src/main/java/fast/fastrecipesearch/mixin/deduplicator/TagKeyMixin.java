package fast.fastrecipesearch.mixin.deduplicator;

import fast.fastrecipesearch.IIngredientHolder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.stream.Stream;

@Mixin(value = TagKey.class)
public class TagKeyMixin implements IIngredientHolder {

    @Unique
    private Ingredient fastrecipesearch$ingredient;

    @Override
    public Ingredient fastrecipesearch$getIngredient() {
        if (fastrecipesearch$ingredient == null) {
            fastrecipesearch$ingredient = new Ingredient(Stream.of(new Ingredient.TagValue((TagKey) (Object) this)));
        }
        return fastrecipesearch$ingredient;
    }
}
