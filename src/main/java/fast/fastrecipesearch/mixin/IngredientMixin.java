package fast.fastrecipesearch.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Ingredient.class)
public abstract class IngredientMixin {

    @Shadow
    @Final
    public Ingredient.Value[] values;

    @Final
    @Shadow(remap = false)
    private boolean isVanilla;

    @Inject(method = "toNetwork", at = @At("HEAD"), cancellable = true)
    private void toNetwork(FriendlyByteBuf buf, CallbackInfo ci) {
        if (this.isVanilla) {
            Ingredient.Value[] values = this.values;
            if (values.length == 1) {
                Ingredient.Value value = values[0];
                if (value instanceof Ingredient.TagValue tagValue) {
                    buf.writeVarInt(-2);
                    buf.writeBoolean(true);
                    buf.writeResourceLocation(tagValue.tag.location());
                    ci.cancel();
                } else if (value instanceof Ingredient.ItemValue itemValue) {
                    buf.writeVarInt(-2);
                    buf.writeBoolean(false);
                    buf.writeInt(BuiltInRegistries.ITEM.getId(itemValue.item.getItem()));
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "fromNetwork", at = @At(value = "HEAD"), cancellable = true)
    private static void fromNetwork(FriendlyByteBuf buffer, CallbackInfoReturnable<Ingredient> cir) {
        buffer.markReaderIndex();
        int size = buffer.readVarInt();
        if (size == -2) {
            if (buffer.readBoolean()) {
                var tag = TagKey.create(Registries.ITEM, buffer.readResourceLocation());
                cir.setReturnValue(Ingredient.of(tag));
            } else {
                var item = BuiltInRegistries.ITEM.byId(buffer.readInt());
                cir.setReturnValue(Ingredient.of(item));
            }
        } else {
            buffer.resetReaderIndex();
        }
    }
}
