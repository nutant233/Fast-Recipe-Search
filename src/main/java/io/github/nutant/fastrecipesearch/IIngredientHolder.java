package io.github.nutant.fastrecipesearch;

import net.minecraft.world.item.crafting.Ingredient;

public interface IIngredientHolder {

    static Ingredient getIngredient(Object o) {
        return ((IIngredientHolder) o).fastrecipesearch$getIngredient();
    }

    Ingredient fastrecipesearch$getIngredient();
}
