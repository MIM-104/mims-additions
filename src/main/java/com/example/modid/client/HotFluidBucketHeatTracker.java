package com.example.modid.client;

import com.example.modid.Tags;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public final class HotFluidBucketHeatTracker {

    private static final int MELT_TICKS = 20 * 5;
    private static final int HOT_FLUID_KELVIN = 1812;
    private static final Map<UUID, MeltTracker> MELT_TRACKERS = new HashMap<>();
    private static final Map<ItemStack, Float> HEAT_MAP = new IdentityHashMap<>();

    private HotFluidBucketHeatTracker() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if (player == null) {
            return;
        }

        int mainSize = player.inventory.mainInventory.size();
        int offhandSize = player.inventory.offHandInventory.size();
        int totalSize = mainSize + offhandSize;
        MeltTracker tracker = getTracker(player.getUniqueID(), totalSize);
        HEAT_MAP.clear();

        int index = 0;
        for (ItemStack stack : player.inventory.mainInventory) {
            float ratio = updateSlot(stack, tracker, index++);
            tracker.ratios[index - 1] = ratio;
            if (ratio > 0.0f) {
                HEAT_MAP.put(stack, ratio);
            }
        }
        for (ItemStack stack : player.inventory.offHandInventory) {
            float ratio = updateSlot(stack, tracker, index++);
            tracker.ratios[index - 1] = ratio;
            if (ratio > 0.0f) {
                HEAT_MAP.put(stack, ratio);
            }
        }
    }

    public static float getHeatRatio(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0.0f;
        }

        Float direct = HEAT_MAP.get(stack);
        if (direct != null) {
            return direct;
        }

        FluidStack stackFluid = getFluidStack(stack);
        if (stackFluid == null || stackFluid.getFluid() == null) {
            return 0.0f;
        }

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if (player == null) {
            return 0.0f;
        }

        int totalSize = player.inventory.mainInventory.size() + player.inventory.offHandInventory.size();
        MeltTracker tracker = getTracker(player.getUniqueID(), totalSize);
        String fluidName = stackFluid.getFluid().getName();
        int index = 0;
        for (ItemStack invStack : player.inventory.mainInventory) {
            if (matchesSlot(stack, invStack, fluidName)) {
                return tracker.getRatio(index);
            }
            index++;
        }
        for (ItemStack invStack : player.inventory.offHandInventory) {
            if (matchesSlot(stack, invStack, fluidName)) {
                return tracker.getRatio(index);
            }
            index++;
        }

        return 0.0f;
    }

    private static float updateSlot(ItemStack stack, MeltTracker tracker, int index) {
        if (stack.isEmpty()) {
            tracker.reset(index);
            return 0.0f;
        }

        FluidStack fluidStack = getFluidStack(stack);
        if (fluidStack == null || fluidStack.getFluid() == null) {
            tracker.reset(index);
            return 0.0f;
        }

        Fluid fluid = fluidStack.getFluid();
        if (fluid.getTemperature(fluidStack) <= HOT_FLUID_KELVIN) {
            tracker.reset(index);
            return 0.0f;
        }

        String name = fluid.getName();
        String prevName = tracker.fluidNames[index];
        int ticks = tracker.ticks[index];
        if (!name.equals(prevName)) {
            ticks = 0;
            tracker.fluidNames[index] = name;
        }

        ticks++;
        tracker.ticks[index] = ticks;
        float ratio = ticks / (float) MELT_TICKS;
        return ratio > 1.0f ? 1.0f : ratio;
    }

    private static MeltTracker getTracker(UUID playerId, int size) {
        MeltTracker tracker = MELT_TRACKERS.get(playerId);
        if (tracker == null || tracker.size != size) {
            tracker = new MeltTracker(size);
            MELT_TRACKERS.put(playerId, tracker);
        }
        return tracker;
    }

    private static final class MeltTracker {
        private final int size;
        private final String[] fluidNames;
        private final int[] ticks;
        private final float[] ratios;

        private MeltTracker(int size) {
            this.size = size;
            this.fluidNames = new String[size];
            this.ticks = new int[size];
            this.ratios = new float[size];
        }

        private void reset(int index) {
            if (index < 0 || index >= size) {
                return;
            }
            fluidNames[index] = null;
            ticks[index] = 0;
            ratios[index] = 0.0f;
        }

        private float getRatio(int index) {
            if (index < 0 || index >= size) {
                return 0.0f;
            }
            return ratios[index];
        }
    }

    private static boolean matchesSlot(ItemStack stack, ItemStack invStack, String fluidName) {
        if (stack == invStack) {
            return true;
        }
        if (invStack.isEmpty()) {
            return false;
        }
        if (stack.getItem() != invStack.getItem()) {
            return false;
        }
        FluidStack invFluid = FluidUtil.getFluidContained(invStack);
        if (invFluid == null || invFluid.getFluid() == null) {
            return false;
        }
        if (!fluidName.equals(invFluid.getFluid().getName())) {
            return false;
        }
        return stack.getItemDamage() == invStack.getItemDamage();
    }

    public static FluidStack getFluidStack(ItemStack stack) {
        FluidStack fluidStack = FluidUtil.getFluidContained(stack);
        if (fluidStack != null) {
            return fluidStack;
        }

        if (stack.hasTagCompound()) {
            fluidStack = FluidStack.loadFluidStackFromNBT(stack.getTagCompound());
            if (fluidStack != null) {
                return fluidStack;
            }
            if (stack.getTagCompound().hasKey("Fluid")) {
                fluidStack = FluidStack.loadFluidStackFromNBT(stack.getTagCompound().getCompoundTag("Fluid"));
                if (fluidStack != null) {
                    return fluidStack;
                }
            }
            if (stack.getTagCompound().hasKey("fluid")) {
                fluidStack = FluidStack.loadFluidStackFromNBT(stack.getTagCompound().getCompoundTag("fluid"));
                if (fluidStack != null) {
                    return fluidStack;
                }
            }
        }

        IFluidHandlerItem handler = FluidUtil.getFluidHandler(stack);
        if (handler != null) {
            fluidStack = handler.drain(Fluid.BUCKET_VOLUME, false);
            if (fluidStack != null) {
                return fluidStack;
            }
            return handler.drain(Integer.MAX_VALUE, false);
        }

        return null;
    }
}
