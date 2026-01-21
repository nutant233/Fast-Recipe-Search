package fast.fastrecipesearch.mixin.deduplicator;

import fast.fastrecipesearch.IIngredientHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.stream.Stream;

@Mixin(Item.class)
public abstract class ItemMixin implements ItemLike, IIngredientHolder {

    @Unique
    private Ingredient fastrecipesearch$ingredient;

    @Override
    public Ingredient fastrecipesearch$getIngredient() {
        if (fastrecipesearch$ingredient == null) {
            fastrecipesearch$ingredient = new Ingredient(Stream.of(new Ingredient.ItemValue(new ItemStack(this))));
        }
        return fastrecipesearch$ingredient;
    }


}
