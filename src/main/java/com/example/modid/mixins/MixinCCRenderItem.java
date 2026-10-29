package com.example.modid.mixins;

import com.example.modid.client.HotFluidBucketGlowRenderer;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Pseudo
@Mixin(targets = "codechicken.lib.render.item.CCRenderItem", remap = false)
public abstract class MixinCCRenderItem {

    @Inject(
            method =
                    "renderItemAndEffectIntoGUI(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;II)V",
            at = @At("HEAD"))
    private void modid$renderHeatGuiGlowEntity(
            @Nullable EntityLivingBase entity, ItemStack stack, int x, int y, CallbackInfo ci) {
        HotFluidBucketGlowRenderer.renderGuiGlow(0.0f, stack, x, y);
    }

    @Inject(method = "renderItemAndEffectIntoGUI(Lnet/minecraft/item/ItemStack;II)V", at = @At("HEAD"))
    private void modid$renderHeatGuiGlow(ItemStack stack, int x, int y, CallbackInfo ci) {
        HotFluidBucketGlowRenderer.renderGuiGlow(0.0f, stack, x, y);
    }
}
