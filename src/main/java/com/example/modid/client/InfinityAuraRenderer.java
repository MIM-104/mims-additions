package com.example.modid.client;

import com.example.modid.common.ModFluids;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.nio.FloatBuffer;

/** Avaritia-style inventory layers: black 10px halo, 60% scaled ghost, then the normal icon. */
@SideOnly(Side.CLIENT)
public final class InfinityAuraRenderer {
    private InfinityAuraRenderer() {}

    public static boolean isInfinity(FluidStack stack) {
        return stack != null && stack.getFluid() != null && stack.getFluid() == ModFluids.moltenInfinity;
    }

    public static boolean renderItemBackdrop(float zLevel, ItemStack stack, int x, int y) {
        if (stack.isEmpty() || !isInfinity(FluidUtil.getFluidContained(stack))) return false;
        Minecraft mc = Minecraft.getMinecraft();
        try (RenderState ignored = new RenderState()) {
            begin(mc);
            double scale = scale();
            drawHalo(mc, x, y, 16, 16, zLevel + 149.0, scale);
            IBakedModel model = mc.getRenderItem().getItemModelWithOverrides(stack, null, null);
            if (!model.isBuiltInRenderer()) {
                GlStateManager.pushMatrix();
                try {
                    GlStateManager.translate(x + 8.0, y + 8.0, zLevel + 149.5);
                    GlStateManager.scale(16.0 * scale, -16.0 * scale, 16.0 * scale);
                    model =
                            ForgeHooksClient.handleCameraTransforms(
                                    model, ItemCameraTransforms.TransformType.GUI, false);
                    GlStateManager.translate(-0.5, -0.5, -0.5);
                    drawItemGhost(mc, model, stack);
                } finally {
                    GlStateManager.popMatrix();
                }
            }
        }
        return true;
    }

    public static void renderFluidBackdrop(FluidStack fluid, int x, int y, int width, int height, double z) {
        if (!isInfinity(fluid) || fluid.amount <= 0 || width <= 0 || height <= 0) return;
        Minecraft mc = Minecraft.getMinecraft();
        try (RenderState ignored = new RenderState()) {
            begin(mc);
            double scale = scale();
            drawHalo(mc, x, y, width, height, z, scale);
            TextureAtlasSprite sprite =
                    mc.getTextureMapBlocks().getAtlasSprite(fluid.getFluid().getStill(fluid).toString());
            double extraX = width * (scale - 1.0) / 2.0;
            double extraY = height * (scale - 1.0) / 2.0;
            drawSprite(sprite, x - extraX, y - extraY, width * scale, height * scale, z + 0.01, 0x99FFFFFF);
        }
    }

    public static int filledHeight(FluidStack fluid, int capacity, int height) {
        if (fluid == null || fluid.amount <= 0 || capacity <= 0 || height <= 0) return 0;
        return (int) Math.min(height, Math.max(1L, (long) fluid.amount * height / capacity));
    }

    private static double scale() {
        return InfinityPulseColors.pulseScale(Minecraft.getSystemTime() / 1000.0);
    }

    private static void begin(Minecraft mc) {
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 771);
        GlStateManager.color(1, 1, 1, 1);
    }

    private static void drawHalo(Minecraft mc, int x, int y, int width, int height, double z, double scale) {
        TextureAtlasSprite halo = mc.getTextureMapBlocks().getAtlasSprite("avaritia:items/halo");
        double spread = 10.0 + (scale - 1.025) * 6.0;
        // The actual Infinity ingot uses an opaque black tint on Avaritia's radial alpha mask.
        drawSprite(halo, x - spread, y - spread, width + spread * 2, height + spread * 2, z, 0xFF000000);
    }

    private static void drawSprite(
            TextureAtlasSprite sprite, double x, double y, double width, double height, double z, int color) {
        int a = color >>> 24, r = color >> 16 & 255, g = color >> 8 & 255, b = color & 255;
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(x, y + height, z).tex(sprite.getMinU(), sprite.getMaxV()).color(r, g, b, a).endVertex();
        buffer.pos(x + width, y + height, z).tex(sprite.getMaxU(), sprite.getMaxV()).color(r, g, b, a).endVertex();
        buffer.pos(x + width, y, z).tex(sprite.getMaxU(), sprite.getMinV()).color(r, g, b, a).endVertex();
        buffer.pos(x, y, z).tex(sprite.getMinU(), sprite.getMinV()).color(r, g, b, a).endVertex();
        Tessellator.getInstance().draw();
    }

    private static void drawItemGhost(Minecraft mc, IBakedModel model, ItemStack stack) {
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
        for (int face = 0; face <= EnumFacing.VALUES.length; face++) {
            EnumFacing side = face == EnumFacing.VALUES.length ? null : EnumFacing.VALUES[face];
            for (BakedQuad quad : model.getQuads(null, side, 0L)) {
                int color = quad.hasTintIndex() ? mc.getItemColors().colorMultiplier(stack, quad.getTintIndex()) : -1;
                LightUtil.renderQuadColor(buffer, quad, (color & 0xFFFFFF) | 0x99000000);
            }
        }
        Tessellator.getInstance().draw();
    }

    /** Restore GL through its state manager, including the atlas binding and alpha/blend settings. */
    private static final class RenderState implements AutoCloseable {
        private final boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
        private final boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        private final boolean alpha = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
        private final boolean lighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
        private final boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        private final boolean texture = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        private final boolean depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        private final int binding = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        private final int src = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
        private final int dst = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
        private final int srcAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
        private final int dstAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        // LWJGL 2's glGetFloat binding requires 16 slots even for GL_CURRENT_COLOR's four values.
        private final FloatBuffer color = BufferUtils.createFloatBuffer(16);

        private RenderState() {
            GL11.glGetFloat(GL11.GL_CURRENT_COLOR, color);
        }

        @Override
        public void close() {
            GlStateManager.tryBlendFuncSeparate(src, dst, srcAlpha, dstAlpha);
            if (blend) GlStateManager.enableBlend();
            else GlStateManager.disableBlend();
            if (depth) GlStateManager.enableDepth();
            else GlStateManager.disableDepth();
            if (alpha) GlStateManager.enableAlpha();
            else GlStateManager.disableAlpha();
            if (lighting) GlStateManager.enableLighting();
            else GlStateManager.disableLighting();
            if (cull) GlStateManager.enableCull();
            else GlStateManager.disableCull();
            if (texture) GlStateManager.enableTexture2D();
            else GlStateManager.disableTexture2D();
            GlStateManager.depthMask(depthMask);
            GlStateManager.bindTexture(binding);
            GlStateManager.color(color.get(0), color.get(1), color.get(2), color.get(3));
        }
    }
}
