package com.example.modid.audit;

import com.example.modid.MimsAdditions;
import com.example.modid.Tags;
import com.example.modid.firmware.*;

import gregtech.api.GTValues;
import gregtech.common.items.MetaItems;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

/** Opt-in GUI fixture in the disposable audit world; never runs during normal play. */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public final class FirmwareWorkbenchPreview {
    private static boolean opened;

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (!Boolean.getBoolean("mim.firmware.preview") || opened || event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null || mc.player == null || mc.currentScreen != null || mc.getIntegratedServer() == null)
            return;
        opened = true;
        org.lwjgl.opengl.Display.setTitle("Firmware Workbench UI Test");
        java.util.UUID playerId = mc.player.getUniqueID();
        mc.getIntegratedServer()
                .addScheduledTask(
                        () -> {
                            EntityPlayerMP player = mc.getIntegratedServer().getPlayerList().getPlayerByUUID(playerId);
                            if (player == null
                                    || !"audit-world"
                                            .equals(
                                                    player.getServerWorld()
                                                            .getSaveHandler()
                                                            .getWorldDirectory()
                                                            .getName())) return;
                            player.setGameType(GameType.CREATIVE);
                            player.getServerWorld().setWorldTime(6000);
                            player.getServerWorld().getGameRules().setOrCreateGameRule("doDaylightCycle", "false");
                            BlockPos pos = player.getPosition().add(2, 0, 0);
                            player.world.setBlockState(pos, FirmwareContent.WORKBENCH.getDefaultState());
                            FirmwareWorkbenchTile bench = (FirmwareWorkbenchTile) player.world.getTileEntity(pos);
                            bench.items.setStackInSlot(0, FirmwareCircuits.variants(GTValues.ZPM).get(0).copy());
                            bench.items.setStackInSlot(2, new ItemStack(FirmwareContent.SOLDERING_IRON));
                            bench.items.setStackInSlot(3, new ItemStack(FirmwareContent.WIRE_STRIPPERS));
                            ItemStack wire = FirmwareMaterials.solder();
                            wire.setCount(16);
                            bench.items.setStackInSlot(4, wire);
                            for (int branch = 0; branch < 5; branch++)
                                bench.items.setStackInSlot(6 + branch, bench.requirement(branch));
                            bench.action(player, 0, 0, FirmwareData.targetKey(MetaItems.ROBOT_ARM_IV.getStackForm()));
                            for (int node = 0; node < 5; node++) bench.rotations[node] = 0;
                            ItemStack tin = FirmwareMaterials.tinWire();
                            tin.setCount(64);
                            player.inventory.setInventorySlotContents(0, tin);
                            ItemStack alloy = FirmwareMaterials.solder();
                            alloy.setCount(64);
                            player.inventory.setInventorySlotContents(1, alloy);
                            player.inventory.setInventorySlotContents(
                                    2, MetaItems.ELECTRONIC_CIRCUIT_LV.getStackForm());
                            player.inventory.setInventorySlotContents(
                                    3, FirmwareCircuits.variants(GTValues.ZPM).get(0).copy());
                            player.openGui(
                                    MimsAdditions.INSTANCE, 41, player.world, pos.getX(), pos.getY(), pos.getZ());
                            MimsAdditions.LOGGER.info("FIRMWARE WORKBENCH PREVIEW OPENED");
                        });
    }
}
