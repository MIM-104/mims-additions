package com.example.modid.mixins;

import com.example.modid.common.MachineTooltipHelper;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.WorkableTieredMetaTileEntity;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = WorkableTieredMetaTileEntity.class, remap = false)
public abstract class MixinUhvSingleblockTooltip {

    @Inject(method = "addInformation", at = @At("TAIL"))
    private void modid$addParallelismTooltip(
            ItemStack stack, World world, List<String> tooltip, boolean advanced, CallbackInfo callbackInfo) {
        if (((ITieredMetaTileEntity) this).getTier() == GTValues.UHV) {
            MachineTooltipHelper.addTooltip(tooltip, TextFormatting.GRAY + "Can be parallelised to 64x");
        }
    }
}
