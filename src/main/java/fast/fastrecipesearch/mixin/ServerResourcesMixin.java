package fast.fastrecipesearch.mixin;

import fast.fastrecipesearch.RecipeManager;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ReloadableServerResources.class)
public class ServerResourcesMixin {

    @Final
    @Shadow
    @Mutable
    private net.minecraft.world.item.crafting.RecipeManager recipes;

    @Shadow
    @Final
    private ReloadableServerResources.ConfigurableRegistryLookup registryLookup;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(RegistryAccess.Frozen registryAccess, FeatureFlagSet enabledFeatures, Commands.CommandSelection commandSelection, int functionCompilationLevel, CallbackInfo ci) {
        this.recipes = new RecipeManager(registryLookup);
    }
}
