package com.example.modid;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.capability.impl.RecipeLogicEnergy;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.util.function.Supplier;

public class SupremeMachine extends SimpleMachineMetaTileEntity {

    private static final int[] PARALLEL_LIMITS = {1, 16, 64};
    private static final String PARALLEL_LIMIT_TAG = "SupremeParallelLimit";

    private final OrientedOverlayRenderer overlayRenderer;

    public SupremeMachine(
            ResourceLocation metaTileEntityId,
            RecipeMap<?> recipeMap,
            OrientedOverlayRenderer renderer,
            boolean hasFrontFacing) {
        super(metaTileEntityId, recipeMap, renderer, GTValues.UHV, hasFrontFacing);
        this.overlayRenderer = renderer;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new SupremeMachine(metaTileEntityId, workable.getRecipeMap(), overlayRenderer, hasFrontFacing());
    }

    @Override
    public int getTier() {
        return GTValues.UHV;
    }

    @Override
    public long getMaxInputOutputAmperage() {
        return 16L; // Can handle massive parallel operations
    }

    @Override
    protected AbstractRecipeLogic createWorkable(RecipeMap<?> recipeMap) {
        return new SupremeRecipeLogic(this, recipeMap, () -> energyContainer);
    }

    @Override
    protected ModularUI.Builder createGuiTemplate(EntityPlayer player) {
        ModularUI.Builder builder = super.createGuiTemplate(player);

        int x = 7;
        if (exportItems.getSlots() > 0) {
            x += 18;
        }
        if (exportFluids.getTanks() > 0) {
            x += 18;
        }
        x += 18;

        int yOffset =
                recipeMap.getMaxInputs() >= 6
                                || recipeMap.getMaxFluidInputs() >= 6
                                || recipeMap.getMaxOutputs() >= 6
                                || recipeMap.getMaxFluidOutputs() >= 6
                        ? 9
                        : 0;

        builder.widget(
                new CycleButtonWidget(
                                x,
                                62 + yOffset,
                                18,
                                18,
                                new String[] {"1x", "16x", "64x"},
                                () -> getParallelOption(workable.getParallelLimit()),
                                this::setParallelOption)
                        .setButtonTexture(GuiTextures.TOGGLE_BUTTON_BACK.getSubArea(0.0, 0.0, 1.0, 0.5))
                        .setTextColor(0xFFFFFF)
                        .setTooltipHoverString(
                                TextFormatting.WHITE
                                        + "Maximum recipe parallelism:"
                                        + TextFormatting.GRAY
                                        + " 1x, 16x, or 64x./n"
                                        + TextFormatting.GRAY
                                        + "Higher settings consume more inputs and EU/t."));
        return builder;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger(PARALLEL_LIMIT_TAG, workable.getParallelLimit());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        int parallelLimit = data.getInteger(PARALLEL_LIMIT_TAG);
        if (parallelLimit != 16 && parallelLimit != 64) {
            parallelLimit = 1;
        }
        workable.setParallelLimit(parallelLimit);
    }

    private static int getParallelOption(int parallelLimit) {
        if (parallelLimit >= 64) {
            return 2;
        }
        if (parallelLimit >= 16) {
            return 1;
        }
        return 0;
    }

    private void setParallelOption(int option) {
        int boundedOption = Math.max(0, Math.min(PARALLEL_LIMITS.length - 1, option));
        workable.setParallelLimit(PARALLEL_LIMITS[boundedOption]);
        markDirty();
    }

    private static final class SupremeRecipeLogic extends RecipeLogicEnergy {
        private SupremeRecipeLogic(
                MetaTileEntity metaTileEntity, RecipeMap<?> recipeMap, Supplier<IEnergyContainer> energyContainer) {
            super(metaTileEntity, recipeMap, energyContainer);
            this.hasPerfectOC = true;
        }
    }
}
