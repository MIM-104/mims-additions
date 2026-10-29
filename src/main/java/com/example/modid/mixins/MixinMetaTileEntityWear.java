package com.example.modid.mixins;

import codechicken.lib.raytracer.CuboidRayTraceResult;

import com.example.modid.common.CrippledMachine;
import com.example.modid.common.WearTrackedRecipeLogic;

import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "gregtech.api.metatileentity.MetaTileEntity", remap = false)
public abstract class MixinMetaTileEntityWear implements CrippledMachine {

    @Unique private boolean modid$crippled;

    @Shadow
    public abstract AbstractRecipeLogic getRecipeLogic();

    /** Intercepts soft mallet clicks on machines & controllers. */
    @Inject(method = "onSoftMalletClick", at = @At("HEAD"), cancellable = true)
    private void modid$onSoftMalletClick(
            EntityPlayer player,
            EnumHand hand,
            EnumFacing facing,
            CuboidRayTraceResult hitResult,
            CallbackInfoReturnable<Boolean> cir) {

        MetaTileEntity mte = (MetaTileEntity) (Object) this;
        World world = mte.getWorld();

        if (world != null && !world.isRemote) {
            if (modid$crippled) {
                return;
            }

            if (world.rand.nextFloat() < 0.05f) {
                BlockPos pos = mte.getPos();

                // Cancel the normal mallet sound when the machine breaks.
                world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_DESTROY, SoundCategory.BLOCKS, 1.5F, 0.85F);

                if (player != null) {
                    player.playSound(SoundEvents.BLOCK_ANVIL_DESTROY, 1.5F, 0.85F);
                    player.sendStatusMessage(
                            new TextComponentString(TextFormatting.DARK_RED + "You broke something!"), true);
                }

                modid$ejectRandomDebris(world, pos);

                modid$setCrippled(true);
                mte.markDirty();

                cir.setReturnValue(true);
            }
        }
    }

    /** Preserves wear data and the IsCrippled tag when breaking/picking up the machine as an item. */
    @Inject(method = "writeItemStackData", at = @At("RETURN"))
    private void modid$writeWearToItem(NBTTagCompound tag, CallbackInfo ci) {

        if (tag == null) {
            return;
        }

        AbstractRecipeLogic logic = getRecipeLogic();

        if (logic instanceof WearTrackedRecipeLogic) {
            WearTrackedRecipeLogic wear = (WearTrackedRecipeLogic) logic;

            tag.setLong("WearTicks", wear.modid$getWearTicks());

            tag.setInteger("WearBaseEUt", wear.modid$getBaseRecipeEUt());
        }

        if (modid$crippled) {
            tag.setBoolean("IsCrippled", true);
        }
    }

    /** Restores wear data and the IsCrippled tag when placing the machine back down. */
    @Inject(method = "initFromItemStackData", at = @At("HEAD"))
    private void modid$readWearFromItem(NBTTagCompound tag, CallbackInfo ci) {

        if (tag == null) {
            return;
        }

        // Restore wear information
        AbstractRecipeLogic logic = getRecipeLogic();

        if (logic instanceof WearTrackedRecipeLogic) {
            WearTrackedRecipeLogic wear = (WearTrackedRecipeLogic) logic;

            if (tag.hasKey("WearTicks")) {
                wear.modid$setWearTicks(tag.getLong("WearTicks"));
            }

            if (tag.hasKey("WearBaseEUt")) {
                wear.modid$setBaseRecipeEUt(tag.getInteger("WearBaseEUt"));
            }
        }

        if (tag.getBoolean("IsCrippled")) {
            modid$setCrippled(true);
        }
    }

    @Inject(method = "writeToNBT", at = @At("RETURN"))
    private void modid$writeCrippledToNbt(NBTTagCompound tag, CallbackInfoReturnable<NBTTagCompound> cir) {

        NBTTagCompound writtenTag = cir.getReturnValue();

        if (writtenTag != null && modid$crippled) {
            writtenTag.setBoolean("IsCrippled", true);
        }
    }

    @Inject(method = "readFromNBT", at = @At("HEAD"))
    private void modid$readCrippledFromNbt(NBTTagCompound tag, CallbackInfo ci) {

        modid$crippled = tag != null && tag.getBoolean("IsCrippled");
    }

    @Inject(method = "addInformation", at = @At("RETURN"))
    private void modid$addCrippledTooltip(
            ItemStack stack, World world, java.util.List<String> tooltip, boolean advanced, CallbackInfo ci) {

        if (stack != null
                && stack.hasTagCompound()
                && stack.getTagCompound().getBoolean("IsCrippled")
                && !tooltip.contains(TextFormatting.DARK_RED + "Crippled")) {
            tooltip.add(TextFormatting.DARK_RED + "Crippled");
        }
    }

    @Override
    public boolean modid$isCrippled() {
        return modid$crippled;
    }

    @Override
    public void modid$setCrippled(boolean crippled) {
        modid$crippled = modid$crippled || crippled;
    }

    /** Ejects 1-3 random GregTech components when the machine breaks. */
    private void modid$ejectRandomDebris(World world, BlockPos pos) {

        ItemStack[] itemPool =
                new ItemStack[] {
                    OreDictUnifier.get(OrePrefix.springSmall, Materials.Steel),
                    OreDictUnifier.get(OrePrefix.gear, Materials.Tin),
                    OreDictUnifier.get(OrePrefix.gearSmall, Materials.Iron),
                    OreDictUnifier.get(OrePrefix.bolt, Materials.Tin),
                    OreDictUnifier.get(OrePrefix.screw, Materials.WroughtIron),
                    OreDictUnifier.get(OrePrefix.spring, Materials.Iron),
                    OreDictUnifier.get(OrePrefix.dustSmall, Materials.WroughtIron),
                    OreDictUnifier.get(OrePrefix.stick, Materials.WroughtIron)
                };

        // Random count between 1 and 3
        int count = 1 + world.rand.nextInt(3);

        for (int i = 0; i < count; i++) {

            ItemStack stack = itemPool[world.rand.nextInt(itemPool.length)].copy();

            if (stack.isEmpty()) {
                continue;
            }

            stack.setCount(1);

            // Spawn position slightly above the machine
            double x = pos.getX() + 0.5 + (world.rand.nextDouble() - 0.5) * 0.3;

            double y = pos.getY() + 0.8;

            double z = pos.getZ() + 0.5 + (world.rand.nextDouble() - 0.5) * 0.3;

            EntityItem entity = new EntityItem(world, x, y, z, stack);

            // Small random sideways velocity
            entity.motionX = world.rand.nextGaussian() * 0.05;

            // Upward velocity
            entity.motionY = 0.2;

            entity.motionZ = world.rand.nextGaussian() * 0.05;

            world.spawnEntity(entity);
        }
    }
}
