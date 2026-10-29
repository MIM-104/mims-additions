package com.example.modid.firmware;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.ingredients.GTRecipeInput;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

import java.util.ArrayDeque;

/** The target catalogue discovers applications from recipes that consume actual tiered circuits. */
public final class FirmwareMatching {
    public static volatile boolean enabled;
    private static final ThreadLocal<ArrayDeque<Recipe>> MATCHES = ThreadLocal.withInitial(ArrayDeque::new);

    private FirmwareMatching() {}

    public static void begin(Recipe recipe) {
        MATCHES.get().push(recipe);
    }

    public static void end() {
        ArrayDeque<Recipe> stack = MATCHES.get();
        if (!stack.isEmpty()) stack.pop();
    }

    public static boolean accepts(Recipe recipe, GTRecipeInput ingredient, ItemStack stack) {
        if (!ingredient.acceptsStack(stack)) return false;
        if (!enabled) return true;
        if (recipe.getOutputs().isEmpty()) return true;
        ItemStack output = recipe.getOutputs().get(0);
        // Circuit manufacturing and recipes without a circuit-consuming output application are unchanged.
        if (!FirmwareData.requiresFirmware(output) || FirmwareData.configured(output)) return true;
        if (MATCHES.get().isEmpty()
                || MATCHES.get().peek() != recipe
                || ingredient.isNonConsumable()
                || !FirmwareData.isCircuit(stack)) return true;
        return FirmwareData.target(stack).equals(FirmwareData.targetKey(output));
    }

    public static boolean validGrid(IRecipe recipe, InventoryCrafting grid) {
        if (!enabled) return true;
        ItemStack output = recipe.getRecipeOutput();
        if (!FirmwareData.requiresFirmware(output) || FirmwareData.configured(output)) return true;
        String target = FirmwareData.targetKey(output);
        for (int i = 0; i < grid.getSizeInventory(); i++) {
            ItemStack stack = grid.getStackInSlot(i);
            if (FirmwareData.isCircuit(stack) && !target.equals(FirmwareData.target(stack))) return false;
        }
        return true;
    }

    public static boolean craftingMatches(IRecipe recipe, InventoryCrafting grid, World world) {
        return recipe.matches(grid, world) && validGrid(recipe, grid);
    }

    public static boolean needsCraftingLookup(InventoryCrafting grid) {
        if (!enabled) return false;
        for (int i = 0; i < grid.getSizeInventory(); i++)
            if (FirmwareData.isCircuit(grid.getStackInSlot(i))) return true;
        return false;
    }

    public static IRecipe findCraftingRecipe(InventoryCrafting grid, World world) {
        // Resolve live NBT rather than allowing a cache to reuse a recipe for another firmware purpose.
        if (FirmwareEraseRecipe.INSTANCE.matches(grid, world)) return FirmwareEraseRecipe.INSTANCE;
        for (IRecipe recipe : CraftingManager.REGISTRY) if (craftingMatches(recipe, grid, world)) return recipe;
        return null;
    }
}
