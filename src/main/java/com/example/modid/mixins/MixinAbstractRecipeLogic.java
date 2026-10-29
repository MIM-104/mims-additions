package com.example.modid.mixins;

import com.example.modid.MicroverseProjector4;
import com.example.modid.common.CrippledMachine;
import com.example.modid.common.FactoryDecay;
import com.example.modid.common.WearTrackedRecipeLogic;
import com.example.modid.common.WearUtil;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityItemBus;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "gregtech.api.capability.impl.AbstractRecipeLogic", remap = false)
public abstract class MixinAbstractRecipeLogic implements WearTrackedRecipeLogic {

    @Shadow protected int recipeEUt;

    @Shadow protected int progressTime;

    @Shadow protected boolean hasNotEnoughEnergy;

    @Unique private long modid$wearTicks;

    @Unique private int modid$baseRecipeEUt;

    @Inject(method = "update", at = @At("TAIL"))
    private void modid$applyWearAndScale(CallbackInfo ci) {
        if (modid$baseRecipeEUt <= 0 && recipeEUt > 0) {
            modid$baseRecipeEUt = recipeEUt;
        }

        MetaTileEntity metaTileEntity = modid$getMetaTileEntity();
        World world = metaTileEntity == null ? null : metaTileEntity.getWorld();
        if (progressTime > 0 && modid$baseRecipeEUt > 0 && FactoryDecay.isMidGameActive(world)) {
            modid$incrementWearTicks();
        }

        if (modid$baseRecipeEUt > 0) {
            recipeEUt = modid$applyFactoryDecayEuT(modid$baseRecipeEUt);
        }
    }

    @Inject(method = "updateRecipeProgress", at = @At("HEAD"))
    private void modid$updateScaledRecipeEUt(CallbackInfo ci) {
        if (modid$baseRecipeEUt <= 0 && recipeEUt > 0) {
            modid$baseRecipeEUt = recipeEUt;
        }

        if (modid$baseRecipeEUt > 0) {
            recipeEUt = modid$applyFactoryDecayEuT(modid$baseRecipeEUt);
        }
    }

    @Inject(method = "getRecipeEUt", at = @At("HEAD"), cancellable = true)
    private void modid$getRecipeEUtWithWear(CallbackInfoReturnable<Integer> cir) {
        if (modid$baseRecipeEUt <= 0 && recipeEUt > 0) {
            modid$baseRecipeEUt = recipeEUt;
        }

        if (modid$baseRecipeEUt > 0) {
            cir.setReturnValue(modid$applyFactoryDecayEuT(modid$baseRecipeEUt));
        }
    }

