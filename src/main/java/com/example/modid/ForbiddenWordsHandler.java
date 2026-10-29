package com.example.modid;

import com.brandon3055.draconicevolution.blocks.reactor.tileentity.TileReactorCore;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class ForbiddenWordsHandler {

    private static final java.util.Random RANDOM = new java.util.Random();
    private static final Pattern FORBIDDEN_PATTERN =
            Pattern.compile("(gex|pex|grex|lex|dex)", Pattern.CASE_INSENSITIVE);

    @SubscribeEvent
    public static void onPlayerChat(ServerChatEvent event) {
        String message = event.getMessage();
        EntityPlayer player = event.getPlayer();
        World world = player.world;

        Matcher matcher = FORBIDDEN_PATTERN.matcher(message);

        if (!matcher.find()) return;

        final String badWord = matcher.group();

        MimsAdditions.LOGGER.info("Player {} triggered forbidden word: {}", player.getName(), badWord);

        Timer timer = new Timer(true);

        // After 2 seconds: pause before the reply.
        timer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        world.getMinecraftServer()
                                .addScheduledTask(
                                        () -> {
                                            player.sendMessage(new TextComponentString("..."));
                                        });
                    }
                },
                2000);

        timer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        world.getMinecraftServer()
                                .addScheduledTask(
                                        () -> {
                                            player.sendMessage(
                                                    new TextComponentString("How about I " + badWord + " YOU!"));
                                        });
                    }
                },
                3000);

        // 1 second later: spawn reactor + trigger internal DE explosion
        timer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        world.getMinecraftServer()
                                .addScheduledTask(
                                        () -> {
                                            BlockPos pos = player.getPosition();

                                            world.setBlockState(
                                                    pos,
                                                    com.brandon3055.draconicevolution.DEFeatures.reactorCore
                                                            .getDefaultState());

                                            TileEntity te = world.getTileEntity(pos);
                                            if (!(te instanceof TileReactorCore)) return;

                                            TileReactorCore reactor = (TileReactorCore) te;

                                            // Push the reactor past its failure thresholds.
                                            reactor.reactableFuel.value = 9999.0;
                                            reactor.convertedFuel.value = 0.0;

                                            double totalFuel = reactor.reactableFuel.value;
                                            reactor.maxShieldCharge.value = totalFuel * 96.45061728395062 * 100;
                                            reactor.maxSaturation.value = (int) (totalFuel * 96.45061728395062 * 1000);

                                            reactor.temperature.value = 10000;
                                            reactor.shieldCharge.value = 0;
                                            reactor.saturation.value = 0;

                                            reactor.reactorState.value = TileReactorCore.ReactorState.BEYOND_HOPE;

                                            // The explosion entry point differs between DE versions.
                                            try {
                                                Method explodeMethod;
                                                try {
                                                    explodeMethod = TileReactorCore.class.getDeclaredMethod("explode");
                                                } catch (NoSuchMethodException e) {
                                                    explodeMethod = TileReactorCore.class.getDeclaredMethod("detonate");
                                                }
                                                explodeMethod.setAccessible(true);
                                                explodeMethod.invoke(reactor);
                                            } catch (Exception e) {
                                                MimsAdditions.LOGGER.error(
                                                        "Failed to trigger instant reactor explosion", e);
                                            }
                                        });
                    }
                },
                4000);
    }
}
