package com.vicvic.bobbieslighthouse.mixin;

import com.vicvic.bobbieslighthouse.LodestoneFarClient;
import com.vicvic.bobbieslighthouse.render.AnchorRenderCoordinator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SectionOcclusionGraph.class)
public abstract class SectionOcclusionGraphMixin {
    @Inject(method = "addSectionsInFrustum", at = @At("TAIL"))
    private void bobbieslighthouse$addLighthouseSections(
            Frustum frustum,
            List<SectionRenderDispatcher.RenderSection> visibleSections,
            List<SectionRenderDispatcher.RenderSection> nearbyVisibleSections,
            CallbackInfo ci
    ) {
        AnchorRenderCoordinator coordinator = LodestoneFarClient.renderCoordinator();
        Minecraft client = Minecraft.getInstance();
        if (coordinator == null || client.level == null) {
            return;
        }
        ViewArea viewArea = ((SectionOcclusionGraphAccessor) this).bobbieslighthouse$getViewArea();
        if (viewArea == null) {
            return;
        }
        int injected = 0;
        for (long chunk : coordinator.managedChunkSnapshot()) {
            int x = ChunkPos.getX(chunk);
            int z = ChunkPos.getZ(chunk);
            for (int y = client.level.getMinSectionY(); y < client.level.getMaxSectionY(); y++) {
                SectionRenderDispatcher.RenderSection section =
                        ((ViewAreaAccessor) viewArea).bobbieslighthouse$getRenderSection(SectionPos.asLong(x, y, z));
                if (section == null || visibleSections.contains(section)) {
                    continue;
                }
                visibleSections.add(section);
                injected++;
            }
        }
        if (injected > 0) {
            coordinator.recordVisibleSectionsInjected(injected);
        }
    }
}
