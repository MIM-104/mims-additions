package com.example.modid.mixins;

import com.example.modid.firmware.FirmwareMatching;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
        value = {ShapedOreRecipe.class, ShapelessOreRecipe.class},
        remap = false)
public abstract class MixinFirmwareOreCrafting {
    @Inject(
            method = {
                "matches(Lnet/minecraft/inventory/InventoryCrafting;Lnet/minecraft/world/World;)Z",
                "func_77569_a(Lnet/minecraft/inventory/InventoryCrafting;Lnet/minecraft/world/World;)Z"
            },
            at = @At("RETURN"),
            cancellable = true,
            remap = false)
    private void modid$requireFirmwareInModdedTables(
            InventoryCrafting grid, World world, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && !FirmwareMatching.validGrid((IRecipe) (Object) this, grid))
            cir.setReturnValue(false);
    }
}
