package com.vicvic.bobbieslighthouse.anchor;

import com.vicvic.bobbieslighthouse.config.LodestoneFarConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.util.ArrayDeque;
import java.util.Queue;

public final class AnchorScanner {
    private final Minecraft client;
    private final LodestoneFarConfig config;
    private final AnchorStore anchorStore;
    private final Queue<Long> pendingChunks = new ArrayDeque<>();
    private int ticksUntilScan;

    public AnchorScanner(Minecraft client, LodestoneFarConfig config, AnchorStore anchorStore) {
        this.client = client;
        this.config = config;
        this.anchorStore = anchorStore;
    }

    public void requestFullRefresh() {
        pendingChunks.clear();
        ticksUntilScan = 0;
        enqueueVisibleChunks();
    }

    public void tick() {
        if (!config.enabled || client.level == null || client.player == null) {
            return;
        }
        if (--ticksUntilScan <= 0 && pendingChunks.isEmpty()) {
            ticksUntilScan = Math.max(1, config.scanIntervalTicks);
            enqueueVisibleChunks();
        }
        for (int i = 0; i < config.maxChunksScannedPerTick && !pendingChunks.isEmpty(); i++) {
            long packed = pendingChunks.poll();
            scanChunk(ChunkPos.getX(packed), ChunkPos.getZ(packed));
        }
    }

    private void enqueueVisibleChunks() {
        ChunkPos center = client.player.chunkPosition();
        int distance = Math.max(2, client.options.renderDistance().get());
        for (int x = center.x - distance; x <= center.x + distance; x++) {
            for (int z = center.z - distance; z <= center.z + distance; z++) {
                pendingChunks.add(ChunkPos.asLong(x, z));
            }
        }
    }

    private void scanChunk(int chunkX, int chunkZ) {
        LevelChunk chunk = client.level.getChunkSource().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
        if (chunk == null || isBobbyFakeChunk(chunk)) {
            return;
        }

        String dimension = anchorStore.dimensionKey();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        LevelChunkSection[] sections = chunk.getSections();
        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            LevelChunkSection section = sections[sectionIndex];
            if (!section.maybeHas(state -> state.is(Blocks.LODESTONE))) {
                continue;
            }
            int sectionY = client.level.getSectionYFromSectionIndex(sectionIndex);
            int minY = SectionPos.sectionToBlockCoord(sectionY);
            int maxY = minY + 16;
            for (int localX = 0; localX < 16; localX++) {
                int blockX = (chunkX << 4) + localX;
                for (int localZ = 0; localZ < 16; localZ++) {
                    int blockZ = (chunkZ << 4) + localZ;
                    for (int y = minY; y < maxY; y++) {
                        pos.set(blockX, y, blockZ);
                        if (chunk.getBlockState(pos).is(Blocks.LODESTONE)) {
                            anchorStore.upsert(new LodestoneAnchor(dimension, blockX, y, blockZ));
                        }
                    }
                }
            }
        }

        confirmKnownAnchorsInChunk(chunkX, chunkZ);
    }

    private void confirmKnownAnchorsInChunk(int chunkX, int chunkZ) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean changed = false;
        for (LodestoneAnchor anchor : anchorStore.all()) {
            if (!anchor.dimension.equals(anchorStore.dimensionKey()) || anchor.chunkX != chunkX || anchor.chunkZ != chunkZ) {
                continue;
            }
            pos.set(anchor.blockX, anchor.blockY, anchor.blockZ);
            boolean stillPresent = client.level.getBlockState(pos).is(Blocks.LODESTONE);
            anchor.lastConfirmedAtMillis = System.currentTimeMillis();
            if (!stillPresent && anchor.enabled) {
                anchor.enabled = false;
                anchor.disabledReason = "lodestone_missing_on_refresh";
            }
            changed = true;
        }
        if (changed) {
            anchorStore.save();
        }
    }

    private static boolean isBobbyFakeChunk(LevelChunk chunk) {
        return chunk.getClass().getName().equals("de.johni0702.minecraft.bobby.FakeChunk");
    }
}
