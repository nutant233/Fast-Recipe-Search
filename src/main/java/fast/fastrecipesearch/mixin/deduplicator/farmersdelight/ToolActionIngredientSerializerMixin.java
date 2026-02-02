package fast.fastrecipesearch.mixin.deduplicator.farmersdelight;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.ToolAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import vectorwing.farmersdelight.common.crafting.ingredient.ToolActionIngredient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(value = ToolActionIngredient.Serializer.class, remap = false)
public class ToolActionIngredientSerializerMixin {

    @Unique
    private static final Map<String, ToolActionIngredient> fastrecipesearch$CACHE = new ConcurrentHashMap<>();

    /**
     * @author nutant233
     * @reason deduplicator
     */
    @Overwrite
    public ToolActionIngredient parse(JsonObject json) {
        return fastrecipesearch$CACHE.computeIfAbsent(json.get("action").getAsString(), k -> new ToolActionIngredient(ToolAction.get(k)));
    }

    /**
     * @author nutant233
     * @reason deduplicator
     */
    @Overwrite
    public ToolActionIngredient parse(FriendlyByteBuf buffer) {
        return fastrecipesearch$CACHE.computeIfAbsent(buffer.readUtf(), k -> new ToolActionIngredient(ToolAction.get(k)));
    }
}
