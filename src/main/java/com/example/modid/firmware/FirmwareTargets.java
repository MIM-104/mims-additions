package com.example.modid.firmware;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.common.items.MetaItems;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Built after scripts/recipes load; no hand-maintained list of machine metadata IDs. */
public final class FirmwareTargets {
    private static final Map<String, FirmwareTarget> TARGETS = new LinkedHashMap<>();
    private static boolean ready;
    private static boolean rebuilding;
    private static int generation;

    public static int generation() {
        return generation;
    }

    private FirmwareTargets() {}

    public static synchronized void rebuild() {
        if (rebuilding) return;
        rebuilding = true;
        try {
            FirmwareData.invalidateTierCache();
            TARGETS.clear();
            for (IRecipe recipe : ForgeRegistries.RECIPES) {
                if (recipe.getRecipeOutput().isEmpty()) continue;
                for (Ingredient ingredient : recipe.getIngredients()) {
                    for (ItemStack stack : ingredient.getMatchingStacks())
                        if (FirmwareData.isCircuit(stack)) {
                            add(recipe.getRecipeOutput(), stack);
                        }
                }
            }
            for (RecipeMap<?> map : RecipeMap.getRecipeMaps()) {
                if (map instanceof FirmwareRecipeMap) continue;
                for (Recipe recipe : map.getRecipeList()) {
                    if (recipe.getOutputs().isEmpty()) continue;
                    for (GTRecipeInput input : recipe.getInputs()) {
                        if (input.isNonConsumable() || input.getInputStacks() == null) continue;
                        for (ItemStack stack : input.getInputStacks())
                            if (FirmwareData.isCircuit(stack)) {
                                add(recipe.getOutputs().get(0), stack);
                            }
                    }
                }
            }
            ready = true;
            generation++;
        } finally {
            rebuilding = false;
        }
    }

    private static void add(ItemStack output, ItemStack circuit) {
        if (!FirmwareData.isCircuit(circuit)
                || output.isEmpty()
                || FirmwareData.isCircuit(output)
                || FirmwareData.configured(output)
                || IntCircuitIngredient.isIntegratedCircuit(output)) return;
        FirmwareTarget description = FirmwareData.describe(output);
        FirmwareTarget target = TARGETS.computeIfAbsent(description.key, k -> description);
        if (target.exampleCircuit.isEmpty() && !circuit.isEmpty()) {
            target.exampleCircuit = FirmwareData.withoutFirmware(circuit).copy();
            target.exampleCircuit.setCount(1);
        }
        if (!circuit.isEmpty())
            target.circuitTiers.putIfAbsent(FirmwareData.tier(circuit), FirmwareData.withoutFirmware(circuit).copy());
    }

    public static synchronized List<FirmwareTarget> all() {
        if (!ready && !rebuilding) rebuild();
        List<FirmwareTarget> result = new ArrayList<>(TARGETS.values());
        result.sort(Comparator.comparing((FirmwareTarget t) -> t.kind).thenComparing(t -> t.name));
        return result;
    }

    public static synchronized FirmwareTarget get(String key) {
        if (!ready && !rebuilding) rebuild();
        return TARGETS.get(key);
    }

    public static ItemStack example(FirmwareTarget target) {
        return target.exampleCircuit.isEmpty()
                ? MetaItems.ELECTRONIC_CIRCUIT_LV.getStackForm()
                : target.exampleCircuit.copy();
    }
}
