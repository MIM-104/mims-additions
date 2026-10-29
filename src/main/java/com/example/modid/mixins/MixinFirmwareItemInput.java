package com.example.modid.mixins;

import com.example.modid.firmware.FirmwareData;

import gregtech.api.recipes.ingredients.GTRecipeItemInput;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Remove only this mod's firmware tag for base item identity; the recipe checks the purpose separately. */
@Mixin(value = GTRecipeItemInput.class, remap = false)
public abstract class MixinFirmwareItemInput {
    @ModifyVariable(method = "acceptsStack", at = @At("HEAD"), argsOnly = true)
    private ItemStack modid$preserveCircuitIdentity(ItemStack stack) {
        return stack == null ? null : FirmwareData.withoutFirmware(stack);
    }
}
