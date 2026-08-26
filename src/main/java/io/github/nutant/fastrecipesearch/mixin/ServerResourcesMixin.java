package io.github.nutant.fastrecipesearch.mixin;

import io.github.nutant.fastrecipesearch.RecipeManager;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.DataPackContents;
import net.minecraft.server.command.CommandManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DataPackContents.class)
public class ServerResourcesMixin {

    @Final
    @Shadow
    private DataPackContents.ConfigurableWrapperLookup registryLookup;

    @Final
    @Shadow
    @Mutable
    private net.minecraft.recipe.RecipeManager recipeManager;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(DynamicRegistryManager.Immutable dynamicRegistryManager, FeatureSet enabledFeatures, CommandManager.RegistrationEnvironment environment, int functionPermissionLevel, CallbackInfo ci) {
        this.recipeManager = new RecipeManager(registryLookup);
    }
}
