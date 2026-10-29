package com.example.modid;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.common.metatileentities.MetaTileEntities;

import net.minecraft.util.ResourceLocation;

public class MachineRegistry {

    // Supreme Machines - Starting at ID 32000
    public static SimpleMachineMetaTileEntity SUPREME_ELECTRIC_FURNACE;
    public static SimpleMachineMetaTileEntity SUPREME_MACERATOR;
    public static SimpleMachineMetaTileEntity SUPREME_ALLOY_SMELTER;
    public static SimpleMachineMetaTileEntity SUPREME_ARC_FURNACE;
    public static SimpleMachineMetaTileEntity SUPREME_ASSEMBLER;
    public static SimpleMachineMetaTileEntity SUPREME_CIRCUIT_ASSEMBLER;
    public static SimpleMachineMetaTileEntity SUPREME_AUTOCLAVE;
    public static SimpleMachineMetaTileEntity SUPREME_BENDER;
    public static SimpleMachineMetaTileEntity SUPREME_BREWERY;
    public static SimpleMachineMetaTileEntity SUPREME_CANNER;
    public static SimpleMachineMetaTileEntity SUPREME_CENTRIFUGE;
    public static SimpleMachineMetaTileEntity SUPREME_CHEMICAL_BATH;
    public static SimpleMachineMetaTileEntity SUPREME_CHEMICAL_REACTOR;
    public static SimpleMachineMetaTileEntity SUPREME_COMPRESSOR;
    public static SimpleMachineMetaTileEntity SUPREME_CUTTER;
    public static SimpleMachineMetaTileEntity SUPREME_DISTILLERY;
    public static SimpleMachineMetaTileEntity SUPREME_ELECTROLYZER;
    public static SimpleMachineMetaTileEntity SUPREME_ELECTROMAGNETIC_SEPARATOR;
    public static SimpleMachineMetaTileEntity SUPREME_EXTRACTOR;
    public static SimpleMachineMetaTileEntity SUPREME_FERMENTER;
    public static SimpleMachineMetaTileEntity SUPREME_FLUID_EXTRACTOR;
    public static SimpleMachineMetaTileEntity SUPREME_FLUID_SOLIDIFIER;
    public static SimpleMachineMetaTileEntity SUPREME_FORGE_HAMMER;
    public static SimpleMachineMetaTileEntity SUPREME_FORMING_PRESS;
    public static SimpleMachineMetaTileEntity SUPREME_LATHE;
    public static SimpleMachineMetaTileEntity SUPREME_MIXER;
    public static SimpleMachineMetaTileEntity SUPREME_ORE_WASHER;
    public static SimpleMachineMetaTileEntity SUPREME_PACKER;
    public static SimpleMachineMetaTileEntity SUPREME_POLARIZER;
    public static SimpleMachineMetaTileEntity SUPREME_LASER_ENGRAVER;
    public static SimpleMachineMetaTileEntity SUPREME_SIFTER;
    public static SimpleMachineMetaTileEntity SUPREME_THERMAL_CENTRIFUGE;
    public static SimpleMachineMetaTileEntity SUPREME_WIREMILL;
    public static SimpleMachineMetaTileEntity SUPREME_ITEM_COLLECTOR;
    public static SimpleMachineMetaTileEntity SUPREME_FISHER;
    public static SimpleMachineMetaTileEntity SUPREME_PUMP;
    public static SimpleMachineMetaTileEntity SUPREME_AIR_COLLECTOR;
    public static SimpleMachineMetaTileEntity SUPREME_BLOCK_BREAKER;

    // Microverse
    public static MetaTileEntity MICROVERSE_PROJECTOR_4;
    private static boolean microverseRegistered = false;

