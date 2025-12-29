package fast.fastrecipesearch;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.ObjIntConsumer;

public class Fastrecipesearch implements ModInitializer {

    public static final String MODID = "fastrecipesearch";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    static final Map<Class, BiConsumer> CUSTOM = new Reference2ReferenceOpenHashMap<>();

    static final boolean polymorph;

    @Override
    public void onInitialize() {
    }

    public static <T extends Ingredient> void registerCustom(Class<T> clazz, BiConsumer<T, ObjIntConsumer<Item>> consumer) {
        CUSTOM.put(clazz, consumer);
    }

    static {
        polymorph = FabricLoader.getInstance().isModLoaded("polymorph");
    }


}
