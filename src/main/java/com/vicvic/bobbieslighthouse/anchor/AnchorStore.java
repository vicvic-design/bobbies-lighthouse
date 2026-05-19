package com.vicvic.bobbieslighthouse.anchor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vicvic.bobbieslighthouse.LodestoneFarClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AnchorStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Minecraft client;
    private final Map<String, LodestoneAnchor> anchors = new LinkedHashMap<>();
    private Path currentFile;
    private String currentWorldKey = "";

    public AnchorStore(Minecraft client) {
        this.client = client;
    }

    public void openForCurrentWorld() {
        if (client.level == null) {
            return;
        }
        String worldKey = worldKey();
        if (worldKey.equals(currentWorldKey)) {
            return;
        }
        currentWorldKey = worldKey;
        currentFile = client.gameDirectory.toPath()
                .resolve(".bobby")
                .resolve("bobbieslighthouse")
                .resolve(safe(worldKey))
                .resolve(safe(dimensionKey()))
                .resolve("anchors.json");
        load();
    }

    public void close() {
        save();
        anchors.clear();
        currentFile = null;
        currentWorldKey = "";
    }

    public Collection<LodestoneAnchor> all() {
        return anchors.values();
    }

    public String currentWorldKey() {
        return currentWorldKey;
    }

    public String currentFilePath() {
        return currentFile == null ? "" : currentFile.toString();
    }

    public int size() {
        return anchors.size();
    }

    public void upsert(LodestoneAnchor anchor) {
        anchors.put(anchor.key(), anchor);
        save();
    }

    public int clear() {
        int count = anchors.size();
        anchors.clear();
        save();
        return count;
    }

    public String dimensionKey() {
        if (client.level == null) {
            return "unknown";
        }
        return client.level.dimension().location().toString();
    }

    private void load() {
        anchors.clear();
        if (currentFile == null || !Files.exists(currentFile)) {
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(currentFile)) {
            AnchorFile file = GSON.fromJson(reader, AnchorFile.class);
            if (file != null && file.anchors != null) {
                for (LodestoneAnchor anchor : file.anchors) {
                    anchors.put(anchor.key(), anchor);
                }
            }
        } catch (Exception e) {
            LodestoneFarClient.LOGGER.warn("Failed to load anchor index {}", currentFile, e);
        }
    }

    public void save() {
        if (currentFile == null) {
            return;
        }
        try {
            Files.createDirectories(currentFile.getParent());
            AnchorFile file = new AnchorFile();
            file.anchors = anchors.values().toArray(new LodestoneAnchor[0]);
            try (Writer writer = Files.newBufferedWriter(currentFile)) {
                GSON.toJson(file, writer);
            }
        } catch (IOException e) {
            LodestoneFarClient.LOGGER.warn("Failed to save anchor index {}", currentFile, e);
        }
    }

    private String worldKey() {
        if (client.getSingleplayerServer() != null) {
            return client.getSingleplayerServer().getWorldData().getLevelName();
        }
        ServerData server = client.getCurrentServer();
        if (server != null) {
            return server.ip;
        }
        return "unknown";
    }

    private static String safe(String value) {
        return value.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private static final class AnchorFile {
        LodestoneAnchor[] anchors = new LodestoneAnchor[0];
    }
}
