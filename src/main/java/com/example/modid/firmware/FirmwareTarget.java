package com.example.modid.firmware;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public final class FirmwareTarget {
    public final String key, name, kind;
    public final ItemStack icon;
    public ItemStack exampleCircuit = ItemStack.EMPTY;
    public final java.util.SortedMap<Integer, ItemStack> circuitTiers = new java.util.TreeMap<>();

    public FirmwareTarget(String key, String name, String kind, ItemStack icon) {
        this.key = key;
        this.name = name;
        this.kind = kind;
        this.icon = icon.copy();
        this.icon.setCount(1);
    }

    public NBTTagCompound write() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Key", key);
        tag.setString("Name", name);
        tag.setString("Kind", kind);
        tag.setTag("Icon", icon.writeToNBT(new NBTTagCompound()));
        return tag;
    }

    public static FirmwareTarget read(NBTTagCompound tag) {
        return new FirmwareTarget(
                tag.getString("Key"),
                tag.getString("Name"),
                tag.getString("Kind"),
                new ItemStack(tag.getCompoundTag("Icon")));
    }
}