    public static void init() {
        MimsAdditions.LOGGER.info("Registering Supreme Machines...");

        // Register all supreme machines
        SUPREME_ELECTRIC_FURNACE =
                registerSupremeMachine(
                        32000, "supreme_electric_furnace", RecipeMaps.FURNACE_RECIPES, Textures.FURNACE_OVERLAY);

        SUPREME_MACERATOR =
                registerSupremeMachine(
                        32001, "supreme_macerator", RecipeMaps.MACERATOR_RECIPES, Textures.MACERATOR_OVERLAY);

        SUPREME_ALLOY_SMELTER =
                registerSupremeMachine(
                        32002,
                        "supreme_alloy_smelter",
                        RecipeMaps.ALLOY_SMELTER_RECIPES,
                        Textures.ALLOY_SMELTER_OVERLAY);

        SUPREME_ARC_FURNACE =
                registerSupremeMachine(
                        32003, "supreme_arc_furnace", RecipeMaps.ARC_FURNACE_RECIPES, Textures.ARC_FURNACE_OVERLAY);

        SUPREME_ASSEMBLER =
                registerSupremeMachine(
                        32004, "supreme_assembler", RecipeMaps.ASSEMBLER_RECIPES, Textures.ASSEMBLER_OVERLAY);

        SUPREME_AUTOCLAVE =
                registerSupremeMachine(
                        32005, "supreme_autoclave", RecipeMaps.AUTOCLAVE_RECIPES, Textures.AUTOCLAVE_OVERLAY);

        SUPREME_BENDER =
                registerSupremeMachine(32006, "supreme_bender", RecipeMaps.BENDER_RECIPES, Textures.BENDER_OVERLAY);

        SUPREME_BREWERY =
                registerSupremeMachine(32007, "supreme_brewery", RecipeMaps.BREWING_RECIPES, Textures.BREWERY_OVERLAY);

        SUPREME_CANNER =
                registerSupremeMachine(32008, "supreme_canner", RecipeMaps.CANNER_RECIPES, Textures.CANNER_OVERLAY);

        SUPREME_CENTRIFUGE =
                registerSupremeMachine(
                        32009, "supreme_centrifuge", RecipeMaps.CENTRIFUGE_RECIPES, Textures.CENTRIFUGE_OVERLAY);

        SUPREME_CHEMICAL_BATH =
                registerSupremeMachine(
                        32010,
                        "supreme_chemical_bath",
                        RecipeMaps.CHEMICAL_BATH_RECIPES,
                        Textures.CHEMICAL_BATH_OVERLAY);

        SUPREME_CHEMICAL_REACTOR =
                registerSupremeMachine(
                        32011,
                        "supreme_chemical_reactor",
                        RecipeMaps.CHEMICAL_RECIPES,
                        Textures.CHEMICAL_REACTOR_OVERLAY);

        SUPREME_COMPRESSOR =
                registerSupremeMachine(
                        32012, "supreme_compressor", RecipeMaps.COMPRESSOR_RECIPES, Textures.COMPRESSOR_OVERLAY);

        SUPREME_CUTTER =
                registerSupremeMachine(32013, "supreme_cutter", RecipeMaps.CUTTER_RECIPES, Textures.CUTTER_OVERLAY);

        SUPREME_DISTILLERY =
                registerSupremeMachine(
                        32014, "supreme_distillery", RecipeMaps.DISTILLERY_RECIPES, Textures.DISTILLERY_OVERLAY);

        SUPREME_ELECTROLYZER =
                registerSupremeMachine(
                        32015, "supreme_electrolyzer", RecipeMaps.ELECTROLYZER_RECIPES, Textures.ELECTROLYZER_OVERLAY);

        SUPREME_ELECTROMAGNETIC_SEPARATOR =
                registerSupremeMachine(
                        32016,
                        "supreme_electromagnetic_separator",
                        RecipeMaps.ELECTROMAGNETIC_SEPARATOR_RECIPES,
                        Textures.ELECTROMAGNETIC_SEPARATOR_OVERLAY);

        SUPREME_EXTRACTOR =
                registerSupremeMachine(
                        32017, "supreme_extractor", RecipeMaps.EXTRACTOR_RECIPES, Textures.EXTRACTOR_OVERLAY);

        SUPREME_FERMENTER =
                registerSupremeMachine(
                        32018, "supreme_fermenter", RecipeMaps.FERMENTING_RECIPES, Textures.FERMENTER_OVERLAY);

        SUPREME_FLUID_EXTRACTOR =
                registerSupremeMachine(
                        32019, "supreme_fluid_extractor", RecipeMaps.EXTRACTOR_RECIPES, Textures.EXTRACTOR_OVERLAY);

        SUPREME_FLUID_SOLIDIFIER =
                registerSupremeMachine(
                        32020,
                        "supreme_fluid_solidifier",
                        RecipeMaps.FLUID_SOLIDFICATION_RECIPES,
                        Textures.FLUID_SOLIDIFIER_OVERLAY);

        SUPREME_FORGE_HAMMER =
                registerSupremeMachine(
                        32021, "supreme_forge_hammer", RecipeMaps.FORGE_HAMMER_RECIPES, Textures.FORGE_HAMMER_OVERLAY);

        SUPREME_FORMING_PRESS =
                registerSupremeMachine(
                        32022,
                        "supreme_forming_press",
                        RecipeMaps.FORMING_PRESS_RECIPES,
                        Textures.FORMING_PRESS_OVERLAY);

        SUPREME_LATHE =
                registerSupremeMachine(32023, "supreme_lathe", RecipeMaps.LATHE_RECIPES, Textures.LATHE_OVERLAY);

        SUPREME_MIXER =
                registerSupremeMachine(32024, "supreme_mixer", RecipeMaps.MIXER_RECIPES, Textures.MIXER_OVERLAY);

        SUPREME_ORE_WASHER =
                registerSupremeMachine(
                        32025, "supreme_ore_washer", RecipeMaps.ORE_WASHER_RECIPES, Textures.ORE_WASHER_OVERLAY);

        SUPREME_PACKER =
                registerSupremeMachine(32026, "supreme_packer", RecipeMaps.PACKER_RECIPES, Textures.PACKER_OVERLAY);

        SUPREME_POLARIZER =
                registerSupremeMachine(
                        32027, "supreme_polarizer", RecipeMaps.POLARIZER_RECIPES, Textures.POLARIZER_OVERLAY);

        SUPREME_LASER_ENGRAVER =
                registerSupremeMachine(
                        32028,
                        "supreme_laser_engraver",
                        RecipeMaps.LASER_ENGRAVER_RECIPES,
                        Textures.LASER_ENGRAVER_OVERLAY);

        SUPREME_SIFTER =
                registerSupremeMachine(32029, "supreme_sifter", RecipeMaps.SIFTER_RECIPES, Textures.SIFTER_OVERLAY);

        SUPREME_THERMAL_CENTRIFUGE =
                registerSupremeMachine(
                        32030,
                        "supreme_thermal_centrifuge",
                        RecipeMaps.THERMAL_CENTRIFUGE_RECIPES,
                        Textures.THERMAL_CENTRIFUGE_OVERLAY);

        SUPREME_WIREMILL =
                registerSupremeMachine(
                        32031, "supreme_wiremill", RecipeMaps.WIREMILL_RECIPES, Textures.WIREMILL_OVERLAY);

        SUPREME_ITEM_COLLECTOR =
                registerSupremeMachine(
                        32032,
                        "supreme_item_collector",
                        RecipeMaps.ROCK_BREAKER_RECIPES,
                        Textures.ROCK_BREAKER_OVERLAY,
                        false);

        SUPREME_FISHER =
                registerSupremeMachine(
                        32033, "supreme_fisher", RecipeMaps.ROCK_BREAKER_RECIPES, Textures.ROCK_BREAKER_OVERLAY, false);

        SUPREME_PUMP =
                registerSupremeMachine(
                        32034, "supreme_pump", RecipeMaps.ROCK_BREAKER_RECIPES, Textures.ROCK_BREAKER_OVERLAY, false);

        SUPREME_AIR_COLLECTOR =
                registerSupremeMachine(
                        32035,
                        "supreme_air_collector",
                        RecipeMaps.ROCK_BREAKER_RECIPES,
                        Textures.ROCK_BREAKER_OVERLAY,
                        false);

        SUPREME_BLOCK_BREAKER =
                registerSupremeMachine(
                        32036,
                        "supreme_block_breaker",
                        RecipeMaps.ROCK_BREAKER_RECIPES,
                        Textures.ROCK_BREAKER_OVERLAY,
                        false);

        SUPREME_CIRCUIT_ASSEMBLER =
                registerSupremeMachine(
                        32038,
                        "supreme_circuit_assembler",
                        RecipeMaps.CIRCUIT_ASSEMBLER_RECIPES,
                        Textures.ASSEMBLER_OVERLAY);

        MimsAdditions.LOGGER.info("Registered 38 Supreme Machines!");
    }

