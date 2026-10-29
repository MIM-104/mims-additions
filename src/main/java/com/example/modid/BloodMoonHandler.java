package com.example.modid;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class BloodMoonHandler {

    private static final String BLOOD_MOON_MOB_TAG = "MIMBloodMoonMob";
    private static final int BLOOD_MOON_CHANCE_DENOMINATOR = 8;
    private static final int SPAWN_INTERVAL_TICKS = 40;
    private static final int MOBS_PER_WAVE = 2;
    private static final int MAX_BLOOD_MOON_MOBS = 40;
    private static final Map<World, BloodMoonState> WORLD_STATES = new WeakHashMap<>();

    private BloodMoonHandler() {}

    @SubscribeEvent
    public static void onPlayerSleepInBed(PlayerSleepInBedEvent event) {
        if (!isBloodMoonActive(event.getEntityPlayer().world)) {
            return;
        }

        event.setResult(EntityPlayer.SleepResult.OTHER_PROBLEM);
        TextComponentString message = new TextComponentString("You don't feel tired!");
        message.getStyle().setColor(TextFormatting.RED);
        event.getEntityPlayer().sendStatusMessage(message, true);
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!(event.world instanceof WorldServer) || event.world.provider.getDimension() != 0) {
            return;
        }
        WorldServer world = (WorldServer) event.world;

        BloodMoonState state = getState(world);
        if (world.isDaytime()) {
            state.active = false;
            return;
        }

        updateForCurrentNight(world, state);

        if (!state.active || world.getTotalWorldTime() % SPAWN_INTERVAL_TICKS != 0) {
            return;
        }

        spawnWave(world);
    }

    public static boolean isBloodMoonMob(Entity entity) {
        return entity instanceof EntityLivingBase && entity.getEntityData().getBoolean(BLOOD_MOON_MOB_TAG);
    }

    private static boolean isBloodMoonActive(World world) {
        if (world == null || world.isRemote || world.provider.getDimension() != 0) {
            return false;
        }

        if (world.isDaytime()) {
            return false;
        }

        BloodMoonState state = getState(world);
        updateForCurrentNight(world, state);
        return state.active;
    }

    private static BloodMoonState getState(World world) {
        BloodMoonState state = WORLD_STATES.get(world);
        if (state == null) {
            state = new BloodMoonState();
            WORLD_STATES.put(world, state);
        }
        return state;
    }

    private static void updateForCurrentNight(World world, BloodMoonState state) {
        long day = world.getWorldTime() / 24000L;
        if (state.day != day) {
            state.day = day;
            state.active =
                    new Random(world.getSeed() ^ (day * 341873128712L)).nextInt(BLOOD_MOON_CHANCE_DENOMINATOR) == 0;
        }
    }

    private static void spawnWave(WorldServer world) {
        if (countBloodMoonMobs(world) >= MAX_BLOOD_MOON_MOBS) {
            return;
        }

        for (int i = 0; i < MOBS_PER_WAVE && countBloodMoonMobs(world) < MAX_BLOOD_MOON_MOBS; i++) {
            EntityMob mob = world.rand.nextBoolean() ? new EntityZombie(world) : new EntitySkeleton(world);
            BlockPos spawnPos = findSpawnPosition(world);
            if (spawnPos == null) {
                continue;
            }

            mob.setPosition(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D);
            mob.onInitialSpawn(world.getDifficultyForLocation(spawnPos), null);
            mob.getEntityData().setBoolean(BLOOD_MOON_MOB_TAG, true);
            equipBloodMoonMob(mob);
            mob.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 20 * 60 * 10, 0));

            world.spawnEntity(mob);
        }
    }

    private static int countBloodMoonMobs(WorldServer world) {
        int count = 0;
        for (Entity entity : world.loadedEntityList) {
            if (entity instanceof EntityMob && entity.getEntityData().getBoolean(BLOOD_MOON_MOB_TAG)) {
                count++;
            }
        }
        return count;
    }

    private static BlockPos findSpawnPosition(WorldServer world) {
        if (!(world.getChunkProvider() instanceof ChunkProviderServer)) {
            return null;
        }

        Collection<Chunk> loadedChunks = ((ChunkProviderServer) world.getChunkProvider()).getLoadedChunks();
        if (loadedChunks.isEmpty()) {
            return null;
        }

        Chunk[] chunks = loadedChunks.toArray(new Chunk[0]);
        for (int attempt = 0; attempt < 12; attempt++) {
            Chunk chunk = chunks[world.rand.nextInt(chunks.length)];
            int x = (chunk.x << 4) + world.rand.nextInt(16);
            int z = (chunk.z << 4) + world.rand.nextInt(16);
            BlockPos spawnPos = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z));
            if (spawnPos.getY() <= 0
                    || spawnPos.getY() >= world.getHeight()
                    || !world.isAirBlock(spawnPos)
                    || !world.isAirBlock(spawnPos.up())
                    || !world.isSideSolid(spawnPos.down(), EnumFacing.UP)) {
                continue;
            }

            return spawnPos;
        }
        return null;
    }

    private static void equipBloodMoonMob(EntityLiving mob) {
        mob.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        mob.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        mob.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
        mob.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
        mob.setDropChance(EntityEquipmentSlot.HEAD, 0.0F);
        mob.setDropChance(EntityEquipmentSlot.CHEST, 0.0F);
        mob.setDropChance(EntityEquipmentSlot.LEGS, 0.0F);
        mob.setDropChance(EntityEquipmentSlot.FEET, 0.0F);

        if (mob instanceof EntitySkeleton) {
            mob.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        } else {
            mob.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        }
    }

    private static final class BloodMoonState {
        private long day = Long.MIN_VALUE;
        private boolean active;
    }
}
