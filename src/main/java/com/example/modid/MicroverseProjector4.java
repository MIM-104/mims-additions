package com.example.modid;

import com.nomiceu.nomilabs.LabsValues;
import com.nomiceu.nomilabs.block.registry.LabsBlocks;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import team.chisel.common.carving.Carving;

import java.util.ArrayList;
import java.util.List;

public class MicroverseProjector4 extends RecipeMapMultiblockController {

    public MicroverseProjector4(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, ModRecipeMaps.MICROVERSE_4_RECIPES);
    }

    protected TraceabilityPredicate getCasingPredicateMain() {
        return states(LabsBlocks.MICROVERSE_CASING.getDefaultState());
    }

    protected TraceabilityPredicate getCasingPredicateGlass() {
        return states(MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.FUSION_GLASS));
    }

    protected TraceabilityPredicate getCasingPredicateDiamond() {
        if (Loader.isModLoaded(LabsValues.CHISEL_MODID)) {
            team.chisel.api.carving.ICarvingGroup group =
                    Carving.chisel.getGroup(Blocks.DIAMOND_BLOCK.getDefaultState());
            if (group != null) {
                return states(group.getVariations().get(4).getBlockState()); // Space Diamond Block
            }
        }
        return air();
    }

    protected TraceabilityPredicate getCasingPredicateEngine() {
        return states(
                MetaBlocks.MULTIBLOCK_CASING.getState(
                        BlockMultiblockCasing.MultiblockCasingType.EXTREME_ENGINE_INTAKE_CASING));
    }

    protected IBlockState getCasingStateMain() {
        return LabsBlocks.MICROVERSE_CASING.getDefaultState();
    }

    protected IBlockState getCasingStateGlass() {
        return MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.FUSION_GLASS);
    }

    protected IBlockState getCasingStateDiamond() {
        if (Loader.isModLoaded(LabsValues.CHISEL_MODID)) {
            team.chisel.api.carving.ICarvingGroup group =
                    Carving.chisel.getGroup(Blocks.DIAMOND_BLOCK.getDefaultState());
            if (group != null) {
                return group.getVariations().get(4).getBlockState();
            }
        }
        return Blocks.AIR.getDefaultState();
    }

    protected IBlockState getCasingStateEngine() {
        return MetaBlocks.MULTIBLOCK_CASING.getState(
                BlockMultiblockCasing.MultiblockCasingType.EXTREME_ENGINE_INTAKE_CASING);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ICubeRenderer getBaseTexture(IMultiblockPart part) {
        // The superclass texture lookup is incompatible with this GT version.
        return ModTextures.SPACE_DIAMOND != null ? ModTextures.SPACE_DIAMOND : Textures.VOLTAGE_CASINGS[GTValues.IV];
    }

    @Override
    @SideOnly(Side.CLIENT)
    @NotNull
    protected ICubeRenderer getFrontOverlay() {
        return Textures.COMPRESSOR_OVERLAY;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return true;
    }

    @Override
    public boolean canBeDistinct() {
        return true;
    }

    @Override
    public SoundEvent getBreakdownSound() {
        return GTSoundEvents.BREAKDOWN_ELECTRICAL;
    }

    @Override
    @NotNull
    protected BlockPattern createStructurePattern() {
        // Microverse 3 shape, but swapped meanings:
        // X = Space Diamond, D = Microverse casing
        return FactoryBlockPattern.start()
                .aisle(
                        "#########",
                        "#########",
                        "##XXXXX##",
                        "##XVXVX##",
                        "##XXXXX##",
                        "##XVXVX##",
                        "##XXXXX##",
                        "#########",
                        "#########")
                .aisle(
                        "#########",
                        "##XGGGX##",
                        "#XDDDDDX#",
                        "#GDDDDDG#",
                        "#GDDDDDG#",
                        "#GDDDDDG#",
                        "#XDDDDDX#",
                        "##XGGGX##",
                        "#########")
                .aisle(
                        "##XXXXX##",
                        "#XDDDDDX#",
                        "XDDDDDDDX",
                        "XDDDDDDDX",
                        "XDDDDDDDX",
                        "XDDDDDDDX",
                        "XDDDDDDDX",
                        "#XDDDDDX#",
                        "##XXXXX##")
                .aisle(
                        "##XGGGX##",
                        "#GDDDDDG#",
                        "XDDDDDDDX",
                        "GDD---DDG",
                        "GDD---DDG",
                        "GDD---DDG",
                        "XDDDDDDDX",
                        "#GDDDDDG#",
                        "##XGGGX##")
                .setRepeatable(3)
                .aisle(
                        "##XXXXX##",
                        "#XDDDDDX#",
                        "XDDDDDDDX",
                        "XDDDDDDDX",
                        "XDDDDDDDX",
                        "XDDDDDDDX",
                        "XDDDDDDDX",
                        "#XDDDDDX#",
                        "##XXXXX##")
                .aisle(
                        "#########",
                        "##XGGGX##",
                        "#XDDDDDX#",
                        "#GDDDDDG#",
                        "#GDDDDDG#",
                        "#GDDDDDG#",
                        "#XDDDDDX#",
                        "##XGGGX##",
                        "#########")
                .aisle(
                        "#########",
                        "#########",
                        "##XXSXX##",
                        "##XGGGX##",
                        "##XGGGX##",
                        "##XGGGX##",
                        "##XXXXX##",
                        "#########",
                        "#########")
                .where('S', selfPredicate())
                .where('G', getCasingPredicateGlass())
                .where('V', getCasingPredicateEngine())
                .where('X', getCasingPredicateDiamond().setMinGlobalLimited(115).or(autoAbilities()))
                .where('D', getCasingPredicateMain())
                .where('-', air())
                .where('#', any())
                .build();
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<MultiblockShapeInfo>();

        shapeInfo.add(
                MultiblockShapeInfo.builder()
                        .aisle(
                                "#########",
                                "#########",
                                "##XEEMX##",
                                "##XVXVX##",
                                "##XXHXX##",
                                "##XVXVX##",
                                "##XXXXX##",
                                "#########",
                                "#########")
                        .aisle(
                                "#########",
                                "##XGGGX##",
                                "#XDDDDDX#",
                                "#GDDDDDG#",
                                "#GDDDDDG#",
                                "#GDDDDDG#",
                                "#XDDDDDX#",
                                "##XGGGX##",
                                "#########")
                        .aisle(
                                "##XXXXX##",
                                "#XDDDDDX#",
                                "XDDDDDDDX",
                                "XDDDDDDDX",
                                "XDDDDDDDX",
                                "XDDDDDDDX",
                                "XDDDDDDDX",
                                "#XDDDDDX#",
                                "##XXXXX##")
                        .aisle(
                                "##XGGGX##",
                                "#GDDDDDG#",
                                "XDDDDDDDX",
                                "GDD###DDG",
                                "GDD###DDG",
                                "GDD###DDG",
                                "XDDDDDDDX",
                                "#GDDDDDG#",
                                "##XGGGX##")
                        .aisle(
                                "##XGGGX##",
                                "#GDDDDDG#",
                                "XDDDDDDDX",
                                "GDD###DDG",
                                "GDD###DDG",
                                "GDD###DDG",
                                "XDDDDDDDX",
                                "#GDDDDDG#",
                                "##XGGGX##")
                        .aisle(
                                "##XGGGX##",
                                "#GDDDDDG#",
                                "XDDDDDDDX",
                                "GDD###DDG",
                                "GDD###DDG",
                                "GDD###DDG",
                                "XDDDDDDDX",
                                "#GDDDDDG#",
                                "##XGGGX##")
                        .aisle(
                                "##XXXXX##",
                                "#XDDDDDX#",
                                "XDDDDDDDX",
                                "XDDDDDDDX",
                                "XDDDDDDDX",
                                "XDDDDDDDX",
                                "XDDDDDDDX",
                                "#XDDDDDX#",
                                "##XXXXX##")
                        .aisle(
                                "#########",
                                "##XGGGX##",
                                "#XDDDDDX#",
                                "#GDDDDDG#",
                                "#GDDDDDG#",
                                "#GDDDDDG#",
                                "#XDDDDDX#",
                                "##XGGGX##",
                                "#########")
                        .aisle(
                                "#########",
                                "#########",
                                "##XISOX##",
                                "##XGGGX##",
                                "##XGGGX##",
                                "##XGGGX##",
                                "##XXXXX##",
                                "#########",
                                "#########")
                        .where('X', getCasingStateDiamond())
                        .where('D', getCasingStateMain())
                        .where('G', getCasingStateGlass())
                        .where('V', getCasingStateEngine())
                        .where('S', MachineRegistry.MICROVERSE_PROJECTOR_4, EnumFacing.SOUTH)
                        .where('H', MetaTileEntities.MUFFLER_HATCH[GTValues.LV], EnumFacing.NORTH)
                        .where('I', MetaTileEntities.ITEM_IMPORT_BUS[GTValues.LV], EnumFacing.SOUTH)
                        .where('O', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.LV], EnumFacing.SOUTH)
                        .where('E', MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.ZPM], EnumFacing.NORTH)
                        .where(
                                'M',
                                () ->
                                        ConfigHolder.machines.enableMaintenance
                                                ? MetaTileEntities.MAINTENANCE_HATCH
                                                : getCasingStateMain(),
                                EnumFacing.NORTH)
                        .where('#', Blocks.AIR.getDefaultState())
                        .build());

        return shapeInfo;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MicroverseProjector4(metaTileEntityId);
    }
}
