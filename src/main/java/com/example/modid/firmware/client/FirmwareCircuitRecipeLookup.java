package com.example.modid.firmware.client;

import com.example.modid.firmware.FirmwareData;

import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/** A programmed board also links to the recipes which manufacture its original circuit. */
public final class FirmwareCircuitRecipeLookup {
    private FirmwareCircuitRecipeLookup() {}

    private static IFocus<ItemStack> rawFocus(IRecipeRegistry registry, IFocus<?> focus) {
        if (focus.getMode() != IFocus.Mode.OUTPUT || !(focus.getValue() instanceof ItemStack)) return null;
        ItemStack circuit = (ItemStack) focus.getValue();
        if (!FirmwareData.isCircuit(circuit) || !FirmwareData.configured(circuit)) return null;
        return registry.createFocus(IFocus.Mode.OUTPUT, FirmwareData.withoutFirmware(circuit));
    }

    public static List<IRecipeCategory> categories(
            IRecipeRegistry registry, IFocus<?> focus, List<IRecipeCategory> original) {
        IFocus<ItemStack> raw = rawFocus(registry, focus);
        if (raw == null) return original;
        LinkedHashSet<IRecipeCategory> categories = new LinkedHashSet<>(original);
        for (IRecipeCategory<?> category : registry.getRecipeCategories(raw)) {
            if (!category.getUid().equals(FirmwareJeiPlugin.categoryId())) categories.add(category);
        }
        return new ArrayList<>(categories);
    }

    public static <T extends IRecipeWrapper> List<T> recipes(
            IRecipeRegistry registry, IRecipeCategory<T> category, IFocus<?> focus, List<T> original) {
        if (category.getUid().equals(FirmwareJeiPlugin.categoryId())) return original;
        IFocus<ItemStack> raw = rawFocus(registry, focus);
        if (raw == null) return original;
        LinkedHashSet<T> recipes = new LinkedHashSet<>(original);
        recipes.addAll(registry.getRecipeWrappers(category, raw));
        return new ArrayList<>(recipes);
    }
}
