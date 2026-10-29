package com.example.modid.common;

import com.example.modid.Tags;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

public final class FactoryDecayWorldData extends WorldSavedData {

    private static final String DATA_NAME = Tags.MOD_ID + "_factory_decay";
    private static final String LATE_GAME_START_TAG = "LateGameStartTime";
    private static final String HIGHEST_UNLOCKED_TIER_TAG = "HighestUnlockedCircuitTier";

    private long lateGameStartTime = -1L;
    private int highestUnlockedCircuitTier = -1;

    public FactoryDecayWorldData() {
        super(DATA_NAME);
    }

    public FactoryDecayWorldData(String name) {
        super(name);
    }

    public static FactoryDecayWorldData get(World world) {
        if (world == null || world.getMinecraftServer() == null) {
            throw new IllegalArgumentException("Factory decay data requires a server world");
        }

        WorldServer overworld = world.getMinecraftServer().getWorld(0);
        MapStorage storage = overworld.getPerWorldStorage();
        FactoryDecayWorldData data =
                (FactoryDecayWorldData) storage.getOrLoadData(FactoryDecayWorldData.class, DATA_NAME);
        if (data == null) {
            data = new FactoryDecayWorldData();
            storage.setData(DATA_NAME, data);
        }
        return data;
    }

    public long getLateGameStartTime() {
        return lateGameStartTime;
    }

    public int getHighestUnlockedCircuitTier() {
        return highestUnlockedCircuitTier;
    }

    public int unlockCircuitTier(int circuitTier) {
        if (circuitTier > highestUnlockedCircuitTier) {
            highestUnlockedCircuitTier = circuitTier;
            markDirty();
        }
        return highestUnlockedCircuitTier;
    }

    public void startLateGame(long worldTime) {
        if (lateGameStartTime < 0L) {
            lateGameStartTime = worldTime;
            markDirty();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        lateGameStartTime = nbt.hasKey(LATE_GAME_START_TAG) ? nbt.getLong(LATE_GAME_START_TAG) : -1L;
        highestUnlockedCircuitTier =
                nbt.hasKey(HIGHEST_UNLOCKED_TIER_TAG) ? nbt.getInteger(HIGHEST_UNLOCKED_TIER_TAG) : -1;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        if (lateGameStartTime >= 0L) {
            nbt.setLong(LATE_GAME_START_TAG, lateGameStartTime);
        }
        nbt.setInteger(HIGHEST_UNLOCKED_TIER_TAG, highestUnlockedCircuitTier);
        return nbt;
    }
}
