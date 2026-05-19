package com.vicvic.bobbieslighthouse.command;

import com.vicvic.bobbieslighthouse.anchor.AnchorScanner;
import com.vicvic.bobbieslighthouse.anchor.AnchorStore;
import com.vicvic.bobbieslighthouse.anchor.LodestoneAnchor;
import com.vicvic.bobbieslighthouse.config.LodestoneFarConfig;
import com.vicvic.bobbieslighthouse.render.AnchorRenderCoordinator;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.network.chat.Component;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class LodestoneFarCommands {
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
                                            + ", Bobby bridge: " + (renderCoordinator.isBobbyAvailable() ? "ok" : "unavailable")
                                            + ", shape: " + config.shape
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
}
