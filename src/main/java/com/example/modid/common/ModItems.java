package com.example.modid.common;

import com.example.modid.Tags;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.List;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class ModItems {

    public static Item titaniumCoilMicroMiner;
    public static Item omniumPlate;
    public static Item microminerMicrominer;
    public static Item stabilizedEpsteinium;
    public static Item babyVillagerEssence;
    public static Item epsteiniumInfusedWetwareMainframe;
    public static Item rtaNeutronateFineWire;

    private ModItems() {}

    public static class ItemGlint extends Item {
        @Override
        public boolean hasEffect(ItemStack stack) {
            return true;
        }
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();

        titaniumCoilMicroMiner =
                new Item() {

                    @Override
                    public void addInformation(
                            ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
                        tooltip.add(
                                TextFormatting.WHITE
                                        + ""
                                        + TextFormatting.ITALIC
                                        + "Extracts coils from obscure coil microverses.");
                    }

                    @Override
                    public String getItemStackDisplayName(ItemStack stack) {
                        return TextFormatting.YELLOW + "Titanium Plated Coil Micro Miner";
                    }
                }.setRegistryName(Tags.MOD_ID, "titaniumcoilmicrominer")
                        .setTranslationKey(Tags.MOD_ID + ".titaniumcoilmicrominer")
                        .setCreativeTab(ModCreativeTabs.MIMS_ADDITIONS)
                        .setMaxStackSize(16);

        omniumPlate =
                new ItemGlint() {

                    @Override
                    public String getItemStackDisplayName(ItemStack stack) {
                        return TextFormatting.LIGHT_PURPLE + "Omnium Plate";
                    }

                    @Override
                    public void addInformation(
                            ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
                        tooltip.add(TextFormatting.YELLOW + "Nm");
                    }
                }.setRegistryName(Tags.MOD_ID, "omnium_plate")
                        .setTranslationKey(Tags.MOD_ID + ".omnium_plate")
                        .setCreativeTab(ModCreativeTabs.MIMS_ADDITIONS)
                        .setMaxStackSize(64);

        microminerMicrominer =
                new Item() {

                    @Override
                    public void addInformation(
                            ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
                        tooltip.add(
                                TextFormatting.WHITE
                                        + ""
                                        + TextFormatting.ITALIC
                                        + "Flies to inconceivable player microverses to harvest them for their microminers.");
                    }
                }.setRegistryName(Tags.MOD_ID, "microminermicrominer")
                        .setTranslationKey(Tags.MOD_ID + ".microminermicrominer")
                        .setCreativeTab(ModCreativeTabs.MIMS_ADDITIONS)
                        .setMaxStackSize(16);

        stabilizedEpsteinium =
                new Item() {

                    @Override
                    public String getItemStackDisplayName(ItemStack stack) {
                        return TextFormatting.WHITE + "Stabilized Epsteinium";
                    }

                    @Override
                    public void addInformation(
                            ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
                        tooltip.add(TextFormatting.YELLOW + "Ep");
                    }
                }.setRegistryName(Tags.MOD_ID, "stabilized_epsteinium")
                        .setTranslationKey(Tags.MOD_ID + ".stabilized_epsteinium")
                        .setCreativeTab(ModCreativeTabs.MIMS_ADDITIONS)
                        .setMaxStackSize(64);

        babyVillagerEssence =
                new Item()
                        .setRegistryName(Tags.MOD_ID, "baby_villager_essence")
                        .setTranslationKey(Tags.MOD_ID + ".baby_villager_essence")
                        .setCreativeTab(ModCreativeTabs.MIMS_ADDITIONS)
                        .setMaxStackSize(64);

        epsteiniumInfusedWetwareMainframe =
                new Item() {
                    @Override
                    public String getItemStackDisplayName(ItemStack stack) {
                        return TextFormatting.WHITE + "Epsteinium-Infused Wetware Mainframe";
                    }

                    @Override
                    public void addInformation(
                            ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
                        tooltip.add(TextFormatting.GRAY + "The Ultimate Circuit");
                        tooltip.add(TextFormatting.GREEN + "UEV-Tier Circuit");
                    }
                }.setRegistryName(Tags.MOD_ID, "epsteinium_infused_wetware_mainframe")
                        .setTranslationKey(Tags.MOD_ID + ".epsteinium_infused_wetware_mainframe")
                        .setCreativeTab(ModCreativeTabs.MIMS_ADDITIONS)
                        .setMaxStackSize(64);

        rtaNeutronateFineWire =
                new Item()
                        .setRegistryName(Tags.MOD_ID, "rta_neutronate_fine_wire")
                        .setTranslationKey(Tags.MOD_ID + ".rta_neutronate_fine_wire")
                        .setCreativeTab(ModCreativeTabs.MIMS_ADDITIONS)
                        .setMaxStackSize(64);

        registry.register(titaniumCoilMicroMiner);
        registry.register(omniumPlate);
        registry.register(microminerMicrominer);
        registry.register(stabilizedEpsteinium);
        registry.register(babyVillagerEssence);
        registry.register(epsteiniumInfusedWetwareMainframe);
        registry.register(rtaNeutronateFineWire);
    }
}
