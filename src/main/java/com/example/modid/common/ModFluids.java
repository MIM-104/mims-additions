package com.example.modid.common;

import com.example.modid.Tags;

import gregtech.api.util.FluidTooltipUtil;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.Arrays;

public final class ModFluids {

    public static Fluid epsteinium;
    public static Fluid liquidBabyVillager;
    public static Fluid moltenInfinity;
    private static boolean epsteiniumTooltipRegistered;
    private static boolean babyTooltipRegistered;
    private static boolean infinityTooltipRegistered;

    private ModFluids() {}

    public static void init() {
        epsteinium = ensureEpsteinium();
        if (epsteinium != null && !epsteiniumTooltipRegistered) {
            FluidTooltipUtil.registerTooltip(
                    epsteinium,
                    () ->
                            Arrays.asList(
                                    TextFormatting.YELLOW + "Ep",
                                    TextFormatting.RED + "Temperature: 1300 K",
                                    TextFormatting.GREEN + "State: Liquid"));
            epsteiniumTooltipRegistered = true;
        }

        liquidBabyVillager = ensureLiquidBabyVillager();
        if (liquidBabyVillager != null && !babyTooltipRegistered) {
            FluidTooltipUtil.registerTooltip(
                    liquidBabyVillager,
                    () ->
                            Arrays.asList(
                                    TextFormatting.RED + "Temperature: 310 K", TextFormatting.GREEN + "State: Liquid"));
            babyTooltipRegistered = true;
        }

        moltenInfinity = ensureMoltenInfinity();
        if (moltenInfinity != null && !infinityTooltipRegistered) {
            FluidTooltipUtil.registerTooltip(
                    moltenInfinity,
                    () ->
                            Arrays.asList(
                                    TextFormatting.RED + "Temperature: 10000 K",
                                    TextFormatting.GREEN + "State: Liquid"));
            infinityTooltipRegistered = true;
        }
    }

    private static Fluid ensureEpsteinium() {
        Fluid existing = FluidRegistry.getFluid("epsteinium");
        if (existing != null) {
            return existing;
        }

        ResourceLocation still = new ResourceLocation(Tags.MOD_ID, "blocks/epsteinium_still");
        ResourceLocation flow = new ResourceLocation(Tags.MOD_ID, "blocks/epsteinium_flow");
        Fluid fluid =
                new Fluid("epsteinium", still, flow)
                        .setDensity(1000)
                        .setViscosity(1000)
                        .setTemperature(1300)
                        .setColor(0xFFD28101);

        FluidRegistry.registerFluid(fluid);
        FluidRegistry.addBucketForFluid(fluid);
        return fluid;
    }

    private static Fluid ensureLiquidBabyVillager() {
        Fluid existing = FluidRegistry.getFluid("liquidbabyvillager");
        if (existing != null) {
            return existing;
        }

        ResourceLocation babyStill = new ResourceLocation(Tags.MOD_ID, "blocks/liquidbabyvillager_still");
        ResourceLocation babyFlow = new ResourceLocation(Tags.MOD_ID, "blocks/liquidbabyvillager_flow");

        Fluid fluid =
                new Fluid("liquidbabyvillager", babyStill, babyFlow)
                        .setDensity(1000)
                        .setViscosity(1000)
                        .setTemperature(310);

        FluidRegistry.registerFluid(fluid);
        FluidRegistry.addBucketForFluid(fluid);
        return fluid;
    }

    private static Fluid ensureMoltenInfinity() {
        Fluid existing = FluidRegistry.getFluid("molten.infinity");
        if (existing == null) {
            existing = FluidRegistry.getFluid("molten_infinity");
        }
        if (existing != null) {
            return existing;
        }

        ResourceLocation still = new ResourceLocation(Tags.MOD_ID, "blocks/molten_infinity_still");
        ResourceLocation flow = new ResourceLocation(Tags.MOD_ID, "blocks/molten_infinity_flow");
        Fluid fluid =
                new Fluid("molten_infinity", still, flow)
                        .setDensity(12000)
                        .setViscosity(10000)
                        .setTemperature(10000)
                        .setLuminosity(15)
                        .setColor(0xFFFFFFFF);

        FluidRegistry.registerFluid(fluid);
        FluidRegistry.addBucketForFluid(fluid);
        return fluid;
    }
}
