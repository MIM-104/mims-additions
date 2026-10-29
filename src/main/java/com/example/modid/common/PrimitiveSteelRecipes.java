package com.example.modid.common;

import com.example.modid.MimsAdditions;

import gregtech.api.GTValues;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.builders.PrimitiveRecipeBuilder;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.stack.UnificationEntry;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;

/** PBF steel times are per ingot, including the pack's two-ingot and two-block coke batches. */
public final class PrimitiveSteelRecipes {
    public static final int MIN_TICKS_PER_INGOT = 8 * 60 * 20;
    public static final int MAX_TICKS_PER_INGOT = 570 * 20;

    private PrimitiveSteelRecipes() {}

    public static long steelAmount(Recipe recipe) {
        long amount = 0;
        for (ItemStack output : recipe.getOutputs()) {
            UnificationEntry entry = OreDictUnifier.getUnificationEntry(output);
            if (entry != null && entry.material == Materials.Steel)
                amount += entry.orePrefix.getMaterialAmount(Materials.Steel) * output.getCount();
        }
        return amount;
    }

    private static boolean hasMaterial(Recipe recipe, Material material) {
        for (GTRecipeInput input : recipe.getInputs())
            for (ItemStack option : input.getInputStacks()) {
                UnificationEntry entry = OreDictUnifier.getUnificationEntry(option);
                if (entry != null && entry.material == material) return true;
            }
        return false;
    }

    private static boolean hasCoke(Recipe recipe) {
        if (hasMaterial(recipe, Materials.Coke)) return true;
        for (GTRecipeInput input : recipe.getInputs())
            for (ItemStack option : input.getInputStacks())
                for (int id : OreDictionary.getOreIDs(option)) {
                    String name = OreDictionary.getOreName(id);
                    if (name.equals("fuelCoke")
                            || name.equals("blockFuelCoke")
                            || name.equals("dustCoke")
                            || name.equals("gemCoke")
                            || name.equals("blockCoke")) return true;
                }
        return false;
    }

    public static int targetDuration(Recipe recipe) {
        long amount = steelAmount(recipe);
        if (amount <= 0) return recipe.getDuration();
        boolean wrought = hasMaterial(recipe, Materials.WroughtIron), coke = hasCoke(recipe);
        int seconds = wrought ? (coke ? 480 : 510) : (coke ? 540 : 570);
        long ticks = Math.round(seconds * 20.0 * amount / GTValues.M);
        long minimum = (long) Math.ceil(MIN_TICKS_PER_INGOT * (double) amount / GTValues.M);
        long maximum = (long) Math.floor(MAX_TICKS_PER_INGOT * (double) amount / GTValues.M);
        return (int) Math.max(minimum, Math.min(maximum, ticks));
    }

    public static void enforce() {
        int changed = 0;
        for (Recipe recipe : new ArrayList<>(RecipeMaps.PRIMITIVE_BLAST_FURNACE_RECIPES.getRecipeList())) {
            if (steelAmount(recipe) <= 0) continue;
            int duration = targetDuration(recipe);
            if (duration == recipe.getDuration()) continue;
            Recipe replacement =
                    new PrimitiveRecipeBuilder(recipe, RecipeMaps.PRIMITIVE_BLAST_FURNACE_RECIPES)
                            .duration(duration)
                            .build()
                            .getResult();
            if (replacement == null)
                throw new IllegalStateException("Could not adjust Primitive Blast Furnace steel timing");
            if (RecipeMaps.PRIMITIVE_BLAST_FURNACE_RECIPES.removeRecipe(recipe)) {
                RecipeMaps.PRIMITIVE_BLAST_FURNACE_RECIPES.compileRecipe(replacement);
                changed++;
            }
        }
        if (changed > 0)
            MimsAdditions.LOGGER.info(
                    "Updated {} Primitive Blast Furnace steel recipes to 8–9.5 minutes per ingot", changed);
    }
}
