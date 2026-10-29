package com.example.modid.common;

import com.example.modid.Tags;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class HotFluidErosionHandler {

    private static final int HOT_EROSION_KELVIN = 2274;
    private static final int DISCOVERY_COLUMNS = 4;
    private static final int HOT_COLUMNS = 64;
    private static final int SCAN_INTERVAL_TICKS = 20;
    private static final Map<Integer, Map<Long, Integer>> CHUNK_SCAN_INDICES = new HashMap<>();
    private static final Map<Integer, Set<Long>> HOT_CHUNKS = new HashMap<>();

    private HotFluidErosionHandler() {}

    @SubscribeEvent
    public static void onFluidMotion(FluidEvent.FluidMotionEvent event) {
        World world = event.getWorld();
        if (world.isRemote) {
            return;
        }

        FluidStack fluidStack = event.getFluid();
        if (fluidStack == null || fluidStack.getFluid() == null) {
            return;
        }

        int temp = fluidStack.getFluid().getTemperature(fluidStack);
        if (temp < HOT_EROSION_KELVIN) {
            return;
        }

        markHotChunk(world, event.getPos());
        erodeBelow(world, event.getPos());
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        World world = event.world;
        if (event.phase != TickEvent.Phase.END || world.isRemote) {
            return;
        }

        if (!(world instanceof WorldServer) || world.getTotalWorldTime() % SCAN_INTERVAL_TICKS != 0) {
            return;
        }

        WorldServer worldServer = (WorldServer) world;
        int dimension = world.provider.getDimension();
        Map<Long, Integer> scanIndices = CHUNK_SCAN_INDICES.computeIfAbsent(dimension, key -> new HashMap<>());
        Set<Long> hotChunks = HOT_CHUNKS.computeIfAbsent(dimension, key -> new HashSet<>());
        Set<Long> loadedKeys = new HashSet<>();

        for (Chunk chunk : worldServer.getChunkProvider().getLoadedChunks()) {
            long chunkKey = packChunk(chunk.x, chunk.z);
            loadedKeys.add(chunkKey);
            int nextIndex = scanIndices.getOrDefault(chunkKey, world.rand.nextInt(256));
            int maxY = Math.max(1, world.getActualHeight()) - 1;
            int columns = hotChunks.contains(chunkKey) ? HOT_COLUMNS : DISCOVERY_COLUMNS;

            for (int sample = 0; sample < columns; sample++) {
                int idx = (nextIndex + sample) & 255;
                int localX = idx & 15;
                int localZ = (idx >> 4) & 15;
                int x = (chunk.x << 4) + localX;
                int z = (chunk.z << 4) + localZ;

                boolean found = erodeHotFluidInColumn(world, x, z, maxY);
                if (found) {
                    hotChunks.add(chunkKey);
                }
            }

            scanIndices.put(chunkKey, (nextIndex + columns) & 255);
        }

        scanIndices.keySet().removeIf(key -> !loadedKeys.contains(key));
        hotChunks.removeIf(key -> !loadedKeys.contains(key));
    }

    private static boolean erodeHotFluidInColumn(World world, int x, int z, int maxY) {
        boolean found = false;
        for (int y = maxY; y >= 0; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (!world.isBlockLoaded(pos)) {
                continue;
            }
            FluidStack fluidStack = getFluidStackAt(world, pos);
            if (fluidStack == null || fluidStack.getFluid() == null) {
                continue;
            }
            int temp = fluidStack.getFluid().getTemperature(fluidStack);
            if (temp >= HOT_EROSION_KELVIN) {
                found = true;
                erodeBelow(world, pos);
            }
        }
        return found;
    }

    private static long packChunk(int x, int z) {
        return ((long) x << 32) ^ (z & 0xffffffffL);
    }

    private static void markHotChunk(World world, BlockPos pos) {
        if (!(world instanceof WorldServer)) {
            return;
        }
        int dimension = world.provider.getDimension();
        Set<Long> hotChunks = HOT_CHUNKS.computeIfAbsent(dimension, key -> new HashSet<>());
        hotChunks.add(packChunk(pos.getX() >> 4, pos.getZ() >> 4));
    }

    private static void erodeBelow(World world, BlockPos pos) {
        BlockPos below = pos.down();
        if (!world.isBlockLoaded(below)) {
            return;
        }

        Block block = world.getBlockState(below).getBlock();
        if (block == Blocks.AIR || block == Blocks.BEDROCK) {
            return;
        }

        Material material = world.getBlockState(below).getMaterial();
        if (material.isLiquid()) {
            return;
        }

        if (block.getBlockHardness(world.getBlockState(below), world, below) < 0) {
            return;
        }

        world.destroyBlock(below, true);
    }

    private static FluidStack getFluidStackAt(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        if (block instanceof IFluidBlock) {
            FluidStack drained = ((IFluidBlock) block).drain(world, pos, false);
            if (drained != null) {
                return drained;
            }
            Fluid fluid = ((IFluidBlock) block).getFluid();
            if (fluid != null) {
                return new FluidStack(fluid, Fluid.BUCKET_VOLUME);
            }
        }
        Fluid fluid = FluidRegistry.lookupFluidForBlock(block);
        return fluid == null ? null : new FluidStack(fluid, Fluid.BUCKET_VOLUME);
    }
}
