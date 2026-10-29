package com.example.modid.firmware.client;

import com.example.modid.firmware.*;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.category.GTRecipeCategory;
import gregtech.integration.jei.recipe.GTRecipeWrapper;
import gregtech.integration.jei.recipe.RecipeMapCategory;

import mezz.jei.api.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;

import net.minecraft.item.ItemStack;

import java.util.*;

/** GregTech's standard layout includes its NC marker, total EU, voltage and duration. */
@JEIPlugin
public final class FirmwareJeiPlugin implements IModPlugin {
    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        if (Boolean.getBoolean("mim.firmware.selftest")) FirmwareClientSelfTest.run(runtime);
    }

    private static final GTRecipeCategory CATEGORY =
            GTRecipeCategory.create(
                    "modid", "firmware_synthesis", "recipemap.firmware_synthesis.name", FirmwareSynthesisArray.RECIPES);

    static String categoryId() {
        return CATEGORY.getUniqueID();
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        RecipeMapCategory category =
                new RecipeMapCategory(
                        FirmwareSynthesisArray.RECIPES, CATEGORY, registry.getJeiHelpers().getGuiHelper());
        category.setIcon(new ItemStack(FirmwareContent.MEMORY_BANK));
        registry.addRecipeCategories(category);
    }

    @Override
    public void register(IModRegistry registry) {
        List<GTRecipeWrapper> recipes = new ArrayList<>();
        for (FirmwareSelectors.Selection selection : FirmwareSelectors.all()) {
            List<ItemStack> variants = FirmwareCircuits.variants(selection.tier);
            if (variants.isEmpty()) continue;
            Recipe recipe = FirmwareSynthesisArray.RECIPES.makeRecipe(variants.get(0), selection);
            recipes.add(new FirmwareWrapper(recipe, selection));
        }
        registry.addRecipes(recipes, CATEGORY.getUniqueID());
        registry.addRecipeCatalyst(FirmwareMachines.SYNTHESIS_ARRAY.getStackForm(), CATEGORY.getUniqueID());
    }

    private static final class FirmwareWrapper extends GTRecipeWrapper {
        private final FirmwareSelectors.Selection selection;

        FirmwareWrapper(Recipe recipe, FirmwareSelectors.Selection selection) {
            super(FirmwareSynthesisArray.RECIPES, recipe);
            this.selection = selection;
        }

        @Override
        public void getIngredients(IIngredients ingredients) {
            super.getIngredients(ingredients);
            List<List<ItemStack>> inputs = new ArrayList<>();
            for (List<ItemStack> options : ingredients.getInputs(VanillaTypes.ITEM)) {
                if (!options.isEmpty() && FirmwareData.isCircuit(options.get(0)))
                    inputs.add(FirmwareCircuits.variants(selection.tier));
                else inputs.add(options);
            }
            ingredients.setInputLists(VanillaTypes.ITEM, inputs);
            List<ItemStack> outputs = new ArrayList<>();
            for (ItemStack circuit : FirmwareCircuits.variants(selection.tier))
                outputs.add(FirmwareData.program(circuit, selection.target));
            ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(outputs));
        }
    }
}
