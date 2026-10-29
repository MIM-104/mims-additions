package com.example.modid.mixins;

import com.example.modid.MimsAdditions;
import com.example.modid.common.GregtechFluidBlockRegistrar;

import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.unification.material.properties.FluidPipeProperties;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(value = TileEntityPipeBase.class, remap = false)
public abstract class MixinFluidPipeMeltdown {

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void modid$meltdownUpdate(CallbackInfo ci) {
        TileEntityPipeBase pipe = (TileEntityPipeBase) (Object) this;
        World world = pipe.getWorld();
        if (world == null || world.isRemote) {
            return;
        }

        IFluidHandler handler =
                (IFluidHandler) pipe.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
        Object nodeData = pipe.getNodeData();
        if (handler == null || !(nodeData instanceof FluidPipeProperties)) {
            return;
        }

        IFluidTankProperties[] tanks = handler.getTankProperties();
        FluidPipeProperties properties = (FluidPipeProperties) nodeData;
        for (IFluidTankProperties tank : tanks) {
            FluidStack fluidStack = tank.getContents();
            if (fluidStack == null || fluidStack.getFluid() == null) {
                continue;
            }

            boolean tooHot = fluidStack.getFluid().getTemperature(fluidStack) > properties.getMaxFluidTemperature();
            if (tooHot || !properties.test(fluidStack)) {
                spillFluidAndBreakPipe(world, pipe.getPos(), fluidStack);
                ci.cancel();
                return;
            }
        }
    }

    private static void spillFluidAndBreakPipe(World world, BlockPos pos, FluidStack fluidStack) {
        IBlockState spillState = GregtechFluidBlockRegistrar.getSourceFluidState(fluidStack.getFluid());
        if (spillState == null) {
            MimsAdditions.LOGGER.error(
                    "Cannot spill fluid {} from a failed pipe at {}; its fluid block is not registered",
                    fluidStack.getFluid().getName(),
                    pos);
            return;
        }

        world.destroyBlock(pos, false);
        if (!world.setBlockState(pos, spillState, 3)) {
            MimsAdditions.LOGGER.error(
                    "Failed to place fluid {} after breaking a pipe at {}", fluidStack.getFluid().getName(), pos);
            return;
        }

        world.playSound(null, pos, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 1.0F, 0.8F);
        igniteNearbyBlocks(world, pos);
    }

    private static void igniteNearbyBlocks(World world, BlockPos pos) {
        List<BlockPos> firePositions = new ArrayList<>();
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos candidate = pos.offset(facing);
            if (world.isAirBlock(candidate) && Blocks.FIRE.canPlaceBlockAt(world, candidate)) {
                firePositions.add(candidate);
            }
        }

        Collections.shuffle(firePositions, world.rand);
        int fireCount = Math.min(firePositions.size(), 1 + world.rand.nextInt(2));
        for (int i = 0; i < fireCount; i++) {
            world.setBlockState(firePositions.get(i), Blocks.FIRE.getDefaultState(), 3);
        }
    }
}
