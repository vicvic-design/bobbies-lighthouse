package com.vicvic.bobbieslighthouse.mixin;

import com.vicvic.bobbieslighthouse.LodestoneFarClient;
import com.vicvic.bobbieslighthouse.render.AnchorRenderCoordinator;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.RenderSectionVisitor;
import net.caffeinemc.mods.sodium.client.render.chunk.occlusion.OcclusionCuller;
import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = OcclusionCuller.class, remap = false)
public abstract class SodiumOcclusionCullerMixin {
    @Shadow
    @Final
    private Long2ReferenceMap<RenderSection> sections;

    @Inject(method = "findVisible", at = @At("TAIL"))
    private void bobbieslighthouse$forceManagedLighthouseSectionsIntoSodiumCollector(
            RenderSectionVisitor visitor,
            Viewport viewport,
            float searchDistance,
            boolean useOcclusionCulling,
            int frame,
            CallbackInfo ci
    ) {
        AnchorRenderCoordinator coordinator = LodestoneFarClient.renderCoordinator();
        Minecraft client = Minecraft.getInstance();
        if (coordinator == null || client.level == null) {
            return;
        }

        int injected = 0;
        int skipped = 0;
        for (long chunk : coordinator.managedChunkSnapshot()) {
            int x = ChunkPos.getX(chunk);
            int z = ChunkPos.getZ(chunk);
            for (int y = client.level.getMinSectionY(); y < client.level.getMaxSectionY(); y++) {
                if (!coordinator.shouldInjectLighthouseSection(x, y, z)) {
                    skipped++;
                    continue;
                }
                RenderSection section = sections.get(SectionPos.asLong(x, y, z));
                if (section == null || !OcclusionCuller.isWithinFrustum(viewport, section)) {
                    continue;
                }
                if (!section.isBuilt() && section.getPendingUpdate() == 0) {
                    continue;
                }
                if (section.getLastVisibleFrame() == frame) {
                    continue;
                }
                section.setLastVisibleFrame(frame);
                section.setIncomingDirections(0);
                visitor.visit(section);
                injected++;
            }
        }
        if (injected > 0) {
            coordinator.recordVisibleSectionsInjected(injected);
        }
        if (skipped > 0) {
            coordinator.recordSurfaceSectionsSkipped(skipped);
        }
    }
}
