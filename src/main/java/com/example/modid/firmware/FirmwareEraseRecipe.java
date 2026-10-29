package com.example.modid.firmware;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

/** One programmed item in any crafting slot clears only this mod's firmware tag. */
public final class FirmwareEraseRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
    public static final FirmwareEraseRecipe INSTANCE = new FirmwareEraseRecipe();

    private ItemStack input(InventoryCrafting grid) {
        ItemStack found = ItemStack.EMPTY;
        for (int slot = 0; slot < grid.getSizeInventory(); slot++) {
            ItemStack stack = grid.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            if (!found.isEmpty() || !FirmwareData.configured(stack)) return ItemStack.EMPTY;
            found = stack;
        }
        return found;
    }

    @Override
    public boolean matches(InventoryCrafting grid, World world) {
        return !input(grid).isEmpty();
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting grid) {
        ItemStack stack = input(grid);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack cleared = FirmwareData.withoutFirmware(stack).copy();
        cleared.setCount(1);
        return cleared;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width >= 1 && height >= 1;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting grid) {
        return NonNullList.withSize(grid.getSizeInventory(), ItemStack.EMPTY);
    }
}
