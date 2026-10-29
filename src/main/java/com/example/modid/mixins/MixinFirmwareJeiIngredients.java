package com.example.modid.mixins;

import com.example.modid.firmware.*;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.integration.jei.recipe.GTRecipeWrapper;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(value = GTRecipeWrapper.class, remap = false)
public abstract class MixinFirmwareJeiIngredients {
    @Shadow @Final private Recipe recipe;
    @Shadow @Final private List<GTRecipeInput> sortedInputs;

    @Inject(method = "getIngredients", at = @At("RETURN"))
    private void modid$showRequiredFirmware(IIngredients ingredients, CallbackInfo ci) {
        if (recipe.getOutputs().isEmpty() || !FirmwareData.requiresFirmware(recipe.getOutputs().get(0))) return;
        FirmwareTarget target = FirmwareData.describe(recipe.getOutputs().get(0));
        List<List<ItemStack>> lists = new ArrayList<>();
        for (int i = 0; i < ingredients.getInputs(VanillaTypes.ITEM).size(); i++) {
            List<ItemStack> copies = new ArrayList<>();
            for (ItemStack stack : ingredients.getInputs(VanillaTypes.ITEM).get(i)) {
                if (!sortedInputs.get(i).isNonConsumable() && FirmwareData.isCircuit(stack)) {
                    ItemStack copy = FirmwareData.program(stack, target);
                    copy.setCount(stack.getCount());
                    copies.add(copy);
                } else copies.add(stack);
            }
            lists.add(copies);
        }
        ingredients.setInputLists(VanillaTypes.ITEM, lists);
    }
}
