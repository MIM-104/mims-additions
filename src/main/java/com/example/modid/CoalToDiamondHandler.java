package com.example.modid;

import net.minecraft.block.BlockPistonExtension;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class CoalToDiamondHandler {

    private static Map<EntityItem, Integer> trackedCoal = new HashMap<>();

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) return;

        if (event.getEntity() instanceof EntityItem) {
            EntityItem entityItem = (EntityItem) event.getEntity();
            ItemStack stack = entityItem.getItem();

            if (stack.getItem() == Items.COAL && stack.getMetadata() == 0) {
                trackedCoal.put(entityItem, 0);
            }
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        World world = event.world;
        if (world.isRemote) return;

        trackedCoal.entrySet().removeIf(entry -> entry.getKey().isDead);

        for (Map.Entry<EntityItem, Integer> entry : trackedCoal.entrySet()) {
            EntityItem entityItem = entry.getKey();

            if (entityItem.isDead || entityItem.world != world) continue;

            BlockPos itemPos = new BlockPos(entityItem.posX, entityItem.posY, entityItem.posZ);

            boolean isCrushed = false;

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos checkPos = itemPos.add(x, y, z);
                        IBlockState checkState = world.getBlockState(checkPos);

                        if (checkState.getBlock() == Blocks.PISTON_EXTENSION) {
                            EnumFacing pistonFacing = checkState.getValue(BlockPistonExtension.FACING);

                            BlockPos behindPiston = checkPos.offset(pistonFacing);
                            BlockPos oppositeWall = checkPos.offset(pistonFacing.getOpposite());

                            IBlockState behindState = world.getBlockState(behindPiston);
                            IBlockState oppositeState = world.getBlockState(oppositeWall);

                            boolean hasCoalBetween =
                                    itemPos.equals(behindPiston) || isBetween(itemPos, checkPos, oppositeWall);

                            boolean hasSolidWall =
                                    oppositeState.getMaterial().isSolid()
                                            && oppositeState.getBlock() != Blocks.PISTON_EXTENSION;

                            if (hasCoalBetween && hasSolidWall) {
                                isCrushed = true;
                                MimsAdditions.LOGGER.debug(
                                        "Coal detected being crushed! Piston at {} facing {}, wall at {}",
                                        checkPos,
                                        pistonFacing,
                                        oppositeWall);
                                break;
                            }
                        }
                    }
                    if (isCrushed) break;
                }
                if (isCrushed) break;
            }

            if (isCrushed) {
                int count = entry.getValue() + 1;
                entry.setValue(count);

                if (count >= 1) {
                    ItemStack stack = entityItem.getItem();
                    int coalCount = stack.getCount();

                    entityItem.setDead();
                    trackedCoal.remove(entityItem);

                    ItemStack diamondStack = new ItemStack(Items.DIAMOND, coalCount);
                    EntityItem diamondEntity =
                            new EntityItem(world, entityItem.posX, entityItem.posY, entityItem.posZ, diamondStack);
                    diamondEntity.setDefaultPickupDelay();
                    diamondEntity.motionX = 0;
                    diamondEntity.motionY = 0;
                    diamondEntity.motionZ = 0;
                    world.spawnEntity(diamondEntity);

                    MimsAdditions.LOGGER.debug(
                            "Converted {} coal to {} diamond(s) by piston crushing!", coalCount, coalCount);
                    break;
                }
            } else {
                entry.setValue(0);
            }
        }
    }

    private static boolean isBetween(BlockPos pos, BlockPos a, BlockPos b) {
        double minX = Math.min(a.getX(), b.getX());
        double maxX = Math.max(a.getX(), b.getX());
        double minY = Math.min(a.getY(), b.getY());
        double maxY = Math.max(a.getY(), b.getY());
        double minZ = Math.min(a.getZ(), b.getZ());
        double maxZ = Math.max(a.getZ(), b.getZ());

        return pos.getX() >= minX
                && pos.getX() <= maxX
                && pos.getY() >= minY
                && pos.getY() <= maxY
                && pos.getZ() >= minZ
                && pos.getZ() <= maxZ;
    }
}
