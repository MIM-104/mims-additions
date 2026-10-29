package com.example.modid.mixins;

import gregtech.api.metatileentity.MetaTileEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = MetaTileEntity.class, remap = false)
public class MixinMetaTileEntityExplosion {
    private static final float EXPLOSION_SCALE = 100.0f;
    private static final float MIN_EXPLOSION_RADIUS = 200.0f;
    private static final double KNOCKBACK_BASE_PER_TIER = 5.0d;
    private static final double KNOCKBACK_TIER_MULTIPLIER = 2.5d;
    private static final double KNOCKBACK_MIN_DISTANCE = 2.0d;

    @org.spongepowered.asm.mixin.Unique private float modid$baseExplosionPower;

    @ModifyVariable(method = "doExplosion", at = @At("HEAD"), argsOnly = true)
    private float modid$scaleExplosion(float power) {
        this.modid$baseExplosionPower = power;
        float scaled = power * EXPLOSION_SCALE;
        return scaled < MIN_EXPLOSION_RADIUS ? MIN_EXPLOSION_RADIUS : scaled;
    }

    @org.spongepowered.asm.mixin.injection.Inject(method = "doExplosion", at = @At("TAIL"))
    private void modid$blastNearbyEntities(
            float power, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        MetaTileEntity self = (MetaTileEntity) (Object) this;
        if (self.getWorld() == null || self.getPos() == null) {
            return;
        }
        int tier = Math.max(1, Math.round(this.modid$baseExplosionPower));
        double speed = KNOCKBACK_BASE_PER_TIER + (tier * KNOCKBACK_TIER_MULTIPLIER);
        double radius = Math.max(MIN_EXPLOSION_RADIUS, power);
        double cx = self.getPos().getX() + 0.5d;
        double cy = self.getPos().getY() + 0.5d;
        double cz = self.getPos().getZ() + 0.5d;

        AxisAlignedBB box =
                new AxisAlignedBB(cx - radius, cy - radius, cz - radius, cx + radius, cy + radius, cz + radius);
        java.util.List<Entity> entities = self.getWorld().getEntitiesWithinAABB(Entity.class, box);
        for (Entity entity : entities) {
            if (entity instanceof EntityLivingBase) {
                double dx = entity.posX - cx;
                double dy = entity.posY + (entity.height * 0.5d) - cy;
                double dz = entity.posZ - cz;
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (dist < KNOCKBACK_MIN_DISTANCE) {
                    dist = KNOCKBACK_MIN_DISTANCE;
                }
                double scale = speed / dist;
                entity.motionX += dx * scale;
                entity.motionY += dy * scale;
                entity.motionZ += dz * scale;
                entity.velocityChanged = true;
            }
        }
    }
}
