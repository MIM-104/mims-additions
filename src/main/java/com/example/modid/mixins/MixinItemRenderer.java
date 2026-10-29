package com.example.modid.mixins;

import com.example.modid.client.HotFluidBucketColorHandler;
import com.example.modid.client.HotFluidBucketHeatTracker;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

    @Shadow public RenderItem itemRenderer;

    @Inject(
            method = "renderItemSide",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/RenderItem;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;Z)V",
                            shift = At.Shift.AFTER))
    private void modid$renderHeatGlow(
            EntityLivingBase entity,
            ItemStack stack,
            ItemCameraTransforms.TransformType transform,
            boolean leftHanded,
            CallbackInfo ci) {
        if (stack.isEmpty()) {
            return;
        }

        FluidStack fluidStack = HotFluidBucketHeatTracker.getFluidStack(stack);
        if (fluidStack == null || fluidStack.getFluid() == null) {
            return;
        }

        float ratio = HotFluidBucketHeatTracker.getHeatRatio(stack);
        if (ratio <= 0.0f) {
            return;
        }

        int color = HotFluidBucketColorHandler.getHeatColor(ratio);
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float alpha = 0.35f + 0.45f * ratio;
        float haloAlpha = 0.15f + 0.25f * ratio;
        float haloScale = 1.02f + 0.06f * ratio;

        float prevX = OpenGlHelper.lastBrightnessX;
        float prevY = OpenGlHelper.lastBrightnessY;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        GlStateManager.pushMatrix();
        GlStateManager.scale(haloScale, haloScale, haloScale);
        GlStateManager.color(r, g, b, haloAlpha);
        this.itemRenderer.renderItem(stack, entity, transform, leftHanded);
        GlStateManager.popMatrix();

        GlStateManager.color(r, g, b, alpha);
        this.itemRenderer.renderItem(stack, entity, transform, leftHanded);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, prevX, prevY);
    }
}
