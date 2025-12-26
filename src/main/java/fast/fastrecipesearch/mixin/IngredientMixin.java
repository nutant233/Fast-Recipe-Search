package fast.fastrecipesearch.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Arrays;
import java.util.stream.Stream;

@Mixin(Ingredient.class)
public class IngredientMixin {

    @Shadow
    @Final
    public static StreamCodec<RegistryFriendlyByteBuf, Ingredient> CONTENTS_STREAM_CODEC;

    static {
        CONTENTS_STREAM_CODEC = new StreamCodec<>() {
            private static final StreamCodec<RegistryFriendlyByteBuf, ICustomIngredient> CUSTOM_INGREDIENT_CODEC;

            @Override
            public void encode(RegistryFriendlyByteBuf buf, Ingredient ingredient) {
                if (!ingredient.isCustom() && ingredient.values.length == 1) {
                    if (ingredient.values[0] instanceof Ingredient.ItemValue(ItemStack stack)) {
                        buf.writeVarInt(-2);
                        buf.writeBoolean(false);
                        buf.writeVarInt(BuiltInRegistries.ITEM.getId(stack.getItem()));
                    } else if (ingredient.values[0] instanceof Ingredient.TagValue(TagKey<Item> tag)) {
                        buf.writeVarInt(-2);
                        buf.writeBoolean(true);
                        buf.writeResourceLocation(tag.location());
                    }
                } else if (ingredient.isSimple()) {
                    ItemStack.LIST_STREAM_CODEC.encode(buf, Arrays.asList(ingredient.getItems()));
                } else {
                    buf.writeVarInt(-1);
                    CUSTOM_INGREDIENT_CODEC.encode(buf, ingredient.getCustomIngredient());
                }
            }

            @Override
            public @NotNull Ingredient decode(@NotNull RegistryFriendlyByteBuf buf) {
                int size = buf.readVarInt();
                if (size == -2) {
                    if (buf.readBoolean()) {
                        return Ingredient.of(TagKey.create(Registries.ITEM, buf.readResourceLocation()));
                    } else {
                        return Ingredient.of(BuiltInRegistries.ITEM.byId(buf.readVarInt()));
                    }
                } else if (size == -1) return new Ingredient(CUSTOM_INGREDIENT_CODEC.decode(buf));
                return Ingredient.fromValues(Stream.generate(() -> ItemStack.STREAM_CODEC.decode(buf)).limit(size).map(Ingredient.ItemValue::new));
            }

            static {
                CUSTOM_INGREDIENT_CODEC = ByteBufCodecs.registry(NeoForgeRegistries.Keys.INGREDIENT_TYPES).dispatch((c) -> c.getType(), (t) -> t.streamCodec());
            }
        };
    }
}
