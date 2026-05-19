package com.vicvic.bobbieslighthouse.render;

import com.vicvic.bobbieslighthouse.anchor.AnchorStore;
import com.vicvic.bobbieslighthouse.anchor.LodestoneAnchor;
import com.vicvic.bobbieslighthouse.bobby.BobbyBridge;
import com.vicvic.bobbieslighthouse.config.LodestoneFarConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class AnchorRenderCoordinator {
    private final Minecraft client;
    private final LodestoneFarConfig config;
    private final AnchorStore anchorStore;
    private final BobbyBridge bobbyBridge;
    private final Set<Long> managedChunks = new HashSet<>();
    private final Set<Long> loadingChunks = new HashSet<>();
    private int ticksUntilRefresh;
    private int lastDesiredChunkCount;
    private int lastEligibleAnchorCount;
    private int successfulLoads;
    private int failedLoads;

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

    public void tick() {
        if (!config.enabled || client.player == null || client.level == null) {
            reset();
            return;
        }
        if (--ticksUntilRefresh > 0) {
            return;
        }
        ticksUntilRefresh = Math.max(1, config.renderRefreshIntervalTicks);
        Set<Long> desired = computeDesiredChunks();
        unloadUndesired(desired);
        loadDesired(desired);
    }

    private Set<Long> computeDesiredChunks() {
        Set<Long> desired = new HashSet<>();
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
            if (distanceSquared <= config.maxAnchorDistanceChunks * config.maxAnchorDistanceChunks) {
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
        boolean tooFar = distanceSquared > config.maxAnchorDistanceChunks * config.maxAnchorDistanceChunks;
        boolean alreadyNear = squareDistance <= client.options.renderDistance().get();
        boolean managed = managedChunks.contains(ChunkPos.asLong(anchor.chunkX, anchor.chunkZ));
        boolean loading = loadingChunks.contains(ChunkPos.asLong(anchor.chunkX, anchor.chunkZ));
        return "nearestAnchor block=(" + anchor.blockX + "," + anchor.blockY + "," + anchor.blockZ + ")"
                + " chunk=(" + anchor.chunkX + "," + anchor.chunkZ + ")"
                + " enabled=" + anchor.enabled
                + " dimensionOk=" + !wrongDimension
                + " distanceChunks=" + squareDistance
                + " maxAnchorDistanceChunks=" + config.maxAnchorDistanceChunks
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
        for (int x = anchor.chunkX - radius; x <= anchor.chunkX + radius; x++) {
            for (int z = anchor.chunkZ - radius; z <= anchor.chunkZ + radius; z++) {
                int anchorDx = x - anchor.chunkX;
                int anchorDz = z - anchor.chunkZ;
                if (config.shape == LodestoneFarConfig.Shape.CIRCLE && anchorDx * anchorDx + anchorDz * anchorDz > radiusSquared) {
                    continue;
                }
                int playerDx = Math.abs(x - playerChunk.x);
                int playerDz = Math.abs(z - playerChunk.z);
                if (playerDx <= currentRenderDistance && playerDz <= currentRenderDistance) {
                    continue;
                }
                desired.add(ChunkPos.asLong(x, z));
                if (desired.size() >= config.maxExtraRenderedChunks) {
                    return;
                }
            }
        }
    }

    private void unloadUndesired(Set<Long> desired) {
        for (long chunk : new HashSet<>(managedChunks)) {
            if (!desired.contains(chunk)) {
                bobbyBridge.unloadChunk(ChunkPos.getX(chunk), ChunkPos.getZ(chunk));
                managedChunks.remove(chunk);
            }
        }
        loadingChunks.removeIf(chunk -> !desired.contains(chunk));
    }

    private void loadDesired(Set<Long> desired) {
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
                continue;
            }
            loadingChunks.add(chunk);
            bobbyBridge.loadChunk(x, z, loaded -> {
                loadingChunks.remove(chunk);
                if (loaded) {
                    managedChunks.add(chunk);
                    successfulLoads++;
                } else {
                    failedLoads++;
                }
            });
        }
    }
}
