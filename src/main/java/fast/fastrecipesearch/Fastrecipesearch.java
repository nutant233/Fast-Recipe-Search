package fast.fastrecipesearch;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.crafting.BlockTagIngredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.ObjIntConsumer;

@Mod(Config.MODID)
public class Fastrecipesearch {

    public static final ReferenceOpenHashSet<RecipeType<?>> VANILLA_TYPES = Util.make(() -> {
        var set = new ReferenceOpenHashSet<RecipeType<?>>();
        set.add(RecipeType.CRAFTING);
        set.add(RecipeType.SMELTING);
        set.add(RecipeType.BLASTING);
        set.add(RecipeType.SMOKING);
        set.add(RecipeType.CAMPFIRE_COOKING);
        set.add(RecipeType.STONECUTTING);
        set.add(RecipeType.SMITHING);
        return set;
    });

    static final Map<Class, BiConsumer> CUSTOM = new Reference2ReferenceOpenHashMap<>();

    static final boolean polymorph;

    public static <T extends ICustomIngredient> void registerCustom(Class<T> clazz, BiConsumer<T, ObjIntConsumer<Item>> consumer) {
        CUSTOM.put(clazz, consumer);
    }

    static {
        registerCustom(BlockTagIngredient.class, (i, consumer) -> {
            var o = BuiltInRegistries.BLOCK.get(i.getTag()).orElse(null);
            if (o != null) {
                var hash = i.getTag().location().hashCode();
                o.forEach(h -> consumer.accept(h.value().asItem(), hash));
            }
        });
        registerCustom(DataComponentIngredient.class, (i, consumer) -> {
            if (i.itemSet().size() == 1) {
                var item = i.itemSet().get(0).value();
                consumer.accept(item, BuiltInRegistries.ITEM.getKey(item).hashCode());
            }
        });
        polymorph = FMLLoader.getCurrent().getLoadingModList().getModFileById("polymorph") != null;
    }

}
