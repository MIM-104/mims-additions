package com.example.modid.firmware;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;

import net.minecraft.item.ItemStack;

import java.util.*;

public final class FirmwareCircuits {
    public static Material marker(int tier) {
        Material[] tiers = {
            MarkerMaterials.Tier.ULV,
            MarkerMaterials.Tier.LV,
            MarkerMaterials.Tier.MV,
            MarkerMaterials.Tier.HV,
            MarkerMaterials.Tier.EV,
            MarkerMaterials.Tier.IV,
            MarkerMaterials.Tier.LuV,
            MarkerMaterials.Tier.ZPM,
            MarkerMaterials.Tier.UV,
            MarkerMaterials.Tier.UHV,
            MarkerMaterials.Tier.UEV,
            MarkerMaterials.Tier.UIV,
            MarkerMaterials.Tier.UXV,
            MarkerMaterials.Tier.OpV,
            MarkerMaterials.Tier.MAX
        };
        return tiers[tier];
    }

    public static List<ItemStack> variants(int tier) {
        List<ItemStack> result = new ArrayList<>();
        List<ItemStack> registered = OreDictUnifier.getAll(new UnificationEntry(OrePrefix.circuit, marker(tier)));
        for (ItemStack stack : registered)
            if (FirmwareData.tier(stack) == tier) {
                boolean duplicate = false;
                for (ItemStack old : result)
                    if (old.isItemEqual(stack)) {
                        duplicate = true;
                        break;
                    }
                if (!duplicate) {
                    ItemStack copy = FirmwareData.withoutFirmware(stack).copy();
                    copy.setCount(1);
                    result.add(copy);
                }
            }
        return result;
    }
}
