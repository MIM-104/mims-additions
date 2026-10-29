package com.example.modid.common;

import com.example.modid.Tags;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class HotFluidBucketHandler {

    private static final int MELT_TICKS = 20 * 5;
    private static final int HOT_FLUID_KELVIN = 1812;
    private static final int DAMAGE_START_KELVIN = 400;
    private static final int DAMAGE_INTERVAL_TICKS = 10;
    private static final Map<UUID, MeltTracker> MELT_TRACKERS = new HashMap<>();

    private HotFluidBucketHandler() {}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        EntityPlayer player = event.player;
        World world = player.world;
        if (world.isRemote) {
            return;
        }

        int mainSize = player.inventory.mainInventory.size();
        int offhandSize = player.inventory.offHandInventory.size();
        int totalSize = mainSize + offhandSize;
        MeltTracker tracker = getTracker(player, totalSize);
        int index = 0;
        for (ItemStack stack : player.inventory.mainInventory) {
            handleHotFluidBucket(player, stack, tracker, index++);
        }
        for (ItemStack stack : player.inventory.offHandInventory) {
            handleHotFluidBucket(player, stack, tracker, index++);
        }
        applyHotFluidDamage(player);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        MELT_TRACKERS.remove(event.getEntityPlayer().getUniqueID());
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerLoggedOutEvent event) {
        MELT_TRACKERS.remove(event.player.getUniqueID());
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        if (player.world.isRemote) {
            return;
        }

        if (getFluidAtPlayer(player.world, getFeetPos(player)) == null) {
            return;
        }

        DamageSource source = event.getSource();
        if ((source == DamageSource.LAVA || source == DamageSource.IN_FIRE || source == DamageSource.ON_FIRE)
                && getHottestFluidAtPlayer(player) != null) {
            event.setCanceled(true);
            player.extinguish();
        }
    }

    private static void handleHotFluidBucket(EntityPlayer player, ItemStack stack, MeltTracker tracker, int index) {
        if (stack.isEmpty()) {
            tracker.reset(index);
            return;
        }

        FluidStack fluidStack = FluidUtil.getFluidContained(stack);
        if (fluidStack == null || fluidStack.getFluid() == null) {
            tracker.reset(index);
            return;
        }

        Fluid fluid = fluidStack.getFluid();
        if (fluid.getTemperature(fluidStack) <= HOT_FLUID_KELVIN) {
            tracker.reset(index);
            return;
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
        if (ticks >= MELT_TICKS) {
            meltBucket(player, stack, fluid);
            tracker.reset(index);
        }
    }

    private static void meltBucket(EntityPlayer player, ItemStack stack, Fluid fluid) {
        stack.shrink(1);

        World world = player.world;
        BlockPos pos = getFeetPos(player);
        if (fluid.getBlock() != null) {
            if (world.getBlockState(pos).getBlock().isReplaceable(world, pos)) {
                world.setBlockState(pos, fluid.getBlock().getDefaultState(), 3);
                return;
            }
            BlockPos down = pos.down();
            if (world.getBlockState(down).getBlock().isReplaceable(world, down)) {
                world.setBlockState(down, fluid.getBlock().getDefaultState(), 3);
            }
        }
    }

    private static void applyHotFluidDamage(EntityPlayer player) {
        World world = player.world;
        BlockPos pos = getFeetPos(player);
        Fluid fluid = getHottestFluidAtPlayer(player);
        if (fluid == null) {
            return;
        }

        int temp = fluid.getTemperature(new FluidStack(fluid, 1));
        if (temp <= DAMAGE_START_KELVIN || player.ticksExisted % DAMAGE_INTERVAL_TICKS != 0) {
            return;
        }

        float damage = Math.min(10.0f, 1.0f + (temp - HOT_FLUID_KELVIN) / 200.0f);
        player.attackEntityFrom(new HotFluidDamageSource(fluid), damage);
    }

    private static Fluid getFluidAtPlayer(World world, BlockPos pos) {
        Fluid fluid = FluidRegistry.lookupFluidForBlock(world.getBlockState(pos).getBlock());
        if (fluid != null) {
            return fluid;
        }

        BlockPos down = pos.down();
        return FluidRegistry.lookupFluidForBlock(world.getBlockState(down).getBlock());
    }

    private static BlockPos getFeetPos(EntityPlayer player) {
        double y = player.getEntityBoundingBox().minY + 0.001d;
        return new BlockPos(player.posX, y, player.posZ);
    }

    private static Fluid getHottestFluidAtPlayer(EntityPlayer player) {
        AxisAlignedBB box = player.getEntityBoundingBox().grow(0.001d);
        int minX = (int) Math.floor(box.minX);
        int maxX = (int) Math.floor(box.maxX);
        int minY = (int) Math.floor(box.minY);
        int maxY = (int) Math.floor(box.maxY);
        int minZ = (int) Math.floor(box.minZ);
        int maxZ = (int) Math.floor(box.maxZ);

        Fluid hottest = null;
        int hottestTemp = HOT_FLUID_KELVIN;
        World world = player.world;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    pos.setPos(x, y, z);
                    Fluid fluid = FluidRegistry.lookupFluidForBlock(world.getBlockState(pos).getBlock());
                    if (fluid == null) {
                        continue;
                    }
                    int temp = fluid.getTemperature(new FluidStack(fluid, 1));
                    if (temp > hottestTemp) {
                        hottestTemp = temp;
                        hottest = fluid;
                    }
                }
            }
        }
        return hottest;
    }

    private static final class HotFluidDamageSource extends DamageSource {
        private final Fluid fluid;

        private HotFluidDamageSource(Fluid fluid) {
            super("hot_fluid");
            this.fluid = fluid;
            setFireDamage();
        }

        @Override
        public ITextComponent getDeathMessage(EntityLivingBase entityLivingBase) {
            String fluidName = fluid == null ? "hot fluid" : fluid.getLocalizedName(new FluidStack(fluid, 1));
            return new TextComponentTranslation(
                    "death.attack.hot_fluid", entityLivingBase.getDisplayName(), new TextComponentString(fluidName));
        }
    }

    private static MeltTracker getTracker(EntityPlayer player, int size) {
        UUID id = player.getUniqueID();
        MeltTracker tracker = MELT_TRACKERS.get(id);
        if (tracker == null || tracker.size != size) {
            tracker = new MeltTracker(size);
            MELT_TRACKERS.put(id, tracker);
        }
        return tracker;
    }

    private static final class MeltTracker {
        private final int size;
        private final String[] fluidNames;
        private final int[] ticks;

        private MeltTracker(int size) {
            this.size = size;
            this.fluidNames = new String[size];
            this.ticks = new int[size];
        }

        private void reset(int index) {
            if (index < 0 || index >= size) {
                return;
            }
            fluidNames[index] = null;
            ticks[index] = 0;
        }
    }
}
