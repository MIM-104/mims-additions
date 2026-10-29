package com.example.modid.firmware;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public final class FirmwareContainer extends Container {
    public static final int BOARD_X = 20, BOARD_Y = 32, CELL = 12;
    public static final int BENCH_SLOTS = 10;
    public static final int[] STORAGE_SLOTS = {0, 1, 2, 3, 4, 6, 7, 8, 9, 10};
    public final FirmwareWorkbenchTile bench;

    public FirmwareContainer(InventoryPlayer player, FirmwareWorkbenchTile bench) {
        this.bench = bench;
        bench.returnLegacyWire(player.player);
        addBenchSlot(0, BOARD_X + 5 * CELL - 8, BOARD_Y + 3 * CELL - 8);
        addBenchSlot(1, 272, 158);
        addBenchSlot(2, 12, 158);
        addBenchSlot(3, 38, 158);
        addBenchSlot(4, 12, 198);
        for (int branch = 0; branch < 5; branch++) {
            int[] at = FirmwarePuzzle.TERMINALS[branch];
            addBenchSlot(6 + branch, BOARD_X + at[0] * CELL - 8, BOARD_Y + at[1] * CELL - 8);
        }
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(player, col + row * 9 + 9, 80 + col * 18, 158 + row * 18));
            }
        for (int col = 0; col < 9; col++) addSlotToContainer(new Slot(player, col, 80 + col * 18, 216));
    }

    private void addBenchSlot(int index, int x, int y) {
        addSlotToContainer(
                new SlotItemHandler(bench.items, index, x, y) {
                    @Override
                    public boolean isItemValid(ItemStack stack) {
                        return bench.accepts(index, stack);
                    }

                    @Override
                    public boolean isEnabled() {
                        return index < FirmwareWorkbenchTile.TERMINAL
                                || index - FirmwareWorkbenchTile.TERMINAL < bench.branches()
                                || getHasStack();
                    }

                    @Override
                    public boolean canTakeStack(EntityPlayer player) {
                        return index != FirmwareWorkbenchTile.CIRCUIT || bench.soldered == 0;
                    }
                });
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return bench.getWorld() != null
                && bench.getWorld().getTileEntity(bench.getPos()) == bench
                && player.getDistanceSq(
                                bench.getPos().getX() + 0.5, bench.getPos().getY() + 0.5, bench.getPos().getZ() + 0.5)
                        <= 64;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        Slot slot = inventorySlots.get(index);
        if (!slot.getHasStack() || !slot.canTakeStack(player)) return ItemStack.EMPTY;
        ItemStack stack = slot.getStack(), original = stack.copy();
        if (index < BENCH_SLOTS) {
            if (!mergeItemStack(stack, BENCH_SLOTS, inventorySlots.size(), true)) return ItemStack.EMPTY;
        } else {
            boolean moved = false;
            for (int i = 0; i < BENCH_SLOTS && !stack.isEmpty(); i++) {
                if (inventorySlots.get(i).isItemValid(stack)) moved |= mergeItemStack(stack, i, i + 1, false);
            }
            if (!moved) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.putStack(ItemStack.EMPTY);
        else slot.onSlotChanged();
        slot.onTake(player, stack);
        return original;
    }
}
