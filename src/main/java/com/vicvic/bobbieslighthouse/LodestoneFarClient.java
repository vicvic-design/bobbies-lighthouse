package com.vicvic.bobbieslighthouse;

import com.vicvic.bobbieslighthouse.anchor.AnchorScanner;
import com.vicvic.bobbieslighthouse.anchor.AnchorStore;
import com.vicvic.bobbieslighthouse.bobby.BobbyBridge;
import com.vicvic.bobbieslighthouse.command.LodestoneFarCommands;
import com.vicvic.bobbieslighthouse.config.LodestoneFarConfig;
import com.vicvic.bobbieslighthouse.render.AnchorRenderCoordinator;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LodestoneFarClient implements ClientModInitializer {
    public static final String MOD_ID = "bobbieslighthouse";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private final Minecraft client = Minecraft.getInstance();
    private LodestoneFarConfig config;
    private AnchorStore anchorStore;
    private AnchorScanner anchorScanner;
    private AnchorRenderCoordinator renderCoordinator;
    private static AnchorRenderCoordinator activeRenderCoordinator;
    private static LodestoneFarConfig activeConfig;

    @Override
    public void onInitializeClient() {
        config = LodestoneFarConfig.load(client.gameDirectory.toPath());
        activeConfig = config;
        anchorStore = new AnchorStore(client);
        BobbyBridge bobbyBridge = new BobbyBridge(client);
        anchorScanner = new AnchorScanner(client, config, anchorStore);
        renderCoordinator = new AnchorRenderCoordinator(client, config, anchorStore, bobbyBridge);
        activeRenderCoordinator = renderCoordinator;

        ClientPlayConnectionEvents.JOIN.register((handler, sender, joinedClient) -> {
            anchorStore.openForCurrentWorld();
            renderCoordinator.reset();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, disconnectedClient) -> {
            renderCoordinator.reset();
            anchorStore.close();
        });

        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        LodestoneFarCommands.register(config, anchorStore, anchorScanner, renderCoordinator);
    }

    public static AnchorRenderCoordinator renderCoordinator() {
        return activeRenderCoordinator;
    }

    public static LodestoneFarConfig config() {
        return activeConfig;
    }

    private void tick(Minecraft ignored) {
        if (client.level == null || client.player == null) {
            return;
        }
        anchorStore.openForCurrentWorld();
        anchorScanner.tick();
        renderCoordinator.tick();
    }
}
