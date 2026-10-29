package com.example.modid.firmware;

import gregtech.api.GTValues;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.builders.BlastRecipeBuilder;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public final class FirmwareRecipes {
    private FirmwareRecipes() {}

    public static void register() {
        // Circuit-free steel bootstrap: no powered assembler can be required before the first firmware.
        ModHandler.addShapedRecipe(
                "precision_firmware_workbench",
                new ItemStack(FirmwareContent.WORKBENCH),
                "PSP",
                "GTG",
                "FWF",
                'P',
                "plateSteel",
                'S',
                "screwSteel",
                'G',
                "gearSmallSteel",
                'T',
                new ItemStack(Blocks.CRAFTING_TABLE),
                'F',
                "frameGtSteel",
                'W',
                "wireFineCopper");
        ModHandler.addShapedRecipe(
                "precision_soldering_iron",
                new ItemStack(FirmwareContent.SOLDERING_IRON),
                "fPC",
                "RSW",
                "BRh",
                'P',
                "plateCopper",
                'C',
                "wireFineTin",
                'R',
                "stickSteel",
                'S',
                "screwSteel",
                'W',
                "wireFineCopper",
                'B',
                "plateRubber");
        ModHandler.addShapedRecipe(
                "precision_wire_strippers",
                new ItemStack(FirmwareContent.WIRE_STRIPPERS),
                "PfP",
                "RSR",
                "BhB",
                'P',
                "plateSteel",
                'R',
                "stickSteel",
                'S',
                "screwSteel",
                'B',
                "plateRubber");

        RecipeMap<?> abs = RecipeMap.getByName("alloy_blast_smelter");
        if (abs == null) throw new IllegalStateException("Inconel 600 requires the Alloy Blast Smelter recipe map");
        // Replace the generated alloy recipes with the pack's IV recipe, preserving its two fluid states.
        for (Recipe recipe : new java.util.ArrayList<>(abs.getRecipeList())) {
            for (FluidStack output : recipe.getFluidOutputs())
                if (output.isFluidEqual(FirmwareAlloy.molten(1))
                        || output.isFluidEqual(FirmwareAlloy.INCONEL_600.getFluid(1))) {
                    abs.removeRecipe(recipe);
                    break;
                }
        }
        ((BlastRecipeBuilder) abs.recipeBuilder())
                .input(OrePrefix.dust, Materials.Nickel, 15)
                .input(OrePrefix.dust, Materials.Chrome, 3)
                .input(OrePrefix.dust, Materials.Iron, 2)
                .circuitMeta(3)
                .fluidOutputs(FirmwareAlloy.molten(2880))
                .blastFurnaceTemp(3600)
                .duration(1200)
                .EUt((int) GTValues.VA[GTValues.IV])
                .buildAndRegister();
        boolean hasCasting = false;
        for (Recipe recipe : RecipeMaps.VACUUM_RECIPES.getRecipeList())
            for (GTRecipeInput input : recipe.getFluidInputs())
                if (input.acceptsFluid(FirmwareAlloy.molten(144))) hasCasting = true;
        if (!hasCasting)
            RecipeMaps.VACUUM_RECIPES
                    .recipeBuilder()
                    .fluidInputs(FirmwareAlloy.molten(144))
                    .notConsumable(MetaItems.SHAPE_MOLD_INGOT)
                    .output(OrePrefix.ingot, FirmwareAlloy.INCONEL_600)
                    .duration(100)
                    .EUt((int) GTValues.VA[GTValues.MV])
                    .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES
                .recipeBuilder()
                .input(OrePrefix.plate, Materials.TungstenSteel, 6)
                .input(OrePrefix.frameGt, Materials.Titanium)
                .input(OrePrefix.wireFine, Materials.Platinum, 8)
                .input(OrePrefix.bolt, Materials.Titanium, 8)
                .fluidInputs(Materials.SolderingAlloy.getFluid(288))
                .circuitMeta(6)
                .outputs(new ItemStack(FirmwareContent.LOGIC_CASING, 4))
                .duration(300)
                .EUt((int) GTValues.VA[GTValues.IV])
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES
                .recipeBuilder()
                .input(OrePrefix.plate, Materials.TungstenSteel, 4)
                .input(OrePrefix.frameGt, Materials.Titanium)
                .input(MetaItems.RANDOM_ACCESS_MEMORY, 32)
                .input(MetaItems.NAND_MEMORY_CHIP, 16)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.IV, 2)
                .input(OrePrefix.wireFine, FirmwareAlloy.INCONEL_600, 16)
                .fluidInputs(Materials.SolderingAlloy.getFluid(576))
                .circuitMeta(14)
                .outputs(new ItemStack(FirmwareContent.MEMORY_BANK))
                .duration(400)
                .EUt((int) GTValues.VA[GTValues.IV])
                .buildAndRegister();
        // GT coil convention: wound double wire, metallic foil, frame and molten binder.
        RecipeMaps.ASSEMBLER_RECIPES
                .recipeBuilder()
                .input(OrePrefix.wireGtDouble, FirmwareAlloy.INCONEL_600, 16)
                .input(OrePrefix.foil, Materials.TungstenSteel, 16)
                .input(OrePrefix.frameGt, Materials.Titanium)
                .input(OrePrefix.bolt, Materials.Titanium, 8)
                .input(OrePrefix.dust, Materials.SiliconDioxide, 4)
                .fluidInputs(Materials.SolderingAlloy.getFluid(576))
                .circuitMeta(4)
                .outputs(new ItemStack(FirmwareContent.NOETIC_COIL))
                .duration(400)
                .EUt((int) GTValues.VA[GTValues.IV])
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES
                .recipeBuilder()
                .inputs(
                        new ItemStack(FirmwareContent.LOGIC_CASING, 4),
                        new ItemStack(FirmwareContent.MEMORY_BANK, 2),
                        MetaTileEntities.HULL[GTValues.IV].getStackForm())
                .input(MetaItems.ROBOT_ARM_IV, 2)
                .input(MetaItems.EMITTER_IV, 2)
                .input(MetaItems.SENSOR_IV, 2)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.IV, 4)
                .input(OrePrefix.wireFine, FirmwareAlloy.INCONEL_600, 32)
                .fluidInputs(Materials.SolderingAlloy.getFluid(1152))
                .outputs(FirmwareMachines.SYNTHESIS_ARRAY.getStackForm())
                .duration(1200)
                .EUt((int) GTValues.VA[GTValues.IV])
                .buildAndRegister();
        enforceAbsOnly();
    }

    /** Keep existing hot-ingot/fluid registrations compatible, while primary alloy production is ABS-only. */
    public static void enforceAbsOnly() {
        for (RecipeMap<?> map :
                new RecipeMap<?>[] {
                    RecipeMaps.BLAST_RECIPES, RecipeMaps.MIXER_RECIPES, RecipeMaps.ALLOY_SMELTER_RECIPES
                }) {
            for (Recipe recipe : new java.util.ArrayList<>(map.getRecipeList())) {
                for (ItemStack output : recipe.getOutputs()) {
                    UnificationEntry entry = OreDictUnifier.getUnificationEntry(output);
                    if (entry != null && entry.material == FirmwareAlloy.INCONEL_600) {
                        map.removeRecipe(recipe);
                        break;
                    }
                }
            }
        }
    }
}
