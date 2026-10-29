package com.example.modid.common;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;

import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class FactoryDecay {

    private static final long TAX_INTERVAL_TICKS = 20L * 60L * 60L * 50L;
    private static final double TAX_PER_INTERVAL = 0.10d;
    private static final double CONTROLLER_REDUCTION = 0.02d;
    private static final long CONTROLLER_CACHE_TICKS = 20L;

    private static final Map<World, ControllerTypeCache> CONTROLLER_TYPE_CACHE = new WeakHashMap<>();

    private FactoryDecay() {}

    public static boolean isMidGameActive(World world) {
        return CircuitProgression.getHighestCircuitTier(world) >= GTValues.EV;
    }

    public static int applyRecipeEuT(MetaTileEntity machine, int baseEuT, long wearTicks) {
        if (baseEuT <= 0 || machine == null) {
            return baseEuT;
        }

        World world = machine.getWorld();
        if (world == null || world.isRemote) {
            return baseEuT;
        }

        int circuitTier = CircuitProgression.getHighestCircuitTier(world);
        int scaledEuT = circuitTier >= GTValues.EV ? WearUtil.scaleEuT(baseEuT, wearTicks) : baseEuT;

        if (circuitTier >= GTValues.ULV && isLonelySingleblock(machine)) {
            scaledEuT = scaleEuT(scaledEuT, 1.5d);
        }

        if (circuitTier >= GTValues.ZPM) {
            scaledEuT = scaleEuT(scaledEuT, getLateGameMultiplier(world));
        }

        return scaledEuT;
    }

    private static boolean isLonelySingleblock(MetaTileEntity machine) {
        if (machine instanceof MultiblockControllerBase
                || !(machine instanceof ITieredMetaTileEntity)
                || machine.getRecipeLogic() == null) {
            return false;
        }

        World world = machine.getWorld();
        BlockPos pos = machine.getPos();
        if (world == null || pos == null) {
            return false;
        }

        int tier = ((ITieredMetaTileEntity) machine).getTier();
        int adjacentSameTierMachines = 0;
        for (EnumFacing facing : EnumFacing.VALUES) {
            BlockPos adjacentPos = pos.offset(facing);
            if (!world.isBlockLoaded(adjacentPos)) {
                continue;
            }

            TileEntity tile = world.getTileEntity(adjacentPos);
            if (!(tile instanceof IGregTechTileEntity)) {
                continue;
            }

            MetaTileEntity adjacent = ((IGregTechTileEntity) tile).getMetaTileEntity();
            if (adjacent == null
                    || adjacent instanceof MultiblockControllerBase
                    || !(adjacent instanceof ITieredMetaTileEntity)
                    || adjacent.getRecipeLogic() == null) {
                continue;
            }

            if (((ITieredMetaTileEntity) adjacent).getTier() == tier && ++adjacentSameTierMachines >= 2) {
                return false;
            }
        }

        return adjacentSameTierMachines < 2;
    }

    private static double getLateGameMultiplier(World world) {
        MinecraftServer server = world.getMinecraftServer();
        if (server == null) {
            return 1.0d;
        }

        WorldServer overworld = server.getWorld(0);
        FactoryDecayWorldData data = FactoryDecayWorldData.get(overworld);
        long startTime = data.getLateGameStartTime();
        if (startTime < 0L) {
            return 1.0d;
        }

        long elapsedTicks = Math.max(0L, overworld.getTotalWorldTime() - startTime);
        long completedIntervals = elapsedTicks / TAX_INTERVAL_TICKS;
        if (completedIntervals == 0L) {
            return 1.0d;
        }

        double tax = completedIntervals * TAX_PER_INTERVAL;
        double reduction = CONTROLLER_REDUCTION * harmonicNumber(getActiveControllerTypeCount(overworld));
        reduction = Math.min(1.0d, reduction);
        return 1.0d + tax * (1.0d - reduction);
    }

    private static int getActiveControllerTypeCount(WorldServer overworld) {
        long worldTime = overworld.getTotalWorldTime();
        synchronized (CONTROLLER_TYPE_CACHE) {
            ControllerTypeCache cache = CONTROLLER_TYPE_CACHE.get(overworld);
            if (cache != null && worldTime >= cache.worldTime && worldTime - cache.worldTime < CONTROLLER_CACHE_TICKS) {
                return cache.activeTypeCount;
            }

            Set<ResourceLocation> activeControllerTypes = new HashSet<>();
            for (WorldServer world : overworld.getMinecraftServer().worlds) {
                if (world == null) {
                    continue;
                }

                for (TileEntity tile : world.loadedTileEntityList) {
                    if (!(tile instanceof IGregTechTileEntity)) {
                        continue;
                    }

                    MetaTileEntity metaTileEntity = ((IGregTechTileEntity) tile).getMetaTileEntity();
                    if (!(metaTileEntity instanceof MultiblockControllerBase)
                            || !((MultiblockControllerBase) metaTileEntity).isStructureFormed()) {
                        continue;
                    }

                    AbstractRecipeLogic recipeLogic = metaTileEntity.getRecipeLogic();
                    ResourceLocation metaTileEntityId = metaTileEntity.metaTileEntityId;
                    if (recipeLogic != null && recipeLogic.isActive() && metaTileEntityId != null) {
                        activeControllerTypes.add(metaTileEntityId);
                    }
                }
            }

            int count = activeControllerTypes.size();
            CONTROLLER_TYPE_CACHE.put(overworld, new ControllerTypeCache(worldTime, count));
            return count;
        }
    }

    private static double harmonicNumber(int count) {
        double result = 0.0d;
        for (int i = 1; i <= count; i++) {
            result += 1.0d / i;
        }
        return result;
    }

    private static int scaleEuT(int baseEuT, double multiplier) {
        double scaled = baseEuT * multiplier;
        if (scaled >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return Math.max(1, (int) Math.ceil(scaled));
    }

    private static final class ControllerTypeCache {
        private final long worldTime;
        private final int activeTypeCount;

        private ControllerTypeCache(long worldTime, int activeTypeCount) {
            this.worldTime = worldTime;
            this.activeTypeCount = activeTypeCount;
        }
    }
}
