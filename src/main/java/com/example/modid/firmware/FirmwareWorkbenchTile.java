package com.example.modid.firmware;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.items.ItemStackHandler;

public class FirmwareWorkbenchTile extends TileEntity {
    public static final int CIRCUIT = 0, OUTPUT = 1, IRON = 2, STRIPPERS = 3, WIRE = 4, TERMINAL = 6;
    // Keep saved terminal indices stable. Slot 5 is only used to recover old two-supply inventories.
    private static final int LEGACY_WIRE = 5;
    public String selectedTarget = "";
    public String status = "Insert an unconfigured circuit and select its purpose.";
    public byte[] rotations = new byte[25];
    public int soldered;
    private long seed;
    private boolean loading;
    public final ItemStackHandler items =
            new ItemStackHandler(11) {
                @Override
                protected void onContentsChanged(int slot) {
                    if (loading || world == null || world.isRemote) return;
                    if (slot == CIRCUIT && soldered == 0) newLayout();
                    changed();
                }

                @Override
                public int getSlotLimit(int slot) {
                    return slot <= OUTPUT || slot == IRON || slot == STRIPPERS ? 1 : 64;
                }

                @Override
                public boolean isItemValid(int slot, ItemStack stack) {
                    return accepts(slot, stack);
                }
            };

    public boolean accepts(int slot, ItemStack stack) {
        if (stack.isEmpty() || slot == OUTPUT) return false;
        switch (slot) {
            case CIRCUIT:
                return soldered == 0 && FirmwareData.isCircuit(stack) && !FirmwareData.configured(stack);
            case IRON:
                return stack.getItem() == FirmwareContent.SOLDERING_IRON;
            case STRIPPERS:
                return stack.getItem() == FirmwareContent.WIRE_STRIPPERS;
            case WIRE:
                return FirmwareMaterials.isSolderWire(stack);
            case LEGACY_WIRE:
                return false;
            default:
                return slot >= TERMINAL
                        && slot < TERMINAL + branches()
                        && FirmwareMaterials.same(stack, requirement(slot - TERMINAL));
        }
    }

    public int tier() {
        return FirmwareData.tier(items.getStackInSlot(CIRCUIT));
    }

    public int branches() {
        return tier() < 0 ? 0 : FirmwarePuzzle.branches(tier());
    }

    public ItemStack requirement(int branch) {
        return FirmwareMaterials.branch(branch, Math.max(0, tier()));
    }

    public boolean complete(int branch) {
        return (soldered & (1 << branch)) != 0;
    }

    public int nextSolderableBranch() {
        int wear = FirmwareMaterials.solderWear(items.getStackInSlot(WIRE));
        if (FirmwareTargets.get(selectedTarget) == null
                || wear == 0
                || !toolReady(IRON, wear)
                || !toolReady(STRIPPERS, wear)) return -1;
        for (int branch = 0; branch < branches(); branch++) {
            if (!complete(branch)
                    && FirmwarePuzzle.connected(branch, rotations)
                    && has(TERMINAL + branch, requirement(branch))) return branch;
        }
        return -1;
    }

    public boolean canFlash() {
        return branches() > 0
                && FirmwareTargets.get(selectedTarget) != null
                && soldered == (1 << branches()) - 1
                && items.getStackInSlot(OUTPUT).isEmpty()
                && FirmwareMaterials.isSolderWire(items.getStackInSlot(WIRE));
    }

    public void returnLegacyWire(EntityPlayer player) {
        if (world == null || world.isRemote || items.getStackInSlot(LEGACY_WIRE).isEmpty()) return;
        ItemStack remainder = items.getStackInSlot(LEGACY_WIRE).copy();
        items.setStackInSlot(LEGACY_WIRE, ItemStack.EMPTY);
        player.inventory.addItemStackToInventory(remainder);
        if (!remainder.isEmpty()) player.dropItem(remainder, false);
    }

    private void newLayout() {
        seed = world == null ? seed + 1 : world.rand.nextLong();
        rotations = FirmwarePuzzle.shuffled(seed ^ selectedTarget.hashCode(), branches());
    }

    public void action(EntityPlayer player, int action, int value, String targetKey) {
        if (world == null || world.isRemote) return;
        if (action == 0) {
            if (soldered != 0) {
                status = "Finish or reset the current soldering job first.";
                changed();
                return;
            }
            FirmwareTarget target = FirmwareTargets.get(targetKey);
            if (target == null) return;
            selectedTarget = target.key;
            newLayout();
            status = "Rotate traces, then solder each connected branch.";
        } else if (action == 1) {
            int branch = value / FirmwarePuzzle.STRIDE, node = value % FirmwarePuzzle.STRIDE;
            if (value < 0 || branch >= branches() || node >= FirmwarePuzzle.PATHS[branch].length || complete(branch))
                return;
            rotations[value] = (byte) ((rotations[value] + 1) & 3);
        } else if (action == 2) {
            solder(player, value);
        } else if (action == 3) {
            flash();
        } else if (action == 4) {
            soldered = 0;
            newLayout();
            status = "Board reset. Soldered materials are not recoverable.";
        } else return;
        changed();
    }

