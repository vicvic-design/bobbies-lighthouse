package com.vicvic.bobbieslighthouse.mixin;

import com.vicvic.bobbieslighthouse.LodestoneFarClient;
import com.vicvic.bobbieslighthouse.render.AnchorRenderCoordinator;
import net.minecraft.client.renderer.ViewArea;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ViewArea.class)
public abstract class ViewAreaMixin {
    @ModifyVariable(method = "setViewDistance", at = @At("HEAD"), argsOnly = true)
    private int bobbieslighthouse$extendViewDistance(int viewDistance) {
        AnchorRenderCoordinator coordinator = LodestoneFarClient.renderCoordinator();
        if (coordinator == null) {
            return viewDistance;
        }
        return Math.max(viewDistance, coordinator.rendererHorizonChunks());
    }
}
