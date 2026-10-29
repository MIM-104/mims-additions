package com.example.modid.firmware;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;

import net.minecraft.item.ItemStack;

public final class FirmwareMaterials {
    private FirmwareMaterials() {}

    public static Material replicationWire(int tier) {
        Material[] materials = {
            Materials.Copper,
            Materials.Copper,
            Materials.Gold,
            Materials.Electrum,
            Materials.Platinum,
            Materials.Platinum,
            Materials.Iridium,
            Materials.Osmium,
            Materials.Naquadah
        };
        return materials[Math.min(Math.max(0, tier), materials.length - 1)];
    }

    public static ItemStack wire(int tier) {
        return OreDictUnifier.get(OrePrefix.wireFine, replicationWire(tier));
    }

    public static ItemStack solder() {
        return OreDictUnifier.get(OrePrefix.wireFine, Materials.SolderingAlloy);
    }

    public static ItemStack tinWire() {
        return OreDictUnifier.get(OrePrefix.wireFine, Materials.Tin);
    }

    public static int solderWear(ItemStack wire) {
        if (same(wire, solder())) return 1;
        if (same(wire, tinWire())) return 2;
        return 0;
    }

    public static boolean isSolderWire(ItemStack wire) {
        return solderWear(wire) > 0;
    }

    public static ItemStack branch(int branch, int tier) {
        Material mechanical = tier >= 4 ? Materials.Titanium : tier >= 2 ? Materials.Steel : Materials.Iron;
        switch (branch) {
            case 0:
                return wire(tier);
            case 1:
                return OreDictUnifier.get(OrePrefix.bolt, mechanical);
            case 2:
                return OreDictUnifier.get(OrePrefix.screw, mechanical);
            case 3:
                return OreDictUnifier.get(OrePrefix.wireFine, Materials.Tin);
            default:
                return OreDictUnifier.get(OrePrefix.foil, Materials.Gold);
        }
    }

    public static boolean same(ItemStack a, ItemStack b) {
        if (a.isEmpty() || b.isEmpty()) return false;
        if (a.isItemEqual(b)) return true;
        Object entry = OreDictUnifier.getUnificationEntry(a);
        return entry != null && entry.equals(OreDictUnifier.getUnificationEntry(b));
    }
}
