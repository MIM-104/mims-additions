package com.example.modid.mixins;

import com.example.modid.firmware.FirmwareDisplayIngredients;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;

import net.minecraft.item.crafting.IRecipe;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The JEI base wrapper also serves GT's shaped/shapeless ore recipes. */
@Mixin(targets = "mezz.jei.plugins.vanilla.crafting.ShapelessRecipeWrapper", remap = false)
public abstract class MixinFirmwareCraftingJei {
    @Shadow @Final protected IRecipe recipe;

    @Inject(method = "getIngredients", at = @At("RETURN"))
    private void modid$displayCraftingFirmware(IIngredients ingredients, CallbackInfo ci) {
        ingredients.setInputLists(
                VanillaTypes.ITEM,
                FirmwareDisplayIngredients.forOutput(
                        recipe.getRecipeOutput(), ingredients.getInputs(VanillaTypes.ITEM)));
    }
}
