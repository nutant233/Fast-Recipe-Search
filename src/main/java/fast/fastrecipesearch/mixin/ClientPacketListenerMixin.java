package fast.fastrecipesearch.mixin;

import com.mojang.authlib.GameProfile;
import fast.fastrecipesearch.RecipeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.telemetry.WorldSessionTelemetryManager;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientPacketListener.class, priority = 2000)
public class ClientPacketListenerMixin {

    @Shadow
    @Final
    @Mutable
    private net.minecraft.world.item.crafting.RecipeManager recipeManager;

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void createManager(Minecraft p_253924_, Screen p_254239_, Connection p_253614_, ServerData p_254072_, GameProfile p_254079_, WorldSessionTelemetryManager p_262115_, CallbackInfo ci) {
        recipeManager = new RecipeManager(net.minecraftforge.common.crafting.conditions.ICondition.IContext.EMPTY);
    }
}