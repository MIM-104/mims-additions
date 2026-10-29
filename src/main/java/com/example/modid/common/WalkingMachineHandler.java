package com.example.modid.common;

import com.example.modid.Tags;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class WalkingMachineHandler {

    private static final float CHANCE_PER_TICK = 0.00000007F;

    private WalkingMachineHandler() {}

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || event.world.isRemote
                || event.world.provider.getDimension() != 0
                || event.world.rand.nextFloat() >= CHANCE_PER_TICK) {
            return;
        }

        WorldServer[] worlds = event.world.getMinecraftServer().worlds;
        List<MachineTarget> candidates = new ArrayList<>();
        for (WorldServer world : worlds) {
            if (world == null) {
                continue;
            }
            for (TileEntity tile : world.loadedTileEntityList) {
                if (!(tile instanceof IGregTechTileEntity)) {
                    continue;
                }

                MetaTileEntity machine = ((IGregTechTileEntity) tile).getMetaTileEntity();
                if (machine == null
                        || machine instanceof MultiblockControllerBase
                        || machine.getRecipeLogic() == null
                        || machine.getWorld() != world) {
                    continue;
                }

                BlockPos pos = machine.getPos();
                if (pos != null && world.isBlockLoaded(pos)) {
                    candidates.add(
                            new MachineTarget(
                                    world,
                                    pos,
                                    machine.getStackForm(),
                                    world.getBlockState(pos),
                                    tile.writeToNBT(new NBTTagCompound())));
                }
            }
        }

        if (candidates.isEmpty()) {
            return;
        }

        MachineTarget target = candidates.get(event.world.rand.nextInt(candidates.size()));
        float direction = target.world.rand.nextFloat() * 360.0F;
        EntityWalkingMachine walker =
                new EntityWalkingMachine(
                        target.world,
                        target.pos.getX() + 0.5D,
                        target.pos.getY(),
                        target.pos.getZ() + 0.5D,
                        target.machineStack,
                        direction,
                        target.machineBlockState,
                        target.machineTileData);

        if (target.world.spawnEntity(walker)) {
            if (!target.world.setBlockToAir(target.pos)) {
                walker.setDead();
            }
        }
    }

    private static final class MachineTarget {
        private final WorldServer world;
        private final BlockPos pos;
        private final ItemStack machineStack;
        private final IBlockState machineBlockState;
        private final NBTTagCompound machineTileData;

        private MachineTarget(
                WorldServer world,
                BlockPos pos,
                ItemStack machineStack,
                IBlockState machineBlockState,
                NBTTagCompound machineTileData) {
            this.world = world;
            this.pos = pos;
            this.machineStack = machineStack;
            this.machineBlockState = machineBlockState;
            this.machineTileData = machineTileData;
        }
    }
}
