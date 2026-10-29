package com.example.modid.mixins;

import com.brandon3055.draconicevolution.blocks.reactor.ProcessExplosion;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ProcessExplosion.class, remap = false)
public abstract class MixinProcessExplosion {

    @Shadow protected boolean calculationComplete;

    @Shadow
    public abstract void updateCalculation();

    @Shadow
    public abstract boolean detonate();

    @Inject(method = "updateProcess", at = @At("HEAD"), cancellable = true)
    private void forceInstantExplosion(CallbackInfo ci) {
        // Finish the explosion calculation in this tick; there is no grace period.
        while (!this.calculationComplete) {
            this.updateCalculation();
        }

        this.detonate();

        ci.cancel();
    }
}
