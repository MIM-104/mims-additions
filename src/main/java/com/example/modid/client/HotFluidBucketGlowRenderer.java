package com.example.modid.client;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;

public final class HotFluidBucketGlowRenderer {

    private HotFluidBucketGlowRenderer() {}

    public static void renderGuiGlow(float zLevel, ItemStack stack, int x, int y) {
        if (stack.isEmpty()) {
            return;
        }

        float ratio = HotFluidBucketHeatTracker.getHeatRatio(stack);
        if (ratio <= 0.0f) {
            return;
        }

        int color = HotFluidBucketColorHandler.getHeatColor(ratio);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        float expand = 0.25f + 0.75f * ratio;
        float alpha = 0.15f + 0.45f * ratio;
        int a = Math.min(255, Math.max(0, Math.round(alpha * 255.0f)));
        float left = x - expand;
        float right = x + 16 + expand;
        float top = y - expand;
        float bottom = y + 16 + expand;
        float z = zLevel - 75.0f;

        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(left, bottom, z).color(r, g, b, a).endVertex();
        bufferbuilder.pos(right, bottom, z).color(r, g, b, a).endVertex();
        bufferbuilder.pos(right, top, z).color(r, g, b, a).endVertex();
        bufferbuilder.pos(left, top, z).color(r, g, b, a).endVertex();
        tessellator.draw();

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
    }
}
