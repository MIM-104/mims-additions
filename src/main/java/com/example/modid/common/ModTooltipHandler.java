package com.example.modid.common;

import com.example.modid.Tags;

import gregtech.api.GTValues;
import gregtech.api.util.GTUtility;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class ModTooltipHandler {
    private static final char FORMAT_CODE = '\u00A7';

    private ModTooltipHandler() {}

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }

        FluidStack fluid = FluidUtil.getFluidContained(stack);
        if (fluid != null && fluid.getFluid() == ModFluids.epsteinium) {
            return;
        }

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            return;
        }
        List<String> tooltip = event.getToolTip();
        if (tag.getBoolean("IsCrippled") && !tooltip.contains(TextFormatting.DARK_RED + "Crippled")) {
            tooltip.add(TextFormatting.DARK_RED + "Crippled");
        }

        long wearTicks = tag.getLong("WearTicks");
        for (int i = 0; i < tooltip.size(); i++) {
            String line = tooltip.get(i);
            if (line.contains("Voltage IN:")) {
                String updated = updateVoltageLine(line, wearTicks);
                if (updated != null) {
                    tooltip.set(i, updated);
                }
                break;
            }
        }
    }

    private static String updateVoltageLine(String line, long wearTicks) {
        int labelIndex = line.indexOf("Voltage IN:");
        if (labelIndex < 0) {
            return null;
        }
        int i = labelIndex + "Voltage IN:".length();
        int len = line.length();
        while (i < len) {
            char c = line.charAt(i);
            if (c == FORMAT_CODE && i + 1 < len) {
                i += 2;
                continue;
            }
            if (Character.isDigit(c)) {
                break;
            }
            i++;
        }
        if (i >= len || !Character.isDigit(line.charAt(i))) {
            return null;
        }
        int start = i;
        int end = i;
        while (end < len) {
            char c = line.charAt(end);
            if (c == ',' || Character.isDigit(c)) {
                end++;
                continue;
            }
            break;
        }
        String rawNumber = line.substring(start, end).replace(",", "");
        int baseEuT;
        try {
            baseEuT = Integer.parseInt(rawNumber);
        } catch (NumberFormatException ex) {
            return null;
        }
        int scaledEuT = WearUtil.scaleEuT(baseEuT, wearTicks);
        String scaledText = WearUtil.formatWithCommas(scaledEuT);
        String updated = line.substring(0, start) + scaledText + line.substring(end);
        int parenOpen = updated.indexOf('(', start);
        if (parenOpen < 0) {
            return updated;
        }
        int parenClose = updated.indexOf(')', parenOpen + 1);
        if (parenClose < 0) {
            return updated;
        }
        String tierText = getTierText(scaledEuT) + FORMAT_CODE + "f";
        return updated.substring(0, parenOpen + 1) + tierText + updated.substring(parenClose);
    }

    private static String getTierText(int eut) {
        int tier = GTUtility.getTierByVoltage(eut);
        if (tier < 0) {
            tier = 0;
        }
        if (tier >= GTValues.VNF.length) {
            tier = GTValues.VNF.length - 1;
        }
        return GTValues.VNF[tier];
    }
}
