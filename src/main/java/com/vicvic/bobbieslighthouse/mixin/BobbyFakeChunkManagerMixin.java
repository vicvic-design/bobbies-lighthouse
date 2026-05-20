package com.vicvic.bobbieslighthouse.mixin;

import com.vicvic.bobbieslighthouse.LodestoneFarClient;
import com.vicvic.bobbieslighthouse.render.AnchorRenderCoordinator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "de.johni0702.minecraft.bobby.FakeChunkManager", remap = false)
public abstract class BobbyFakeChunkManagerMixin {
    @ModifyVariable(method = "update(ZLjava/util/function/BooleanSupplier;I)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int bobbieslighthouse$clampNormalBobbyDistance(int requestedViewDistance) {
        AnchorRenderCoordinator coordinator = LodestoneFarClient.renderCoordinator();
        if (coordinator == null) {
            return requestedViewDistance;
        }
        return coordinator.filteredBobbyViewDistance(requestedViewDistance);
    }

    @Inject(method = "shouldBeLoaded", at = @At("RETURN"), cancellable = true)
    private void bobbieslighthouse$filterFarBobbyChunks(int x, int z, CallbackInfoReturnable<Boolean> cir) {
        AnchorRenderCoordinator coordinator = LodestoneFarClient.renderCoordinator();
        if (coordinator == null) {
            return;
        }
        if (coordinator.isWithinBobbyFilterCutoff(x, z)) {
            return;
        }
        cir.setReturnValue(coordinator.isAllowedFarBobbyChunk(x, z));
    }
}
