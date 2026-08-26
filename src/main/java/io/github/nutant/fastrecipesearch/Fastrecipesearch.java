package io.github.nutant.fastrecipesearch;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.ObjIntConsumer;

@Mod(Config.MODID)
public class Fastrecipesearch {

    public static boolean DEBUG = false;

    private static final Map<Class, BiConsumer> CUSTOM = new Reference2ReferenceOpenHashMap<>();

    static final boolean polymorph;

    public static BiConsumer<Ingredient, ObjIntConsumer<Item>> getCustomIngredientAction(Class<?> clazz) {
        return CUSTOM.get(clazz);
    }

    public synchronized static <T extends Ingredient> void registerCustomIngredientAction(Class<T> clazz, BiConsumer<T, ObjIntConsumer<Item>> consumer) {
        CUSTOM.put(clazz, consumer);
    }

    static {
        if (DEBUG) new FastSuiteTest();
        registerCustomIngredientAction(PartialNBTIngredient.class, (i, consumer) -> {
            if (i.values.length == 1 && i.values[0] instanceof Ingredient.ItemValue itemValue) {
                var item = itemValue.item.getItem();
                if (item != Items.AIR) {
                    consumer.accept(item, BuiltInRegistries.ITEM.getKey(item).hashCode());
                }
            }
        });
        registerCustomIngredientAction(StrictNBTIngredient.class, (i, consumer) -> {
            if (i.values.length == 1 && i.values[0] instanceof Ingredient.ItemValue itemValue) {
                var item = itemValue.item.getItem();
                if (item != Items.AIR) {
                    consumer.accept(item, BuiltInRegistries.ITEM.getKey(item).hashCode());
                }
            }
        });
        polymorph = FMLLoader.getLoadingModList().getModFileById("polymorph") != null;
    }
}
