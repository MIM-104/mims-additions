package com.example.modid.firmware;

import gregtech.api.GTValues;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.unification.ore.OrePrefix;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

/** On-demand recipes support every valid target/physical circuit pair without tens of thousands of recipes. */
public final class FirmwareRecipeMap extends RecipeMap<SimpleRecipeBuilder> {
    public FirmwareRecipeMap() {
        super("firmware_synthesis", 3, 1, 0, 0, new SimpleRecipeBuilder(), false);
    }

    @Override
    public Recipe findRecipe(long voltage, List<ItemStack> inputs, List<FluidStack> fluids, boolean exactVoltage) {
        if (voltage < GTValues.VA[GTValues.IV]) return null;
        for (ItemStack selector : inputs) {
            if (!IntCircuitIngredient.isIntegratedCircuit(selector)) continue;
            int number = IntCircuitIngredient.getCircuitConfiguration(selector);
            for (ItemStack blank : inputs)
                for (ItemStack wire : inputs) {
                    FirmwareSelectors.Selection selection = FirmwareSelectors.find(blank, number, wire);
                    if (selection == null) continue;
                    Recipe recipe = makeRecipe(blank, selection);
                    if (recipe.matches(false, inputs, fluids)) return recipe;
                }
        }
        return null;
    }

    public Recipe makeRecipe(ItemStack blank, FirmwareSelectors.Selection selection) {
        return recipeBuilder()
                // Bind this run to the physical circuit. A cached recipe must not transmute another variant.
                .input(new FirmwareRecipeInput(blank, false, 1))
                .input(OrePrefix.wireFine, selection.wire)
                .circuitMeta(selection.configuration)
                .outputs(FirmwareData.program(blank, selection.target))
                .duration(160 + 40 * selection.tier)
                .EUt((int) GTValues.VA[GTValues.IV])
                .build()
                .getResult();
    }
}
