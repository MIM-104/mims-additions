package com.example.modid.mixins;

import com.example.modid.Tags;
import com.example.modid.common.MachineTooltipHelper;

import gregtech.api.metatileentity.MetaTileEntity;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(targets = "gregtech.api.metatileentity.SimpleMachineMetaTileEntity", remap = false)
public abstract class MixinSupremeMachineTooltip {

    @Inject(method = "addInformation", at = @At("TAIL"))
    private void modid$addSupremeMachineDescription(
            ItemStack stack, World world, List<String> tooltip, boolean advanced, CallbackInfo callbackInfo) {
        MetaTileEntity machine = (MetaTileEntity) (Object) this;
        if (!Tags.MOD_ID.equals(machine.metaTileEntityId.getNamespace())) {
            return;
        }

        String description = MachineTooltipHelper.getDescription(machine.metaTileEntityId.getPath());
        if (description != null) {
            MachineTooltipHelper.addTooltip(tooltip, TextFormatting.GRAY + description);
        }
    }
}
