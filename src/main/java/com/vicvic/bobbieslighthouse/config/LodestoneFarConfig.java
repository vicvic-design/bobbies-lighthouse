package com.vicvic.bobbieslighthouse.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vicvic.bobbieslighthouse.LodestoneFarClient;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LodestoneFarConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean enabled = true;
    public int anchorRadiusChunks = 5;
    public int maxAnchorDistanceChunks = 96;
    public Shape shape = Shape.SQUARE;
    public int scanIntervalTicks = 40;
    public int maxChunksScannedPerTick = 2;
    public int renderRefreshIntervalTicks = 20;
    public int maxActiveAnchors = 8;
    public int maxExtraRenderedChunks = 512;
    public int maxChunkLoadsStartedPerTick = 4;
    public int maxRendererHorizonChunks = 128;
    public boolean enableDevCommands = true;
    private transient Path loadedPath;

    public static LodestoneFarConfig load(Path gameDirectory) {
        Path path = gameDirectory.resolve("config").resolve("bobbieslighthouse.json");
        LodestoneFarConfig config = new LodestoneFarConfig();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                LodestoneFarConfig loaded = GSON.fromJson(reader, LodestoneFarConfig.class);
                if (loaded != null) {
                    config = loaded;
                }
            } catch (Exception e) {
                LodestoneFarClient.LOGGER.warn("Failed to load bobbieslighthouse config, using defaults", e);
            }
        }
        config.save(path);
        return config;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        save();
    }

    public void setAnchorRadiusChunks(int radiusChunks) {
        anchorRadiusChunks = Math.max(0, radiusChunks);
        save();
    }

    public void setMaxAnchorDistanceChunks(int distanceChunks) {
        maxAnchorDistanceChunks = Math.max(1, distanceChunks);
        save();
    }

    public void setMaxActiveAnchors(int activeAnchors) {
        maxActiveAnchors = Math.max(1, activeAnchors);
        save();
    }

    public void setMaxExtraRenderedChunks(int chunks) {
        maxExtraRenderedChunks = Math.max(16, chunks);
        save();
    }

    public void setMaxChunkLoadsStartedPerTick(int chunksPerTick) {
        maxChunkLoadsStartedPerTick = Math.max(1, chunksPerTick);
        save();
    }

    public void setMaxRendererHorizonChunks(int chunks) {
        maxRendererHorizonChunks = Math.max(2, chunks);
        save();
    }

    public void setShape(Shape shape) {
        this.shape = shape == null ? Shape.SQUARE : shape;
        save();
    }

    public void save() {
        if (loadedPath != null) {
            save(loadedPath);
        }
    }

    public void save(Path path) {
        loadedPath = path;
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            LodestoneFarClient.LOGGER.warn("Failed to save bobbieslighthouse config", e);
        }
    }

    public enum Shape {
        SQUARE,
        CIRCLE
    }
}
