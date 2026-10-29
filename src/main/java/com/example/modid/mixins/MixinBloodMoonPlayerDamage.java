package com.example.modid.mixins;

import com.example.modid.BloodMoonHandler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityPlayer.class)
public abstract class MixinBloodMoonPlayerDamage {

    @Redirect(
            method = "attackEntityFrom",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/World;getDifficulty()Lnet/minecraft/world/EnumDifficulty;"))
    private EnumDifficulty modid$allowBloodMoonDamageInPeaceful(World world, DamageSource source, float amount) {
        Entity attacker = source.getTrueSource();
        return BloodMoonHandler.isBloodMoonMob(attacker) ? EnumDifficulty.NORMAL : world.getDifficulty();
    }
}
