package fast.fastrecipesearch;

import com.fast.recipesearch.IntLongMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.fabricmc.api.ModInitializer;
import net.minecraft.recipe.Ingredient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.function.BiConsumer;

public class Fastrecipesearch implements ModInitializer {

    public static final String MODID = "fastrecipesearch";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    static final Map<Class, BiConsumer> CUSTOM = new Reference2ReferenceOpenHashMap<>();

    @Override
    public void onInitialize() {
    }

    public static <T extends Ingredient> void registerCustom(Class<T> clazz, BiConsumer<T, IntLongMap> consumer) {
        CUSTOM.put(clazz, consumer);
    }


}
