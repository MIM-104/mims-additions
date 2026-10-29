package com.example.modid.client;

import com.example.modid.mixins.MixinAnimationSpriteClock;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

/** Generates the current surface on the atlas; no stored animation frames or UV scrolling. */
@SideOnly(Side.CLIENT)
public final class InfinityPulseSprite extends TextureAtlasSprite {
    private static final ResourceLocation INGOT = new ResourceLocation("avaritia", "items/resource/infinity_ingot");
    private int mipmapLevels;
    private TextureAtlasSprite ingot;
    private int[][] surfaces;

    public InfinityPulseSprite(ResourceLocation location) {
        super(location.toString());
    }

    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
        return true;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.singletonList(INGOT);
    }

    @Override
    public boolean load(
            IResourceManager manager,
            ResourceLocation location,
            Function<ResourceLocation, TextureAtlasSprite> textureGetter) {
        setIconWidth(InfinityPulseColors.SIZE);
        setIconHeight(InfinityPulseColors.SIZE);
        ingot = textureGetter.apply(INGOT);
        surfaces = null;
        if (ingot != null && ingot.getFrameCount() > 0) {
            surfaces = new int[ingot.getFrameCount()][];
            for (int i = 0; i < surfaces.length; i++) {
                int[][] frame = ingot.getFrameTextureData(i);
                surfaces[i] =
                        frame == null
                                ? InfinityPulseColors.createFrame(0.0)
                                : InfinityPulseColors.fromIngotFrame(frame[0], ingot.getIconWidth());
            }
        }
        setFramesTextureData(Collections.singletonList(new int[][] {currentSurface()}));
        // TextureMap's custom-loader branch stitches the sprite when load returns false.
        return false;
    }

    @Override
    public boolean hasAnimationMetadata() {
        // Enrol in the atlas update loop, including buckets and GUI fluid slots.
        return true;
    }

    @Override
    public void generateMipmaps(int level) {
        mipmapLevels = level;
        int[][] levels = new int[level + 1][];
        levels[0] = getFrameTextureData(0)[0];
        setFramesTextureData(Collections.singletonList(levels));
        super.generateMipmaps(level);
    }

    @Override
    public void updateAnimation() {
        int[][] levels = new int[mipmapLevels + 1][];
        levels[0] = currentSurface();
        int[][] mipmaps = TextureUtil.generateMipmapData(mipmapLevels, getIconWidth(), levels);
        TextureUtil.uploadTextureMipmap(
                mipmaps, getIconWidth(), getIconHeight(), getOriginX(), getOriginY(), false, false);
    }

    private int[] currentSurface() {
        if (surfaces == null || surfaces.length == 0) return InfinityPulseColors.createFrame(0.0);
        AnimationMetadataSection animation = ingot.animationMetadata;
        if (animation == null || animation.getFrameCount() == 0) return surfaces[0];
        MixinAnimationSpriteClock clock = (MixinAnimationSpriteClock) ingot;
        int frame = clock.modid$getFrameCounter() % animation.getFrameCount();
        int from = animation.getFrameIndex(frame);
        int to = animation.getFrameIndex((frame + 1) % animation.getFrameCount());
        double fraction =
                animation.isInterpolate()
                        ? (double) clock.modid$getTickCounter() / animation.getFrameTimeSingle(frame)
                        : 0.0;
        return InfinityPulseColors.interpolate(surfaces[from], surfaces[to], fraction);
    }
}
