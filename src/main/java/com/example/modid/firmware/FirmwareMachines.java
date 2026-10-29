package com.example.modid.firmware;

import com.example.modid.Tags;

import gregtech.common.metatileentities.MetaTileEntities;

import net.minecraft.util.ResourceLocation;

public final class FirmwareMachines {
    public static FirmwareSynthesisArray SYNTHESIS_ARRAY;

    private FirmwareMachines() {}

    public static void init() {
        SYNTHESIS_ARRAY =
                MetaTileEntities.registerMetaTileEntity(
                        32039,
                        new FirmwareSynthesisArray(new ResourceLocation(Tags.MOD_ID, "firmware_synthesis_array")));
    }
}
