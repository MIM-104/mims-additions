package com.example.modid.audit;

import com.example.modid.Tags;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

/** Starts only the disposable audit world, when explicitly enabled by the test launcher. */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public final class ProgressionAuditClient {
    private static boolean launched;

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (!Boolean.getBoolean("mim.progression.audit") || event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (ProgressionAudit.finished && Boolean.getBoolean("mim.progression.auditExit")) {
            mc.shutdown();
            return;
        }
        if (!launched && mc.currentScreen instanceof GuiMainMenu) {
            launched = true;
            WorldSettings settings = new WorldSettings(0, GameType.SURVIVAL, false, false, WorldType.FLAT);
            mc.launchIntegratedServer("audit-world", "Progression audit", settings);
        }
    }
}
