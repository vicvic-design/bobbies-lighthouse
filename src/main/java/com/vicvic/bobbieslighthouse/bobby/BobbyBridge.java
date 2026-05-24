package com.vicvic.bobbieslighthouse.bobby;

import com.vicvic.bobbieslighthouse.LodestoneFarClient;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class BobbyBridge {
    private static final String[] CLIENT_CHUNK_EXT_CLASS_NAMES = {
            "de.johni0702.minecraft.bobby.ext.ClientChunkManagerExt",
            "de.johni0702.minecraft.bobby.ext.ClientChunkCacheExt"
    };

    private final Minecraft client;
    private Class<?> clientChunkCacheExtClass;
    private String clientChunkExtClassName;
    private Class<?> chunkSerializerClass;
    private Method getFakeChunkManagerMethod;
    private Method getChunkMethod;
    private Method unloadMethod;
    private Method loadMethod;
    private Method loadTagMethod;
    private Method shouldBeLoadedMethod;
    private Method deserializeMethod;
    private Method getFakeChunksMethod;
    private boolean warnedUnavailable;

    public BobbyBridge(Minecraft client) {
        this.client = client;
    }

    public boolean isAvailable() {
        return manager() != null;
    }

    public String diagnostics() {
        if (client.level == null) {
            return "no_world";
        }
        Object chunkSource = client.level.getChunkSource();
        try {
            initializeReflection();
        } catch (ReflectiveOperationException e) {
            return "bobby_reflection_failed:" + e.getClass().getSimpleName() + ":" + e.getMessage();
        }
        if (!clientChunkCacheExtClass.isInstance(chunkSource)) {
            return "bobby_mixin_missing:extClass=" + clientChunkExtClassName + ":chunkSource=" + chunkSource.getClass().getName();
        }
        try {
            Object fakeChunkManager = getFakeChunkManagerMethod.invoke(chunkSource);
            if (fakeChunkManager == null) {
                return "bobby_manager_null:bobby_disabled_or_not_initialized";
            }
            return "ok:manager=" + fakeChunkManager.getClass().getName();
        } catch (ReflectiveOperationException e) {
            return "bobby_manager_failed:" + e.getClass().getSimpleName() + ":" + e.getMessage();
        }
    }

    public String probe() {
        if (client.level == null) {
            return "world=no_world";
        }
        StringBuilder result = new StringBuilder();
        result.append("chunkSource=").append(client.level.getChunkSource().getClass().getName());
        try {
            initializeReflection();
            result.append(", extClass=").append(clientChunkExtClassName);
            result.append(", managerClass=found");
        } catch (ReflectiveOperationException e) {
            return result.append(", reflection=")
                    .append(e.getClass().getSimpleName())
                    .append(":")
                    .append(e.getMessage())
                    .toString();
        }
        Object manager = manager();
        if (manager == null) {
            return result.append(", manager=null, diagnostic=").append(diagnostics()).toString();
        }
        result.append(", manager=").append(manager.getClass().getName());
        result.append(", fakeChunks=").append(fakeChunkCount(manager));
        return result.toString();
    }

    public int fakeChunkCount() {
        Object manager = manager();
        if (manager == null) {
            return 0;
        }
        return fakeChunkCount(manager);
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

    public boolean isInNormalBobbyRange(int x, int z) {
        Object manager = manager();
        if (manager == null) {
            return false;
        }
        try {
            if (shouldBeLoadedMethod == null) {
                return false;
            }
            Object value = shouldBeLoadedMethod.invoke(manager, x, z);
            return value instanceof Boolean && (Boolean) value;
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
                warn("Bobby mixin interface " + clientChunkExtClassName + " is not present on chunk source " + chunkSource.getClass().getName(), null);
                return null;
            }
            Object fakeChunkManager = getFakeChunkManagerMethod.invoke(chunkSource);
            if (fakeChunkManager == null) {
                warn("Bobby fake chunk manager is null. Bobby may be disabled, not initialized for this world, or inactive in singleplayer.", null);
            }
            return fakeChunkManager;
        } catch (ReflectiveOperationException e) {
            warn("Bobby reflection failed while resolving fake chunk manager", e);
            return null;
        }
    }

    private void initializeReflection() throws ReflectiveOperationException {
        if (clientChunkCacheExtClass != null) {
            return;
        }
        clientChunkCacheExtClass = findClientChunkExtClass();
        chunkSerializerClass = Class.forName("de.johni0702.minecraft.bobby.ChunkSerializer");
        getFakeChunkManagerMethod = clientChunkCacheExtClass.getMethod("bobby_getFakeChunkManager");

        Class<?> fakeChunkManagerClass = Class.forName("de.johni0702.minecraft.bobby.FakeChunkManager");
        getChunkMethod = fakeChunkManagerClass.getMethod("getChunk", int.class, int.class);
        unloadMethod = fakeChunkManagerClass.getMethod("unload", int.class, int.class, boolean.class);
        loadMethod = fakeChunkManagerClass.getMethod("load", int.class, int.class, LevelChunk.class);
        try {
            shouldBeLoadedMethod = fakeChunkManagerClass.getMethod("shouldBeLoaded", int.class, int.class);
        } catch (NoSuchMethodException ignored) {
            shouldBeLoadedMethod = null;
        }
        loadTagMethod = fakeChunkManagerClass.getDeclaredMethod("loadTag", ChunkPos.class, int.class);
        loadTagMethod.setAccessible(true);
        getFakeChunksMethod = fakeChunkManagerClass.getMethod("getFakeChunks");
        deserializeMethod = chunkSerializerClass.getMethod("deserialize", ChunkPos.class, CompoundTag.class, Level.class);
    }

    private Class<?> findClientChunkExtClass() throws ClassNotFoundException {
        ClassNotFoundException lastFailure = null;
        for (String className : CLIENT_CHUNK_EXT_CLASS_NAMES) {
            try {
                Class<?> extClass = Class.forName(className);
                clientChunkExtClassName = className;
                return extClass;
            } catch (ClassNotFoundException e) {
                lastFailure = e;
            }
        }
        throw lastFailure == null ? new ClassNotFoundException("No Bobby client chunk extension class names configured") : lastFailure;
    }

    private int fakeChunkCount(Object manager) {
        try {
            Object value = getFakeChunksMethod.invoke(manager);
            if (value instanceof Collection<?> collection) {
                return collection.size();
            }
        } catch (ReflectiveOperationException e) {
            warn("Failed to read Bobby fake chunk count", e);
        }
        return -1;
    }

    private void warn(String message, Exception e) {
        if (!warnedUnavailable) {
            warnedUnavailable = true;
            if (e == null) {
                LodestoneFarClient.LOGGER.warn("Bobby bridge unavailable; BobbiesLightHouse rendering is paused. {}", message);
            } else {
                LodestoneFarClient.LOGGER.warn("Bobby bridge unavailable; BobbiesLightHouse rendering is paused. {}", message, e);
            }
        }
    }

    private void warn(Exception e) {
        warn("Bobby reflection failed", e);
    }
}
