package fast.fastrecipesearch.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Arrays;
import java.util.stream.Stream;

@Mixin(Ingredient.class)
public class IngredientMixin {

    @Shadow
    @Final
    @Mutable
    public static PacketCodec<RegistryByteBuf, Ingredient> PACKET_CODEC;

    static {
        PACKET_CODEC = new PacketCodec<>() {

            @Override
            public void encode(RegistryByteBuf buf, Ingredient ingredient) {
                if (ingredient.getClass() == Ingredient.class && ingredient.entries.length == 1) {
                    if (ingredient.entries[0] instanceof Ingredient.StackEntry(ItemStack stack)) {
                        buf.writeVarInt(-2);
                        buf.writeBoolean(false);
                        buf.writeVarInt(Registries.ITEM.getRawId(stack.getItem()));
                    } else if (ingredient.entries[0] instanceof Ingredient.TagEntry(TagKey<Item> tag)) {
                        buf.writeVarInt(-2);
                        buf.writeBoolean(true);
                        buf.writeIdentifier(tag.id());
                    }
                } else {
                    ItemStack.LIST_PACKET_CODEC.encode(buf, Arrays.asList(ingredient.getMatchingStacks()));
                }
            }

            @Override
            public Ingredient decode(RegistryByteBuf buf) {
                int size = buf.readVarInt();
                if (size == -2) {
                    if (buf.readBoolean()) {
                        return Ingredient.fromTag(TagKey.of(RegistryKeys.ITEM, buf.readIdentifier()));
                    } else {
                        return Ingredient.ofItems(Registries.ITEM.get(buf.readVarInt()));
                    }
                }
                return Ingredient.ofEntries(Stream.generate(() -> ItemStack.PACKET_CODEC.decode(buf)).limit(size).map(Ingredient.StackEntry::new));
            }
        };
    }
}
