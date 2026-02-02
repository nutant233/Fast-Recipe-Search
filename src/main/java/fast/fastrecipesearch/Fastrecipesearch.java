package fast.fastrecipesearch;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.crafting.BlockTagIngredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.ObjIntConsumer;

@Mod(Config.MODID)
public class Fastrecipesearch {

    static final Map<Class, BiConsumer> CUSTOM = new Reference2ReferenceOpenHashMap<>();

    static final boolean polymorph;

    public static <T extends ICustomIngredient> void registerCustom(Class<T> clazz, BiConsumer<T, ObjIntConsumer<Item>> consumer) {
        CUSTOM.put(clazz, consumer);
    }

    static {
        registerCustom(BlockTagIngredient.class, (i, consumer) -> {
            var o = BuiltInRegistries.BLOCK.getTag(i.getTag()).orElse(null);
            if (o != null) {
                var hash = i.getTag().location().hashCode();
                o.forEach(h -> consumer.accept(h.value().asItem(), hash));
            }
        });
        registerCustom(DataComponentIngredient.class, (i, consumer) -> {
            if (i.items().size() == 1) {
                var item = i.items().get(0).value();
                consumer.accept(item, BuiltInRegistries.ITEM.getKey(item).hashCode());
            }
        });
        polymorph = FMLLoader.getLoadingModList().getModFileById("polymorph") != null;
    }

}
