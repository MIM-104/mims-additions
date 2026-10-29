package com.example.modid;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class ChestShuffleHandler {

    private static final int TICKS_PER_SECOND = 20;
    private static final int MIN_INTERVAL_TICKS = 5 * 60 * TICKS_PER_SECOND;
    private static final int MAX_INTERVAL_TICKS = 60 * 60 * TICKS_PER_SECOND;
    private static final int MAX_STACKS_TO_SHUFFLE = 20;
    private static final Map<World, Long> NEXT_SHUFFLE_TICK = new WeakHashMap<>();

    private ChestShuffleHandler() {}

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.world instanceof WorldServer)) {
            return;
        }

        WorldServer world = (WorldServer) event.world;
        long worldTime = world.getTotalWorldTime();
        Long nextShuffleTick = NEXT_SHUFFLE_TICK.get(world);
        if (nextShuffleTick == null) {
            NEXT_SHUFFLE_TICK.put(world, worldTime + nextInterval(world.rand));
            return;
        }
        if (worldTime < nextShuffleTick) {
            return;
        }

        shuffleLoadedStorage(world);
        NEXT_SHUFFLE_TICK.put(world, worldTime + nextInterval(world.rand));
    }

    private static long nextInterval(Random random) {
        return MIN_INTERVAL_TICKS + random.nextInt(MAX_INTERVAL_TICKS - MIN_INTERVAL_TICKS + 1);
    }

    private static void shuffleLoadedStorage(WorldServer world) {
        List<TileEntity> eligibleStorage = new ArrayList<>();
        for (TileEntity tileEntity : world.loadedTileEntityList) {
            if (isStorageBlock(tileEntity)
                    && !tileEntity.isInvalid()
                    && world.isBlockLoaded(tileEntity.getPos())
                    && !isInventoryOpen(world, tileEntity)
                    && hasItems((IInventory) tileEntity)) {
                eligibleStorage.add(tileEntity);
            }
        }

        for (TileEntity tileEntity : eligibleStorage) {
            shuffleInventory((IInventory) tileEntity, world.rand);
        }
    }

    private static boolean isStorageBlock(TileEntity tileEntity) {
        if (!(tileEntity instanceof IInventory)) {
            return false;
        }

        String className = tileEntity.getClass().getName().toLowerCase(Locale.ROOT);
        if (className.startsWith("appeng.")) {
            return false;
        }

        if (tileEntity instanceof TileEntityChest) {
            return tileEntity.getBlockType() == Blocks.CHEST || tileEntity.getBlockType() == Blocks.TRAPPED_CHEST;
        }

        if (tileEntity.getBlockType().getRegistryName() == null) {
            return false;
        }
        String namespace = tileEntity.getBlockType().getRegistryName().getNamespace();
        if ("appliedenergistics2".equals(namespace) || "ae2".equals(namespace)) {
            return false;
        }

        String blockName = tileEntity.getBlockType().getRegistryName().getPath().toLowerCase(Locale.ROOT);
        return hasStorageName(blockName) || hasStorageName(tileEntity.getClass().getSimpleName());
    }

    private static boolean hasStorageName(String name) {
        String normalized = name.toLowerCase(Locale.ROOT);
        return normalized.contains("chest")
                || normalized.contains("crate")
                || normalized.contains("barrel")
                || normalized.contains("storage")
                || normalized.contains("drawer")
                || normalized.contains("vault")
                || normalized.contains("safe")
                || normalized.contains("locker")
                || normalized.contains("cache");
    }

    private static boolean isInventoryOpen(WorldServer world, TileEntity tileEntity) {
        IInventory inventory = (IInventory) tileEntity;
        for (EntityPlayer player : world.playerEntities) {
            Container openContainer = player.openContainer;
            if (tileEntity instanceof TileEntityChest && openContainer instanceof ContainerChest) {
                return true;
            }
            for (Slot slot : openContainer.inventorySlots) {
                if (slot.inventory == inventory) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasItems(IInventory inventory) {
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            if (!inventory.getStackInSlot(slot).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static void shuffleInventory(IInventory chest, Random random) {
        List<Integer> occupiedSlots = new ArrayList<>();
        for (int slot = 0; slot < chest.getSizeInventory(); slot++) {
            if (!chest.getStackInSlot(slot).isEmpty()) {
                occupiedSlots.add(slot);
            }
        }
        if (occupiedSlots.isEmpty()) {
            return;
        }

        Collections.shuffle(occupiedSlots, random);
        int stackCount = 1 + random.nextInt(Math.min(MAX_STACKS_TO_SHUFFLE, occupiedSlots.size()));
        List<Integer> sourceSlots = new ArrayList<>(occupiedSlots.subList(0, stackCount));
        Set<Integer> selectedSlots = new HashSet<>(sourceSlots);
        List<Integer> destinationSlots = new ArrayList<>();
        List<ItemStack> stacks = new ArrayList<>();
        List<ItemStack> displacedStacks = new ArrayList<>();
        List<Integer> returnSlots = new ArrayList<>();

        for (int slot = 0; slot < chest.getSizeInventory(); slot++) {
            destinationSlots.add(slot);
        }
        Collections.shuffle(destinationSlots, random);
        destinationSlots = new ArrayList<>(destinationSlots.subList(0, stackCount));
        Set<Integer> destinationSet = new HashSet<>(destinationSlots);

        for (int sourceSlot : sourceSlots) {
            stacks.add(chest.getStackInSlot(sourceSlot).copy());
            if (!destinationSet.contains(sourceSlot)) {
                returnSlots.add(sourceSlot);
            }
        }
        for (int destinationSlot : destinationSlots) {
            if (!selectedSlots.contains(destinationSlot)) {
                ItemStack displacedStack = chest.getStackInSlot(destinationSlot);
                if (!displacedStack.isEmpty()) {
                    displacedStacks.add(displacedStack.copy());
                }
            }
        }

        Collections.shuffle(stacks, random);
        Collections.shuffle(returnSlots, random);
        for (int sourceSlot : sourceSlots) {
            chest.setInventorySlotContents(sourceSlot, ItemStack.EMPTY);
        }
        for (int destinationSlot : destinationSlots) {
            chest.setInventorySlotContents(destinationSlot, ItemStack.EMPTY);
        }
        for (int i = 0; i < stackCount; i++) {
            chest.setInventorySlotContents(destinationSlots.get(i), stacks.get(i));
        }
        for (int i = 0; i < displacedStacks.size(); i++) {
            chest.setInventorySlotContents(returnSlots.get(i), displacedStacks.get(i));
        }
        chest.markDirty();
    }
}
