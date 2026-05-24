package com.vicvic.bobbieslighthouse.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
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
    public int lighthouseRangeChunks = 128;
    public Shape shape = Shape.SQUARE;
    public int scanIntervalTicks = 40;
    public int maxChunksScannedPerTick = 2;
    public int renderRefreshIntervalTicks = 20;
    public int maxActiveAnchors = 8;
    public int maxExtraRenderedChunks = 512;
    public int maxChunkLoadsStartedPerTick = 32;
    public boolean enableDevCommands = true;
    private transient Path loadedPath;

    public static LodestoneFarConfig load(Path gameDirectory) {
        Path path = gameDirectory.resolve("config").resolve("bobbieslighthouse.json");
        LodestoneFarConfig config = new LodestoneFarConfig();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                migrateRange(json);
                LodestoneFarConfig loaded = GSON.fromJson(json, LodestoneFarConfig.class);
                if (loaded != null) {
                    config = loaded;
                }
            } catch (Exception e) {
                LodestoneFarClient.LOGGER.warn("Failed to load bobbieslighthouse config, using defaults", e);
            }
        }
        config.normalize();
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

    public void setLighthouseRangeChunks(int rangeChunks) {
        lighthouseRangeChunks = clampToStep(rangeChunks, 128, 1024, 128);
        save();
    }

    public void setMaxActiveAnchors(int activeAnchors) {
        maxActiveAnchors = clampToStep(activeAnchors, 2, 64, 2);
        save();
    }

    public void setMaxExtraRenderedChunks(int chunks) {
        maxExtraRenderedChunks = Math.max(16, chunks);
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

    private void normalize() {
        lighthouseRangeChunks = clampToStep(lighthouseRangeChunks, 128, 1024, 128);
        maxActiveAnchors = clampToStep(maxActiveAnchors, 2, 64, 2);
        maxChunkLoadsStartedPerTick = Math.max(16, maxChunkLoadsStartedPerTick);
        shape = shape == null ? Shape.SQUARE : shape;
    }

    private static void migrateRange(JsonObject json) {
        if (json == null) {
            return;
        }
        int range = json.has("lighthouseRangeChunks") ? json.get("lighthouseRangeChunks").getAsInt() : 128;
        if (json.has("maxAnchorDistanceChunks")) {
            range = Math.max(range, json.get("maxAnchorDistanceChunks").getAsInt());
            json.remove("maxAnchorDistanceChunks");
        }
        if (json.has("maxRendererHorizonChunks")) {
            range = Math.max(range, json.get("maxRendererHorizonChunks").getAsInt());
            json.remove("maxRendererHorizonChunks");
        }
        json.addProperty("lighthouseRangeChunks", range);
    }

    private static int clampToStep(int value, int min, int max, int step) {
        int clamped = Math.max(min, Math.min(max, value));
        return min + Math.round((float) (clamped - min) / step) * step;
    }

    public enum Shape {
        SQUARE,
        CIRCLE
    }
}
