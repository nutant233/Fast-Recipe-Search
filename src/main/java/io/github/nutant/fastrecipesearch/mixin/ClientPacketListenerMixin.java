package io.github.nutant.fastrecipesearch.mixin;

import io.github.nutant.fastrecipesearch.Fastrecipesearch;
import io.github.nutant.fastrecipesearch.RecipeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Mutable
    @Final
    @Shadow
    private net.minecraft.world.item.crafting.RecipeManager recipeManager;

    @Shadow
    @Final
    private RegistryAccess.Frozen registryAccess;

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void createManager(Minecraft p_253924_, Connection p_253614_, CommonListenerCookie p_295121_, CallbackInfo ci) {
        RecipeManager recipeManager = new RecipeManager(this.registryAccess);
        Fastrecipesearch.copyKubeJsRecipeManagerState(this.recipeManager, recipeManager);
        this.recipeManager = recipeManager;
    }
}
