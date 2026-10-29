package com.example.modid;

import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.items.MetaItems.*;

import com.blakebr0.extendedcrafting.crafting.table.TableRecipeManager;
import com.example.modid.common.ModFluids;
import com.example.modid.common.ModItems;
import com.example.modid.common.ModMaterialTweaks;
import com.example.modid.firmware.FirmwareData;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.loaders.recipe.CraftingComponent;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DefaultRecipes {

    private static final int MOLTEN_INFINITY_PER_UHV_COMPONENT = 72;
    private static final int ROBOT_ARM_MOLTEN_INFINITY = 144;
    private static boolean registered = false;

    public static void registerDefaultRecipes() {
        if (registered) {
            MimsAdditions.LOGGER.warn("Recipes already registered, skipping duplicate registration");
            return;
        }

        registerCreativeChestTrapRecipe();

        if (Loader.isModLoaded("nomilabs") && Loader.isModLoaded("gregtech")) {
            registerMicroverseProjector2CoilRecipes();
            registerMicroverseProjector4MicrominerRecipes();
            registerMicroverseProjector4ControllerRecipe();
        } else {
            MimsAdditions.LOGGER.warn(
                    "Skipping NomiLabs-dependent Microverse recipes because NomiLabs or GregTech is not loaded");
        }

        if (Loader.isModLoaded("nomilabs")
                && Loader.isModLoaded("gregtech")
                && Loader.isModLoaded("draconicevolution")
                && Loader.isModLoaded("extendedcrafting")) {
            registerMicrominerMicrominerRecipe();
        } else {
            MimsAdditions.LOGGER.warn(
                    "Skipping Microminer Microminer recipe because NomiLabs, GregTech, Draconic Evolution, and Extended Crafting are not all loaded");
        }

        if (Loader.isModLoaded("nomilabs")
                && Loader.isModLoaded("simplyjetpacks")
                && Loader.isModLoaded("gregtech")
                && Loader.isModLoaded("extendedcrafting")) {
            registerTitaniumCoilMicroMinerRecipe();
        } else {
            MimsAdditions.LOGGER.warn(
                    "Skipping Titanium Coil Micro Miner recipe because NomiLabs, Simply Jetpacks, GregTech, and Extended Crafting are not all loaded");
        }

        registerEpsteiniumSolidifierRecipe();
        registerEpsteiniumFusionRecipe();
        registerBabyVillagerExtractorRecipe();
        registerInfinityIngotExtractorRecipe();
        registerOmniumPlateBenderRecipe();
        registerEpsteiniumWetwareMainframeRecipe();
        registerEpsteiniumMainframeAsGregTechCircuit();
        registerSupremeMachineCraftingRecipes();
        replaceUhvCircuitAssemblerRecipe();
        registerRtaNeutronateFineWireRecipe();
        registerUhvComponentAssemblyLineRecipes();

        registered = true;
        MimsAdditions.LOGGER.info("Registered addon recipes");
    }

    private static void registerEpsteiniumMainframeAsGregTechCircuit() {
        ItemStack mainframe = new ItemStack(ModItems.epsteiniumInfusedWetwareMainframe);
        if (mainframe.isEmpty()) {
            throw new IllegalStateException("The Epsteinium-Infused Wetware Mainframe item is not registered");
        }
        OreDictUnifier.registerOre(mainframe, circuit, MarkerMaterials.Tier.UEV);
        FirmwareData.invalidateTierCache();
    }

    private static void registerSupremeMachineCraftingRecipes() {
        registerUhvMachineRecipe(MachineRegistry.SUPREME_ELECTRIC_FURNACE, MetaTileEntities.ELECTRIC_FURNACE);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_MACERATOR, MetaTileEntities.MACERATOR);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_ALLOY_SMELTER, MetaTileEntities.ALLOY_SMELTER);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_ARC_FURNACE, MetaTileEntities.ARC_FURNACE);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_ASSEMBLER, MetaTileEntities.ASSEMBLER);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_AUTOCLAVE, MetaTileEntities.AUTOCLAVE);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_BENDER, MetaTileEntities.BENDER);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_BREWERY, MetaTileEntities.BREWERY);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_CANNER, MetaTileEntities.CANNER);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_CENTRIFUGE, MetaTileEntities.CENTRIFUGE);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_CHEMICAL_BATH, MetaTileEntities.CHEMICAL_BATH);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_CHEMICAL_REACTOR, MetaTileEntities.CHEMICAL_REACTOR);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_COMPRESSOR, MetaTileEntities.COMPRESSOR);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_CUTTER, MetaTileEntities.CUTTER);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_DISTILLERY, MetaTileEntities.DISTILLERY);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_ELECTROLYZER, MetaTileEntities.ELECTROLYZER);
        registerUhvMachineRecipe(
                MachineRegistry.SUPREME_ELECTROMAGNETIC_SEPARATOR, MetaTileEntities.ELECTROMAGNETIC_SEPARATOR);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_EXTRACTOR, MetaTileEntities.EXTRACTOR);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_FERMENTER, MetaTileEntities.FERMENTER);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_FLUID_EXTRACTOR, MetaTileEntities.EXTRACTOR);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_FLUID_SOLIDIFIER, MetaTileEntities.FLUID_SOLIDIFIER);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_FORGE_HAMMER, MetaTileEntities.FORGE_HAMMER);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_FORMING_PRESS, MetaTileEntities.FORMING_PRESS);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_LATHE, MetaTileEntities.LATHE);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_MIXER, MetaTileEntities.MIXER);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_ORE_WASHER, MetaTileEntities.ORE_WASHER);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_PACKER, MetaTileEntities.PACKER);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_POLARIZER, MetaTileEntities.POLARIZER);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_LASER_ENGRAVER, MetaTileEntities.LASER_ENGRAVER);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_SIFTER, MetaTileEntities.SIFTER);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_THERMAL_CENTRIFUGE, MetaTileEntities.THERMAL_CENTRIFUGE);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_WIREMILL, MetaTileEntities.WIREMILL);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_ITEM_COLLECTOR, MetaTileEntities.ROCK_BREAKER);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_FISHER, MetaTileEntities.FISHER);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_PUMP, MetaTileEntities.PUMP);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_AIR_COLLECTOR, MetaTileEntities.GAS_COLLECTOR);
        registerUhvMachineRecipe(MachineRegistry.SUPREME_BLOCK_BREAKER, MetaTileEntities.BLOCK_BREAKER);
    }

    private static void registerUhvMachineRecipe(MetaTileEntity supremeMachine, MetaTileEntity[] standardMachines) {
        MetaTileEntity standardMachine = getHighestTierMachine(standardMachines);
        if (supremeMachine == null || standardMachine == null) {
            throw new IllegalStateException(
                    "Cannot register a Supreme machine recipe because its GregTech template "
                            + "machine is unavailable");
        }
        ItemStack standardOutput = standardMachine.getStackForm();
        IRecipe sourceRecipe = findCraftingRecipeOrNull(standardOutput);
        if (sourceRecipe == null) {
            MimsAdditions.LOGGER.warn(
                    "Skipping Supreme machine recipe {} because GregTech has no crafting recipe "
                            + "for its source machine {}",
                    supremeMachine.getMetaName(),
                    standardOutput);
            return;
        }
        if (!(sourceRecipe instanceof IShapedRecipe)) {
            throw new IllegalStateException("GregTech UHV machine has no cloneable shaped recipe: " + standardOutput);
        }

        IShapedRecipe shapedRecipe = (IShapedRecipe) sourceRecipe;
        ResourceLocation recipeId = new ResourceLocation(Tags.MOD_ID, supremeMachine.getMetaName() + "_crafting");
        NonNullList<Ingredient> ingredients = copyRecipeIngredients(sourceRecipe);
        int sourceTier = getMachineTier(standardMachine);
        int upgradedComponents = upgradeTieredComponents(ingredients, sourceTier, GTValues.UHV);
        if (upgradedComponents < 0) {
            MimsAdditions.LOGGER.error(
                    "Skipping Supreme machine recipe {} because a UHV-tier ingredient is unavailable",
                    supremeMachine.getMetaName());
            return;
        }
        if (sourceTier < GTValues.UHV && upgradedComponents == 0) {
            MimsAdditions.LOGGER.error(
                    "Could not identify any tiered GregTech components in the {}; "
                            + "skipping its Supreme machine recipe",
                    standardMachine.getMetaName());
            return;
        }
        ShapedRecipes supremeRecipe =
                new ShapedRecipes(
                        Tags.MOD_ID,
                        shapedRecipe.getRecipeWidth(),
                        shapedRecipe.getRecipeHeight(),
                        ingredients,
                        supremeMachine.getStackForm());
        supremeRecipe.setRegistryName(recipeId);
        ForgeRegistries.RECIPES.register(supremeRecipe);
    }

    private static void replaceUhvCircuitAssemblerRecipe() {
        if (MachineRegistry.SUPREME_CIRCUIT_ASSEMBLER == null) {
            MimsAdditions.LOGGER.error("Supreme Circuit Assembler is unavailable; skipping its crafting recipe");
            return;
        }
        MetaTileEntity uvCircuitAssembler =
                MetaTileEntities.CIRCUIT_ASSEMBLER.length > GTValues.UV
                        ? MetaTileEntities.CIRCUIT_ASSEMBLER[GTValues.UV]
                        : null;
        if (uvCircuitAssembler == null) {
            MimsAdditions.LOGGER.error(
                    "Cannot register the Supreme Circuit Assembler recipe because the UV "
                            + "Circuit Assembler template is unavailable");
            return;
        }
        IRecipe sourceRecipe = findCraftingRecipeOrNull(uvCircuitAssembler.getStackForm());
        if (sourceRecipe == null) {
            MimsAdditions.LOGGER.error(
                    "Cannot register the Supreme Circuit Assembler recipe because the UV "
                            + "Circuit Assembler crafting recipe is unavailable");
            return;
        }
        if (!(sourceRecipe instanceof IShapedRecipe)) {
            MimsAdditions.LOGGER.error(
                    "Cannot register the Supreme Circuit Assembler recipe because its crafting "
                            + "template is not shaped");
            return;
        }
        ItemStack uevCircuit = new ItemStack(ModItems.epsteiniumInfusedWetwareMainframe);
        IShapedRecipe shapedRecipe = (IShapedRecipe) sourceRecipe;
        NonNullList<Ingredient> ingredients = copyRecipeIngredients(sourceRecipe);

        if (upgradeTieredComponents(ingredients, GTValues.UV, GTValues.UHV) < 0) {
            MimsAdditions.LOGGER.error("Could not upgrade the UV Circuit Assembler ingredients for its Supreme recipe");
            return;
        }

        int circuitIngredients = 0;
        for (int i = 0; i < ingredients.size(); i++) {
            if (isUhvCircuitIngredient(ingredients.get(i))) {
                ingredients.set(i, Ingredient.fromStacks(uevCircuit));
                circuitIngredients++;
            }
        }
        if (circuitIngredients == 0) {
            MimsAdditions.LOGGER.error(
                    "Could not identify the upgraded UHV circuit ingredient in the UV Circuit "
                            + "Assembler template; skipping the Supreme recipe rather than registering it with UHV circuits");
            return;
        }

        ItemStack supremeCircuitAssembler = MachineRegistry.SUPREME_CIRCUIT_ASSEMBLER.getStackForm();
        ResourceLocation recipeId = new ResourceLocation(Tags.MOD_ID, "supreme_circuit_assembler_crafting");
        ShapedRecipes replacement =
                new ShapedRecipes(
                        Tags.MOD_ID,
                        shapedRecipe.getRecipeWidth(),
                        shapedRecipe.getRecipeHeight(),
                        ingredients,
                        supremeCircuitAssembler);
        replacement.setRegistryName(recipeId);

        ForgeRegistries.RECIPES.register(replacement);
        MimsAdditions.LOGGER.debug(
                "Registered Supreme Circuit Assembler crafting recipe, replacing {} UHV circuit "
                        + "ingredient(s) with the Epsteinium-Infused Wetware Mainframe",
                circuitIngredients);
    }

    private static boolean isUhvCircuitIngredient(Ingredient ingredient) {
        ItemStack uhvWetwareMainframe = getUhvComponentStack("CIRCUIT");
        return !uhvWetwareMainframe.isEmpty() && ingredient.apply(uhvWetwareMainframe);
    }

    private static MetaTileEntity getHighestTierMachine(MetaTileEntity[] machines) {
        for (int tier = Math.min(GTValues.UHV, machines.length - 1); tier >= 0; tier--) {
            if (machines[tier] != null) {
                return machines[tier];
            }
        }
        return null;
    }

    private static int getMachineTier(MetaTileEntity machine) {
        if (!(machine instanceof ITieredMetaTileEntity)) {
            throw new IllegalStateException("GregTech machine is not tiered: " + machine.getMetaName());
        }
        return ((ITieredMetaTileEntity) machine).getTier();
    }

    private static int upgradeTieredComponents(NonNullList<Ingredient> ingredients, int sourceTier, int targetTier) {
        if (sourceTier == targetTier) {
            return 0;
        }

        int upgraded = 0;
        for (int slot = 0; slot < ingredients.size(); slot++) {
            Ingredient ingredient = ingredients.get(slot);
            for (Field field : CraftingComponent.class.getFields()) {
                if (field.getType() != CraftingComponent.Component.class) {
                    continue;
                }
                try {
                    CraftingComponent.Component component = (CraftingComponent.Component) field.get(null);
                    if (component == null) {
                        continue;
                    }
                    ItemStack sourceComponent = getComponentStack(component.getIngredient(sourceTier));
                    if (sourceComponent.isEmpty() || !ingredient.apply(sourceComponent)) {
                        continue;
                    }
                    ItemStack targetComponent = getUhvComponentStack(field.getName());
                    if (field.getName().equals("ROTOR") && targetComponent.isEmpty()) {
                        MimsAdditions.LOGGER.error(
                                "GregTech has no Neutronium turbine blade; cannot upgrade this "
                                        + "UHV machine's rotor ingredient");
                        return -1;
                    }
                    if (targetComponent.isEmpty()) {
                        targetComponent = getComponentStack(component.getIngredient(targetTier));
                    }
                    if (targetComponent.isEmpty()) {
                        MimsAdditions.LOGGER.warn(
                                "GregTech has no tier-{} crafting ingredient for component {}; "
                                        + "the affected Supreme machine recipe will be skipped",
                                targetTier,
                                field.getName());
                        return -1;
                    }
                    ingredients.set(slot, Ingredient.fromStacks(targetComponent));
                    upgraded++;
                    break;
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Could not read GregTech crafting component " + field.getName(), e);
                }
            }
        }
        return upgraded;
    }

    private static ItemStack getUhvComponentStack(String componentName) {
        switch (componentName) {
            case "CIRCUIT":
            case "BETTER_CIRCUIT":
                return MetaItems.WETWARE_MAINFRAME_UHV.getStackForm();
            case "PUMP":
                return MetaItems.ELECTRIC_PUMP_UHV.getStackForm();
            case "MOTOR":
                return MetaItems.ELECTRIC_MOTOR_UHV.getStackForm();
            case "SENSOR":
                return MetaItems.SENSOR_UHV.getStackForm();
            case "PISTON":
                return MetaItems.ELECTRIC_PISTON_UHV.getStackForm();
            case "EMITTER":
                return MetaItems.EMITTER_UHV.getStackForm();
            case "CONVEYOR":
                return MetaItems.CONVEYOR_MODULE_UHV.getStackForm();
            case "ROBOT_ARM":
                return MetaItems.ROBOT_ARM_UHV.getStackForm();
            case "FIELD_GENERATOR":
                return MetaItems.FIELD_GENERATOR_UHV.getStackForm();
            case "ROTOR":
                return OreDictUnifier.get(turbineBlade, Neutronium);
            case "PLATE":
                return OreDictUnifier.get(plate, Neutronium);
            case "DOUBLE_PLATE":
                return OreDictUnifier.get(plateDouble, Neutronium);
            case "HULL_PLATE":
                return OreDictUnifier.get(plate, Neutronium);
            case "COIL_HEATING":
                return OreDictUnifier.get(wireGtDouble, Tritanium);
            case "COIL_HEATING_DOUBLE":
                return OreDictUnifier.get(wireGtQuadruple, Tritanium);
            case "WIRE_ELECTRIC":
                return OreDictUnifier.get(wireGtSingle, Tritanium);
            case "WIRE_OCT":
                return OreDictUnifier.get(wireGtOctal, RutheniumTriniumAmericiumNeutronate);
            case "PIPE_LARGE":
                return OreDictUnifier.get(pipeLargeFluid, Neutronium);
            default:
                return ItemStack.EMPTY;
        }
    }

    private static void registerUhvComponentAssemblyLineRecipes() {
        Fluid moltenInfinity = ModFluids.moltenInfinity;
        if (moltenInfinity == null) {
            MimsAdditions.LOGGER.warn(
                    "Molten Infinity is not registered; skipping UHV component "
                            + "assembly-line recipes in this runtime");
            return;
        }

        Material molybdenumDisilicide = ModMaterialTweaks.findMolybdenumDisilicide();
        Material omnium = ModMaterialTweaks.findOmnium();
        ItemStack molybdenumDisilicideRing =
                molybdenumDisilicide == null ? ItemStack.EMPTY : OreDictUnifier.get(ring, molybdenumDisilicide);
        ItemStack omniumSingleWire = omnium == null ? ItemStack.EMPTY : OreDictUnifier.get(wireGtSingle, omnium);
        FluidStack conveyorRubber = StyreneButadieneRubber.getFluid(3456);
        ItemStack rutheniumTriniumAmericiumNeutronateFineWire =
                ModItems.rtaNeutronateFineWire == null
                        ? ItemStack.EMPTY
                        : new ItemStack(ModItems.rtaNeutronateFineWire);

        registerUhvComponentAssemblyLineRecipe(
                "motor",
                MetaItems.ELECTRIC_MOTOR_UHV.getStackForm(),
                MetaItems.ELECTRIC_MOTOR_UV.getStackForm(),
                new ItemStack[] {
                    OreDictUnifier.get(stickLong, Neutronium, 4),
                    withCount(molybdenumDisilicideRing, 16),
                    OreDictUnifier.get(round, Neutronium, 32),
                    withCount(rutheniumTriniumAmericiumNeutronateFineWire, 64),
                    withCount(omniumSingleWire, 2)
                },
                MOLTEN_INFINITY_PER_UHV_COMPONENT,
                4000,
                moltenInfinity,
                null);

        registerUhvComponentAssemblyLineRecipe(
                "pump",
                MetaItems.ELECTRIC_PUMP_UHV.getStackForm(),
                MetaItems.ELECTRIC_PUMP_UV.getStackForm(),
                new ItemStack[] {
                    MetaItems.ELECTRIC_MOTOR_UHV.getStackForm(),
                    OreDictUnifier.get(pipeLargeFluid, Neutronium, 2),
                    OreDictUnifier.get(plate, Neutronium, 4),
                    OreDictUnifier.get(bolt, Neutronium, 8),
                    OreDictUnifier.get(screw, Neutronium, 8),
                    OreDictUnifier.get(turbineBlade, Neutronium, 2),
                    withCount(omniumSingleWire, 2)
                },
                MOLTEN_INFINITY_PER_UHV_COMPONENT,
                4000,
                moltenInfinity,
                null);

        registerUhvComponentAssemblyLineRecipe(
                "piston",
                MetaItems.ELECTRIC_PISTON_UHV.getStackForm(),
                MetaItems.ELECTRIC_PISTON_UV.getStackForm(),
                new ItemStack[] {
                    MetaItems.ELECTRIC_MOTOR_UHV.getStackForm(),
                    OreDictUnifier.get(plate, Neutronium, 6),
                    withCount(molybdenumDisilicideRing, 16),
                    OreDictUnifier.get(round, Neutronium, 32),
                    OreDictUnifier.get(stick, Neutronium, 4),
                    OreDictUnifier.get(gear, Neutronium, 1),
                    OreDictUnifier.get(gearSmall, Darmstadtium, 2),
                    OreDictUnifier.get(screw, Neutronium, 16),
                    withCount(omniumSingleWire, 2)
                },
                MOLTEN_INFINITY_PER_UHV_COMPONENT,
                4000,
                moltenInfinity,
                null);

        registerUhvComponentAssemblyLineRecipe(
                "conveyor",
                MetaItems.CONVEYOR_MODULE_UHV.getStackForm(),
                MetaItems.CONVEYOR_MODULE_UV.getStackForm(),
                new ItemStack[] {
                    MetaItems.ELECTRIC_MOTOR_UHV.getStackForm(),
                    OreDictUnifier.get(plate, Neutronium, 4),
                    withCount(molybdenumDisilicideRing, 16),
                    OreDictUnifier.get(round, Neutronium, 32),
                    OreDictUnifier.get(gearSmall, Darmstadtium, 4),
                    withCount(omniumSingleWire, 2)
                },
                MOLTEN_INFINITY_PER_UHV_COMPONENT,
                4000,
                moltenInfinity,
                conveyorRubber);

        registerUhvComponentAssemblyLineRecipe(
                "robot_arm",
                MetaItems.ROBOT_ARM_UHV.getStackForm(),
                MetaItems.ROBOT_ARM_UV.getStackForm(),
                new ItemStack[] {
                    OreDictUnifier.get(stickLong, Neutronium, 4),
                    OreDictUnifier.get(gear, Neutronium, 1),
                    OreDictUnifier.get(gearSmall, Darmstadtium, 4),
                    MetaItems.ELECTRIC_MOTOR_UHV.getStackForm().copy(),
                    MetaItems.ELECTRIC_MOTOR_UHV.getStackForm().copy(),
                    MetaItems.ELECTRIC_PISTON_UHV.getStackForm(),
                    MetaItems.WETWARE_MAINFRAME_UHV.getStackForm(),
                    MetaItems.WETWARE_MAINFRAME_UHV.getStackForm(),
                    OreDictUnifier.get(screw, Neutronium, 16),
                    withCount(omniumSingleWire, 8),
                    OreDictUnifier.get(circuit, MarkerMaterials.Tier.UHV, 2),
                    OreDictUnifier.get(circuit, MarkerMaterials.Tier.ZPM, 4),
                    OreDictUnifier.get(circuit, MarkerMaterials.Tier.LuV, 8)
                },
                ROBOT_ARM_MOLTEN_INFINITY,
                8000,
                moltenInfinity,
                null);

        registerUhvComponentAssemblyLineRecipe(
                "sensor",
                MetaItems.SENSOR_UHV.getStackForm(),
                MetaItems.SENSOR_UV.getStackForm(),
                new ItemStack[] {
                    OreDictUnifier.get(frameGt, Neutronium, 1),
                    MetaItems.ELECTRIC_MOTOR_UHV.getStackForm(),
                    OreDictUnifier.get(plate, Neutronium, 6),
                    GRAVI_STAR.getStackForm(6),
                    MetaItems.WETWARE_MAINFRAME_UHV.getStackForm(),
                    withCount(rutheniumTriniumAmericiumNeutronateFineWire, 64),
                    withCount(omniumSingleWire, 2)
                },
                MOLTEN_INFINITY_PER_UHV_COMPONENT,
                4000,
                moltenInfinity,
                null);

        registerUhvComponentAssemblyLineRecipe(
                "emitter",
                MetaItems.EMITTER_UHV.getStackForm(),
                MetaItems.EMITTER_UV.getStackForm(),
                new ItemStack[] {
                    OreDictUnifier.get(frameGt, Neutronium, 1),
                    MetaItems.ELECTRIC_MOTOR_UHV.getStackForm(),
                    GRAVI_STAR.getStackForm(4),
                    MetaItems.WETWARE_MAINFRAME_UHV.getStackForm(),
                    MetaItems.WETWARE_MAINFRAME_UHV.getStackForm(),
                    OreDictUnifier.get(plate, Neutronium, 64),
                    withCount(rutheniumTriniumAmericiumNeutronateFineWire, 64),
                    withCount(omniumSingleWire, 2)
                },
                MOLTEN_INFINITY_PER_UHV_COMPONENT,
                4000,
                moltenInfinity,
                null);

        registerUhvComponentAssemblyLineRecipe(
                "field_generator",
                MetaItems.FIELD_GENERATOR_UHV.getStackForm(),
                MetaItems.FIELD_GENERATOR_UV.getStackForm(),
                new ItemStack[] {
                    OreDictUnifier.get(frameGt, Neutronium, 1),
                    OreDictUnifier.get(plate, Neutronium, 6),
                    GRAVI_STAR.getStackForm(4),
                    MetaItems.EMITTER_UHV.getStackForm(),
                    MetaItems.EMITTER_UHV.getStackForm(),
                    MetaItems.WETWARE_MAINFRAME_UHV.getStackForm(),
                    MetaItems.WETWARE_MAINFRAME_UHV.getStackForm(),
                    MetaItems.WETWARE_MAINFRAME_UHV.getStackForm(),
                    MetaItems.WETWARE_MAINFRAME_UHV.getStackForm(),
                    withCount(rutheniumTriniumAmericiumNeutronateFineWire, 64),
                    withCount(omniumSingleWire, 8)
                },
                MOLTEN_INFINITY_PER_UHV_COMPONENT,
                4000,
                moltenInfinity,
                null);
    }

    private static ItemStack withCount(ItemStack stack, int count) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = stack.copy();
        result.setCount(count);
        return result;
    }

    private static void registerUhvComponentAssemblyLineRecipe(
            String componentName,
            ItemStack output,
            ItemStack researchStack,
            ItemStack[] inputs,
            int infinityAmount,
            int lubricantAmount,
            Fluid moltenInfinity,
            FluidStack additionalFluid) {
        if (output.isEmpty() || researchStack.isEmpty() || TOOL_DATA_MODULE.getStackForm().isEmpty()) {
            MimsAdditions.LOGGER.warn(
                    "Missing GregTech UHV component or research item for {}; skipping its " + "assembly-line recipe",
                    componentName);
            return;
        }
        if ("conveyor".equals(componentName) && additionalFluid == null) {
            MimsAdditions.LOGGER.error("Liquid styrene-butadiene rubber is unavailable; skipping UHV conveyor recipe");
            return;
        }
        inputs = mergeAssemblyLineInputs(inputs);
        for (int i = 0; i < inputs.length; i++) {
            ItemStack input = inputs[i];
            if (input == null || input.isEmpty()) {
                MimsAdditions.LOGGER.warn(
                        "Missing ingredient at slot {} of {} for UHV {}; skipping its " + "assembly-line recipe",
                        i + 1,
                        inputs.length,
                        componentName);
                return;
            }
        }

        List<FluidStack> fluidInputs = new ArrayList<>();
        fluidInputs.add(SolderingAlloy.getFluid(1152));
        if (additionalFluid != null) {
            fluidInputs.add(additionalFluid);
        }
        fluidInputs.add(Lubricant.getFluid(lubricantAmount));
        fluidInputs.add(new FluidStack(moltenInfinity, infinityAmount));

        RecipeMaps.ASSEMBLY_LINE_RECIPES
                .recipeBuilder()
                .inputs(inputs)
                .fluidInputs(fluidInputs.toArray(new FluidStack[0]))
                .stationResearch(
                        r ->
                                r.researchStack(researchStack)
                                        .dataStack(TOOL_DATA_MODULE.getStackForm())
                                        .researchId("mim_uhv_" + componentName)
                                        .EUt((int) GTValues.V[GTValues.UV])
                                        .CWUt(32, 1600))
                .outputs(output)
                .duration(20 * 60)
                .EUt((int) GTValues.V[GTValues.UV])
                .buildAndRegister();

        if (!hasVisibleAssemblyLineRecipe(output)) {
            MimsAdditions.LOGGER.error(
                    "GregTech did not expose the UHV {} assembly-line recipe; it will not appear in JEI",
                    componentName);
            return;
        }

        MimsAdditions.LOGGER.debug(
                "Registered UV-power UHV {} assembly-line recipe with {} mB lubricant "
                        + "followed by {} mB Molten Infinity",
                componentName,
                lubricantAmount,
                infinityAmount);
    }

    private static ItemStack[] mergeAssemblyLineInputs(ItemStack[] inputs) {
        List<ItemStack> mergedInputs = new ArrayList<>();
        for (ItemStack input : inputs) {
            boolean merged = false;
            for (ItemStack existing : mergedInputs) {
                if (ItemStack.areItemsEqual(existing, input) && ItemStack.areItemStackTagsEqual(existing, input)) {
                    existing.grow(input.getCount());
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                mergedInputs.add(input.copy());
            }
        }
        return mergedInputs.toArray(new ItemStack[0]);
    }

    private static boolean hasVisibleAssemblyLineRecipe(ItemStack output) {
        for (Recipe recipe : RecipeMaps.ASSEMBLY_LINE_RECIPES.getRecipeList()) {
            if (recipe.isHidden()) {
                continue;
            }
            for (ItemStack recipeOutput : recipe.getAllItemOutputs()) {
                if (recipeOutput.getItem() == output.getItem() && recipeOutput.getMetadata() == output.getMetadata()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static ItemStack getComponentStack(Object component) {
        if (component == null) {
            return ItemStack.EMPTY;
        }
        if (component instanceof ItemStack) {
            return ((ItemStack) component).copy();
        }
        if (component instanceof UnificationEntry) {
            return OreDictUnifier.get((UnificationEntry) component);
        }
        if (component instanceof Item) {
            return new ItemStack((Item) component);
        }
        if (component instanceof String) {
            return OreDictUnifier.get((String) component);
        }
        throw new IllegalStateException(
                "Unsupported GregTech crafting component value: " + component.getClass().getName());
    }

    private static NonNullList<Ingredient> copyRecipeIngredients(IRecipe recipe) {
        NonNullList<Ingredient> ingredients;
        if (recipe instanceof ShapedRecipes) {
            ingredients = ((ShapedRecipes) recipe).getIngredients();
        } else if (recipe instanceof ShapedOreRecipe) {
            ingredients = ((ShapedOreRecipe) recipe).getIngredients();
        } else {
            throw new IllegalStateException("Unsupported shaped recipe class: " + recipe.getClass().getName());
        }

        NonNullList<Ingredient> copy = NonNullList.create();
        copy.addAll(ingredients);
        return copy;
    }

    private static IRecipe findCraftingRecipe(ItemStack output) {
        IRecipe recipe = findCraftingRecipeOrNull(output);
        if (recipe != null) {
            return recipe;
        }
        throw new IllegalStateException("No crafting recipe found for GregTech machine " + output);
    }

    private static IRecipe findCraftingRecipeOrNull(ItemStack output) {
        for (IRecipe recipe : ForgeRegistries.RECIPES.getValuesCollection()) {
            ItemStack recipeOutput = recipe.getRecipeOutput();
            if (!recipeOutput.isEmpty() && ItemStack.areItemsEqual(recipeOutput, output)) {
                return recipe;
            }
        }
        return null;
    }

    private static void registerMicrominerMicrominerRecipe() {
        NonNullList<Ingredient> ingredients = NonNullList.create();

        Ingredient D = new OreIngredient("plateDoubleDuranium");
        Ingredient T = new OreIngredient("plateDoubleTrinium");
        Ingredient N = new OreIngredient("plateDoubleNeutronium");

        Ingredient O = Ingredient.fromStacks(new ItemStack(ModItems.omniumPlate));

        Ingredient E = Ingredient.fromStacks(MetaItems.EMITTER_UV.getStackForm());
        Ingredient C = Ingredient.fromStacks(MetaItems.SENSOR_UV.getStackForm());
        Ingredient F = Ingredient.fromStacks(MetaItems.FIELD_GENERATOR_UV.getStackForm());

        Ingredient V = Ingredient.fromStacks(MetaItems.GRAVI_STAR.getStackForm());

        Ingredient Q = Ingredient.fromStacks(MetaTileEntities.QUANTUM_CHEST[5].getStackForm());

        Ingredient U =
                Ingredient.fromStacks(
                        new ItemStack(
                                ForgeRegistries.ITEMS.getValue(
                                        new ResourceLocation("nomilabs", "universalnavigator"))));
        Ingredient I =
                Ingredient.fromStacks(
                        new ItemStack(
                                ForgeRegistries.ITEMS.getValue(
                                        new ResourceLocation("nomilabs", "stabilizedeinsteinium"))));
        Ingredient W =
                Ingredient.fromStacks(
                        new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("nomilabs", "warpengine"))));
        Ingredient L =
                Ingredient.fromStacks(
                        new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("nomilabs", "t3laser"))));
        Ingredient S =
                Ingredient.fromStacks(
                        new ItemStack(
                                ForgeRegistries.ITEMS.getValue(
                                        new ResourceLocation("nomilabs", "stellarcreationdata"))));
        Ingredient G =
                Ingredient.fromStacks(
                        new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("nomilabs", "t2guidance"))));
        Ingredient H =
                Ingredient.fromStacks(
                        new ItemStack(
                                ForgeRegistries.ITEMS.getValue(new ResourceLocation("nomilabs", "heartofauniverse"))));

        // Draconic Evolution reactor stabilizer
        Ingredient R =
                Ingredient.fromStacks(
                        new ItemStack(
                                ForgeRegistries.ITEMS.getValue(
                                        new ResourceLocation("draconicevolution", "reactor_component"))));

        Ingredient EMPTY = Ingredient.EMPTY;

        // Each row is nine slots wide.
        String[] rows =
                new String[] {
                    "___LGL___",
                    "__TFLFT__",
                    "_TOCUCOT_",
                    "_TOSVSOT_",
                    "_DOINIOD_",
                    "_DONHNOD_",
                    "DORINIROD",
                    "DDOOEOODD",
                    "__WWOWW__"
                };

        for (String row : rows) {
            if (row.length() != 9) {
                throw new IllegalStateException(
                        "Ultimate recipe row is not 9 chars: '" + row + "' (" + row.length() + ")");
            }
            for (int i = 0; i < 9; i++) {
                char c = row.charAt(i);
                switch (c) {
                    case 'D':
                        ingredients.add(D);
                        break;
                    case 'T':
                        ingredients.add(T);
                        break;
                    case 'N':
                        ingredients.add(N);
                        break;

                    case 'O':
                        ingredients.add(O);
                        break;

                    case 'E':
                        ingredients.add(E);
                        break;
                    case 'C':
                        ingredients.add(C);
                        break;
                    case 'F':
                        ingredients.add(F);
                        break;

                    case 'V':
                        ingredients.add(V);
                        break;
                    case 'Q':
                        ingredients.add(Q);
                        break;

                    case 'U':
                        ingredients.add(U);
                        break;
                    case 'I':
                        ingredients.add(I);
                        break;
                    case 'H':
                        ingredients.add(H);
                        break;
                    case 'W':
                        ingredients.add(W);
                        break;
                    case 'L':
                        ingredients.add(L);
                        break;
                    case 'S':
                        ingredients.add(S);
                        break;
                    case 'G':
                        ingredients.add(G);
                        break;

                    case 'R':
                        ingredients.add(R);
                        break;

                    default:
                        ingredients.add(EMPTY);
                        break;
                }
            }
        }

        TableRecipeManager.getInstance().addShaped(0, new ItemStack(ModItems.microminerMicrominer), 9, 9, ingredients);
        MimsAdditions.LOGGER.debug("Registered 9x9 Ultimate Crafting Table recipe for Microminer Microminer");
    }

    /**
     * Condensed 7x7 Elite Crafting Table recipe for Titanium Coil Micro Miner Uses string-pattern + ingredient mapping
     * (same style as microminerMicrominer recipe)
     *
     * <p>Legend: G = nomilabs:t2guidance T = plateDoubleTitanium W = gregtech:wire_coil:2 L = nomilabs:t2laser S =
     * gregtech:meta_item_1 (meta 235) [SENSOR_EV] C = gregtech:machine:1560 [super_chest.mv] P = gregtech:meta_item_1
     * (meta 205) M = nomilabs:tieroneship [T1 Micro Miner] J = simplyjetpacks:metaitemmods:10 _ = empty
     */
    private static void registerTitaniumCoilMicroMinerRecipe() {
        NonNullList<Ingredient> ingredients = NonNullList.create();

        Ingredient EMPTY = Ingredient.EMPTY;

        Ingredient G = getItemIngredient("nomilabs:t2guidance");
        Ingredient T = new OreIngredient("plateDoubleTitanium");
        Ingredient W = getItemIngredient("gregtech:wire_coil:2");
        Ingredient L = getItemIngredient("nomilabs:t2laser");
        Ingredient S = getMetaItem(235);
        Ingredient C = getItemIngredient("gregtech:machine:1560");
        Ingredient P = getMetaItem(205);
        Ingredient M = getItemIngredient("nomilabs:tieroneship");
        Ingredient J = getItemIngredient("simplyjetpacks:metaitemmods:10");

        // Elite Crafting Table pattern.
        String[] rows = new String[] {"__G_G__", "_TWLWT_", "_TTSTT_", "_TWCWT_", "TTSPSTT", "_TWMWT_", "__JJJ__"};

        for (String row : rows) {
            if (row.length() != 7) {
                throw new IllegalStateException(
                        "Elite recipe row is not 7 chars: '" + row + "' (" + row.length() + ")");
            }
            for (int i = 0; i < 7; i++) {
                char c = row.charAt(i);
                switch (c) {
                    case 'G':
                        ingredients.add(G);
                        break;
                    case 'T':
                        ingredients.add(T);
                        break;
                    case 'W':
                        ingredients.add(W);
                        break;
                    case 'L':
                        ingredients.add(L);
                        break;
                    case 'S':
                        ingredients.add(S);
                        break;
                    case 'C':
                        ingredients.add(C);
                        break;
                    case 'P':
                        ingredients.add(P);
                        break;
                    case 'M':
                        ingredients.add(M);
                        break;
                    case 'J':
                        ingredients.add(J);
                        break;
                    default:
                        ingredients.add(EMPTY);
                        break; // '_' or anything else
                }
            }
        }

        TableRecipeManager.getInstance()
                .addShaped(
                        0, // tier 0 = any table size (Basic, Advanced, Elite, Ultimate)
                        new ItemStack(ModItems.titaniumCoilMicroMiner),
                        7,
                        7,
                        ingredients);

        MimsAdditions.LOGGER.debug("Registered 7x7 Elite Crafting Table recipe for Titanium Coil Micro Miner");
    }

    private static void registerCreativeChestTrapRecipe() {
        if (ModItems.stabilizedEpsteinium == null || MetaTileEntities.CREATIVE_CHEST == null) {
            MimsAdditions.LOGGER.error(
                    "Missing Stabilized Epsteinium or Creative Chest; skipping the chest trap recipe");
            return;
        }

        ModRecipeMaps.MICROVERSE_4_RECIPES
                .recipeBuilder()
                .inputs(new ItemStack(ModItems.stabilizedEpsteinium, 64))
                .outputs(MetaTileEntities.CREATIVE_CHEST.getStackForm())
                .duration(20 * 10)
                .EUt((int) GTValues.V[GTValues.UEV])
                .buildAndRegister();

        MimsAdditions.LOGGER.debug("Registered Microverse IV chest trap recipe");
    }

    /**
     * Registers Microverse Projector 2 recipes: Coil Micro Miner + (quantumflux x N) + (ingotX) -> 16x wire_coil(metaX)
     *
     * <p>Quantum Flux cost groups: - Cupronickel, Kanthal: 4x - Nichrome, RTM Alloy, HSS-G: 16x - Naquadah, Trinium,
     * Tritanium: 64x
     *
     * <p>EU/t: - Cupronickel..Kanthal: HV (512) - Nichrome..HSS-G: IV (8192) - Naquadah: LuV (32768) - Trinium: ZPM
     * (131072) - Tritanium: UV (524288)
     *
     * <p>Duration: 30s (600 ticks) for all
     */
    private static void registerMicroverseProjector2CoilRecipes() {
        try {
            Object projector2Map = getMicroverseProjectorRecipeMapTier(2);
            if (projector2Map == null) {
                MimsAdditions.LOGGER.warn(
                        "Could not locate NomiLabs Microverse Projector 2 RecipeMap; skipping coil recipes");
                return;
            }

            Item quantumFluxItem = Item.getByNameOrId("nomilabs:quantumflux");
            if (quantumFluxItem == null) {
                MimsAdditions.LOGGER.warn("Could not find nomilabs:quantumflux; skipping coil recipes");
                return;
            }

            Item coilItem = Item.getByNameOrId("gregtech:wire_coil");
            if (coilItem == null) {
                MimsAdditions.LOGGER.warn("Could not find gregtech:wire_coil; skipping coil recipes");
                return;
            }

            final int durationTicks = 20 * 30;

            // metas from BlockWireCoil.CoilType ordinal order:
            // 0 CUPRONICKEL, 1 KANTHAL, 2 NICHROME, 3 RTM_ALLOY, 4 HSS_G, 5 NAQUADAH, 6 TRINIUM, 7 TRITANIUM

            // HV group (4 quantumflux)
            registerOneCoilRecipe(
                    projector2Map,
                    coilItem,
                    quantumFluxItem,
                    0,
                    16,
                    4,
                    512,
                    durationTicks,
                    resolveIngot("ingotCupronickel"));
            registerOneCoilRecipe(
                    projector2Map,
                    coilItem,
                    quantumFluxItem,
                    1,
                    16,
                    4,
                    512,
                    durationTicks,
                    resolveIngot("ingotKanthal"));

            // IV group (16 quantumflux)
            registerOneCoilRecipe(
                    projector2Map,
                    coilItem,
                    quantumFluxItem,
                    2,
                    16,
                    16,
                    8192,
                    durationTicks,
                    resolveIngot("ingotNichrome"));
            registerOneCoilRecipe(
                    projector2Map,
                    coilItem,
                    quantumFluxItem,
                    3,
                    16,
                    16,
                    8192,
                    durationTicks,
                    resolveIngot("ingotRTMAlloy", "ingotRtmAlloy", "ingotRTM_alloy", "ingotRtm_alloy"));
            registerOneCoilRecipe(
                    projector2Map,
                    coilItem,
                    quantumFluxItem,
                    4,
                    16,
                    16,
                    8192,
                    durationTicks,
                    resolveIngot("ingotHSSG", "ingotHssg", "ingotHSS_G", "ingotHss_g"));

            // High tier group (64 quantumflux)
            registerOneCoilRecipe(
                    projector2Map,
                    coilItem,
                    quantumFluxItem,
                    5,
                    16,
                    64,
                    32768,
                    durationTicks,
                    resolveIngot("ingotNaquadah"));
            registerOneCoilRecipe(
                    projector2Map,
                    coilItem,
                    quantumFluxItem,
                    6,
                    16,
                    64,
                    131072,
                    durationTicks,
                    resolveIngot("ingotTrinium"));
            registerOneCoilRecipe(
                    projector2Map,
                    coilItem,
                    quantumFluxItem,
                    7,
                    16,
                    64,
                    524288,
                    durationTicks,
                    resolveIngot("ingotTritanium"));

            MimsAdditions.LOGGER.debug("Registered Microverse Projector 2 coil recipes (all tiers)");
        } catch (Throwable t) {
            MimsAdditions.LOGGER.error("Failed to register Microverse Projector 2 coil recipes", t);
        }
    }

    private static void registerOneCoilRecipe(
            Object recipeMap,
            Item coilItem,
            Item quantumFluxItem,
            int coilMeta,
            int coilOutCount,
            int quantumFluxCount,
            int eut,
            int durationTicks,
            ItemStack ingotStack)
            throws Exception {

        if (ingotStack == null || ingotStack.isEmpty()) {
            MimsAdditions.LOGGER.warn("Skipping coil meta {} recipe: ingot not found in OreDict", coilMeta);
            return;
        }

        ItemStack microMiner = new ItemStack(ModItems.titaniumCoilMicroMiner, 1, 0);
        ItemStack quantumFlux = new ItemStack(quantumFluxItem, quantumFluxCount, 0);
        ItemStack output = new ItemStack(coilItem, coilOutCount, coilMeta);

        Method recipeBuilderM = recipeMap.getClass().getMethod("recipeBuilder");
        Object builder = recipeBuilderM.invoke(recipeMap);

        Method inputsM = builder.getClass().getMethod("inputs", ItemStack[].class);
        inputsM.invoke(builder, (Object) new ItemStack[] {microMiner, quantumFlux, ingotStack.copy()});

        Method outputsM = builder.getClass().getMethod("outputs", ItemStack[].class);
        outputsM.invoke(builder, (Object) new ItemStack[] {output});

        builder.getClass().getMethod("duration", int.class).invoke(builder, durationTicks);
        builder.getClass().getMethod("EUt", int.class).invoke(builder, eut);
        builder.getClass().getMethod("buildAndRegister").invoke(builder);

        MimsAdditions.LOGGER.debug(
                "Registered coil recipe: meta={} qf={} EUt={} -> {}x wire_coil",
                coilMeta,
                quantumFluxCount,
                eut,
                coilOutCount);
    }

    /**
     * Microverse Projector 4: MMM + TierN Micro Miner + Quantum Flux -> (OutputCount x TierN Micro Miner)
     *
     * <p>Voltage: UV -> UHV -> UEV only Quantum Flux: evenly distributed, capped at 128 at tier 10 Each tier's full
     * yield is represented by one counted output stack.
     *
     * <p>NOTE: All inputs are consumed, including MMM.
     */
    private static void registerMicroverseProjector4MicrominerRecipes() {

        Item quantumFluxItem = Item.getByNameOrId("nomilabs:quantumflux");
        if (quantumFluxItem == null) {
            MimsAdditions.LOGGER.warn("Could not find nomilabs:quantumflux; skipping Projector4 microminer recipes");
            return;
        }

        if (ModItems.microminerMicrominer == null) {
            MimsAdditions.LOGGER.warn("ModItems.microminerMicrominer is null; skipping Projector4 microminer recipes");
            return;
        }

        final String[] minerIds = {
            "nomilabs:tieroneship",
            "nomilabs:tiertwoship",
            "nomilabs:tierthreeship",
            "nomilabs:tierfourship",
            "nomilabs:tierfiveship",
            "nomilabs:tiersixship",
            "nomilabs:tiersevenship",
            "nomilabs:tiereightship",
            "nomilabs:tiernineship",
            "nomilabs:tiertenship"
        };

        final int[] outputs = {
            2048, // T1
            1024, // T2
            512, // T3
            256, // T4
            128, // T5
            48, // T6
            24, // T7
            16, // T8
            9, // T9
            4 // T10
        };

        final int[] qf = {13, 26, 38, 51, 64, 77, 90, 102, 115, 128};
        final int[] ticks = {
            20 * 100, 20 * 122, 20 * 144, 20 * 166, 20 * 188,
            20 * 211, 20 * 233, 20 * 255, 20 * 277, 20 * 300
        };

        final int UV = 524288;
        final int UHV = 2097152;
        final int UEV = 8388608;

        ItemStack MMM = new ItemStack(ModItems.microminerMicrominer, 1, 0);

        for (int i = 0; i < 10; i++) {
            Item minerItem = Item.getByNameOrId(minerIds[i]);
            if (minerItem == null) {
                MimsAdditions.LOGGER.warn("Missing microminer item: {} (tier {}), skipping", minerIds[i], (i + 1));
                continue;
            }

            int eut = (i <= 2) ? UV : (i <= 6) ? UHV : UEV;

            // Build input stacks: MMM (1), tierN microminer (1), then split quantum flux
            List<ItemStack> inputStacks = new ArrayList<>();
            inputStacks.add(MMM);
            inputStacks.add(new ItemStack(minerItem, 1, 0));
            int qfCount = qf[i];
            final int qfMax = 64;
            while (qfCount > 0) {
                int stackSize = Math.min(qfCount, qfMax);
                inputStacks.add(new ItemStack(quantumFluxItem, stackSize, 0));
                qfCount -= stackSize;
            }

            ItemStack outputStack = new ItemStack(minerItem, outputs[i], 0);

            ModRecipeMaps.MICROVERSE_4_RECIPES
                    .recipeBuilder()
                    .inputs(inputStacks.toArray(new ItemStack[0]))
                    .outputs(outputStack)
                    .duration(ticks[i])
                    .EUt(eut)
                    .buildAndRegister();
        }

        MimsAdditions.LOGGER.debug("Registered Microverse Projector 4 microminer recipes");
    }

    private static void registerMicroverseProjector4ControllerRecipe() {
        if (MachineRegistry.MICROVERSE_PROJECTOR_4 == null) {
            MimsAdditions.LOGGER.warn(
                    "Microverse Projector 4 controller is not registered; skipping assembly line recipe");
            return;
        }

        ItemStack projector3Stack =
                resolveResearchStack(
                        "nomilabs:microverse_projector_3", "gregtech:machine", "microverse_projector_3", 32102);
        if (projector3Stack.isEmpty()) {
            MimsAdditions.LOGGER.warn(
                    "Missing Microverse Projector III (ids: nomilabs:microverse_projector_3 or gregtech:machine:32102); skipping research-gated Projector 4 recipe");
            return;
        }

        RecipeMaps.ASSEMBLY_LINE_RECIPES
                .recipeBuilder()
                .input(FIELD_GENERATOR_UV, 4)
                .input(EMITTER_UV, 4)
                .input(SENSOR_UV, 4)
                .input(GRAVI_STAR, 2)
                .input(plateDouble, Neutronium, 16)
                .input(plateDouble, Trinium, 16)
                .input(plateDouble, Duranium, 16)
                .input(WETWARE_MAINFRAME_UHV, 4)
                .inputs(MetaTileEntities.QUANTUM_CHEST[5].getStackForm())
                .fluidInputs(SolderingAlloy.getFluid(5760))
                .fluidInputs(Lubricant.getFluid(4000))
                .stationResearch(
                        r ->
                                r.researchStack(projector3Stack)
                                        .dataStack(TOOL_DATA_MODULE.getStackForm())
                                        .researchId("microverse_projector_4")
                                        .EUt((int) GTValues.V[GTValues.ZPM])
                                        .CWUt(32, 1600))
                .outputs(MachineRegistry.MICROVERSE_PROJECTOR_4.getStackForm())
                .duration(20 * 60)
                .EUt((int) GTValues.V[GTValues.UV])
                .buildAndRegister();

        MimsAdditions.LOGGER.debug("Registered Microverse Projector 4 controller assembly line recipe");
    }

    private static void registerEpsteiniumSolidifierRecipe() {
        if (ModFluids.epsteinium == null) {
            MimsAdditions.LOGGER.warn("Epsteinium fluid is not registered; skipping solidifier recipe");
            return;
        }

        RecipeMaps.FLUID_SOLIDFICATION_RECIPES
                .recipeBuilder()
                .notConsumable(SHAPE_MOLD_BALL)
                .fluidInputs(new FluidStack(ModFluids.epsteinium, 1000))
                .outputs(new ItemStack(ModItems.stabilizedEpsteinium, 1))
                .duration(20 * 60)
                .EUt((int) GTValues.V[GTValues.LV])
                .buildAndRegister();

        MimsAdditions.LOGGER.debug("Registered Epsteinium fluid solidification recipe");
    }

    private static void registerRtaNeutronateFineWireRecipe() {
        if (ModItems.rtaNeutronateFineWire == null) {
            MimsAdditions.LOGGER.error("RTA fine-wire item is not registered; skipping its wiremill recipe");
            return;
        }

        ItemStack singleWire = OreDictUnifier.get(wireGtSingle, RutheniumTriniumAmericiumNeutronate);
        if (singleWire.isEmpty()) {
            MimsAdditions.LOGGER.error(
                    "Ruthenium Trinium Americium Neutronate single wire is unavailable; "
                            + "cannot register the fine-wire recipe");
            return;
        }

        ItemStack fineWire = new ItemStack(ModItems.rtaNeutronateFineWire, 4);
        RecipeMaps.WIREMILL_RECIPES
                .recipeBuilder()
                .inputs(singleWire)
                .outputs(fineWire)
                .duration(20 * 5)
                .EUt((int) GTValues.V[GTValues.LV])
                .buildAndRegister();

        if (!hasRecipeOutput(RecipeMaps.WIREMILL_RECIPES.getRecipeList(), fineWire)) {
            MimsAdditions.LOGGER.error("GregTech did not register the RTA fine-wire wiremill recipe");
            return;
        }
        MimsAdditions.LOGGER.debug("Registered RTA fine-wire wiremill recipe");
    }

    private static void registerEpsteiniumFusionRecipe() {
        if (ModFluids.epsteinium == null || ModFluids.liquidBabyVillager == null) {
            MimsAdditions.LOGGER.warn("Required fluids missing; skipping Epsteinium fusion recipe");
            return;
        }

        RecipeMaps.FUSION_RECIPES
                .recipeBuilder()
                .fluidInputs(new FluidStack(ModFluids.liquidBabyVillager, 100))
                .fluidInputs(DrillingFluid.getFluid(25))
                .fluidOutputs(new FluidStack(ModFluids.epsteinium, 125))
                .duration(20 * 5)
                .EUt((int) GTValues.V[GTValues.UV])
                .EUToStart(640_000_000L)
                .buildAndRegister();

        MimsAdditions.LOGGER.debug("Registered Epsteinium fusion recipe (MK3-scale split)");
    }

    private static void registerBabyVillagerExtractorRecipe() {
        if (ModFluids.liquidBabyVillager == null || ModItems.babyVillagerEssence == null) {
            MimsAdditions.LOGGER.warn("Liquid Baby Villager or essence missing; skipping extractor recipe");
            return;
        }

        RecipeMaps.EXTRACTOR_RECIPES
                .recipeBuilder()
                .inputs(new ItemStack(ModItems.babyVillagerEssence, 1))
                .fluidOutputs(new FluidStack(ModFluids.liquidBabyVillager, 1000))
                .duration(20 * 5)
                .EUt((int) GTValues.V[GTValues.IV])
                .buildAndRegister();

        MimsAdditions.LOGGER.debug("Registered Baby Villager Essence extractor recipe");
    }

    private static void registerInfinityIngotExtractorRecipe() {
        Fluid moltenInfinity = ModFluids.moltenInfinity;
        ItemStack infinityIngot = resolveOreStack(1, "ingotInfinity");
        if (moltenInfinity == null || infinityIngot.isEmpty()) {
            MimsAdditions.LOGGER.error(
                    "Molten Infinity or an Infinity ingot is unavailable; "
                            + "skipping the Infinity ingot extractor recipe");
            return;
        }

        ItemStack ingot = infinityIngot.copy();
        RecipeMaps.EXTRACTOR_RECIPES
                .recipeBuilder()
                .inputs(ingot)
                .fluidOutputs(new FluidStack(moltenInfinity, 144))
                .duration(20 * 10)
                .EUt((int) GTValues.V[GTValues.UV])
                .buildAndRegister();

        FluidStack moltenOutput = new FluidStack(moltenInfinity, 144);
        if (!hasFluidOutput(RecipeMaps.EXTRACTOR_RECIPES.getRecipeList(), moltenOutput)) {
            MimsAdditions.LOGGER.error("GregTech did not register the Infinity ingot extractor recipe");
            return;
        }
        MimsAdditions.LOGGER.debug(
                "Registered 10-second, 1A UV Infinity ingot extractor recipe for 144 mB Molten Infinity");
    }

    private static boolean hasRecipeOutput(Collection<Recipe> recipes, ItemStack output) {
        for (Recipe recipe : recipes) {
            for (ItemStack recipeOutput : recipe.getAllItemOutputs()) {
                if (ItemStack.areItemsEqual(recipeOutput, output)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasFluidOutput(Collection<Recipe> recipes, FluidStack expectedOutput) {
        for (Recipe recipe : recipes) {
            for (FluidStack fluidOutput : recipe.getAllFluidOutputs()) {
                if (fluidOutput.isFluidEqual(expectedOutput) && fluidOutput.amount == expectedOutput.amount) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void registerOmniumPlateBenderRecipe() {
        Item ultimate = Item.getByNameOrId("extendedcrafting:material");
        Item omniumIngot = Item.getByNameOrId("nomilabs:meta_ingot");
        if ((ultimate == null && omniumIngot == null) || ModItems.omniumPlate == null) {
            MimsAdditions.LOGGER.warn("Missing ingot or omnium plate; skipping bender recipes");
            return;
        }

        if (ultimate != null) {
            RecipeMaps.BENDER_RECIPES
                    .recipeBuilder()
                    .circuitMeta(1)
                    .inputs(new ItemStack(ultimate, 1, 32))
                    .outputs(new ItemStack(ModItems.omniumPlate, 1))
                    .duration(20 * 30)
                    .EUt((int) GTValues.V[GTValues.LV])
                    .buildAndRegister();
        }

        if (omniumIngot != null) {
            RecipeMaps.BENDER_RECIPES
                    .recipeBuilder()
                    .circuitMeta(1)
                    .inputs(new ItemStack(omniumIngot, 1, 4))
                    .outputs(new ItemStack(ModItems.omniumPlate, 1))
                    .duration(20 * 30)
                    .EUt((int) GTValues.V[GTValues.LV])
                    .buildAndRegister();
        }

        MimsAdditions.LOGGER.debug("Registered Omnium Plate bender recipes with circuit 1");
    }

    private static void registerEpsteiniumWetwareMainframeRecipe() {
        if (ModItems.epsteiniumInfusedWetwareMainframe == null) {
            MimsAdditions.LOGGER.warn("Missing Epsteinium-Infused Wetware Mainframe; skipping recipe");
            return;
        }
        if (ModFluids.epsteinium == null) {
            MimsAdditions.LOGGER.warn(
                    "Epsteinium fluid is not registered; skipping Epsteinium-Infused Wetware Mainframe recipe");
            return;
        }

        ItemStack draconicWire =
                resolveOreStack(
                        8,
                        "wireGtDoubleDraconicSuperconductor",
                        "wireGtDoubleDraconiumSuperconductor",
                        "wireGtDoubleDraconic",
                        "wireGtDoubleDraconium");
        if (draconicWire.isEmpty()) {
            MimsAdditions.LOGGER.warn("Missing draconic superconductor wire; skipping recipe");
            return;
        }

        RecipeMaps.ASSEMBLY_LINE_RECIPES
                .recipeBuilder()
                .input(WETWARE_MAINFRAME_UHV, 1)
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 64)
                .input(ADVANCED_SMD_CAPACITOR, 64)
                .input(ADVANCED_SMD_DIODE, 64)
                .input(ADVANCED_SMD_RESISTOR, 64)
                .input(ADVANCED_SMD_TRANSISTOR, 64)
                .input(ADVANCED_SMD_INDUCTOR, 64)
                .input(RANDOM_ACCESS_MEMORY, 64)
                .input(GRAVI_STAR, 1)
                .inputs(new ItemStack(ModItems.omniumPlate, 8))
                .inputs(draconicWire)
                .input(circuit, MarkerMaterials.Tier.ZPM, 2)
                .input(circuit, MarkerMaterials.Tier.IV, 4)
                .input(circuit, MarkerMaterials.Tier.HV, 8)
                .input(circuit, MarkerMaterials.Tier.LV, 16)
                .fluidInputs(SolderingAlloy.getFluid(5760))
                .fluidInputs(Lubricant.getFluid(4000))
                .fluidInputs(Neutronium.getFluid(576))
                .fluidInputs(new FluidStack(ModFluids.epsteinium, 4000))
                .stationResearch(
                        r ->
                                r.researchStack(WETWARE_MAINFRAME_UHV.getStackForm())
                                        .dataStack(TOOL_DATA_MODULE.getStackForm())
                                        .researchId("epsteinium_infused_wetware_mainframe")
                                        .EUt((int) GTValues.V[GTValues.UHV])
                                        .CWUt(64, 3200))
                .outputs(new ItemStack(ModItems.epsteiniumInfusedWetwareMainframe, 1))
                .duration(20 * 120)
                .EUt((int) GTValues.V[GTValues.UHV])
                .buildAndRegister();

        MimsAdditions.LOGGER.debug("Registered Epsteinium-Infused Wetware Mainframe assembly line recipe");
    }

    /**
     * Returns the first matching OreDictionary ingot stack (count=1) from the provided keys, or ItemStack.EMPTY if none
     * exist.
     */
    private static ItemStack resolveIngot(String... oreKeys) {
        for (String key : oreKeys) {
            List<ItemStack> ores = OreDictionary.getOres(key);
            if (ores != null && !ores.isEmpty()) {
                ItemStack s = ores.get(0).copy();
                s.setCount(1);
                return s;
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack resolveOreStack(int count, String... oreKeys) {
        for (String key : oreKeys) {
            List<ItemStack> ores = OreDictionary.getOres(key);
            if (ores != null && !ores.isEmpty()) {
                ItemStack s = ores.get(0).copy();
                s.setCount(count);
                return s;
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack resolveResearchStack(
            String directId, String fallbackId, String keyContains, int fallbackMeta) {
        Item direct = Item.getByNameOrId(directId);
        if (direct != null) {
            return new ItemStack(direct, 1, 0);
        }

        Item fallback = Item.getByNameOrId(fallbackId);
        if (fallback == null) {
            return ItemStack.EMPTY;
        }

        if (fallbackMeta >= 0) {
            return new ItemStack(fallback, 1, fallbackMeta);
        }

        NonNullList<ItemStack> variants = NonNullList.create();
        try {
            fallback.getSubItems(CreativeTabs.SEARCH, variants);
        } catch (Throwable t) {
            return ItemStack.EMPTY;
        }

        for (ItemStack stack : variants) {
            String key = stack.getTranslationKey();
            if (key != null && key.contains(keyContains)) {
                ItemStack out = stack.copy();
                out.setCount(1);
                return out;
            }
        }

        return ItemStack.EMPTY;
    }

    /**
     * NomiLabs stores the microverse recipemaps in:
     * com.nomiceu.nomilabs.gregtech.recipe.LabsRecipeMaps.MICROVERSE_RECIPES
     *
     * <p>Tier 1 => index 0 Tier 2 => index 1 Tier 3 => index 2
     */
    private static Object getMicroverseProjectorRecipeMapTier(int tier) {
        try {
            Class<?> labsRecipeMapsClz = Class.forName("com.nomiceu.nomilabs.gregtech.recipe.LabsRecipeMaps");
            Field listField = labsRecipeMapsClz.getField("MICROVERSE_RECIPES");
            Object listObj = listField.get(null);

            if (!(listObj instanceof List)) {
                MimsAdditions.LOGGER.warn("LabsRecipeMaps.MICROVERSE_RECIPES is not a java.util.List");
                return null;
            }

            List<?> maps = (List<?>) listObj;
            int index = tier - 1;

            if (index < 0 || index >= maps.size() || maps.get(index) == null) {
                MimsAdditions.LOGGER.warn(
                        "LabsRecipeMaps.MICROVERSE_RECIPES missing tier {} (index {}). size={}",
                        tier,
                        index,
                        maps.size());
                return null;
            }

            return maps.get(index);
        } catch (Throwable t) {
            MimsAdditions.LOGGER.error("Failed to access LabsRecipeMaps.MICROVERSE_RECIPES for tier " + tier, t);
            return null;
        }
    }

    private static Ingredient getItemIngredient(String itemString) {
        // Parse item string like "nomilabs:advancedmicrominerguide" or "gregtech:machine:1561"
        String[] parts = itemString.split(":");
        if (parts.length >= 2) {
            String modid = parts[0];
            String itemName = parts[1];
            int meta = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;

            Item item = Item.getByNameOrId(modid + ":" + itemName);
            if (item != null) {
                return Ingredient.fromStacks(new ItemStack(item, 1, meta));
            }
        }

        MimsAdditions.LOGGER.warn("Could not find item: " + itemString + " - using stick as fallback");
        return Ingredient.fromStacks(new ItemStack(Items.STICK));
    }

    private static Ingredient getMetaItem(int metaId) {
        // GregTech meta items are stored as "gregtech:meta_item_1" with metadata
        Item item = Item.getByNameOrId("gregtech:meta_item_1");
        if (item != null) {
            return Ingredient.fromStacks(new ItemStack(item, 1, metaId));
        }
        MimsAdditions.LOGGER.warn("Could not find GregTech meta_item_1 - using stick as fallback");
        return Ingredient.fromStacks(new ItemStack(Items.STICK));
    }

    private static ItemStack stack(String id, int count) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        if (item == null) throw new IllegalStateException("Missing item: " + id);
        return new ItemStack(item, count, 0);
    }
}