    /**
     * Register Microverse machines AFTER other mods (like NomiLabs) have initialized their recipe maps. Call this from
     * postInit (FMLPostInitializationEvent).
     */
    public static void registerMicroverse() {
        if (microverseRegistered) {
            MimsAdditions.LOGGER.warn("Microverse machines already registered, skipping duplicate registration");
            return;
        }

        MICROVERSE_PROJECTOR_4 =
                MetaTileEntities.registerMetaTileEntity(
                        32037, new MicroverseProjector4(new ResourceLocation(Tags.MOD_ID, "microverse_projector_4")));
        MimsAdditions.LOGGER.info("Registered microverse_projector_4 at ID {}", 32037);

        microverseRegistered = true;
    }

    /** Helper method to register a supreme machine */
    private static SimpleMachineMetaTileEntity registerSupremeMachine(
            int id, String name, RecipeMap<?> recipeMap, OrientedOverlayRenderer renderer) {
        return registerSupremeMachine(id, name, recipeMap, renderer, true);
    }

    /** Helper method to register a supreme machine with custom hasFrontFacing */
    private static SimpleMachineMetaTileEntity registerSupremeMachine(
            int id, String name, RecipeMap<?> recipeMap, OrientedOverlayRenderer renderer, boolean hasFrontFacing) {

        ResourceLocation resourceLocation = new ResourceLocation(Tags.MOD_ID, name);

        SimpleMachineMetaTileEntity machine =
                (SimpleMachineMetaTileEntity)
                        MetaTileEntities.registerMetaTileEntity(
                                id, new SupremeMachine(resourceLocation, recipeMap, renderer, hasFrontFacing));

        MimsAdditions.LOGGER.info("Registered {} at ID {}", name, id);
        return machine;
    }
}
