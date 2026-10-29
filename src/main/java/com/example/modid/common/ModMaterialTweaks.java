package com.example.modid.common;

import com.example.modid.MimsAdditions;
import com.example.modid.Tags;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.event.PostMaterialEvent;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.WireProperties;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.pipelike.cable.BlockCable;
import gregtech.core.unification.material.internal.MaterialRegistryManager;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class ModMaterialTweaks {
    private static final int HEX_WIRE_MULTIPLIER = 16;
    private static final int TARGET_HEX_MAX_AMPS = 9_999_999;
    private static final int OMNIUM_BASE_AMPS =
            (int) Math.ceil((double) TARGET_HEX_MAX_AMPS / (double) HEX_WIRE_MULTIPLIER);

    private ModMaterialTweaks() {}

    public static void apply() {
        enforceWireLoss();
        // Refresh the actual registered cable blocks as well as the material definitions.
        for (BlockCable[] variants : MetaBlocks.CABLES.values()) {
            for (BlockCable cable : variants) {
                for (Material material : new java.util.ArrayList<>(cable.getEnabledMaterials())) {
                    WireProperties properties = material.getProperty(PropertyKey.WIRE);
                    if (properties != null) cable.addCableMaterial(material, properties);
                }
            }
        }
        Material omnium = findOmnium();
        if (omnium == null) {
            MimsAdditions.LOGGER.warn("Omnium material not found; skipping wire amperage override");
            return;
        }
        if (!omnium.hasProperty(PropertyKey.WIRE)) {
            MimsAdditions.LOGGER.warn("Omnium has no wire properties; skipping wire amperage override");
            return;
        }
        WireProperties wire = omnium.getProperty(PropertyKey.WIRE);
        if (wire == null) {
            MimsAdditions.LOGGER.warn("Omnium wire properties are null; skipping wire amperage override");
            return;
        }
        wire.setAmperage(OMNIUM_BASE_AMPS);
        MimsAdditions.LOGGER.info(
                "Set Omnium wire base amperage to {} (hex max {})",
                OMNIUM_BASE_AMPS,
                OMNIUM_BASE_AMPS * HEX_WIRE_MULTIPLIER);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void afterMaterials(PostMaterialEvent event) {
        // Set the flags before GT generates wire/cable forms and insulation recipes.
        enforceWireLoss();
    }

    private static void enforceWireLoss() {
        Material omnium = findOmnium();
        int changed = 0;
        for (Material material : MaterialRegistryManager.getInstance().getRegisteredMaterials()) {
            WireProperties wire = material.getProperty(PropertyKey.WIRE);
            if (wire == null) continue;
            if (material == omnium) {
                if (!wire.isSuperconductor() || wire.getLossPerBlock() != 0) changed++;
                wire.setSuperconductor(true);
                wire.setLossPerBlock(0);
            } else {
                if (wire.isSuperconductor()
                        || wire.getLossPerBlock() < 1
                        || wire.getSuperconductorCriticalTemperature() != 0) changed++;
                wire.setSuperconductor(false);
                wire.setSuperconductorCriticalTemperature(0);
                wire.setLossPerBlock(Math.max(1, wire.getLossPerBlock()));
            }
        }
        if (changed > 0)
            MimsAdditions.LOGGER.info(
                    "Updated {} wire materials: only Omnium is superconducting; all others have loss >= 1 EU/block",
                    changed);
    }

    public static Material findOmnium() {
        Material fromLabs = getOmniumFromLabsMaterials();
        if (fromLabs != null) {
            return fromLabs;
        }
        MaterialRegistryManager registry = MaterialRegistryManager.getInstance();
        Material mat = registry.getMaterial("nomilabs:omnium");
        if (mat != null) {
            return mat;
        }
        return registry.getMaterial("omnium");
    }

    public static Material findMolybdenumDisilicide() {
        MaterialRegistryManager registry = MaterialRegistryManager.getInstance();
        Material material = registry.getMaterial("molybdenum_disilicide");
        if (material == null) {
            material = registry.getMaterial("gregtech:molybdenum_disilicide");
        }
        if (material == null) {
            material = registry.getMaterial("nomilabs:molybdenum_disilicide");
        }
        return material;
    }

    private static Material getOmniumFromLabsMaterials() {
        try {
            Class<?> labsMaterials = Class.forName("com.nomiceu.nomilabs.gregtech.material.registry.LabsMaterials");
            Field field = labsMaterials.getDeclaredField("Omnium");
            Object value = field.get(null);
            if (value instanceof Material) {
                return (Material) value;
            }
        } catch (Exception e) {
            MimsAdditions.LOGGER.debug("LabsMaterials Omnium lookup failed", e);
        }
        return null;
    }
}
