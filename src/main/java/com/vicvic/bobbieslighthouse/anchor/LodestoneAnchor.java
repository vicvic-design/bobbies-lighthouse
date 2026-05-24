package com.vicvic.bobbieslighthouse.anchor;

public final class LodestoneAnchor {
    public String dimension;
    public int blockX;
    public int blockY;
    public int blockZ;
    public int chunkX;
    public int chunkZ;
    public long discoveredAtMillis;
    public long lastConfirmedAtMillis;
    public boolean enabled = true;
    public String disabledReason = "";

    public LodestoneAnchor() {
    }

    public LodestoneAnchor(String dimension, int blockX, int blockY, int blockZ) {
        this.dimension = dimension;
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
        this.chunkX = Math.floorDiv(blockX, 16);
        this.chunkZ = Math.floorDiv(blockZ, 16);
        long now = System.currentTimeMillis();
        this.discoveredAtMillis = now;
        this.lastConfirmedAtMillis = now;
    }

    public String key() {
        return dimension + ":" + blockX + ":" + blockY + ":" + blockZ;
    }
}
