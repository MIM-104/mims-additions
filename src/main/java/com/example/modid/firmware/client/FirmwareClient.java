package com.example.modid.firmware.client;

import com.example.modid.Tags;
import com.example.modid.firmware.*;

import gregtech.client.renderer.pipe.CableRenderer;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.pipelike.cable.BlockCable;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public final class FirmwareClient {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void models(ModelRegistryEvent event) {
        Item[] items = {
            Item.getItemFromBlock(FirmwareContent.WORKBENCH),
            Item.getItemFromBlock(FirmwareContent.LOGIC_CASING),
            Item.getItemFromBlock(FirmwareContent.MEMORY_BANK),
            Item.getItemFromBlock(FirmwareContent.NOETIC_COIL),
            FirmwareContent.SOLDERING_IRON,
            FirmwareContent.WIRE_STRIPPERS
        };
        for (Item item : items)
            ModelLoader.setCustomModelResourceLocation(
                    item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        // GT's generic pipe registration reserves metadata zero for a static block model.
        // Material ID zero needs an explicit cable renderer binding.
        BlockCable[] cables = MetaBlocks.CABLES.get(Tags.MOD_ID);
        if (cables != null)
            for (BlockCable cable : cables) {
                ModelLoader.setCustomMeshDefinition(
                        Item.getItemFromBlock(cable), stack -> CableRenderer.INSTANCE.getModelLocation());
                ModelLoader.setCustomModelResourceLocation(
                        Item.getItemFromBlock(cable),
                        FirmwareAlloy.INCONEL_600.getId(),
                        CableRenderer.INSTANCE.getModelLocation());
            }
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (FirmwareData.isCircuit(stack)) {
            if (FirmwareData.configured(stack)) {
                NBTTagCompound data = stack.getSubCompound(FirmwareData.TAG);
                event.getToolTip()
                        .add(1, TextFormatting.AQUA + "Firmware: " + TextFormatting.WHITE + data.getString("Name"));
            } else {
                event.getToolTip().add(1, TextFormatting.GOLD + "Unconfigured");
            }
        }
        if (stack.getItem() == FirmwareContent.SOLDERING_IRON || stack.getItem() == FirmwareContent.WIRE_STRIPPERS) {
            boolean shown =
                    event.getToolTip().stream()
                            .anyMatch(
                                    line ->
                                            TextFormatting.getTextWithoutFormattingCodes(line)
                                                    .startsWith("Durability:"));
            if (!shown)
                event.getToolTip()
                        .add(
                                TextFormatting.GRAY
                                        + "Durability: "
                                        + (stack.getMaxDamage() - stack.getItemDamage())
                                        + " / "
                                        + stack.getMaxDamage());
        }
        if (stack.getItem() == Item.getItemFromBlock(FirmwareContent.WORKBENCH)) {
            event.getToolTip().add(TextFormatting.GRAY + "Programs circuits by hand");
            event.getToolTip().add(TextFormatting.GRAY + "Requires tools and Fine Tin or Soldering Alloy Wires");
        }
    }
}
