package com.example.modid.mixins;

import com.example.modid.firmware.FirmwareData;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Preserve GT's own subtype data, adding only the board's application. */
@Mixin(targets = "mezz.jei.runtime.SubtypeRegistry", remap = false)
public abstract class MixinFirmwareJeiSubtypes {
    @Inject(
            method = "getSubtypeInfo(Lnet/minecraft/item/ItemStack;)Ljava/lang/String;",
            at = @At("RETURN"),
            cancellable = true)
    private void modid$firmwareIdentity(ItemStack stack, CallbackInfoReturnable<String> cir) {
        if (FirmwareData.isCircuit(stack)) {
            String original = cir.getReturnValue();
            cir.setReturnValue((original == null ? "" : original) + ";firmware=" + FirmwareData.target(stack));
        }
    }

    @Inject(method = "hasSubtypeInterpreter(Lnet/minecraft/item/ItemStack;)Z", at = @At("RETURN"), cancellable = true)
    private void modid$hasFirmwareIdentity(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (FirmwareData.isCircuit(stack)) cir.setReturnValue(true);
    }
}
