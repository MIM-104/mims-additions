package com.example.modid.firmware.client;

import com.example.modid.MimsAdditions;
import com.example.modid.Tags;
import com.example.modid.common.PrimitiveSteelRecipes;
import com.example.modid.firmware.*;

import gregtech.api.GTValues;
import gregtech.api.recipes.Recipe;
import gregtech.client.renderer.pipe.CableRenderer;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.pipelike.cable.BlockCable;
import gregtech.integration.jei.recipe.GTRecipeWrapper;

import mezz.jei.Internal;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.ingredients.Ingredients;
import mezz.jei.plugins.vanilla.crafting.ShapelessRecipeWrapper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.oredict.ShapedOreRecipe;

/** Opt-in checks against JEI's actual transformed wrapper and Minecraft's baked item models. */
public final class FirmwareClientSelfTest {
    public static void run(mezz.jei.api.IJeiRuntime runtime) {
        int checks = 0;
        try {
            for (BlockCable cable : MetaBlocks.CABLES.get(Tags.MOD_ID)) {
                ItemStack stack = cable.getItem(FirmwareAlloy.INCONEL_600);
                if (Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, null, null)
                        != CableRenderer.INSTANCE)
                    throw new IllegalStateException("Wire uses a static model: " + stack.getDisplayName());
                checks++;
            }
            for (ItemStack output :
                    new ItemStack[] {
                        MetaTileEntities.MACERATOR[GTValues.LV].getStackForm(),
                        MetaItems.ROBOT_ARM_LV.getStackForm(),
                        MetaItems.SMART_FILTER.getStackForm(),
                        MetaItems.PROSPECTOR_LV.getStackForm(),
                        MetaItems.PROSPECTOR_HV.getStackForm(),
                        MetaItems.PROSPECTOR_LUV.getStackForm()
                    }) {
                ItemStack raw = MetaItems.ELECTRONIC_CIRCUIT_LV.getStackForm();
                ShapedOreRecipe recipe =
                        new ShapedOreRecipe(
                                new ResourceLocation(Tags.MOD_ID, "jei_firmware_check"), output, "C", 'C', raw);
                Ingredients ingredients = new Ingredients();
                new ShapelessRecipeWrapper<>(Internal.getHelpers(), recipe).getIngredients(ingredients);
                ItemStack shown = ingredients.getInputs(VanillaTypes.ITEM).get(0).get(0);
                if (!FirmwareData.target(shown).equals(FirmwareData.targetKey(output)))
                    throw new IllegalStateException("JEI shows unconfigured circuit for " + output.getDisplayName());
                checks++;
            }
            mezz.jei.api.IRecipeRegistry registry = runtime.getRecipeRegistry();
            int primitiveShown = 0;
            for (mezz.jei.api.recipe.IRecipeCategory available : registry.getRecipeCategories()) {
                if (!available.getUid().contains("primitive_blast_furnace")) continue;
                for (Object wrapper : registry.getRecipeWrappers(available)) {
                    if (!(wrapper instanceof GTRecipeWrapper)) continue;
                    Recipe recipe = ((GTRecipeWrapper) wrapper).getRecipe();
                    long amount = PrimitiveSteelRecipes.steelAmount(recipe);
                    if (amount <= 0) continue;
                    double perIngot = recipe.getDuration() * (double) GTValues.M / amount;
                    if (perIngot < 9600 || perIngot > 11400)
                        throw new IllegalStateException("JEI shows outdated PBF steel duration");
                    primitiveShown++;
                    checks++;
                }
            }
            if (primitiveShown == 0) throw new IllegalStateException("No PBF steel recipes checked in JEI");
            MimsAdditions.LOGGER.info("JEI PBF TIMING VERIFIED: {} displayed steel recipes", primitiveShown);
            mezz.jei.api.recipe.IRecipeCategory category = registry.getRecipeCategory(FirmwareJeiPlugin.categoryId());
            FirmwareTarget arm = FirmwareTargets.get(FirmwareData.targetKey(MetaItems.ROBOT_ARM_IV.getStackForm()));
            for (ItemStack circuit : FirmwareCircuits.variants(GTValues.IV)) {
                ItemStack focused = FirmwareData.program(circuit, arm);
                java.util.List<?> matches =
                        registry.getRecipeWrappers(
                                category, registry.createFocus(mezz.jei.api.recipe.IFocus.Mode.OUTPUT, focused));
                if (matches.size() != 1)
                    throw new IllegalStateException(
                            "Exact robot arm firmware lookup returned " + matches.size() + " recipes");
                checks++;
            }
            int manufacturingCategories = 0;
            for (int tier = GTValues.LV; tier <= GTValues.UV; tier++) {
                for (ItemStack circuit : FirmwareCircuits.variants(tier)) {
                    ItemStack programmed = FirmwareData.program(circuit, arm);
                    mezz.jei.api.recipe.IFocus<ItemStack> rawFocus =
                            registry.createFocus(mezz.jei.api.recipe.IFocus.Mode.OUTPUT, circuit);
                    mezz.jei.api.recipe.IFocus<ItemStack> programmedFocus =
                            registry.createFocus(mezz.jei.api.recipe.IFocus.Mode.OUTPUT, programmed);
                    java.util.Set<String> shown = new java.util.HashSet<>();
                    for (mezz.jei.api.recipe.IRecipeCategory<?> available :
                            registry.getRecipeCategories(programmedFocus)) shown.add(available.getUid());
                    for (mezz.jei.api.recipe.IRecipeCategory available : registry.getRecipeCategories(rawFocus)) {
                        if (available.getUid().equals(FirmwareJeiPlugin.categoryId())) continue;
                        java.util.List<?> original = registry.getRecipeWrappers(available, rawFocus);
                        if (original.isEmpty()) continue;
                        if (!shown.contains(available.getUid()))
                            throw new IllegalStateException(
                                    "Programmed circuit hides manufacturing tab "
                                            + available.getUid()
                                            + " for "
                                            + circuit.getDisplayName());
                        java.util.List<?> linked = registry.getRecipeWrappers(available, programmedFocus);
                        if (!linked.containsAll(original))
                            throw new IllegalStateException(
                                    "Programmed circuit hides base recipes for " + circuit.getDisplayName());
                        manufacturingCategories++;
                        checks++;
                    }
                }
            }
            if (manufacturingCategories == 0)
                throw new IllegalStateException("No base circuit manufacturing recipes checked");
            FirmwareTarget evArm = FirmwareTargets.get(FirmwareData.targetKey(MetaItems.ROBOT_ARM_EV.getStackForm()));
            for (ItemStack circuit : FirmwareCircuits.variants(GTValues.EV)) {
                ItemStack programmed = FirmwareData.program(circuit, evArm);
                mezz.jei.api.recipe.IFocus<ItemStack> focus =
                        registry.createFocus(mezz.jei.api.recipe.IFocus.Mode.OUTPUT, programmed);
                if (registry.getRecipeWrappers(category, focus).size() != 1)
                    throw new IllegalStateException(
                            "EV Robot Arm firmware is not a single recipe for " + circuit.getDisplayName());
                java.util.List<mezz.jei.api.recipe.IRecipeCategory> tabs = registry.getRecipeCategories(focus);
                if (tabs.size() < 2)
                    throw new IllegalStateException("EV Robot Arm board is missing its manufacturing tabs");
                MimsAdditions.LOGGER.info(
                        "FIRMWARE LOOKUP VERIFIED: {} -> {} tabs, exactly one synthesis recipe",
                        circuit.getDisplayName(),
                        tabs.size());
                checks++;
            }
            for (ITooltipFlag flag :
                    new ITooltipFlag[] {ITooltipFlag.TooltipFlags.NORMAL, ITooltipFlag.TooltipFlags.ADVANCED}) {
                ItemStack tool = new ItemStack(FirmwareContent.SOLDERING_IRON);
                tool.setItemDamage(2);
                long lines =
                        tool.getTooltip(Minecraft.getMinecraft().player, flag).stream()
                                .map(TextFormatting::getTextWithoutFormattingCodes)
                                .filter(line -> line.startsWith("Durability:"))
                                .count();
                if (lines != 1) throw new IllegalStateException("Tool has " + lines + " durability lines");
                checks++;
            }
            if (FirmwareMaterials.solder().isEmpty())
                throw new IllegalStateException("Missing fine soldering alloy wire");
            checks++;
            FirmwareWorkbenchTile layoutBench = new FirmwareWorkbenchTile();
            FirmwareContainer layoutContainer = new FirmwareContainer(new InventoryPlayer(null), layoutBench);
            java.util.List<FirmwareWorkbenchLayout.Rect> controls =
                    new java.util.ArrayList<>(
                            java.util.Arrays.asList(
                                    FirmwareWorkbenchLayout.SEARCH,
                                    FirmwareWorkbenchLayout.PREVIOUS,
                                    FirmwareWorkbenchLayout.NEXT,
                                    FirmwareWorkbenchLayout.RESET,
                                    FirmwareWorkbenchLayout.SOLDER,
                                    FirmwareWorkbenchLayout.FLASH,
                                    FirmwareWorkbenchLayout.STATUS));
            for (int row = 0; row < 4; row++) controls.add(FirmwareWorkbenchLayout.row(row));
            java.util.List<FirmwareWorkbenchLayout.Rect> occupied = new java.util.ArrayList<>(controls);
            for (Slot slot : layoutContainer.inventorySlots)
                occupied.add(new FirmwareWorkbenchLayout.Rect(slot.xPos - 1, slot.yPos - 1, 18, 18));
            for (int i = 0; i < occupied.size(); i++) {
                FirmwareWorkbenchLayout.Rect rect = occupied.get(i);
                if (rect.x < 0 || rect.y < 0 || rect.x + rect.w > 320 || rect.y + rect.h > 240)
                    throw new IllegalStateException("Workbench control outside minimum GUI bounds");
                for (int j = i + 1; j < occupied.size(); j++)
                    if (rect.overlaps(occupied.get(j)))
                        throw new IllegalStateException("Workbench interactive regions overlap: " + i + " / " + j);
                checks++;
            }
            MimsAdditions.LOGGER.info("FIRMWARE CLIENT SELFTEST PASSED: {} native-model and JEI checks", checks);
        } catch (Throwable error) {
            MimsAdditions.LOGGER.error("FIRMWARE CLIENT SELFTEST FAILED after {} checks", checks, error);
        } finally {
            if (Boolean.getBoolean("mim.firmware.testExit"))
                Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().shutdown());
        }
    }
}
