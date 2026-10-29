package com.example.modid.firmware;

import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;

import net.minecraft.item.ItemStack;

import java.util.*;

/** Unique selector pairs per input tier; allocation is deterministic for a given pack recipe set. */
public final class FirmwareSelectors {
    public static final class Selection {
        public final FirmwareTarget target;
        public final int tier, configuration;
        public final Material wire;

        Selection(FirmwareTarget target, int tier, int configuration, Material wire) {
            this.target = target;
            this.tier = tier;
            this.configuration = configuration;
            this.wire = wire;
        }

        public ItemStack selector() {
            return IntCircuitIngredient.getIntegratedCircuit(configuration);
        }

        public ItemStack wireStack() {
            return OreDictUnifier.get(OrePrefix.wireFine, wire);
        }
    }

    private static List<Selection> selections = Collections.emptyList();
    private static int generation = -1;

    public static synchronized List<Selection> all() {
        if (generation >= 0 && generation == FirmwareTargets.generation()) return selections;
        List<FirmwareTarget> targets = FirmwareTargets.all();
        List<Material> wires = new ArrayList<>();
        for (Material wire :
                new Material[] {
                    Materials.Copper,
                    Materials.Tin,
                    Materials.RedAlloy,
                    Materials.Gold,
                    Materials.Silver,
                    Materials.Electrum,
                    Materials.Platinum,
                    Materials.Cupronickel,
                    Materials.Steel,
                    Materials.TungstenSteel,
                    FirmwareAlloy.INCONEL_600
                }) {
            if (wire != null && !OreDictUnifier.get(OrePrefix.wireFine, wire).isEmpty()) wires.add(wire);
        }
        if (wires.isEmpty()) throw new IllegalStateException("No fine wires are available for firmware recipes");
        targets.sort(Comparator.comparing(target -> target.key));
        int[] counts = new int[15];
        List<Selection> result = new ArrayList<>();
        for (FirmwareTarget target : targets)
            for (int tier : target.circuitTiers.keySet()) {
                int index = counts[tier]++, configuration = index / wires.size() + 1;
                if (configuration > 32)
                    throw new IllegalStateException("Firmware selector capacity exceeded for tier " + tier);
                result.add(new Selection(target, tier, configuration, wires.get(index % wires.size())));
            }
        selections = Collections.unmodifiableList(result);
        generation = FirmwareTargets.generation();
        return selections;
    }

    public static Selection find(ItemStack blank, int configuration, ItemStack wire) {
        int tier = FirmwareData.tier(blank);
        if (tier < 0 || FirmwareData.configured(blank)) return null;
        for (Selection selection : all())
            if (selection.tier == tier
                    && selection.configuration == configuration
                    && FirmwareMaterials.same(wire, selection.wireStack())) return selection;
        return null;
    }
}
