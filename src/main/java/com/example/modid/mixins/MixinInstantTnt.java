package com.example.modid.mixins;

import net.minecraft.entity.item.EntityTNTPrimed;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTNTPrimed.class)
public class MixinInstantTnt {

    @Shadow private int fuse;

    @Inject(method = "onUpdate", at = @At("HEAD"))
    private void modid$instantExplode(CallbackInfo ci) {
        // 0 makes vanilla's "if (fuse-- <= 0)" trigger this tick
        if (this.fuse > 0) {
            this.fuse = 0;
        }
    }
}