    private boolean has(int slot, ItemStack expected) {
        return FirmwareMaterials.same(items.getStackInSlot(slot), expected);
    }

    private boolean toolReady(int slot, int wear) {
        ItemStack tool = items.getStackInSlot(slot);
        return !tool.isEmpty()
                && tool.getItem() == (slot == IRON ? FirmwareContent.SOLDERING_IRON : FirmwareContent.WIRE_STRIPPERS)
                && tool.getMaxDamage() - tool.getItemDamage() >= wear;
    }

    private void useTool(int slot, int wear) {
        ItemStack tool = items.getStackInSlot(slot).copy();
        // Bench tools always wear on use, including Creative, and do not roll Unbreaking.
        tool.setItemDamage(tool.getItemDamage() + wear);
        items.setStackInSlot(slot, tool.getItemDamage() >= tool.getMaxDamage() ? ItemStack.EMPTY : tool);
    }

    private void solder(EntityPlayer player, int branch) {
        if (branch < 0 || branch >= branches() || complete(branch)) return;
        if (FirmwareTargets.get(selectedTarget) == null) {
            status = "Select a firmware purpose first.";
            return;
        }
        if (!FirmwarePuzzle.connected(branch, rotations)) {
            status = "Open circuit: connect every trace on this branch.";
            return;
        }
        if (!has(TERMINAL + branch, requirement(branch))) {
            status = "Insert the component shown in this terminal.";
            return;
        }
        int wear = FirmwareMaterials.solderWear(items.getStackInSlot(WIRE));
        if (wear == 0) {
            status = "Feed Fine Tin or Fine Soldering Alloy Wire.";
            return;
        }
        if (!toolReady(IRON, wear) || !toolReady(STRIPPERS, wear)) {
            status = "The tools need more durability for this wire.";
            return;
        }
        items.extractItem(TERMINAL + branch, 1, false);
        items.extractItem(WIRE, 1, false);
        useTool(IRON, wear);
        useTool(STRIPPERS, wear);
        soldered |= 1 << branch;
        status =
                "Branch " + (branch + 1) + " soldered. " + Integer.bitCount(soldered) + "/" + branches() + " complete.";
        world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.25F, 1.8F);
    }

    private void flash() {
        FirmwareTarget target = FirmwareTargets.get(selectedTarget);
        if (target == null || branches() == 0) {
            status = "Insert an unconfigured circuit and choose a purpose.";
            return;
        }
        if (soldered != (1 << branches()) - 1) {
            status = "Solder all " + branches() + " branches before flashing.";
            return;
        }
        if (!items.getStackInSlot(OUTPUT).isEmpty()) {
            status = "Collect the previous programmed circuit first.";
            return;
        }
        if (!FirmwareMaterials.isSolderWire(items.getStackInSlot(WIRE))) {
            status = "Insert one Fine Tin or Fine Soldering Alloy Wire.";
            return;
        }
        ItemStack output = FirmwareData.program(items.getStackInSlot(CIRCUIT), target);
        loading = true;
        try {
            items.extractItem(WIRE, 1, false);
            items.setStackInSlot(CIRCUIT, ItemStack.EMPTY);
            items.setStackInSlot(OUTPUT, output);
        } finally {
            loading = false;
        }
        soldered = 0;
        newLayout();
        status = "Collect the programmed circuit.";
        world.playSound(null, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 0.35F, 1.4F);
    }

    private void changed() {
        markDirty();
        if (world != null && !world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setTag("Inventory", items.serializeNBT());
        tag.setString("Purpose", selectedTarget);
        tag.setString("Status", status);
        tag.setInteger("Soldered", soldered);
        tag.setLong("Seed", seed);
        tag.setByteArray("Rotations", rotations);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        loading = true;
        try {
            items.deserializeNBT(tag.getCompoundTag("Inventory"));
            selectedTarget = tag.getString("Purpose");
            ItemStack oldSupply = items.getStackInSlot(LEGACY_WIRE);
            if (!oldSupply.isEmpty()) {
                ItemStack remainder = items.insertItem(WIRE, oldSupply.copy(), false);
                items.setStackInSlot(LEGACY_WIRE, remainder);
            }
            status = tag.getString("Status");
            soldered = tag.getInteger("Soldered") & 31;
            seed = tag.getLong("Seed");
            rotations = tag.getByteArray("Rotations");
            if (rotations.length != 25) rotations = FirmwarePuzzle.shuffled(seed, branches());
        } finally {
            loading = false;
        }
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
    }
}
