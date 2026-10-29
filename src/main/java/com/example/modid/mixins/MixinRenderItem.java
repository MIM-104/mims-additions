package com.example.modid.mixins;

import com.example.modid.client.HotFluidBucketGlowRenderer;
import com.example.modid.client.InfinityAuraRenderer;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(RenderItem.class)
public abstract class MixinRenderItem {

    @Shadow public float zLevel;

    @Inject(
            method =
                    "renderItemAndEffectIntoGUI(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;II)V",
            at = @At("HEAD"))
    private void modid$renderHeatGuiGlow(
            @Nullable EntityLivingBase entity, ItemStack stack, int x, int y, CallbackInfo ci) {
        if (!InfinityAuraRenderer.renderItemBackdrop(this.zLevel, stack, x, y)) {
            HotFluidBucketGlowRenderer.renderGuiGlow(this.zLevel, stack, x, y);
        }
    }

    @Redirect(
            method =
                    "renderItemOverlayIntoGUI(Lnet/minecraft/client/gui/FontRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I"))
    private int modid$abbreviateMicrominerCount(
            FontRenderer fontRenderer,
            String countText,
            float x,
            float y,
            int color,
            FontRenderer originalFontRenderer,
            ItemStack stack,
            int xPosition,
            int yPosition,
            @Nullable String text) {
        ResourceLocation registryName = stack.getItem().getRegistryName();
        if (stack.getCount() >= 1000
                && registryName != null
                && "nomilabs".equals(registryName.getNamespace())
                && registryName.getPath().startsWith("tier")
                && registryName.getPath().endsWith("ship")) {
            countText = Math.round(stack.getCount() / 1000.0f) + "K";
            x = xPosition + 17 - fontRenderer.getStringWidth(countText);
        }
        return fontRenderer.drawStringWithShadow(countText, x, y, color);
    }
}
