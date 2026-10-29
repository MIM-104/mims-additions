package com.example.modid;

import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.cube.SimpleCubeRenderer;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public final class ModTextures {

    private ModTextures() {}

    public static final ResourceLocation SPACE_DIAMOND_RL = new ResourceLocation(Tags.MOD_ID, "blocks/space_diamond");

    // Construct the renderer before the atlas stitch event; the event only registers its sprite.
    public static final ICubeRenderer SPACE_DIAMOND = new SimpleCubeRenderer(Tags.MOD_ID + ":blocks/space_diamond");

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        event.getMap().registerSprite(SPACE_DIAMOND_RL);
        MimsAdditions.LOGGER.info("[{}] Registered sprite: {}", Tags.MOD_ID, SPACE_DIAMOND_RL);
    }
}
