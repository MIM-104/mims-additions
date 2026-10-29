package com.example.modid.firmware;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.client.renderer.texture.cube.SimpleCubeRenderer;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.*;

public final class FirmwareSynthesisArray extends RecipeMapMultiblockController {
    public static final FirmwareRecipeMap RECIPES = new FirmwareRecipeMap();
    public static final ICubeRenderer CASING = new SimpleCubeRenderer("modid:blocks/etched_logic_casing");
    // Each aisle is bottom to top. Only interior cells may be air; the envelope is closed.
    public static final String[][] AISLES = {
        {"CCCCCCC", "CCCCCCC", "CGGGGGC", "CCCCCCC", "CCCCCCC"},
        {"CCCCCCC", "CR###RC", "CR###RC", "CGGGGGC", "CCCCCCC"},
        {"CCCCCCC", "C#KKK#C", "G#K#K#G", "C#KKK#C", "CCCCCCC"},
        {"CCCCCCC", "CR###RC", "GR###RG", "C#####C", "CCCCCCC"},
        {"CCCCCCC", "C#KKK#C", "G#K#K#G", "C#KKK#C", "CCCCCCC"},
        {"CCCCCCC", "CR###RC", "GR###RG", "C#####C", "CCCCCCC"},
        {"CCCCCCC", "C#KKK#C", "G#K#K#G", "C#KKK#C", "CCCCCCC"},
        {"CCCCCCC", "CR###RC", "CR###RC", "CGGGGGC", "CCCCCCC"},
        {"CCCCCCC", "CGGSGGC", "CGGGGGC", "CGGGGGC", "CCCCCCC"}
    };

    public FirmwareSynthesisArray(ResourceLocation id) {
        super(id, RECIPES);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        FactoryBlockPattern pattern = FactoryBlockPattern.start();
        for (String[] aisle : AISLES) pattern.aisle(aisle);
        return pattern.where('S', selfPredicate())
                .where(
                        'C',
                        states(FirmwareContent.LOGIC_CASING.getDefaultState())
                                .setMinGlobalLimited(160)
                                .or(autoAbilities(true, true, true, true, false, false, false)))
                .where('R', states(FirmwareContent.MEMORY_BANK.getDefaultState()))
                .where('G', states(glass()))
                .where('K', states(FirmwareContent.NOETIC_COIL.getDefaultState()))
                .where('#', air())
                .build();
    }

    private static IBlockState glass() {
        return MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.TEMPERED_GLASS);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tile) {
        return new FirmwareSynthesisArray(metaTileEntityId);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart part) {
        return CASING;
    }

    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return Textures.ASSEMBLER_OVERLAY;
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return true;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        MultiblockShapeInfo.Builder shape = MultiblockShapeInfo.builder();
        for (int i = 0; i < AISLES.length; i++) {
            String[] aisle = AISLES[i].clone();
            if (i == 0) {
                aisle[0] = "CICECOC";
                aisle[1] = "CCCMCCC";
            }
            shape.aisle(aisle);
        }
        return Collections.singletonList(
                shape.where('C', FirmwareContent.LOGIC_CASING.getDefaultState())
                        .where('R', FirmwareContent.MEMORY_BANK.getDefaultState())
                        .where('G', glass())
                        .where('K', FirmwareContent.NOETIC_COIL.getDefaultState())
                        .where('#', Blocks.AIR.getDefaultState())
                        .where('S', FirmwareMachines.SYNTHESIS_ARRAY, EnumFacing.SOUTH)
                        .where('I', MetaTileEntities.ITEM_IMPORT_BUS[GTValues.IV], EnumFacing.NORTH)
                        .where('O', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.IV], EnumFacing.NORTH)
                        .where('E', MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.IV], EnumFacing.NORTH)
                        .where('M', MetaTileEntities.MAINTENANCE_HATCH, EnumFacing.NORTH)
                        .build());
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> lines, boolean advanced) {
        super.addInformation(stack, world, lines, advanced);
        lines.add(TextFormatting.GRAY + "Instructions forged in a storm of thought");
        lines.add("Circuit + fine wire + Programmed Circuit (not consumed)");
    }
}
