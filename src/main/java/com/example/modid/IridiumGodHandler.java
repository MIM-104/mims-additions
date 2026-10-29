package com.example.modid;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;

import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class IridiumGodHandler {

    private static final Random RANDOM = new Random();
    private static final Pattern IRIDIUM_PATTERN =
            Pattern.compile("(?i)dear\\s+iridium\\s+god.*?(\\d+).*?iridium", Pattern.DOTALL);

    @SubscribeEvent
    public static void onPlayerChat(ServerChatEvent event) {
        String message = event.getMessage();
        EntityPlayer player = event.getPlayer();
        World world = player.world;

        Matcher matcher = IRIDIUM_PATTERN.matcher(message);

        if (matcher.find()) {
            event.setCanceled(true);

            world.getMinecraftServer()
                    .getPlayerList()
                    .sendMessage(new TextComponentString("<" + player.getName() + "> " + message));

            int requestedAmount = Integer.parseInt(matcher.group(1));

            if (requestedAmount > 1000) {
                requestedAmount = 1000;
            }

            final int finalAmount = requestedAmount;

            new Timer()
                    .schedule(
                            new TimerTask() {
                                @Override
                                public void run() {
                                    world.getMinecraftServer()
                                            .addScheduledTask(
                                                    () -> {
                                                        world.getMinecraftServer()
                                                                .getPlayerList()
                                                                .sendMessage(
                                                                        new TextComponentString(
                                                                                TextFormatting.LIGHT_PURPLE
                                                                                        + "<Iridium God> ..."));
                                                    });
                                }
                            },
                            3000);

            boolean willGrant = RANDOM.nextBoolean();

            if (willGrant) {
                new Timer()
                        .schedule(
                                new TimerTask() {
                                    @Override
                                    public void run() {
                                        world.getMinecraftServer()
                                                .addScheduledTask(
                                                        () -> {
                                                            world.getMinecraftServer()
                                                                    .getPlayerList()
                                                                    .sendMessage(
                                                                            new TextComponentString(
                                                                                    TextFormatting.LIGHT_PURPLE
                                                                                            + "<Iridium God> Your wish has been granted."));
                                                        });
                                    }
                                },
                                6000);

                new Timer()
                        .schedule(
                                new TimerTask() {
                                    @Override
                                    public void run() {
                                        world.getMinecraftServer()
                                                .addScheduledTask(
                                                        () -> {
                                                            ItemStack iridiumStack =
                                                                    OreDictUnifier.get(
                                                                            OrePrefix.ingot,
                                                                            Materials.Iridium,
                                                                            finalAmount);

                                                            MimsAdditions.LOGGER.debug(
                                                                    "Iridium request: {} ingots, empty stack: {}",
                                                                    finalAmount,
                                                                    iridiumStack.isEmpty());

                                                            // Verify the stack was created successfully
                                                            if (iridiumStack.isEmpty()) {
                                                                MimsAdditions.LOGGER.error(
                                                                        "No Iridium ingot form; trying dust");
                                                                iridiumStack =
                                                                        OreDictUnifier.get(
                                                                                OrePrefix.dust,
                                                                                Materials.Iridium,
                                                                                finalAmount);

                                                                if (iridiumStack.isEmpty()) {
                                                                    MimsAdditions.LOGGER.error(
                                                                            "No Iridium dust form; trying plates");
                                                                    iridiumStack =
                                                                            OreDictUnifier.get(
                                                                                    OrePrefix.plate,
                                                                                    Materials.Iridium,
                                                                                    finalAmount);
                                                                }

                                                                if (iridiumStack.isEmpty()) {
                                                                    MimsAdditions.LOGGER.error(
                                                                            "No Iridium item form was found");
                                                                }
                                                            }

                                                            if (!iridiumStack.isEmpty()) {
                                                                MimsAdditions.LOGGER.debug(
                                                                        "Successfully created iridium stack: {} x{}",
                                                                        iridiumStack.getItem().getRegistryName(),
                                                                        iridiumStack.getCount());

                                                                boolean added =
                                                                        player.inventory.addItemStackToInventory(
                                                                                iridiumStack);

                                                                if (!added) {

                                                                    EntityItem iridiumDrop =
                                                                            new EntityItem(
                                                                                    world,
                                                                                    player.posX,
                                                                                    player.posY,
                                                                                    player.posZ,
                                                                                    iridiumStack);
                                                                    iridiumDrop.setNoPickupDelay();
                                                                    iridiumDrop.isImmuneToFire = true;
                                                                    iridiumDrop.setNoDespawn();
                                                                    world.spawnEntity(iridiumDrop);
                                                                    MimsAdditions.LOGGER.debug(
                                                                            "Inventory full, dropped iridium on ground");
                                                                }

                                                                EntityLightningBolt lightning =
                                                                        new EntityLightningBolt(
                                                                                world,
                                                                                player.posX,
                                                                                player.posY,
                                                                                player.posZ,
                                                                                false);
                                                                world.spawnEntity(lightning);

                                                                for (int i = 0; i < 5; i++) {
                                                                    double offsetX = (RANDOM.nextDouble() - 0.5) * 8;
                                                                    double offsetZ = (RANDOM.nextDouble() - 0.5) * 8;
                                                                    EntityLightningBolt extraLightning =
                                                                            new EntityLightningBolt(
                                                                                    world,
                                                                                    player.posX + offsetX,
                                                                                    player.posY,
                                                                                    player.posZ + offsetZ,
                                                                                    false);
                                                                    world.spawnEntity(extraLightning);
                                                                }

                                                                MimsAdditions.LOGGER.debug(
                                                                        "Granted {} {} Iridium items",
                                                                        player.getName(),
                                                                        finalAmount);
                                                            } else {
                                                                MimsAdditions.LOGGER.error(
                                                                        "Could not create Iridium for player {}",
                                                                        player.getName());
                                                                world.getMinecraftServer()
                                                                        .getPlayerList()
                                                                        .sendMessage(
                                                                                new TextComponentString(
                                                                                        TextFormatting.RED
                                                                                                + "<Iridium God> An error occurred. The gift could not be delivered."));
                                                            }
                                                        });
                                    }
                                },
                                8000);

            } else {
                new Timer()
                        .schedule(
                                new TimerTask() {
                                    @Override
                                    public void run() {
                                        world.getMinecraftServer()
                                                .addScheduledTask(
                                                        () -> {
                                                            world.getMinecraftServer()
                                                                    .getPlayerList()
                                                                    .sendMessage(
                                                                            new TextComponentString(
                                                                                    TextFormatting.LIGHT_PURPLE
                                                                                            + "<Iridium God> "
                                                                                            + TextFormatting.RED
                                                                                            + "Scram."));

                                                            for (int i = 0; i < 10; i++) {
                                                                double offsetX = (RANDOM.nextDouble() - 0.5) * 4;
                                                                double offsetZ = (RANDOM.nextDouble() - 0.5) * 4;

                                                                EntityTNTPrimed tnt =
                                                                        new EntityTNTPrimed(
                                                                                world,
                                                                                player.posX + offsetX,
                                                                                player.posY,
                                                                                player.posZ + offsetZ,
                                                                                null);
                                                                tnt.setFuse(20); // 1 second fuse (20 ticks)
                                                                world.spawnEntity(tnt);
                                                            }

                                                            MimsAdditions.LOGGER.debug(
                                                                    "Iridium God denied {} and spawned TNT!",
                                                                    player.getName());
                                                        });
                                    }
                                },
                                6000);
            }
        }
    }
}
