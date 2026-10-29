package com.example.modid.mixins;

import com.example.modid.firmware.client.FirmwareCircuitRecipeLookup;

import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/** Run after plugin callbacks finish: HEI's timed plugin wrappers cannot be entered recursively. */
@Mixin(targets = "mezz.jei.recipes.RecipeRegistry", remap = false)
public abstract class MixinFirmwareJeiRecipeLookup {
    @Inject(
            method = "getRecipeCategories(Lmezz/jei/api/recipe/IFocus;)Ljava/util/List;",
            at = @At("RETURN"),
            cancellable = true)
    private void modid$linkCircuitCategories(IFocus<?> focus, CallbackInfoReturnable<List<IRecipeCategory>> cir) {
        cir.setReturnValue(
                FirmwareCircuitRecipeLookup.categories((IRecipeRegistry) (Object) this, focus, cir.getReturnValue()));
    }

    @Inject(
            method =
                    "getRecipeWrappers(Lmezz/jei/api/recipe/IRecipeCategory;Lmezz/jei/api/recipe/IFocus;)Ljava/util/List;",
            at = @At("RETURN"),
            cancellable = true)
    private <T extends IRecipeWrapper> void modid$linkCircuitRecipes(
            IRecipeCategory<T> category, IFocus<?> focus, CallbackInfoReturnable<List<T>> cir) {
        cir.setReturnValue(
                FirmwareCircuitRecipeLookup.recipes(
                        (IRecipeRegistry) (Object) this, category, focus, cir.getReturnValue()));
    }
}
