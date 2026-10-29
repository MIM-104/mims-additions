package com.example.modid.common;

import com.example.modid.Tags;

import gregtech.api.fluids.GTFluid;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class GregtechFluidBlockRegistrar {

    public static final List<BlockFluidClassic> REGISTERED_BLOCKS = new ArrayList<>();
    private static final Map<Fluid, BlockFluidClassic> FLUID_BLOCKS = new IdentityHashMap<>();
    private static final int HOT_LIGHT_KELVIN = 1812;

    private GregtechFluidBlockRegistrar() {}

    @SubscribeEvent
    public static void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        ModFluids.init();
        registerCustomFluidBlock(event, "epsteinium");
        registerCustomFluidBlock(event, "liquidbabyvillager");

        for (Fluid fluid : FluidRegistry.getRegisteredFluids().values()) {
            if (FLUID_BLOCKS.containsKey(fluid)) {
                continue;
            }

            Material material = fluid.getTemperature() > 1000 ? Material.LAVA : Material.WATER;
            BlockFluidClassic block = new BlockFluidClassic(fluid, material);
            applyHeatLight(block, fluid);
            String prefix = fluid instanceof GTFluid ? "gt_fluid_" : "fluid_";
            String name = prefix + sanitize(fluid.getName());
            block.setRegistryName(new ResourceLocation(Tags.MOD_ID, name));
            block.setTranslationKey(Tags.MOD_ID + "." + name);
            event.getRegistry().register(block);
            REGISTERED_BLOCKS.add(block);
            FLUID_BLOCKS.put(fluid, block);
        }
    }

    public static IBlockState getSourceFluidState(Fluid fluid) {
        if (fluid == null) {
            return null;
        }

        Block block = FLUID_BLOCKS.get(fluid);
        if (!(block instanceof BlockFluidBase)) {
            return null;
        }

        return block.getDefaultState().withProperty(BlockFluidBase.LEVEL, 0);
    }

    private static void registerCustomFluidBlock(RegistryEvent.Register<Block> event, String fluidName) {
        Fluid fluid = FluidRegistry.getFluid(fluidName);
        if (fluid == null || hasLiquidBlock(fluid.getBlock())) {
            return;
        }

        Material material = fluid.getTemperature() > 1000 ? Material.LAVA : Material.WATER;
        BlockFluidClassic block = new BlockFluidClassic(fluid, material);
        applyHeatLight(block, fluid);
        String name = "fluid_" + sanitize(fluid.getName());
        block.setRegistryName(new ResourceLocation(Tags.MOD_ID, name));
        block.setTranslationKey(Tags.MOD_ID + "." + name);
        event.getRegistry().register(block);
        REGISTERED_BLOCKS.add(block);
        FLUID_BLOCKS.put(fluid, block);
    }

    private static boolean hasLiquidBlock(Block block) {
        return block instanceof IFluidBlock || block instanceof BlockLiquid;
    }

    private static void applyHeatLight(BlockFluidClassic block, Fluid fluid) {
        int temp = fluid.getTemperature();
        int luminosity = fluid.getLuminosity();
        if (temp >= HOT_LIGHT_KELVIN) {
            block.setMaxScaledLight(Math.max(1, luminosity));
        } else if (luminosity > 0) {
            block.setMaxScaledLight(0);
        }
    }

    private static String sanitize(String name) {
        return name.toLowerCase().replace('.', '_');
    }
}
