package fast.fastrecipesearch.mixin;

import fast.fastrecipesearch.Fastrecipesearch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Ingredient.class, priority = 100)
public abstract class IngredientMixin {

    @Unique
    private static final ResourceLocation fastrecipesearch$A = new ResourceLocation(Fastrecipesearch.MODID, "a");
    @Unique
    private static final ResourceLocation fastrecipesearch$B = new ResourceLocation(Fastrecipesearch.MODID, "a");

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
                    buf.writeVarInt(-1);
                    buf.writeResourceLocation(fastrecipesearch$A);
                    buf.writeResourceLocation(tagValue.tag.location());
                    ci.cancel();
                } else if (value instanceof Ingredient.ItemValue itemValue) {
                    buf.writeVarInt(-1);
                    buf.writeResourceLocation(fastrecipesearch$B);
                    buf.writeInt(BuiltInRegistries.ITEM.getId(itemValue.item.getItem()));
                    ci.cancel();
                }
            }
        }
    }

    @Redirect(method = "fromNetwork", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/crafting/CraftingHelper;getIngredient(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/network/FriendlyByteBuf;)Lnet/minecraft/world/item/crafting/Ingredient;", remap = false))
    private static Ingredient fromNetwork(ResourceLocation type, FriendlyByteBuf buffer) {
        if (type.equals(fastrecipesearch$A)) {
            return Ingredient.of(TagKey.create(Registries.ITEM, buffer.readResourceLocation()));
        } else if (type.equals(fastrecipesearch$B)) {
            return Ingredient.of(BuiltInRegistries.ITEM.byId(buffer.readInt()));
        }
        return CraftingHelper.getIngredient(type, buffer);
    }
}
