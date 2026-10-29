package com.example.modid.mixins;

import com.example.modid.firmware.FirmwareMatching;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.ingredients.GTRecipeInput;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Recipe.class, remap = false)
public abstract class MixinFirmwareRecipe {
    @Inject(
            method = {
                "matches(ZLnet/minecraftforge/items/IItemHandlerModifiable;Lgregtech/api/capability/IMultipleTankHandler;)Z",
                "matches(ZLjava/util/List;Ljava/util/List;)Z"
            },
            at = @At("HEAD"))
    private void modid$beginFirmwareMatch(CallbackInfoReturnable<Boolean> cir) {
        FirmwareMatching.begin((Recipe) (Object) this);
    }

    @Inject(
            method = {
                "matches(ZLnet/minecraftforge/items/IItemHandlerModifiable;Lgregtech/api/capability/IMultipleTankHandler;)Z",
                "matches(ZLjava/util/List;Ljava/util/List;)Z"
            },
            at = @At("RETURN"))
    private void modid$endFirmwareMatch(CallbackInfoReturnable<Boolean> cir) {
        FirmwareMatching.end();
    }

    @Redirect(
            method = "matchesItems",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lgregtech/api/recipes/ingredients/GTRecipeInput;acceptsStack(Lnet/minecraft/item/ItemStack;)Z"))
    private boolean modid$requireCorrectFirmware(GTRecipeInput ingredient, ItemStack stack) {
        return FirmwareMatching.accepts((Recipe) (Object) this, ingredient, stack);
    }
}
