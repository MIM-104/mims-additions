package com.example.modid.firmware;

import com.example.modid.Tags;

import gregtech.api.GTValues;
import gregtech.api.fluids.FluidBuilder;
import gregtech.api.fluids.store.FluidStorageKey;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.event.MaterialEvent;
import gregtech.api.unification.material.event.MaterialRegistryEvent;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.properties.BlastProperty;
import gregtech.core.unification.material.internal.MaterialRegistryManager;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Inconel 600 and the material forms used by firmware recipes. */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class FirmwareAlloy {
    public static Material INCONEL_600;

    @SubscribeEvent
    public static void registry(MaterialRegistryEvent event) {
        MaterialRegistryManager.getInstance().createRegistry(Tags.MOD_ID);
    }

    @SubscribeEvent
    public static void materials(MaterialEvent event) {
        Materials.SolderingAlloy.addFlags(MaterialFlags.GENERATE_FINE_WIRE, MaterialFlags.GENERATE_FOIL);
        // ZPM boards use Osmium wire; generate the native GT form and its processing recipes.
        Materials.Osmium.addFlags(MaterialFlags.GENERATE_FINE_WIRE, MaterialFlags.GENERATE_FOIL);
        // Keep the saved registry ID so existing alloy items and placed wires upgrade in place.
        INCONEL_600 =
                new Material.Builder(0, new ResourceLocation(Tags.MOD_ID, "noetic_alloy"))
                        .ingot(3)
                        .liquid(new FluidBuilder().translation("modid.fluid.liquid_inconel"))
                        .color(0xB2B6B4)
                        .iconSet(MaterialIconSet.SHINY)
                        .components(Materials.Nickel, 15, Materials.Chrome, 3, Materials.Iron, 2)
                        .flags(
                                MaterialFlags.GENERATE_PLATE,
                                MaterialFlags.GENERATE_FOIL,
                                MaterialFlags.GENERATE_FINE_WIRE,
                                MaterialFlags.NO_SMELTING,
                                MaterialFlags.DISABLE_DECOMPOSITION)
                        .cableProperties(GTValues.V[GTValues.IV], 1, 4)
                        .blastTemp(3600, BlastProperty.GasTier.MID, (int) GTValues.VA[GTValues.IV], 600)
                        .build();
        MaterialRegistryManager.getInstance().getRegistry(Tags.MOD_ID).setFallbackMaterial(INCONEL_600);
    }

    public static FluidStack molten(int amount) {
        FluidStorageKey key = FluidStorageKey.getByName(new ResourceLocation("gcym", "molten"));
        Fluid fluid = key == null ? null : INCONEL_600.getFluid(key);
        if (fluid == null) throw new IllegalStateException("GCYM did not register molten Inconel 600");
        return new FluidStack(fluid, amount);
    }
}
