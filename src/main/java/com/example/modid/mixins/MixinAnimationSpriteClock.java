package com.example.modid.mixins;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Read the ingot's real atlas clock so its fluid and inventory item pulse together. */
@Mixin(TextureAtlasSprite.class)
public interface MixinAnimationSpriteClock {
    @Accessor("frameCounter")
    int modid$getFrameCounter();

    @Accessor("tickCounter")
    int modid$getTickCounter();
}
