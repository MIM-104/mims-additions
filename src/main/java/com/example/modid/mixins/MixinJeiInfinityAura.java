package com.example.modid.mixins;

import com.example.modid.client.InfinityAuraRenderer;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fluids.FluidStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "mezz.jei.plugins.vanilla.ingredients.fluid.FluidStackRenderer", remap = false)
public abstract class MixinJeiInfinityAura {
    @Shadow @Final private int width;
    @Shadow @Final private int height;
    @Shadow @Final private int capacityMb;

    @Inject(
            method = "render(Lnet/minecraft/client/Minecraft;IILnet/minecraftforge/fluids/FluidStack;)V",
            at = @At("HEAD"))
    private void modid$renderInfinityAura(Minecraft minecraft, int x, int y, FluidStack fluid, CallbackInfo ci) {
        int filled = InfinityAuraRenderer.filledHeight(fluid, capacityMb, height);
        InfinityAuraRenderer.renderFluidBackdrop(fluid, x, y + height - filled, width, filled, 99.0);
    }
}
