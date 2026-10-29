package com.example.modid.mixins;

import com.example.modid.client.InfinityAuraRenderer;

import net.minecraftforge.fluids.FluidStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "gregtech.client.utils.RenderUtil", remap = false)
public abstract class MixinGregtechInfinityAura {
    @Inject(method = "drawFluidForGui", at = @At("HEAD"))
    private static void modid$renderInfinityAura(
            FluidStack fluid, int capacity, int x, int y, int width, int height, CallbackInfo ci) {
        int filled = InfinityAuraRenderer.filledHeight(fluid, capacity, height - 1);
        InfinityAuraRenderer.renderFluidBackdrop(fluid, x, y + height - 1 - filled, width - 1, filled, -0.01);
    }
}
