package com.example.modid;

import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.items.MetaItems.*;

import gregtech.api.GTValues;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RecipeModifier {

    public static void modifyRecipes() {
        MimsAdditions.LOGGER.info("Modifying GregTech recipes...");

        modifyMicroprocessorRecipe();
        removeSOCRecipe();

        MimsAdditions.LOGGER.info("Recipe modifications complete!");
    }

    private static void modifyMicroprocessorRecipe() {
        // GCYM's Large Circuit Assembler uses this same recipe map.
        removeAllRecipesByOutput(
                RecipeMaps.CIRCUIT_ASSEMBLER_RECIPES, MICROPROCESSOR_LV.getStackForm(), "Microprocessor LV");

        RecipeMaps.CIRCUIT_ASSEMBLER_RECIPES
                .recipeBuilder()
                .input(PLASTIC_CIRCUIT_BOARD)
                .input(CENTRAL_PROCESSING_UNIT)
                .input(RESISTOR, 2)
                .input(CAPACITOR, 2)
                .input(TRANSISTOR, 2)
                .input(wireFine, Copper, 2)
                .fluidInputs(SolderingAlloy.getFluid(72))
                .output(MICROPROCESSOR_LV, 2)
                .EUt((int) GTValues.V[GTValues.UHV])
                .duration(600)
                .buildAndRegister();

        RecipeMaps.CIRCUIT_ASSEMBLER_RECIPES
                .recipeBuilder()
                .input(PLASTIC_CIRCUIT_BOARD)
                .input(CENTRAL_PROCESSING_UNIT)
                .input(RESISTOR, 2)
                .input(CAPACITOR, 2)
                .input(TRANSISTOR, 2)
                .input(wireFine, Copper, 2)
                .fluidInputs(Tin.getFluid(144))
                .output(MICROPROCESSOR_LV, 2)
                .EUt((int) GTValues.V[GTValues.UHV])
                .duration(600)
                .buildAndRegister();

        MimsAdditions.LOGGER.info("Added modified Microprocessor LV recipes: 1 amp UHV, 30 seconds");
    }

    private static void removeSOCRecipe() {
        removeAllRecipesByOutput(RecipeMaps.CIRCUIT_ASSEMBLER_RECIPES, SYSTEM_ON_CHIP.getStackForm(), "System on Chip");
    }

    private static void removeAllRecipesByOutput(RecipeMap<?> recipeMap, ItemStack outputStack, String recipeName) {
        List<Recipe> allRecipes = new ArrayList<>(recipeMap.getRecipeList());

        int removed = 0;
        for (Recipe recipe : allRecipes) {
            if (recipe.getOutputs() != null && !recipe.getOutputs().isEmpty()) {
                for (ItemStack output : recipe.getOutputs()) {
                    if (output.isItemEqual(outputStack)) {
                        boolean success = recipeMap.removeRecipe(recipe);
                        if (success) {
                            removed++;
                            MimsAdditions.LOGGER.info("Removed {} recipe: {}", recipeName, recipe);
                        }
                        break;
                    }
                }
            }
        }

        MimsAdditions.LOGGER.info("Removed {} {} recipe(s)", removed, recipeName);
    }
}
