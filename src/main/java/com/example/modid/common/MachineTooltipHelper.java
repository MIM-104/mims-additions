package com.example.modid.common;

import com.example.modid.SupremeMachine;
import com.example.modid.Tags;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public final class MachineTooltipHelper {

    private static final String MACHINE_CATEGORY_KEY = "itemGroup.gregtech.machines";
    private static final String PARALLELISM_TOOLTIP = "Can be parallelised to 64x";
    private static final List<String> MACHINE_DESCRIPTIONS =
            Collections.unmodifiableList(
                    Arrays.asList(
                            "Stellar Crucible",
                            "Matter Obliterator",
                            "Elemental Confluence",
                            "Thunderforge Crucible",
                            "Architect of Matter",
                            "Crystalline Genesis",
                            "Titan's Platewright",
                            "Ambrosia Synthesizer",
                            "Hermetic Encapsulator",
                            "Gravitational Fractionator",
                            "Alchemical Immersion Chamber",
                            "Molecular Sovereign",
                            "Singularity Compactor",
                            "Dimensional Cleaver",
                            "Essence Ascendant",
                            "Ionic Unbinder",
                            "Magnetic Arbiter",
                            "Matter Reclaimer",
                            "Primordial Cultivator",
                            "Liquid Essence Harvester",
                            "Phasebound Crystallizer",
                            "Worldshaper's Hammer",
                            "Formwright Prime",
                            "Axial Perfection",
                            "Chaos Harmonizer",
                            "Pristine Ore Sanctum",
                            "Tesseract Packager",
                            "Domain Alignment Array",
                            "Photon Inscription Engine",
                            "Microscopic Arbiter",
                            "Thermogravitic Separator",
                            "Monofilament Ascendant",
                            "Event Horizon Collector",
                            "Abyssal Harvester",
                            "Oceanheart Extractor",
                            "Firmament Condenser",
                            "Continental Render",
                            "Cognition Foundry"));

    private MachineTooltipHelper() {}

    public static String getDescription(String machineName) {
        switch (machineName) {
            case "supreme_electric_furnace":
                return MACHINE_DESCRIPTIONS.get(0);
            case "supreme_macerator":
                return MACHINE_DESCRIPTIONS.get(1);
            case "supreme_alloy_smelter":
                return MACHINE_DESCRIPTIONS.get(2);
            case "supreme_arc_furnace":
                return MACHINE_DESCRIPTIONS.get(3);
            case "supreme_assembler":
                return MACHINE_DESCRIPTIONS.get(4);
            case "supreme_autoclave":
                return MACHINE_DESCRIPTIONS.get(5);
            case "supreme_bender":
                return MACHINE_DESCRIPTIONS.get(6);
            case "supreme_brewery":
                return MACHINE_DESCRIPTIONS.get(7);
            case "supreme_canner":
                return MACHINE_DESCRIPTIONS.get(8);
            case "supreme_centrifuge":
                return MACHINE_DESCRIPTIONS.get(9);
            case "supreme_chemical_bath":
                return MACHINE_DESCRIPTIONS.get(10);
            case "supreme_chemical_reactor":
                return MACHINE_DESCRIPTIONS.get(11);
            case "supreme_compressor":
                return MACHINE_DESCRIPTIONS.get(12);
            case "supreme_cutter":
                return MACHINE_DESCRIPTIONS.get(13);
            case "supreme_distillery":
                return MACHINE_DESCRIPTIONS.get(14);
            case "supreme_electrolyzer":
                return MACHINE_DESCRIPTIONS.get(15);
            case "supreme_electromagnetic_separator":
                return MACHINE_DESCRIPTIONS.get(16);
            case "supreme_extractor":
                return MACHINE_DESCRIPTIONS.get(17);
            case "supreme_fermenter":
                return MACHINE_DESCRIPTIONS.get(18);
            case "supreme_fluid_extractor":
                return MACHINE_DESCRIPTIONS.get(19);
            case "supreme_fluid_solidifier":
                return MACHINE_DESCRIPTIONS.get(20);
            case "supreme_forge_hammer":
                return MACHINE_DESCRIPTIONS.get(21);
            case "supreme_forming_press":
                return MACHINE_DESCRIPTIONS.get(22);
            case "supreme_lathe":
                return MACHINE_DESCRIPTIONS.get(23);
            case "supreme_mixer":
                return MACHINE_DESCRIPTIONS.get(24);
            case "supreme_ore_washer":
                return MACHINE_DESCRIPTIONS.get(25);
            case "supreme_packer":
                return MACHINE_DESCRIPTIONS.get(26);
            case "supreme_polarizer":
                return MACHINE_DESCRIPTIONS.get(27);
            case "supreme_laser_engraver":
                return MACHINE_DESCRIPTIONS.get(28);
            case "supreme_sifter":
                return MACHINE_DESCRIPTIONS.get(29);
            case "supreme_thermal_centrifuge":
                return MACHINE_DESCRIPTIONS.get(30);
            case "supreme_wiremill":
                return MACHINE_DESCRIPTIONS.get(31);
            case "supreme_item_collector":
                return MACHINE_DESCRIPTIONS.get(32);
            case "supreme_fisher":
                return MACHINE_DESCRIPTIONS.get(33);
            case "supreme_pump":
                return MACHINE_DESCRIPTIONS.get(34);
            case "supreme_air_collector":
                return MACHINE_DESCRIPTIONS.get(35);
            case "supreme_block_breaker":
                return MACHINE_DESCRIPTIONS.get(36);
            case "supreme_circuit_assembler":
                return MACHINE_DESCRIPTIONS.get(37);
            default:
                return null;
        }
    }

    public static void addTooltip(List<String> tooltip, String line) {
        if (!tooltip.contains(line)) {
            tooltip.add(line);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void placeMachineTooltips(ItemTooltipEvent event) {
        List<String> tooltip = event.getToolTip();
        MetaTileEntity machine = GTUtility.getMetaTileEntity(event.getItemStack());
        if (machine instanceof SupremeMachine) {
            String description = getDescription(machine.metaTileEntityId.getPath());
            if (description != null) {
                tooltip.removeIf(MachineTooltipHelper::isMachineDescription);
                // ItemTooltipEvent already includes the display name at index zero.
                tooltip.add(Math.min(1, tooltip.size()), TextFormatting.GRAY + description);
            }
        }

        String categoryName = I18n.format(MACHINE_CATEGORY_KEY);
        boolean hasMachineCategory = false;
        for (int index = 0; index < tooltip.size(); index++) {
            if (tooltip.get(index).contains(categoryName)) {
                hasMachineCategory = true;
                break;
            }
        }
        if (!hasMachineCategory) {
            return;
        }

        List<String> parallelism = new ArrayList<>();
        for (int index = tooltip.size() - 1; index >= 0; index--) {
            String line = tooltip.get(index);
            if (line.endsWith(PARALLELISM_TOOLTIP)) {
                parallelism.add(0, line);
                tooltip.remove(index);
            }
        }
        int categoryIndex = -1;
        for (int index = 0; index < tooltip.size(); index++) {
            if (tooltip.get(index).contains(categoryName)) {
                categoryIndex = index;
                break;
            }
        }
        tooltip.addAll(categoryIndex + 1, parallelism);
    }

    private static boolean isMachineDescription(String line) {
        String grayPrefix = TextFormatting.GRAY.toString();
        if (!line.startsWith(grayPrefix)) {
            return false;
        }
        return MACHINE_DESCRIPTIONS.contains(line.substring(grayPrefix.length()));
    }
}
