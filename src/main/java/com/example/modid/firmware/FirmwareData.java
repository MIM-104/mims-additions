package com.example.modid.firmware;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTUtility;
import gregtech.common.items.MetaItems;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.oredict.OreDictionary;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Firmware is carried by the original circuit item; its tier and other NBT are preserved. */
public final class FirmwareData {
    public static final String TAG = "MimFirmware";
    private static final Map<String, Integer> TIERS = new ConcurrentHashMap<>();
    private static Set<String> standardComponents;

    private FirmwareData() {}

    public static void invalidateTierCache() {
        TIERS.clear();
    }

    public static int tier(ItemStack stack) {
        if (stack.isEmpty()) return -1;
        if (standardComponents().contains(stack.getItem().getRegistryName() + "@" + stack.getMetadata())
                || GTUtility.getMetaTileEntity(stack) != null) return -1;
        if (MetaItems.INTEGRATED_CIRCUIT != null && stack.isItemEqual(MetaItems.INTEGRATED_CIRCUIT.getStackForm()))
            return -1;
        String key = stack.getItem().getRegistryName() + "@" + stack.getMetadata();
        Integer known = TIERS.get(key);
        if (known != null) return known;
        int result = -1;
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
        UnificationEntry entry = OreDictUnifier.getUnificationEntry(stack);
        if (entry != null && entry.orePrefix == OrePrefix.circuit) {
            for (int i = 0; i < tiers.length; i++) if (entry.material == tiers[i]) result = i;
        }
        if (result < 0) {
            String[] suffixes = {
                "ulv", "lv", "mv", "hv", "ev", "iv", "luv", "zpm", "uv", "uhv", "uev", "uiv", "uxv", "opv", "max"
            };
            for (int ore : OreDictionary.getOreIDs(stack)) {
                String name = OreDictionary.getOreName(ore);
                for (int i = 0; i < suffixes.length; i++)
                    if (name.equalsIgnoreCase("circuit" + suffixes[i])) result = i;
            }
        }
        // Ore registrations can occur after recipes first inspect an item, particularly addon circuits.
        if (result >= 0) TIERS.put(key, result);
        return result;
    }

    public static boolean isCircuit(ItemStack stack) {
        return tier(stack) >= 0;
    }

    public static boolean configured(ItemStack stack) {
        return !target(stack).isEmpty();
    }

    public static String target(ItemStack stack) {
        return stack.hasTagCompound() ? stack.getTagCompound().getCompoundTag(TAG).getString("Target") : "";
    }

    public static ItemStack program(ItemStack circuit, FirmwareTarget target) {
        ItemStack output = circuit.copy();
        output.setCount(1);
        NBTTagCompound firmware = new NBTTagCompound();
        firmware.setInteger("Version", 1);
        firmware.setString("Target", target.key);
        firmware.setString("Name", target.name);
        firmware.setString("Kind", target.kind);
        output.getOrCreateSubCompound(TAG).merge(firmware);
        return output;
    }

    public static ItemStack withoutFirmware(ItemStack stack) {
        if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey(TAG)) return stack;
        ItemStack raw = stack.copy();
        raw.getTagCompound().removeTag(TAG);
        if (raw.getTagCompound().isEmpty()) raw.setTagCompound(null);
        return raw;
    }

    public static String targetKey(ItemStack output) {
        if (output.isEmpty()) return "";
        MetaTileEntity machine = GTUtility.getMetaTileEntity(output);
        return machine != null
                ? "machine:" + machine.metaTileEntityId
                : "item:" + output.getItem().getRegistryName() + "@" + output.getMetadata();
    }

    /** Every discovered circuit-consuming item recipe, except manufacturing circuits themselves. */
    public static boolean requiresFirmware(ItemStack output) {
        return !output.isEmpty() && !isCircuit(output) && FirmwareTargets.get(targetKey(output)) != null;
    }

    private static synchronized Set<String> standardComponents() {
        if (standardComponents != null) return standardComponents;
        Set<String> result = new HashSet<>();
        String[] prefixes = {
            "ROBOT_ARM_",
            "EMITTER_",
            "SENSOR_",
            "FIELD_GENERATOR_",
            "ELECTRIC_MOTOR_",
            "ELECTRIC_PUMP_",
            "ELECTRIC_PISTON_",
            "CONVEYOR_MODULE_"
        };
        for (java.lang.reflect.Field field : MetaItems.class.getFields()) {
            boolean component = false;
            for (String prefix : prefixes) if (field.getName().startsWith(prefix)) component = true;
            if (!component || !java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
            try {
                Object value = field.get(null);
                if (value instanceof MetaItem.MetaValueItem) {
                    ItemStack stack = ((MetaItem<?>.MetaValueItem) value).getStackForm();
                    result.add(stack.getItem().getRegistryName() + "@" + stack.getMetadata());
                }
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException(exception);
            }
        }
        standardComponents = result;
        return standardComponents;
    }

    public static FirmwareTarget describe(ItemStack output) {
        MetaTileEntity machine = GTUtility.getMetaTileEntity(output);
        String kind =
                machine instanceof MultiblockControllerBase ? "Controller" : machine != null ? "Machine" : "Component";
        return new FirmwareTarget(
                targetKey(output), TextFormatting.getTextWithoutFormattingCodes(output.getDisplayName()), kind, output);
    }
}
