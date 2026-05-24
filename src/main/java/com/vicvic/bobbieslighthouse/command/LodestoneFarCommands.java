package com.vicvic.bobbieslighthouse.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vicvic.bobbieslighthouse.anchor.AnchorScanner;
import com.vicvic.bobbieslighthouse.anchor.AnchorStore;
import com.vicvic.bobbieslighthouse.anchor.LodestoneAnchor;
import com.vicvic.bobbieslighthouse.config.LodestoneFarConfig;
import com.vicvic.bobbieslighthouse.render.AnchorRenderCoordinator;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class LodestoneFarCommands {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private LodestoneFarCommands() {
    }

    public static void register(
            LodestoneFarConfig config,
            AnchorStore anchorStore,
            AnchorScanner scanner,
            AnchorRenderCoordinator renderCoordinator
    ) {
        if (!config.enableDevCommands) {
            return;
        }
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(literal("bobbieslighthouse")
                        .then(literal("status").executes(context -> {
                            context.getSource().sendFeedback(Component.literal(
                                    "Anchors: " + anchorStore.size()
                                            + ", active chunks: " + renderCoordinator.activeChunkCount()
                                            + ", queued loads: " + renderCoordinator.queuedLoadCount()
                                            + ", desired chunks: " + renderCoordinator.lastDesiredChunkCount()
                                            + ", eligible anchors: " + renderCoordinator.lastEligibleAnchorCount()
                                            + ", successful loads: " + renderCoordinator.successfulLoadCount()
                                            + ", failed loads: " + renderCoordinator.failedLoadCount()
                                            + ", stale managed chunks: " + renderCoordinator.staleManagedChunkCount()
                                            + ", render sections dirtied: " + renderCoordinator.renderSectionsDirtiedCount()
                                            + ", renderer horizon chunks: " + renderCoordinator.rendererHorizonChunks()
                                            + ", visible sections injected: " + renderCoordinator.visibleSectionsInjectedCount()
                                            + ", surface sections skipped: " + renderCoordinator.surfaceSectionsSkippedCount()
                                            + ", Bobby bridge: " + (renderCoordinator.isBobbyAvailable() ? "ok" : "unavailable")
                                            + ", diagnostic: " + renderCoordinator.bobbyDiagnostics()
                                            + ", shape: " + config.shape
                                            + ", radius: " + config.anchorRadiusChunks
                                            + ", lighthouse range: " + config.lighthouseRangeChunks
                            ));
                            return 1;
                        }))
                        .then(literal("list").executes(context -> {
                            int count = 0;
                            for (LodestoneAnchor anchor : anchorStore.all()) {
                                context.getSource().sendFeedback(Component.literal(
                                        (anchor.enabled ? "enabled" : "disabled")
                                                + " " + anchor.dimension
                                                + " block=(" + anchor.blockX + "," + anchor.blockY + "," + anchor.blockZ + ")"
                                                + " chunk=(" + anchor.chunkX + "," + anchor.chunkZ + ")"
                                                + (anchor.disabledReason.isEmpty() ? "" : " reason=" + anchor.disabledReason)
                                ));
                                count++;
                                if (count >= 20) {
                                    context.getSource().sendFeedback(Component.literal("List capped at 20 anchors."));
                                    break;
                                }
                            }
                            if (count == 0) {
                                context.getSource().sendFeedback(Component.literal("No lodestone anchors stored."));
                            }
                            return count;
                        }))
                        .then(literal("anchors").executes(context -> {
                            int count = 0;
                            ChunkPos playerChunk = Minecraft.getInstance().player == null ? null : Minecraft.getInstance().player.chunkPosition();
                            for (LodestoneAnchor anchor : anchorStore.all()) {
                                String distance = "unknown";
                                if (playerChunk != null) {
                                    distance = String.valueOf(Math.max(Math.abs(anchor.chunkX - playerChunk.x), Math.abs(anchor.chunkZ - playerChunk.z)));
                                }
                                context.getSource().sendFeedback(Component.literal(
                                        (anchor.enabled ? "enabled" : "disabled")
                                                + " distanceChunks=" + distance
                                                + " dimension=" + anchor.dimension
                                                + " block=(" + anchor.blockX + "," + anchor.blockY + "," + anchor.blockZ + ")"
                                                + " chunk=(" + anchor.chunkX + "," + anchor.chunkZ + ")"
                                                + " lastConfirmed=" + anchor.lastConfirmedAtMillis
                                                + (anchor.disabledReason.isEmpty() ? "" : " reason=" + anchor.disabledReason)
                                ));
                                count++;
                                if (count >= 20) {
                                    context.getSource().sendFeedback(Component.literal("Anchor output capped at 20 entries."));
                                    break;
                                }
                            }
                            if (count == 0) {
                                context.getSource().sendFeedback(Component.literal("No lodestone anchors stored."));
                            }
                            return count;
                        }))
                        .then(literal("explain").executes(context -> {
                            context.getSource().sendFeedback(Component.literal(renderCoordinator.explainNearestAnchor()));
                            return 1;
                        }))
                        .then(literal("bobby").executes(context -> {
                            context.getSource().sendFeedback(Component.literal(renderCoordinator.bobbyProbe()));
                            return 1;
                        }))
                        .then(literal("renderprobe").executes(context -> {
                            context.getSource().sendFeedback(Component.literal(renderCoordinator.renderProbe()));
                            return 1;
                        }))
                        .then(literal("dump").executes(context -> {
                            Path path = writeDiagnosticDump(config, anchorStore, renderCoordinator);
                            context.getSource().sendFeedback(Component.literal("Wrote diagnostic dump: " + path));
                            return 1;
                        }))
                        .then(literal("refresh").executes(context -> {
                            scanner.requestFullRefresh();
                            renderCoordinator.requestRefresh();
                            context.getSource().sendFeedback(Component.literal("BobbiesLightHouse refresh requested."));
                            return 1;
                        }))
                        .then(literal("clear").executes(context -> {
                            renderCoordinator.reset();
                            int count = anchorStore.clear();
                            context.getSource().sendFeedback(Component.literal("Cleared " + count + " lodestone anchors."));
                            return count;
                        }))));
    }

    private static Path writeDiagnosticDump(
            LodestoneFarConfig config,
            AnchorStore anchorStore,
            AnchorRenderCoordinator renderCoordinator
    ) {
        Minecraft client = Minecraft.getInstance();
        Path path = client.gameDirectory.toPath().resolve("logs").resolve("bobbieslighthouse-diagnostic.json");
        Map<String, Object> dump = new LinkedHashMap<>();
        dump.put("createdAt", Instant.now().toString());
        dump.put("mod", "BobbiesLightHouse");
        dump.put("config", config);
        dump.put("worldKey", anchorStore.currentWorldKey());
        dump.put("dimension", anchorStore.dimensionKey());
        dump.put("anchorFile", anchorStore.currentFilePath());
        dump.put("anchorCount", anchorStore.size());
        dump.put("anchors", anchorStore.all());
        dump.put("activeChunks", renderCoordinator.activeChunkCount());
        dump.put("queuedLoads", renderCoordinator.queuedLoadCount());
        dump.put("desiredChunksLastRefresh", renderCoordinator.lastDesiredChunkCount());
        dump.put("eligibleAnchorsLastRefresh", renderCoordinator.lastEligibleAnchorCount());
        dump.put("successfulLoads", renderCoordinator.successfulLoadCount());
        dump.put("failedLoads", renderCoordinator.failedLoadCount());
        dump.put("staleManagedChunksLastRefresh", renderCoordinator.staleManagedChunkCount());
        dump.put("renderSectionsDirtied", renderCoordinator.renderSectionsDirtiedCount());
        dump.put("rendererHorizonChunks", renderCoordinator.rendererHorizonChunks());
        dump.put("visibleSectionsInjected", renderCoordinator.visibleSectionsInjectedCount());
        dump.put("surfaceSectionsSkipped", renderCoordinator.surfaceSectionsSkippedCount());
        dump.put("bobbyDiagnostic", renderCoordinator.bobbyDiagnostics());
        dump.put("bobbyProbe", renderCoordinator.bobbyProbe());
        dump.put("bobbyFakeChunks", renderCoordinator.bobbyFakeChunkCount());
        dump.put("renderProbe", renderCoordinator.renderProbe());
        dump.put("explain", renderCoordinator.explainNearestAnchor());
        if (client.player != null) {
            ChunkPos playerChunk = client.player.chunkPosition();
            dump.put("playerChunk", playerChunk.x + "," + playerChunk.z);
        }
        dump.put("renderDistance", client.options.renderDistance().get());
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(dump, writer);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write diagnostic dump", e);
        }
        return path;
    }
}
