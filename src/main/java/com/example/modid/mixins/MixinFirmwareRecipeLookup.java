package com.example.modid.mixins;

import com.example.modid.firmware.FirmwareData;

import gregtech.api.recipes.RecipeMap;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.Collection;

@Mixin(value = RecipeMap.class, remap = false)
public abstract class MixinFirmwareRecipeLookup {
    @ModifyVariable(method = "prepareRecipeFind", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Collection<ItemStack> modid$lookupOriginalCircuit(Collection<ItemStack> inputs) {
        boolean tagged = false;
        for (ItemStack stack : inputs)
            if (FirmwareData.configured(stack)) {
                tagged = true;
                break;
            }
        if (!tagged) return inputs;
        Collection<ItemStack> result = new ArrayList<>(inputs.size());
        for (ItemStack stack : inputs) result.add(FirmwareData.withoutFirmware(stack));
        return result;
    }
}