    @Inject(method = "update", at = @At("TAIL"))
    private void modid$explodeOnPowerFail(CallbackInfo ci) {

        // Failed recipe starts count as power failures too.
        if (this.hasNotEnoughEnergy && this.recipeEUt > 0) {

            MetaTileEntity mte = modid$getMetaTileEntity();

            if (mte != null) {

                World world = mte.getWorld();

                if (world != null && !world.isRemote) {

                    BlockPos pos = mte.getPos();

                    world.createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), 8.0F, true);

                    // A surviving machine must not explode again on every tick.
                    this.hasNotEnoughEnergy = false;
                    this.progressTime = 0;
                    this.recipeEUt = 0;
                }
            }
        }
    }

    @Inject(method = "setupRecipe", at = @At("TAIL"))
    private void modid$cacheBaseEUt(CallbackInfo ci) {
        modid$baseRecipeEUt = recipeEUt;
    }

    @Inject(method = "completeRecipe", at = @At("TAIL"))
    private void modid$finishRecipe(CallbackInfo ci) {

        modid$baseRecipeEUt = 0;

        MetaTileEntity mte = modid$getMetaTileEntity();

        if (mte instanceof MicroverseProjector4) {

            World world = mte.getWorld();

            if (world != null && !world.isRemote) {

                MicroverseProjector4 projector = (MicroverseProjector4) mte;

                ItemStack targetChest = MetaTileEntities.CREATIVE_CHEST.getStackForm();

                for (IMultiblockPart part : projector.getMultiblockParts()) {

                    if (part instanceof MetaTileEntityItemBus) {

                        MetaTileEntityItemBus bus = (MetaTileEntityItemBus) part;

                        IItemHandler exportInv = bus.getExportItems();

                        if (exportInv instanceof IItemHandlerModifiable) {

                            IItemHandlerModifiable modifiableInv = (IItemHandlerModifiable) exportInv;

                            boolean producedCreativeChest = false;

                            // This recipe intentionally replaces the chest output with a fluid spill.
                            for (int i = 0; i < modifiableInv.getSlots(); i++) {

                                ItemStack slotStack = modifiableInv.getStackInSlot(i);

                                if (!slotStack.isEmpty() && slotStack.isItemEqual(targetChest)) {

                                    modifiableInv.setStackInSlot(i, ItemStack.EMPTY);

                                    producedCreativeChest = true;
                                }
                            }

                            if (producedCreativeChest) {

                                BlockPos busPos = bus.getPos();

                                EnumFacing front = bus.getFrontFacing();

                                BlockPos liquidPos = busPos.offset(front);

                                Fluid milkFluid = FluidRegistry.getFluid("milk");

                                Block blockToPlace = milkFluid != null ? milkFluid.getBlock() : null;

                                if (blockToPlace == null) {
                                    blockToPlace = Blocks.WATER;
                                }

                                if (world.isAirBlock(liquidPos)) {

                                    world.setBlockState(liquidPos, blockToPlace.getDefaultState());

                                    world.playSound(
                                            null,
                                            liquidPos.getX(),
                                            liquidPos.getY(),
                                            liquidPos.getZ(),
                                            SoundEvents.ITEM_BUCKET_EMPTY,
                                            SoundCategory.BLOCKS,
                                            1.0F,
                                            1.0F);
                                }

                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "serializeNBT", at = @At("RETURN"))
    private void modid$writeWearNbt(CallbackInfoReturnable<NBTTagCompound> cir) {

        NBTTagCompound tag = cir.getReturnValue();

        if (tag != null) {
            tag.setLong("WearTicks", modid$wearTicks);
            tag.setInteger("WearBaseEUt", modid$baseRecipeEUt);
        }
    }

    @Inject(method = "deserializeNBT", at = @At("HEAD"))
    private void modid$readWearTicks(NBTTagCompound tag, CallbackInfo ci) {

        if (tag != null && tag.hasKey("WearTicks")) {
            modid$wearTicks = tag.getLong("WearTicks");
        } else {
            modid$wearTicks = 0L;
        }

        if (tag != null && tag.hasKey("WearBaseEUt")) {
            modid$baseRecipeEUt = tag.getInteger("WearBaseEUt");
        } else {
            modid$baseRecipeEUt = 0;
        }
    }

    @Inject(method = "deserializeNBT", at = @At("TAIL"))
    private void modid$readWearBase(NBTTagCompound tag, CallbackInfo ci) {

        if (modid$baseRecipeEUt <= 0 && recipeEUt > 0) {
            modid$baseRecipeEUt = recipeEUt;
        }
    }

    @Override
    public int modid$scaleEuT(int base) {
        MetaTileEntity metaTileEntity = modid$getMetaTileEntity();
        World world = metaTileEntity == null ? null : metaTileEntity.getWorld();
        if (!FactoryDecay.isMidGameActive(world)) {
            return base;
        }
        return WearUtil.scaleEuT(base, modid$wearTicks);
    }

    @Override
    public void modid$incrementWearTicks() {
        modid$wearTicks++;
    }

    @Override
    public long modid$getWearTicks() {
        return modid$wearTicks;
    }

    @Override
    public void modid$setWearTicks(long ticks) {
        modid$wearTicks = Math.max(0L, ticks);
    }

    @Override
    public int modid$getBaseRecipeEUt() {
        return modid$baseRecipeEUt;
    }

    @Override
    public void modid$setBaseRecipeEUt(int base) {
        modid$baseRecipeEUt = base;
    }

    @Unique
    private int modid$applyFactoryDecayEuT(int base) {
        int scaled = FactoryDecay.applyRecipeEuT(modid$getMetaTileEntity(), base, modid$wearTicks);
        if (scaled <= 0 || !modid$isCrippledMachine()) {
            return scaled;
        }

        long adjusted = (long) scaled + modid$getMaxVoltage();
        return adjusted >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) adjusted;
    }

    @Unique
    private int modid$getMaxVoltage() {

        MetaTileEntity metaTileEntity = modid$getMetaTileEntity();

        if (!(metaTileEntity instanceof ITieredMetaTileEntity)) {
            return Integer.MAX_VALUE;
        }

        int tier = ((ITieredMetaTileEntity) metaTileEntity).getTier();

        if (tier < 0 || tier >= GTValues.V.length) {
            return Integer.MAX_VALUE;
        }

        long voltage = GTValues.V[tier];

        if (voltage >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }

        return (int) voltage;
    }

    @Unique
    private boolean modid$isCrippledMachine() {
        MetaTileEntity metaTileEntity = modid$getMetaTileEntity();

        return metaTileEntity instanceof CrippledMachine && ((CrippledMachine) metaTileEntity).modid$isCrippled();
    }

    @Unique
    private MetaTileEntity modid$getMetaTileEntity() {
        return ((MTETrait) (Object) this).getMetaTileEntity();
    }
}
