package com.vicvic.bobbieslighthouse.bobby;

import com.vicvic.bobbieslighthouse.LodestoneFarClient;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class BobbyBridge {
    private final Minecraft client;
    private Class<?> clientChunkCacheExtClass;
    private Class<?> chunkSerializerClass;
    private Method getFakeChunkManagerMethod;
    private Method getChunkMethod;
    private Method unloadMethod;
    private Method loadMethod;
    private Method loadTagMethod;
    private Method deserializeMethod;
    private boolean warnedUnavailable;

    public BobbyBridge(Minecraft client) {
        this.client = client;
    }

    public boolean isAvailable() {
        return manager() != null;
    }

    public boolean hasChunk(int x, int z) {
        Object manager = manager();
        if (manager == null) {
            return false;
        }
        try {
            return getChunkMethod.invoke(manager, x, z) != null;
        } catch (ReflectiveOperationException e) {
            warn(e);
            return false;
        }
    }

    public void loadChunk(int x, int z, Consumer<Boolean> callback) {
        Object manager = manager();
        if (manager == null || client.level == null) {
            callback.accept(false);
            return;
        }
        try {
            @SuppressWarnings("unchecked")
            CompletableFuture<Optional<CompoundTag>> future =
                    (CompletableFuture<Optional<CompoundTag>>) loadTagMethod.invoke(manager, new ChunkPos(x, z), 0);
            Level level = client.level;
            future.thenAccept(optionalTag -> {
                if (optionalTag.isEmpty()) {
                    callback.accept(false);
                    return;
                }
                try {
                    Object pair = deserializeMethod.invoke(null, new ChunkPos(x, z), optionalTag.get(), level);
                    Object supplierObject = pair.getClass().getMethod("getRight").invoke(pair);
                    @SuppressWarnings("unchecked")
                    Supplier<LevelChunk> supplier = (Supplier<LevelChunk>) supplierObject;
                    client.execute(() -> {
                        Object currentManager = manager();
                        if (currentManager == null || client.level == null) {
                            callback.accept(false);
                            return;
                        }
                        try {
                            if (getChunkMethod.invoke(currentManager, x, z) == null) {
                                loadMethod.invoke(currentManager, x, z, supplier.get());
                            }
                            callback.accept(true);
                        } catch (ReflectiveOperationException e) {
                            warn(e);
                            callback.accept(false);
                        }
                    });
                } catch (ReflectiveOperationException e) {
                    warn(e);
                    callback.accept(false);
                }
            });
        } catch (ReflectiveOperationException e) {
            warn(e);
            callback.accept(false);
        }
    }

    public void unloadChunk(int x, int z) {
        Object manager = manager();
        if (manager == null) {
            return;
        }
        try {
            unloadMethod.invoke(manager, x, z, false);
        } catch (ReflectiveOperationException e) {
            warn(e);
        }
    }

    private Object manager() {
        if (client.level == null) {
            return null;
        }
        try {
            initializeReflection();
            Object chunkSource = client.level.getChunkSource();
            if (!clientChunkCacheExtClass.isInstance(chunkSource)) {
                return null;
            }
            return getFakeChunkManagerMethod.invoke(chunkSource);
        } catch (ReflectiveOperationException e) {
            warn(e);
            return null;
        }
    }

    private void initializeReflection() throws ReflectiveOperationException {
        if (clientChunkCacheExtClass != null) {
            return;
        }
        clientChunkCacheExtClass = Class.forName("de.johni0702.minecraft.bobby.ext.ClientChunkCacheExt");
        chunkSerializerClass = Class.forName("de.johni0702.minecraft.bobby.ChunkSerializer");
        getFakeChunkManagerMethod = clientChunkCacheExtClass.getMethod("bobby_getFakeChunkManager");

        Class<?> fakeChunkManagerClass = Class.forName("de.johni0702.minecraft.bobby.FakeChunkManager");
        getChunkMethod = fakeChunkManagerClass.getMethod("getChunk", int.class, int.class);
        unloadMethod = fakeChunkManagerClass.getMethod("unload", int.class, int.class, boolean.class);
        loadMethod = fakeChunkManagerClass.getMethod("load", int.class, int.class, LevelChunk.class);
        loadTagMethod = fakeChunkManagerClass.getDeclaredMethod("loadTag", ChunkPos.class, int.class);
        loadTagMethod.setAccessible(true);
        deserializeMethod = chunkSerializerClass.getMethod("deserialize", ChunkPos.class, CompoundTag.class, Level.class);
    }

    private void warn(Exception e) {
        if (!warnedUnavailable) {
            warnedUnavailable = true;
            LodestoneFarClient.LOGGER.warn("Bobby bridge unavailable; BobbiesLightHouse rendering is paused", e);
        }
    }
}
