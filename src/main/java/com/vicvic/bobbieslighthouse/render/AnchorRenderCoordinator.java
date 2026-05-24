package com.vicvic.bobbieslighthouse.render;

import com.vicvic.bobbieslighthouse.anchor.AnchorStore;
import com.vicvic.bobbieslighthouse.anchor.LodestoneAnchor;
import com.vicvic.bobbieslighthouse.bobby.BobbyBridge;
import com.vicvic.bobbieslighthouse.config.LodestoneFarConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class AnchorRenderCoordinator {
    private final Minecraft client;
    private final LodestoneFarConfig config;
    private final AnchorStore anchorStore;
    private final BobbyBridge bobbyBridge;
    private final Set<Long> managedChunks = new HashSet<>();
    private final Set<Long> loadingChunks = new HashSet<>();
    private final Set<Long> desiredChunks = new LinkedHashSet<>();
    private int ticksUntilRefresh;
    private int lastDesiredChunkCount;
    private int lastEligibleAnchorCount;
    private int successfulLoads;
    private int failedLoads;
    private int staleManagedChunks;
    private int renderSectionsDirtied;
    private int rendererHorizonChunks;
    private int visibleSectionsInjected;
    private int surfaceSectionsSkipped;

    public AnchorRenderCoordinator(
            Minecraft client,
            LodestoneFarConfig config,
            AnchorStore anchorStore,
            BobbyBridge bobbyBridge
    ) {
        this.client = client;
        this.config = config;
        this.anchorStore = anchorStore;
        this.bobbyBridge = bobbyBridge;
    }

    public void reset() {
        for (long chunk : new HashSet<>(managedChunks)) {
            bobbyBridge.unloadChunk(ChunkPos.getX(chunk), ChunkPos.getZ(chunk));
        }
        managedChunks.clear();
        loadingChunks.clear();
        desiredChunks.clear();
        ticksUntilRefresh = 0;
    }

    public void requestRefresh() {
        ticksUntilRefresh = 0;
    }

    public int activeChunkCount() {
        return managedChunks.size();
    }

    public int queuedLoadCount() {
        return loadingChunks.size();
    }

    public int lastDesiredChunkCount() {
        return lastDesiredChunkCount;
    }

    public int lastEligibleAnchorCount() {
        return lastEligibleAnchorCount;
    }

    public int successfulLoadCount() {
        return successfulLoads;
    }

    public int failedLoadCount() {
        return failedLoads;
    }

    public int staleManagedChunkCount() {
        return staleManagedChunks;
    }

    public int renderSectionsDirtiedCount() {
        return renderSectionsDirtied;
    }

    public int rendererHorizonChunks() {
        return rendererHorizonChunks;
    }

    public int visibleSectionsInjectedCount() {
        return visibleSectionsInjected;
    }

    public int surfaceSectionsSkippedCount() {
        return surfaceSectionsSkipped;
    }

    public Set<Long> managedChunkSnapshot() {
        return Collections.unmodifiableSet(new HashSet<>(managedChunks));
    }

    public void recordVisibleSectionsInjected(int count) {
        visibleSectionsInjected += count;
    }

    public void recordSurfaceSectionsSkipped(int count) {
        surfaceSectionsSkipped += count;
    }

    public boolean shouldInjectLighthouseSection(int chunkX, int sectionY, int chunkZ) {
        if (!managedChunks.contains(ChunkPos.asLong(chunkX, chunkZ))) {
            return false;
        }
        if (client.level == null) {
            return true;
        }
        LevelChunk chunk = client.level.getChunkSource().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
        if (chunk == null) {
            return true;
        }
        int sectionTopBlockY = SectionPos.sectionToBlockCoord(sectionY) + 15;
        return sectionTopBlockY >= minSurfaceHeight(chunk) - 16;
    }

    public boolean isBobbyAvailable() {
        return bobbyBridge.isAvailable();
    }

    public String bobbyDiagnostics() {
        return bobbyBridge.diagnostics();
    }

    public String bobbyProbe() {
        return bobbyBridge.probe();
    }

    public int bobbyFakeChunkCount() {
        return bobbyBridge.fakeChunkCount();
    }

    public String explainNearestAnchor() {
        if (client.player == null || client.level == null) {
            return "No world/player loaded.";
        }
        ChunkPos playerChunk = client.player.chunkPosition();
        LodestoneAnchor nearest = null;
        int nearestDistanceSquared = Integer.MAX_VALUE;
        for (LodestoneAnchor anchor : anchorStore.all()) {
            int dx = anchor.chunkX - playerChunk.x;
            int dz = anchor.chunkZ - playerChunk.z;
            int distanceSquared = dx * dx + dz * dz;
            if (distanceSquared < nearestDistanceSquared) {
                nearest = anchor;
                nearestDistanceSquared = distanceSquared;
            }
        }
        if (nearest == null) {
            return "No anchors are stored.";
        }
        return explainAnchor(nearest, playerChunk);
    }

    public String renderProbe() {
        if (client.player == null || client.level == null) {
            return "No world/player loaded.";
        }
        int lighthouseOnly = 0;
        int bobbyRangeOverlap = 0;
        int normalRenderRange = 0;
        int loadedInBobby = 0;
        int missingFromBobby = 0;
        ChunkPos playerChunk = client.player.chunkPosition();
        int renderDistance = client.options.renderDistance().get();
        for (long chunk : managedChunks) {
            int x = ChunkPos.getX(chunk);
            int z = ChunkPos.getZ(chunk);
            int dx = Math.abs(x - playerChunk.x);
            int dz = Math.abs(z - playerChunk.z);
            if (dx <= renderDistance && dz <= renderDistance) {
                normalRenderRange++;
            } else if (bobbyBridge.isInNormalBobbyRange(x, z)) {
                bobbyRangeOverlap++;
            } else {
                lighthouseOnly++;
            }
            if (bobbyBridge.hasChunk(x, z)) {
                loadedInBobby++;
            } else {
                missingFromBobby++;
            }
        }
        return "managed=" + managedChunks.size()
                + ", loading=" + loadingChunks.size()
                + ", lighthouseOnly=" + lighthouseOnly
                + ", bobbyRangeOverlap=" + bobbyRangeOverlap
                + ", normalRenderRange=" + normalRenderRange
                + ", loadedInBobby=" + loadedInBobby
                + ", missingFromBobby=" + missingFromBobby
                + ", staleManagedLastRefresh=" + staleManagedChunks
                + ", renderSectionsDirtied=" + renderSectionsDirtied
                + ", rendererHorizonChunks=" + rendererHorizonChunks
                + ", visibleSectionsInjected=" + visibleSectionsInjected
                + ", surfaceSectionsSkipped=" + surfaceSectionsSkipped
                + ", anchorRadiusChunks=" + config.anchorRadiusChunks
                + ", lighthouseRangeChunks=" + config.lighthouseRangeChunks;
    }

    public void tick() {
        if (!config.enabled || client.player == null || client.level == null) {
            reset();
            return;
        }
        if (--ticksUntilRefresh <= 0) {
            ticksUntilRefresh = Math.max(1, config.renderRefreshIntervalTicks);
            desiredChunks.clear();
            desiredChunks.addAll(computeDesiredChunks());
            updateRendererHorizon(desiredChunks);
            unloadUndesired(desiredChunks);
            reconcileManagedChunks(desiredChunks);
        }
        loadDesired(desiredChunks);
    }

    private Set<Long> computeDesiredChunks() {
        Set<Long> desired = new LinkedHashSet<>();
        ChunkPos playerChunk = client.player.chunkPosition();
        int currentRenderDistance = client.options.renderDistance().get();
        List<LodestoneAnchor> candidates = new ArrayList<>();
        for (LodestoneAnchor anchor : anchorStore.all()) {
            if (!anchor.enabled || !anchor.dimension.equals(anchorStore.dimensionKey())) {
                continue;
            }
            int dx = anchor.chunkX - playerChunk.x;
            int dz = anchor.chunkZ - playerChunk.z;
            int distanceSquared = dx * dx + dz * dz;
            if (distanceSquared <= config.lighthouseRangeChunks * config.lighthouseRangeChunks) {
                candidates.add(anchor);
            }
        }
        lastEligibleAnchorCount = candidates.size();
        candidates.sort(Comparator.comparingInt(anchor -> {
            int dx = anchor.chunkX - playerChunk.x;
            int dz = anchor.chunkZ - playerChunk.z;
            return dx * dx + dz * dz;
        }));

        int activeAnchors = 0;
        for (LodestoneAnchor anchor : candidates) {
            if (activeAnchors++ >= config.maxActiveAnchors || desired.size() >= config.maxExtraRenderedChunks) {
                break;
            }
            addAnchorChunks(anchor, playerChunk, currentRenderDistance, desired);
        }
        lastDesiredChunkCount = desired.size();
        return desired;
    }

    private String explainAnchor(LodestoneAnchor anchor, ChunkPos playerChunk) {
        int dx = anchor.chunkX - playerChunk.x;
        int dz = anchor.chunkZ - playerChunk.z;
        int squareDistance = Math.max(Math.abs(dx), Math.abs(dz));
        int distanceSquared = dx * dx + dz * dz;
        boolean wrongDimension = !anchor.dimension.equals(anchorStore.dimensionKey());
        boolean tooFar = distanceSquared > config.lighthouseRangeChunks * config.lighthouseRangeChunks;
        boolean alreadyNear = squareDistance <= client.options.renderDistance().get();
        boolean managed = managedChunks.contains(ChunkPos.asLong(anchor.chunkX, anchor.chunkZ));
        boolean loading = loadingChunks.contains(ChunkPos.asLong(anchor.chunkX, anchor.chunkZ));
        return "nearestAnchor block=(" + anchor.blockX + "," + anchor.blockY + "," + anchor.blockZ + ")"
                + " chunk=(" + anchor.chunkX + "," + anchor.chunkZ + ")"
                + " enabled=" + anchor.enabled
                + " dimensionOk=" + !wrongDimension
                + " distanceChunks=" + squareDistance
                + " lighthouseRangeChunks=" + config.lighthouseRangeChunks
                + " insideNormalRenderDistance=" + alreadyNear
                + " tooFar=" + tooFar
                + " bobby=" + bobbyBridge.diagnostics()
                + " centerChunkManaged=" + managed
                + " centerChunkLoading=" + loading
                + (anchor.disabledReason.isEmpty() ? "" : " disabledReason=" + anchor.disabledReason);
    }

    private void addAnchorChunks(LodestoneAnchor anchor, ChunkPos playerChunk, int currentRenderDistance, Set<Long> desired) {
        int radius = Math.max(0, config.anchorRadiusChunks);
        int radiusSquared = radius * radius;
        for (int ring = 0; ring <= radius; ring++) {
            for (int anchorDx = -ring; anchorDx <= ring; anchorDx++) {
                addAnchorChunkIfDesired(anchor, playerChunk, currentRenderDistance, desired, anchorDx, -ring, radiusSquared);
                if (desired.size() >= config.maxExtraRenderedChunks) {
                    return;
                }
                if (ring != 0) {
                    addAnchorChunkIfDesired(anchor, playerChunk, currentRenderDistance, desired, anchorDx, ring, radiusSquared);
                    if (desired.size() >= config.maxExtraRenderedChunks) {
                        return;
                    }
                }
            }
            for (int anchorDz = -ring + 1; anchorDz <= ring - 1; anchorDz++) {
                addAnchorChunkIfDesired(anchor, playerChunk, currentRenderDistance, desired, -ring, anchorDz, radiusSquared);
                if (desired.size() >= config.maxExtraRenderedChunks) {
                    return;
                }
                if (ring != 0) {
                    addAnchorChunkIfDesired(anchor, playerChunk, currentRenderDistance, desired, ring, anchorDz, radiusSquared);
                    if (desired.size() >= config.maxExtraRenderedChunks) {
                        return;
                    }
                }
            }
        }
    }

    private void addAnchorChunkIfDesired(
            LodestoneAnchor anchor,
            ChunkPos playerChunk,
            int currentRenderDistance,
            Set<Long> desired,
            int anchorDx,
            int anchorDz,
            int radiusSquared
    ) {
        if (config.shape == LodestoneFarConfig.Shape.CIRCLE && anchorDx * anchorDx + anchorDz * anchorDz > radiusSquared) {
            return;
        }
        int x = anchor.chunkX + anchorDx;
        int z = anchor.chunkZ + anchorDz;
        int playerDx = Math.abs(x - playerChunk.x);
        int playerDz = Math.abs(z - playerChunk.z);
        if (playerDx <= currentRenderDistance && playerDz <= currentRenderDistance) {
            return;
        }
        if (bobbyBridge.isInNormalBobbyRange(x, z)) {
            return;
        }
        desired.add(ChunkPos.asLong(x, z));
    }

    private void unloadUndesired(Set<Long> desired) {
        for (long chunk : new HashSet<>(managedChunks)) {
            if (!desired.contains(chunk)) {
                int x = ChunkPos.getX(chunk);
                int z = ChunkPos.getZ(chunk);
                if (!isInNormalRenderRange(x, z) && !bobbyBridge.isInNormalBobbyRange(x, z)) {
                    bobbyBridge.unloadChunk(x, z);
                }
                managedChunks.remove(chunk);
            }
        }
        loadingChunks.removeIf(chunk -> !desired.contains(chunk));
    }

    private void reconcileManagedChunks(Set<Long> desired) {
        staleManagedChunks = 0;
        if (!bobbyBridge.isAvailable()) {
            managedChunks.clear();
            loadingChunks.clear();
            return;
        }
        for (long chunk : new HashSet<>(managedChunks)) {
            if (!desired.contains(chunk)) {
                continue;
            }
            int x = ChunkPos.getX(chunk);
            int z = ChunkPos.getZ(chunk);
            if (!bobbyBridge.hasChunk(x, z)) {
                managedChunks.remove(chunk);
                loadingChunks.remove(chunk);
                staleManagedChunks++;
            }
        }
    }

    private boolean isInNormalRenderRange(int x, int z) {
        if (client.player == null) {
            return false;
        }
        ChunkPos playerChunk = client.player.chunkPosition();
        int renderDistance = client.options.renderDistance().get();
        return Math.abs(x - playerChunk.x) <= renderDistance && Math.abs(z - playerChunk.z) <= renderDistance;
    }

    private int minSurfaceHeight(LevelChunk chunk) {
        int minSurfaceHeight = Integer.MAX_VALUE;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                minSurfaceHeight = Math.min(minSurfaceHeight, chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z));
            }
        }
        return minSurfaceHeight == Integer.MAX_VALUE ? client.level.getMinY() : minSurfaceHeight;
    }

    private void updateRendererHorizon(Set<Long> desired) {
        if (client.player == null || client.levelRenderer == null) {
            return;
        }
        ChunkPos playerChunk = client.player.chunkPosition();
        int target = client.options.renderDistance().get();
        for (long chunk : desired) {
            int dx = Math.abs(ChunkPos.getX(chunk) - playerChunk.x);
            int dz = Math.abs(ChunkPos.getZ(chunk) - playerChunk.z);
            target = Math.max(target, Math.max(dx, dz) + 1);
        }
        target = Math.min(target, Math.max(client.options.renderDistance().get(), config.lighthouseRangeChunks));
        rendererHorizonChunks = target;
    }

    private void loadDesired(Set<Long> desired) {
        if (!bobbyBridge.isAvailable()) {
            loadingChunks.clear();
            return;
        }
        int started = 0;
        for (long chunk : desired) {
            if (managedChunks.contains(chunk) || loadingChunks.contains(chunk)) {
                continue;
            }
            if (started++ >= config.maxChunkLoadsStartedPerTick) {
                return;
            }
            int x = ChunkPos.getX(chunk);
            int z = ChunkPos.getZ(chunk);
            if (bobbyBridge.hasChunk(x, z)) {
                managedChunks.add(chunk);
                dirtyChunkRenderSections(x, z);
                continue;
            }
            loadingChunks.add(chunk);
            bobbyBridge.loadChunk(x, z, loaded -> {
                loadingChunks.remove(chunk);
                if (loaded) {
                    managedChunks.add(chunk);
                    dirtyChunkRenderSections(x, z);
                    successfulLoads++;
                } else {
                    failedLoads++;
                }
            });
        }
    }

    private void dirtyChunkRenderSections(int x, int z) {
        if (client.level == null || client.levelRenderer == null) {
            return;
        }
        int dirtied = 0;
        for (int y = client.level.getMinSectionY(); y < client.level.getMaxSectionY(); y++) {
            client.levelRenderer.setSectionDirty(x, y, z);
            client.levelRenderer.onSectionBecomingNonEmpty(SectionPos.asLong(x, y, z));
            dirtied++;
        }
        renderSectionsDirtied += dirtied;
    }
}
