package fast.fastrecipesearch;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.ObjIntConsumer;

@Mod(Fastrecipesearch.MODID)
public class Fastrecipesearch {

    public static final String MODID = "fastrecipesearch";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    static final Map<Class, BiConsumer> CUSTOM = new Reference2ReferenceOpenHashMap<>();

    public static <T extends Ingredient> void registerCustom(Class<T> clazz, BiConsumer<T, ObjIntConsumer<Item>> consumer) {
        CUSTOM.put(clazz, consumer);
    }

    static {
        registerCustom(PartialNBTIngredient.class, (i, consumer) -> {
            if (i.values.length == 1 && i.values[0] instanceof Ingredient.ItemValue itemValue) {
                var item = itemValue.item.getItem();
                if (item != Items.AIR) {
                    consumer.accept(item, BuiltInRegistries.ITEM.getKey(item).hashCode());
                }
            }
        });
        registerCustom(StrictNBTIngredient.class, (i, consumer) -> {
            if (i.values.length == 1 && i.values[0] instanceof Ingredient.ItemValue itemValue) {
                var item = itemValue.item.getItem();
                if (item != Items.AIR) {
                    consumer.accept(item, BuiltInRegistries.ITEM.getKey(item).hashCode());
                }
            }
        });
    }
}
