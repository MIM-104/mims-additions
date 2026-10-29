package com.example.modid.common;

import com.example.modid.Tags;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class NoInfiniteFluidSourcesHandler {

    private NoInfiniteFluidSourcesHandler() {}

    @SubscribeEvent
    public static void onCreateFluidSource(BlockEvent.CreateFluidSourceEvent event) {
        Block block = event.getState().getBlock();
        if (block == Blocks.WATER || block == Blocks.FLOWING_WATER) {
            return;
        }

        Fluid fluid = FluidRegistry.lookupFluidForBlock(block);
        if (fluid != null) {
            event.setResult(Event.Result.DENY);
        }
    }
}
