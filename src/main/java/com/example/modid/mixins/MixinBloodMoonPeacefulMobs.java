package com.example.modid.mixins;

import com.example.modid.BloodMoonHandler;

import net.minecraft.entity.monster.EntityMob;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityMob.class)
public abstract class MixinBloodMoonPeacefulMobs {

    @Redirect(
            method = "onUpdate",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/World;getDifficulty()Lnet/minecraft/world/EnumDifficulty;"))
    private EnumDifficulty modid$keepBloodMoonMobsAliveInPeaceful(World world) {
        return BloodMoonHandler.isBloodMoonMob((EntityMob) (Object) this)
                ? EnumDifficulty.NORMAL
                : world.getDifficulty();
    }
}
