package com.example.modid.mixins;

import com.example.modid.firmware.FirmwareMatching;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Apply after crafting-cache overwrites. HEAD callbacks work with both cached and vanilla lookup.
@Mixin(value = CraftingManager.class, priority = 900)
public abstract class MixinFirmwareCrafting {
    @Inject(method = "findMatchingResult", at = @At("HEAD"), cancellable = true)
    private static void modid$clearFirmwareFirst(
            InventoryCrafting grid, World world, CallbackInfoReturnable<ItemStack> cir) {
        if (!FirmwareMatching.needsCraftingLookup(grid)) return;
        IRecipe recipe = FirmwareMatching.findCraftingRecipe(grid, world);
        cir.setReturnValue(recipe == null ? ItemStack.EMPTY : recipe.getCraftingResult(grid));
    }

    @Inject(method = "findMatchingRecipe", at = @At("HEAD"), cancellable = true)
    private static void modid$selectFirmwareErase(
            InventoryCrafting grid, World world, CallbackInfoReturnable<IRecipe> cir) {
        if (FirmwareMatching.needsCraftingLookup(grid))
            cir.setReturnValue(FirmwareMatching.findCraftingRecipe(grid, world));
    }

    @Inject(method = "getRemainingItems", at = @At("HEAD"), cancellable = true)
    private static void modid$eraseRemainders(
            InventoryCrafting grid, World world, CallbackInfoReturnable<NonNullList<ItemStack>> cir) {
        if (!FirmwareMatching.needsCraftingLookup(grid)) return;
        IRecipe recipe = FirmwareMatching.findCraftingRecipe(grid, world);
        if (recipe != null) cir.setReturnValue(recipe.getRemainingItems(grid));
        else {
            NonNullList<ItemStack> remaining = NonNullList.withSize(grid.getSizeInventory(), ItemStack.EMPTY);
            for (int i = 0; i < remaining.size(); i++) remaining.set(i, grid.getStackInSlot(i));
            cir.setReturnValue(remaining);
        }
    }
}
