package com.example.modid.firmware;

import com.example.modid.MachineRegistry;
import com.example.modid.MimsAdditions;
import com.example.modid.common.ModItems;
import com.example.modid.common.ModMaterialTweaks;
import com.example.modid.common.PrimitiveSteelRecipes;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.WireProperties;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.pipelike.cable.BlockCable;
import gregtech.common.pipelike.cable.Insulation;
import gregtech.common.pipelike.cable.ItemBlockCable;
import gregtech.core.unification.material.internal.MaterialRegistryManager;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.*;

/** Opt-in development checks. No blocks are placed and no player inventories are touched. */
public final class FirmwareSelfTest {
    private static int checks;

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new IllegalStateException("Firmware check failed: " + message);
    }

    public static void run(WorldServer world) {
        checks = 0;
        try {
            check(!FirmwareMaterials.solder().isEmpty(), "fine soldering alloy wire is generated");
            check(
                    OreDictUnifier.getUnificationEntry(FirmwareMaterials.solder()).orePrefix == OrePrefix.wireFine,
                    "solder is fine wire rather than an ingot");
            puzzle();
            ItemStack raw = MetaItems.ELECTRONIC_CIRCUIT_LV.getStackForm();
            FirmwareTarget target = FirmwareData.describe(MetaTileEntities.MACERATOR[GTValues.LV].getStackForm());
            FirmwareTarget wrong = FirmwareData.describe(MetaTileEntities.COMPRESSOR[GTValues.LV].getStackForm());
            zpmProgramming(world, target);
            wireEconomy(world, target);
            singleSupplyMigration(world);
            addonCircuitsAndSupreme(world);
            check(FirmwareTargets.get(target.key) != null, "registered machine target");
            for (FirmwareTarget available : FirmwareTargets.all()) {
                check(!available.circuitTiers.isEmpty(), "target has an actual circuit-consuming recipe");
                check(!available.key.contains("primitive_water_pump"), "primitive water pump is not a firmware target");
            }
            check(
                    FirmwareTargets.get(FirmwareData.targetKey(FirmwareMachines.SYNTHESIS_ARRAY.getStackForm()))
                            != null,
                    "array bootstrap target");
            check(FirmwareData.tier(raw) == GTValues.LV, "LV circuit detection");
            check(!FirmwareData.isCircuit(MetaItems.INTEGRATED_CIRCUIT.getStackForm()), "selector circuit exempt");
            ItemStack programmed = FirmwareData.program(raw, target);
            check(!FirmwareData.configured(raw), "programming preserves source");
            check(
                    programmed.isItemEqual(raw) && FirmwareData.target(programmed).equals(target.key),
                    "programmed identity");
            check(ItemStack.areItemStacksEqual(raw, FirmwareData.withoutFirmware(programmed)), "strip only firmware");
            ItemStack tagged = raw.copy();
            tagged.setTagInfo("OtherMod", new NBTTagCompound());
            check(
                    ItemStack.areItemStacksEqual(
                            tagged, FirmwareData.withoutFirmware(FirmwareData.program(tagged, target))),
                    "foreign NBT retained");
            recipes(world, raw, programmed, target, wrong);
            cachedCraftingLookup(world, target, wrong);
            nicheItems(world, raw);
            erasing(world, programmed);
            bench(world, raw, target);
            crossTierAndTools(world, target);
            structureAndParts();
            wireLoss();
            energyHatchLimits();
            primitiveSteelTiming();
            MimsAdditions.LOGGER.info("FIRMWARE SELFTEST PASSED: {} checks", checks);
        } catch (Throwable error) {
            MimsAdditions.LOGGER.error("FIRMWARE SELFTEST FAILED after {} checks", checks, error);
        }
    }

    private static void nicheItems(WorldServer world, ItemStack raw) {
        ItemStack[] items = {
            MetaItems.SMART_FILTER.getStackForm(),
            MetaItems.PROSPECTOR_LV.getStackForm(),
            MetaItems.PROSPECTOR_HV.getStackForm(),
            MetaItems.PROSPECTOR_LUV.getStackForm()
        };
        Container owner =
                new Container() {
                    @Override
                    public boolean canInteractWith(EntityPlayer player) {
                        return true;
                    }
                };
        for (ItemStack output : items) {
            FirmwareTarget target = FirmwareTargets.get(FirmwareData.targetKey(output));
            check(target != null, "niche firmware target: " + output.getDisplayName());
            ShapedOreRecipe recipe =
                    new ShapedOreRecipe(new ResourceLocation("modid", "niche_check"), output, "C", 'C', raw);
            InventoryCrafting grid = new InventoryCrafting(owner, 3, 3);
            grid.setInventorySlotContents(0, raw.copy());
            check(!recipe.matches(grid, world), "niche recipe rejects raw circuits");
            ItemStack programmed = FirmwareData.program(raw, target);
            grid.setInventorySlotContents(0, programmed);
            check(recipe.matches(grid, world), "niche recipe accepts its own firmware");
            ItemStack shown =
                    FirmwareDisplayIngredients.forOutput(
                                    output, Collections.singletonList(Collections.singletonList(raw)))
                            .get(0)
                            .get(0);
            check(FirmwareData.target(shown).equals(target.key), "niche crafting display names firmware target");
        }
        check(
                !FirmwareData.requiresFirmware(MetaItems.INTEGRATED_CIRCUIT.getStackForm()),
                "GT numbered circuit manufacturing stays exempt");
    }

    private static void puzzle() {
        for (int tier = 0; tier <= 14; tier++) {
            int branches = FirmwarePuzzle.branches(tier);
            check(branches >= 2 && branches <= 5, "branch count");
            Set<String> occupied = new HashSet<>();
            for (int b = 0; b < branches; b++) {
                int[] previous = FirmwarePuzzle.TERMINALS[b];
                for (int n = 0; n < FirmwarePuzzle.PATHS[b].length; n++) {
                    int[] at = FirmwarePuzzle.PATHS[b][n];
                    check(Math.abs(at[0] - previous[0]) + Math.abs(at[1] - previous[1]) == 1, "adjacent trace");
                    check(occupied.add(Arrays.toString(at)), "independent rails");
                    check(Integer.bitCount(FirmwarePuzzle.goalMask(b, n)) == 2, "two-pin trace");
                    previous = at;
                }
                int[] port = FirmwarePuzzle.PORTS[b];
                check(Math.abs(port[0] - previous[0]) + Math.abs(port[1] - previous[1]) == 1, "circuit port adjacency");
                check(FirmwarePuzzle.connected(b, new byte[25]), "solved route");
            }
            for (int seed = 0; seed < 30; seed++) {
                byte[] layout = FirmwarePuzzle.shuffled(seed, branches);
                check(Arrays.equals(layout, FirmwarePuzzle.shuffled(seed, branches)), "deterministic save layout");
                for (int b = 0; b < branches; b++)
                    check(!FirmwarePuzzle.connected(b, layout), "initial route needs work");
            }
        }
    }

    private static void recipes(
            WorldServer world, ItemStack raw, ItemStack programmed, FirmwareTarget target, FirmwareTarget wrong) {
        RecipeMap<SimpleRecipeBuilder> map =
                new RecipeMap<>("firmware_development_checks", 1, 1, 0, 0, new SimpleRecipeBuilder(), true);
        Recipe recipe = map.recipeBuilder().inputs(raw).outputs(target.icon).duration(20).EUt(30).build().getResult();
        map.compileRecipe(recipe);
        check(
                !recipe.matches(false, Collections.singletonList(raw), Collections.emptyList()),
                "machine rejects raw circuit");
        check(
                !recipe.matches(
                        false, Collections.singletonList(FirmwareData.program(raw, wrong)), Collections.emptyList()),
                "machine rejects wrong purpose");
        check(
                recipe.matches(false, Collections.singletonList(programmed), Collections.emptyList()),
                "machine accepts correct firmware");
        check(
                map.findRecipe(32, Collections.singletonList(programmed), Collections.emptyList()) == recipe,
                "NBT-aware GT lookup");
        Recipe circuitManufacturing =
                map.recipeBuilder().inputs(raw).outputs(raw).duration(20).EUt(30).build().getResult();
        check(
                circuitManufacturing.matches(false, Collections.singletonList(raw), Collections.emptyList()),
                "circuit manufacture exempt");
        Recipe component =
                map.recipeBuilder()
                        .inputs(raw)
                        .outputs(MetaItems.ROBOT_ARM_LV.getStackForm())
                        .duration(20)
                        .EUt(30)
                        .build()
                        .getResult();
        check(
                !component.matches(false, Collections.singletonList(raw), Collections.emptyList()),
                "component requires firmware");
        check(
                component.matches(
                        false,
                        Collections.singletonList(
                                FirmwareData.program(
                                        raw, FirmwareData.describe(MetaItems.ROBOT_ARM_LV.getStackForm()))),
                        Collections.emptyList()),
                "component firmware works");
        Container owner =
                new Container() {
                    @Override
                    public boolean canInteractWith(EntityPlayer player) {
                        return true;
                    }
                };
        InventoryCrafting grid = new InventoryCrafting(owner, 3, 3);
        ShapedOreRecipe shaped =
                new ShapedOreRecipe(new ResourceLocation("modid", "firmware_check"), target.icon, "C", 'C', raw);
        grid.setInventorySlotContents(0, raw.copy());
        check(!shaped.matches(grid, world), "modded crafting table rejects raw");
        grid.setInventorySlotContents(0, FirmwareData.program(raw, wrong));
        check(!shaped.matches(grid, world), "modded crafting table rejects wrong purpose");
        grid.setInventorySlotContents(0, programmed.copy());
        check(shaped.matches(grid, world), "modded crafting table accepts firmware");
        FirmwareRecipeMap copier = FirmwareSynthesisArray.RECIPES;
        FirmwareSelectors.Selection selection = null;
        Set<String> pairs = new HashSet<>();
        for (FirmwareSelectors.Selection entry : FirmwareSelectors.all()) {
            check(
                    pairs.add(entry.tier + "/" + entry.configuration + "/" + entry.wire.getRegistryName()),
                    "unique wire/configuration/tier");
            check(entry.configuration >= 1 && entry.configuration <= 32, "valid GT numbered selector");
            if (entry.tier == GTValues.LV && entry.target.key.equals(target.key)) selection = entry;
        }
        check(selection != null, "selector assigned for LV macerator");
        List<ItemStack> input = new ArrayList<>(Arrays.asList(selection.selector(), raw.copy(), selection.wireStack()));
        check(
                copier.findRecipe(GTValues.VA[GTValues.EV], input, Collections.emptyList()) == null,
                "automation locked below IV");
        Recipe copy = copier.findRecipe(GTValues.VA[GTValues.IV], input, Collections.emptyList());
        check(copy != null, "IV synthesis recipe found");
        check(copy.matches(true, input, Collections.emptyList()), "IV synthesis consumes valid input");
        check(
                input.get(0).getCount() == 1 && IntCircuitIngredient.isIntegratedCircuit(input.get(0)),
                "numbered selector retained");
        check(input.get(1).isEmpty() && input.get(2).isEmpty(), "blank and wire consumed");
        check(FirmwareData.target(copy.getOutputs().get(0)).equals(target.key), "synthesis selects exact purpose");
        check(copy.getPropertyCount() == 0, "firmware needs no cleanroom");
        check(
                copier.findRecipe(8192, Arrays.asList(programmed, raw, selection.wireStack()), Collections.emptyList())
                        == null,
                "firmware circuit cannot replace numbered selector");
        for (ItemStack variant : FirmwareCircuits.variants(GTValues.LV)) {
            Recipe alternate =
                    copier.findRecipe(
                            8192,
                            Arrays.asList(selection.selector(), variant.copy(), selection.wireStack()),
                            Collections.emptyList());
            check(
                    alternate != null && alternate.getOutputs().get(0).isItemEqual(variant),
                    "any tier variant accepted and preserved");
            if (!variant.isItemEqual(raw))
                check(
                        !copy.matches(
                                false,
                                Arrays.asList(selection.selector(), variant.copy(), selection.wireStack()),
                                Collections.emptyList()),
                        "cached recipe cannot transmute circuit variants");
        }
    }

    private static void bench(WorldServer world, ItemStack raw, FirmwareTarget target) {
        EntityPlayer player = FakePlayerFactory.getMinecraft(world);
        player.capabilities.isCreativeMode = false;
        FirmwareWorkbenchTile bench = new FirmwareWorkbenchTile();
        bench.setWorld(world);
        bench.setPos(new BlockPos(world.getSpawnPoint().getX(), 255, world.getSpawnPoint().getZ()));
        bench.items.setStackInSlot(0, raw.copy());
        bench.action(player, 0, 0, target.key);
        bench.action(player, 3, 0, "");
        check(bench.items.getStackInSlot(1).isEmpty(), "unsoldered board cannot flash");
        bench.rotations = new byte[25];
        bench.items.setStackInSlot(2, new ItemStack(FirmwareContent.SOLDERING_IRON));
        bench.items.setStackInSlot(3, new ItemStack(FirmwareContent.WIRE_STRIPPERS));
        ItemStack tin = FirmwareMaterials.solder();
        tin.setCount(3);
        bench.items.setStackInSlot(4, tin);
        for (int b = 0; b < bench.branches(); b++) bench.items.setStackInSlot(6 + b, bench.requirement(b));
        bench.action(player, 2, 0, "");
        check(bench.complete(0) && bench.items.getStackInSlot(6).isEmpty(), "terminal consumed on solder");
        check(bench.items.getStackInSlot(4).getCount() == 2, "one wire from the shared supply per branch");
        check(
                bench.items.getStackInSlot(2).getItemDamage() == 1
                        && bench.items.getStackInSlot(3).getItemDamage() == 1,
                "one durability per solder action");
        bench.action(player, 0, 0, FirmwareData.targetKey(MetaItems.ROBOT_ARM_LV.getStackForm()));
        check(bench.selectedTarget.equals(target.key), "purpose locked during soldering");
        NBTTagCompound saved = bench.writeToNBT(new NBTTagCompound());
        FirmwareWorkbenchTile restored = new FirmwareWorkbenchTile();
        restored.readFromNBT(saved);
        check(
                restored.soldered == bench.soldered
                        && restored.selectedTarget.equals(target.key)
                        && Arrays.equals(restored.rotations, bench.rotations),
                "board progress persists");
        player.capabilities.isCreativeMode = true;
        for (int b = 1; b < bench.branches(); b++) bench.action(player, 2, b, "");
        check(
                bench.items.getStackInSlot(2).getItemDamage() == 2
                        && bench.items.getStackInSlot(3).getItemDamage() == 2,
                "Creative tools still wear exactly once");
        player.capabilities.isCreativeMode = false;
        bench.items.setStackInSlot(4, ItemStack.EMPTY);
        bench.action(player, 3, 0, "");
        check(bench.items.getStackInSlot(1).isEmpty(), "flashing requires one wire left in the shared supply");
        bench.items.setStackInSlot(4, OreDictUnifier.get(OrePrefix.ingot, Materials.SolderingAlloy));
        bench.action(player, 3, 0, "");
        check(bench.items.getStackInSlot(1).isEmpty(), "whole solder ingots cannot flash a board");
        bench.items.setStackInSlot(4, FirmwareMaterials.solder());
        bench.action(player, 3, 0, "");
        check(FirmwareData.target(bench.items.getStackInSlot(1)).equals(target.key), "complete manual programming");
        check(
                bench.items.getStackInSlot(0).isEmpty()
                        && bench.items.getStackInSlot(4).isEmpty()
                        && bench.soldered == 0,
                "flash consumes circuit/alloy and resets");
    }

    private static void cachedCraftingLookup(WorldServer world, FirmwareTarget target, FirmwareTarget wrong) {
        IRecipe chosen = null;
        for (IRecipe recipe : CraftingManager.REGISTRY)
            if (recipe instanceof ShapedOreRecipe
                    && recipe.getRecipeOutput().isItemEqual(target.icon)
                    && recipe.getIngredients().size() == 9) {
                chosen = recipe;
                break;
            }
        check(chosen != null, "registered machine recipe available for crafting-cache checks");
        Container owner =
                new Container() {
                    @Override
                    public boolean canInteractWith(EntityPlayer player) {
                        return true;
                    }
                };
        InventoryCrafting grid = new InventoryCrafting(owner, 3, 3);
        for (int slot = 0; slot < 9; slot++) {
            ItemStack[] options = chosen.getIngredients().get(slot).getMatchingStacks();
            if (options.length > 0) grid.setInventorySlotContents(slot, options[0].copy());
        }
        for (int attempt = 0; attempt < 6; attempt++) {
            boolean correct = attempt % 3 == 0, raw = attempt % 3 == 1;
            for (int slot = 0; slot < 9; slot++)
                if (FirmwareData.isCircuit(grid.getStackInSlot(slot))) {
                    ItemStack circuit = FirmwareData.withoutFirmware(grid.getStackInSlot(slot));
                    grid.setInventorySlotContents(
                            slot, raw ? circuit : FirmwareData.program(circuit, correct ? target : wrong));
                }
            ItemStack output = CraftingManager.findMatchingResult(grid, world);
            check(
                    correct ? output.isItemEqual(target.icon) : output.isEmpty(),
                    "crafting result rechecks firmware when only NBT changes");
            IRecipe found = CraftingManager.findMatchingRecipe(grid, world);
            check(
                    correct ? found != null && found.getRecipeOutput().isItemEqual(target.icon) : found == null,
                    "cached recipe lookup cannot accept raw or wrong firmware");
            check(
                    CraftingManager.getRemainingItems(grid, world).size() == 9,
                    "remaining-item lookup supports circuit grids with the crafting cache");
        }
    }

    private static void erasing(WorldServer world, ItemStack programmed) {
        IRecipe erase = ForgeRegistries.RECIPES.getValue(new ResourceLocation("modid", "erase_firmware"));
        check(erase instanceof FirmwareEraseRecipe, "wipe recipe is registered");
        Container owner =
                new Container() {
                    @Override
                    public boolean canInteractWith(EntityPlayer player) {
                        return true;
                    }
                };
        for (int size : new int[] {2, 3})
            for (int slot = 0; slot < size * size; slot++) {
                InventoryCrafting grid = new InventoryCrafting(owner, size, size);
                ItemStack input = programmed.copy();
                input.setCount(12);
                NBTTagCompound other = new NBTTagCompound();
                other.setString("Data", "preserved");
                input.setTagInfo("OtherMod", other);
                grid.setInventorySlotContents(slot, input);
                check(erase.matches(grid, world), "wipe works in every crafting slot");
                ItemStack output = erase.getCraftingResult(grid);
                check(
                        !FirmwareData.configured(output) && output.isItemEqual(programmed) && output.getCount() == 1,
                        "wipe preserves item and returns one");
                check(
                        output.getSubCompound("OtherMod").getString("Data").equals("preserved"),
                        "wipe preserves other NBT");
                check(
                        FirmwareData.configured(input) && input.getCount() == 12,
                        "wipe preview does not mutate input stack");
                if (slot == 0)
                    check(
                            ItemStack.areItemStacksEqual(CraftingManager.findMatchingResult(grid, world), output),
                            "wipe works through normal crafting lookup");
                check(
                        erase.getRemainingItems(grid).stream().allMatch(ItemStack::isEmpty),
                        "wipe has no duplicate remainder");
                int extra = (slot + 1) % (size * size);
                grid.setInventorySlotContents(extra, programmed.copy());
                check(
                        !erase.matches(grid, world) && erase.getCraftingResult(grid).isEmpty(),
                        "wipe rejects extra occupied slots");
            }
        InventoryCrafting empty = new InventoryCrafting(owner, 2, 2);
        check(!erase.matches(empty, world), "empty grid cannot wipe");
        empty.setInventorySlotContents(0, MetaItems.INTEGRATED_CIRCUIT.getStackForm());
        check(!erase.matches(empty, world), "numbered GT circuit is unaffected");
        empty.setInventorySlotContents(0, FirmwareData.withoutFirmware(programmed));
        check(!erase.matches(empty, world), "raw circuits cannot be wiped again");
    }

    private static void wireEconomy(WorldServer world, FirmwareTarget target) {
        check(
                FirmwareContent.SOLDERING_IRON.getMaxDamage() == 320
                        && FirmwareContent.WIRE_STRIPPERS.getMaxDamage() == 320,
                "both precision tools have 320 durability");
        for (boolean alloy : new boolean[] {true, false}) {
            int boards = alloy ? 64 : 32, wear = alloy ? 1 : 2;
            ItemStack supply = alloy ? FirmwareMaterials.solder() : FirmwareMaterials.tinWire();
            FirmwareWorkbenchTile bench = new FirmwareWorkbenchTile();
            bench.setWorld(world);
            bench.setPos(world.getSpawnPoint().up(100));
            bench.items.setStackInSlot(2, new ItemStack(FirmwareContent.SOLDERING_IRON));
            bench.items.setStackInSlot(3, new ItemStack(FirmwareContent.WIRE_STRIPPERS));
            EntityPlayer player = FakePlayerFactory.getMinecraft(world);
            for (int board = 0; board < boards; board++) {
                bench.items.setStackInSlot(0, FirmwareCircuits.variants(GTValues.ZPM).get(0).copy());
                bench.action(player, 0, 0, target.key);
                bench.rotations = new byte[25];
                ItemStack feed = supply.copy();
                feed.setCount(6);
                bench.items.setStackInSlot(4, feed);
                check(
                        bench.accepts(4, supply) && !bench.accepts(5, supply),
                        "one supply slot accepts either wire; legacy slot rejects insertion");
                for (int branch = 0; branch < 5; branch++) {
                    bench.items.setStackInSlot(6 + branch, bench.requirement(branch));
                    bench.action(player, 2, branch, "");
                    check(bench.complete(branch), "soldered branch using " + (alloy ? "alloy" : "tin"));
                }
                if (board + 1 < boards)
                    check(
                            bench.items.getStackInSlot(2).getItemDamage() == (board + 1) * 5 * wear,
                            "wire-specific durability accumulates per branch");
                bench.action(player, 3, 0, "");
                check(
                        FirmwareData.configured(bench.items.getStackInSlot(1)),
                        "finished board can flash using either wire");
                bench.items.setStackInSlot(1, ItemStack.EMPTY);
            }
            check(
                    bench.items.getStackInSlot(2).isEmpty() && bench.items.getStackInSlot(3).isEmpty(),
                    "one tool set completes exactly " + boards + " five-branch boards");
        }
    }

    private static void singleSupplyMigration(WorldServer world) {
        for (int scenario = 0; scenario < 3; scenario++) {
            FirmwareWorkbenchTile old = new FirmwareWorkbenchTile();
            ItemStack alloy = FirmwareMaterials.solder();
            alloy.setCount(scenario == 0 ? 40 : 8);
            ItemStack legacy = scenario == 0 ? FirmwareMaterials.solder() : FirmwareMaterials.tinWire();
            legacy.setCount(scenario == 0 ? 40 : 12);
            if (scenario != 2) old.items.setStackInSlot(4, alloy);
            old.items.setStackInSlot(5, legacy);
            FirmwareWorkbenchTile migrated = new FirmwareWorkbenchTile();
            migrated.readFromNBT(old.writeToNBT(new NBTTagCompound()));
            if (scenario == 0)
                check(
                        migrated.items.getStackInSlot(4).getCount() == 64
                                && migrated.items.getStackInSlot(5).getCount() == 16,
                        "legacy same-wire stacks merge without losing overflow");
            if (scenario == 1)
                check(
                        migrated.items.getStackInSlot(4).getCount() == 8
                                && migrated.items.getStackInSlot(5).getCount() == 12,
                        "legacy mixed wires remain distinct until recovery");
            if (scenario == 2)
                check(
                        migrated.items.getStackInSlot(4).getCount() == 12 && migrated.items.getStackInSlot(5).isEmpty(),
                        "old feed-only inventory migrates to the single supply");
            migrated.setWorld(world);
            migrated.setPos(world.getSpawnPoint().up(100));
            EntityPlayer fake =
                    FakePlayerFactory.get(world, new com.mojang.authlib.GameProfile(UUID.randomUUID(), "[Wire Test]"));
            FirmwareContainer container = new FirmwareContainer(fake.inventory, migrated);
            check(container.inventorySlots.size() == 46, "container exposes ten bench slots and 36 player slots");
            check(migrated.items.getStackInSlot(5).isEmpty(), "legacy overflow returned on opening the bench");
            int recovered = 0;
            for (ItemStack stack : fake.inventory.mainInventory)
                if (FirmwareMaterials.isSolderWire(stack)) recovered += stack.getCount();
            check(
                    recovered == (scenario == 0 ? 16 : scenario == 1 ? 12 : 0),
                    "all legacy overflow recovered without loss");
            if (scenario == 2) {
                ItemStack refill = FirmwareMaterials.tinWire();
                refill.setCount(4);
                fake.inventory.setInventorySlotContents(0, refill);
                container.transferStackInSlot(fake, FirmwareContainer.BENCH_SLOTS + 27);
                check(
                        migrated.items.getStackInSlot(4).getCount() == 16 && fake.inventory.getStackInSlot(0).isEmpty(),
                        "shift-click routes wire into the one visible supply");
                ItemStack other = FirmwareMaterials.solder();
                fake.inventory.setInventorySlotContents(0, other);
                check(
                        container.transferStackInSlot(fake, FirmwareContainer.BENCH_SLOTS + 27).isEmpty()
                                && migrated.items.getStackInSlot(4).getCount() == 16,
                        "a second wire type cannot enter the occupied supply");
            }
        }
    }

    private static void addonCircuitsAndSupreme(WorldServer world) {
        ItemStack mainframe = new ItemStack(ModItems.epsteiniumInfusedWetwareMainframe);
        check(FirmwareData.tier(mainframe) == GTValues.UEV, "Epsteinium mainframe is a UEV circuit");
        check(
                FirmwareCircuits.variants(GTValues.UEV).stream().anyMatch(s -> s.isItemEqual(mainframe)),
                "Epsteinium mainframe participates in GT circuit-tier choices");
        FirmwareTarget target =
                FirmwareTargets.get(FirmwareData.targetKey(MachineRegistry.SUPREME_CIRCUIT_ASSEMBLER.getStackForm()));
        check(
                target != null && target.circuitTiers.containsKey(GTValues.UEV),
                "Supreme circuit assembler requests UEV firmware");
        Container owner =
                new Container() {
                    @Override
                    public boolean canInteractWith(EntityPlayer p) {
                        return true;
                    }
                };
        InventoryCrafting grid = new InventoryCrafting(owner, 3, 3);
        ShapedOreRecipe example =
                new ShapedOreRecipe(
                        new ResourceLocation("modid", "mainframe_firmware_check"),
                        MachineRegistry.SUPREME_CIRCUIT_ASSEMBLER.getStackForm(),
                        "C",
                        'C',
                        mainframe);
        grid.setInventorySlotContents(0, mainframe.copy());
        check(!example.matches(grid, world), "raw Epsteinium mainframe cannot bypass firmware");
        grid.setInventorySlotContents(0, FirmwareData.program(mainframe, target));
        check(example.matches(grid, world), "configured Epsteinium mainframe satisfies machine firmware");
        erasing(world, FirmwareData.program(mainframe, target));
        for (java.lang.reflect.Field field : MachineRegistry.class.getFields()) {
            if (!field.getName().startsWith("SUPREME_")) continue;
            try {
                MetaTileEntity machine = (MetaTileEntity) field.get(null);
                IRecipe found = null;
                for (IRecipe recipe : ForgeRegistries.RECIPES)
                    if (recipe.getRecipeOutput().isItemEqual(machine.getStackForm())) {
                        found = recipe;
                        break;
                    }
                check(found != null, "Supreme crafting recipe exists: " + field.getName());
                for (Ingredient ingredient : found.getIngredients())
                    if (ingredient != Ingredient.EMPTY)
                        check(
                                ingredient.getMatchingStacks().length > 0,
                                "Supreme ingredient resolves: " + field.getName());
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private static void zpmProgramming(WorldServer world, FirmwareTarget target) {
        ItemStack wire = FirmwareMaterials.wire(GTValues.ZPM);
        check(!wire.isEmpty(), "Fine Osmium Wire exists for ZPM boards");
        check(OreDictUnifier.getUnificationEntry(wire).material == Materials.Osmium, "ZPM terminal remains Osmium");
        check(
                RecipeMaps.WIREMILL_RECIPES.getRecipeList().stream()
                        .anyMatch(recipe -> recipe.getOutputs().stream().anyMatch(output -> output.isItemEqual(wire))),
                "Fine Osmium Wire has a Wiremill recipe");
        boolean handRecipe = false;
        for (IRecipe recipe : ForgeRegistries.RECIPES)
            if (recipe.getRecipeOutput().isItemEqual(wire)) handRecipe = true;
        check(handRecipe, "Fine Osmium Wire has a crafting recipe");
        FirmwareWorkbenchTile bench = new FirmwareWorkbenchTile();
        bench.setWorld(world);
        bench.setPos(new BlockPos(world.getSpawnPoint().getX(), 255, world.getSpawnPoint().getZ()));
        ItemStack raw = FirmwareCircuits.variants(GTValues.ZPM).get(0).copy();
        EntityPlayer player = FakePlayerFactory.getMinecraft(world);
        bench.items.setStackInSlot(0, raw);
        bench.action(player, 0, 0, target.key);
        check(bench.branches() == 5, "ZPM board retains five branches");
        bench.items.setStackInSlot(2, new ItemStack(FirmwareContent.SOLDERING_IRON));
        bench.items.setStackInSlot(3, new ItemStack(FirmwareContent.WIRE_STRIPPERS));
        ItemStack feed = FirmwareMaterials.solder();
        feed.setCount(6);
        bench.items.setStackInSlot(4, feed);
        bench.rotations = new byte[25];
        for (int branch = 0; branch < 5; branch++) {
            ItemStack required = bench.requirement(branch);
            check(!required.isEmpty(), "ZPM branch " + branch + " has an obtainable component");
            bench.items.setStackInSlot(6 + branch, required);
            bench.action(player, 2, branch, "");
            check(bench.complete(branch), "ZPM branch " + branch + " solders successfully");
        }
        check(
                bench.items.getStackInSlot(2).getItemDamage() == 5
                        && bench.items.getStackInSlot(3).getItemDamage() == 5,
                "ZPM board consumes exactly five durability per tool");
        bench.action(player, 3, 0, "");
        ItemStack output = bench.items.getStackInSlot(1);
        check(
                output.isItemEqual(raw) && FirmwareData.target(output).equals(target.key),
                "ZPM board flashes with its original circuit intact");
    }

    private static void crossTierAndTools(WorldServer world, FirmwareTarget lowTierTarget) {
        List<ItemStack> variants = FirmwareCircuits.variants(GTValues.UHV);
        check(!variants.isEmpty(), "UHV circuit exists for cross-tier check");
        FirmwareWorkbenchTile bench = new FirmwareWorkbenchTile();
        bench.setWorld(world);
        bench.setPos(new BlockPos(world.getSpawnPoint().getX(), 255, world.getSpawnPoint().getZ()));
        EntityPlayer player = FakePlayerFactory.getMinecraft(world);
        player.capabilities.isCreativeMode = true;
        bench.items.setStackInSlot(0, variants.get(0).copy());
        bench.action(player, 0, 0, lowTierTarget.key);
        check(bench.selectedTarget.equals(lowTierTarget.key), "low-tier firmware can be selected for UHV circuit");
        bench.items.setStackInSlot(2, new ItemStack(FirmwareContent.SOLDERING_IRON));
        bench.items.setStackInSlot(3, new ItemStack(FirmwareContent.WIRE_STRIPPERS));
        ItemStack tin = FirmwareMaterials.solder();
        tin.setCount(6);
        bench.items.setStackInSlot(4, tin);
        bench.rotations = new byte[25];
        for (int branch = 0; branch < bench.branches(); branch++) {
            bench.items.setStackInSlot(6 + branch, bench.requirement(branch));
            bench.action(player, 2, branch, "");
        }
        check(
                bench.items.getStackInSlot(2).getItemDamage() == 5
                        && bench.items.getStackInSlot(3).getItemDamage() == 5,
                "five branches use five durability");
        bench.action(player, 3, 0, "");
        check(
                FirmwareData.target(bench.items.getStackInSlot(1)).equals(lowTierTarget.key)
                        && bench.items.getStackInSlot(1).isItemEqual(variants.get(0)),
                "cross-tier firmware flashing is preserved");
        bench.items.setStackInSlot(0, variants.get(0).copy());
        bench.rotations = new byte[25];
        ItemStack iron = new ItemStack(FirmwareContent.SOLDERING_IRON);
        iron.setItemDamage(iron.getMaxDamage() - 1);
        ItemStack strippers = new ItemStack(FirmwareContent.WIRE_STRIPPERS);
        strippers.setItemDamage(strippers.getMaxDamage() - 1);
        bench.items.setStackInSlot(2, iron);
        bench.items.setStackInSlot(3, strippers);
        ItemStack feed = FirmwareMaterials.solder();
        feed.setCount(2);
        bench.items.setStackInSlot(4, feed);
        bench.items.setStackInSlot(6, bench.requirement(0));
        bench.action(player, 2, 0, "");
        check(
                bench.complete(0) && bench.items.getStackInSlot(2).isEmpty() && bench.items.getStackInSlot(3).isEmpty(),
                "tools break on their final use");
        bench.items.setStackInSlot(7, bench.requirement(1));
        bench.action(player, 2, 1, "");
        check(
                !bench.complete(1)
                        && !bench.items.getStackInSlot(7).isEmpty()
                        && bench.items.getStackInSlot(4).getCount() == 1,
                "failed soldering consumes neither parts nor durability");
        player.capabilities.isCreativeMode = false;
        check(FirmwareMachines.SYNTHESIS_ARRAY.hasMaintenanceMechanics(), "array maintenance is enabled");
    }

    private static void wireLoss() {
        Material omnium = ModMaterialTweaks.findOmnium();
        check(omnium != null, "Omnium exception exists");
        int materials = 0, variants = 0;
        for (Material material : MaterialRegistryManager.getInstance().getRegisteredMaterials()) {
            WireProperties wire = material.getProperty(PropertyKey.WIRE);
            if (wire == null) continue;
            materials++;
            check(
                    material == omnium
                            ? wire.isSuperconductor() && wire.getLossPerBlock() == 0
                            : !wire.isSuperconductor()
                                    && wire.getLossPerBlock() >= 1
                                    && wire.getSuperconductorCriticalTemperature() == 0,
                    "material loss and superconducting flag: " + material);
        }
        for (BlockCable[] types : MetaBlocks.CABLES.values())
            for (BlockCable cable : types)
                for (Material material : cable.getEnabledMaterials()) {
                    Insulation type = cable.getItemPipeType(cable.getItem(material));
                    WireProperties actual = cable.createItemProperties(cable.getItem(material));
                    WireProperties base = material.getProperty(PropertyKey.WIRE);
                    check(
                            material == omnium
                                    ? actual.isSuperconductor() && actual.getLossPerBlock() == 0
                                    : !actual.isSuperconductor() && actual.getLossPerBlock() >= 1,
                            "actual wire/cable loss: " + material + " / " + type);
                    check(
                            actual.getVoltage() == base.getVoltage()
                                    && actual.getAmperage() == base.getAmperage() * type.amperage,
                            "wire/cable voltage and amperage preserved");
                    variants++;
                }
        check(materials > 0 && variants > 0, "tested registered conductor materials and generated forms");
        MimsAdditions.LOGGER.info(
                "WIRE LOSS VERIFIED: {} materials, {} wire/cable variants; only Omnium is lossless",
                materials,
                variants);
    }

    private static void energyHatchLimits() {
        for (MultiblockAbility<?> ability :
                new MultiblockAbility<?>[] {
                    MultiblockAbility.INPUT_ENERGY,
                    MultiblockAbility.OUTPUT_ENERGY,
                    MultiblockAbility.SUBSTATION_INPUT_ENERGY,
                    MultiblockAbility.SUBSTATION_OUTPUT_ENERGY
                }) {
            TraceabilityPredicate limit =
                    MultiblockControllerBase.abilities(ability).setMinGlobalLimited(1).setMaxGlobalLimited(2);
            check(
                    !limit.limited.isEmpty() && limit.limited.stream().allMatch(p -> p.maxGlobalCount == 2),
                    "two-hatch maximum enforced");
            TraceabilityPredicate preview = MultiblockControllerBase.abilities(ability).setMaxGlobalLimited(2, 1);
            check(
                    !preview.limited.isEmpty()
                            && preview.limited.stream().allMatch(p -> p.maxGlobalCount == 2 && p.previewCount == 1),
                    "preview overload preserves the hatch limit");
            limit.setMaxGlobalLimited(1);
            check(
                    limit.limited.stream().allMatch(p -> p.maxGlobalCount == 1),
                    "stricter native structure limits preserved");
        }
        MimsAdditions.LOGGER.info("ENERGY HATCH LIMIT VERIFIED: native two-hatch maximum restored");
    }

    private static void primitiveSteelTiming() {
        int checked = 0;
        double fastest = Double.POSITIVE_INFINITY, slowest = 0;
        for (Recipe recipe : RecipeMaps.PRIMITIVE_BLAST_FURNACE_RECIPES.getRecipeList()) {
            long amount = PrimitiveSteelRecipes.steelAmount(recipe);
            if (amount <= 0) continue;
            double ticks = recipe.getDuration() * (double) GTValues.M / amount;
            check(ticks >= 9600 && ticks <= 11400, "every PBF steel recipe takes 8–9.5 minutes per ingot");
            fastest = Math.min(fastest, ticks);
            slowest = Math.max(slowest, ticks);
            checked++;
        }
        check(
                checked > 0 && fastest == 9600 && slowest == 11400,
                "fastest and slowest PBF steel recipes reach both requested endpoints");
        Recipe pair =
                RecipeMaps.PRIMITIVE_BLAST_FURNACE_RECIPES
                        .recipeBuilder()
                        .input(OrePrefix.ingot, Materials.WroughtIron, 2)
                        .input(OrePrefix.dust, Materials.Coke)
                        .output(OrePrefix.ingot, Materials.Steel, 2)
                        .duration(600)
                        .build()
                        .getResult();
        check(PrimitiveSteelRecipes.targetDuration(pair) == 19200, "pack's two-ingot coke batch takes 16 minutes");
        Recipe blocks =
                RecipeMaps.PRIMITIVE_BLAST_FURNACE_RECIPES
                        .recipeBuilder()
                        .input(OrePrefix.block, Materials.WroughtIron, 2)
                        .input(OrePrefix.block, Materials.Coke)
                        .output(OrePrefix.block, Materials.Steel, 2)
                        .duration(5400)
                        .build()
                        .getResult();
        check(PrimitiveSteelRecipes.targetDuration(blocks) == 172800, "two-block coke batch scales to 18 ingots");
        PrimitiveSteelRecipes.enforce();
        check(
                RecipeMaps.PRIMITIVE_BLAST_FURNACE_RECIPES.getRecipeList().stream()
                                .filter(r -> PrimitiveSteelRecipes.steelAmount(r) > 0)
                                .count()
                        == checked,
                "repeated timing enforcement does not duplicate recipes");
        MimsAdditions.LOGGER.info(
                "PBF STEEL TIMING VERIFIED: {} recipes, 8–9.5 min/ingot, multi-output batches scaled", checked);
    }

    private static void structureAndParts() {
        int coils = 0, memories = 0, controllers = 0;
        String[][] aisles = FirmwareSynthesisArray.AISLES;
        check(aisles.length == 9, "nine-deep chamber");
        for (int z = 0; z < 9; z++)
            for (int y = 0; y < 5; y++) {
                check(aisles[z][y].length() == 7, "seven-wide chamber");
                for (int x = 0; x < 7; x++) {
                    char block = aisles[z][y].charAt(x);
                    if (x == 0 || x == 6 || y == 0 || y == 4 || z == 0 || z == 8)
                        check(block != '#', "no exterior gaps");
                    if (block == 'K') coils++;
                    if (block == 'R') memories++;
                    if (block == 'S') controllers++;
                }
            }
        check(coils == 24 && memories == 16 && controllers == 1, "three coil rings and sixteen memory banks");
        check(
                !FirmwareData.isCircuit(MetaItems.ROBOT_ARM_IV.getStackForm()),
                "robot arm is not an unconfigured circuit");
        check(!FirmwareData.isCircuit(MetaItems.EMITTER_IV.getStackForm()), "emitter is not an unconfigured circuit");
        check(
                !FirmwareData.isCircuit(MetaItems.FIELD_GENERATOR_IV.getStackForm()),
                "field generator is not an unconfigured circuit");
        for (OrePrefix form :
                new OrePrefix[] {
                    OrePrefix.dust, OrePrefix.ingot,
                    OrePrefix.wireFine, OrePrefix.wireGtDouble
                }) {
            check(!OreDictUnifier.get(form, FirmwareAlloy.INCONEL_600).isEmpty(), "generated alloy form " + form);
        }
        check(FirmwareAlloy.INCONEL_600.getFluid() != null, "alloy molten form");
        for (Insulation type : Insulation.VALUES) {
            BlockCable cable = MetaBlocks.CABLES.get("modid")[type.ordinal()];
            ItemStack wire = cable.getItem(FirmwareAlloy.INCONEL_600);
            check(wire.getItem() instanceof ItemBlockCable, "native GT electrical wire/cable " + type);
            check(
                    cable.getEnabledMaterials().contains(FirmwareAlloy.INCONEL_600),
                    "conductive material enabled " + type);
            check(
                    cable.getItemMaterial(wire).getMaterialRGB() == FirmwareAlloy.INCONEL_600.getMaterialRGB(),
                    "wire and ingot share tint " + type);
            check(
                    cable.createItemProperties(wire).getAmperage() == type.amperage,
                    "native amperage multiplier " + type);
        }
        for (ItemStack output :
                Arrays.asList(
                        MetaTileEntities.MACERATOR[GTValues.LV].getStackForm(),
                        MetaItems.ROBOT_ARM_LV.getStackForm())) {
            ItemStack raw = MetaItems.ELECTRONIC_CIRCUIT_LV.getStackForm();
            List<List<ItemStack>> ingredients = Collections.singletonList(Collections.singletonList(raw));
            ItemStack display = FirmwareDisplayIngredients.forOutput(output, ingredients).get(0).get(0);
            check(
                    FirmwareData.target(display).equals(FirmwareData.targetKey(output)),
                    "crafting-grid JEI circuit has exact output firmware");
            check(!FirmwareData.configured(raw), "display does not mutate registered circuit ingredients");
        }
        boolean absRecipe = false;
        for (Recipe recipe : RecipeMap.getByName("alloy_blast_smelter").getRecipeList()) {
            for (FluidStack fluid : recipe.getFluidOutputs()) {
                if (fluid.isFluidEqual(FirmwareAlloy.molten(1))) absRecipe = true;
                check(
                        !fluid.isFluidEqual(FirmwareAlloy.INCONEL_600.getFluid(1)),
                        "ABS does not produce liquid Inconel");
            }
        }
        check(absRecipe, "Inconel production registered in ABS");
        check(
                !FirmwareAlloy.molten(1).isFluidEqual(FirmwareAlloy.INCONEL_600.getFluid(1)),
                "separate molten and liquid alloy states");
        boolean extractsLiquid = false;
        for (Recipe recipe : RecipeMaps.EXTRACTOR_RECIPES.getRecipeList()) {
            for (FluidStack fluid : recipe.getFluidOutputs()) {
                check(!fluid.isFluidEqual(FirmwareAlloy.molten(1)), "extractor never produces molten Inconel");
                if (fluid.isFluidEqual(FirmwareAlloy.INCONEL_600.getFluid(1))) extractsLiquid = true;
            }
        }
        check(extractsLiquid, "alloy extraction produces liquid Inconel");
        for (Recipe recipe : RecipeMaps.MIXER_RECIPES.getRecipeList()) {
            for (ItemStack output : recipe.getOutputs()) {
                UnificationEntry entry = OreDictUnifier.getUnificationEntry(output);
                check(entry == null || entry.material != FirmwareAlloy.INCONEL_600, "no Inconel mixer route");
            }
        }
        boolean casingBatch = false;
        for (Recipe recipe : RecipeMaps.ASSEMBLER_RECIPES.getRecipeList()) {
            for (ItemStack output : recipe.getOutputs())
                if (output.isItemEqual(new ItemStack(FirmwareContent.LOGIC_CASING))) {
                    check(output.getCount() == 4, "four logic casings per batch");
                    casingBatch = true;
                }
        }
        check(casingBatch, "logic casing assembler recipe exists");
        check(!FirmwareMachines.SYNTHESIS_ARRAY.getMatchingShapes().isEmpty(), "complete multiblock preview builds");
        FirmwareTarget array =
                FirmwareTargets.get(FirmwareData.targetKey(FirmwareMachines.SYNTHESIS_ARRAY.getStackForm()));
        check(
                array.circuitTiers.containsKey(GTValues.IV) && !array.circuitTiers.containsKey(GTValues.EV),
                "array controller requires IV circuits");
        Recipe controllerRecipe = null;
        for (Recipe candidate : RecipeMaps.ASSEMBLER_RECIPES.getRecipeList()) {
            if (!candidate.getOutputs().isEmpty() && candidate.getOutputs().get(0).isItemEqual(array.icon))
                controllerRecipe = candidate;
        }
        check(controllerRecipe != null, "controller is an assembler recipe");
        List<ItemStack> inputs = new ArrayList<>();
        int circuitSlot = -1;
        for (GTRecipeInput ingredient : controllerRecipe.getInputs()) {
            ItemStack stack = ingredient.getInputStacks()[0].copy();
            stack.setCount(ingredient.getAmount());
            if (FirmwareData.isCircuit(stack)) circuitSlot = inputs.size();
            inputs.add(stack);
        }
        List<FluidStack> fluids = new ArrayList<>();
        for (GTRecipeInput ingredient : controllerRecipe.getFluidInputs())
            fluids.add(ingredient.getInputFluidStack().copy());
        check(circuitSlot >= 0, "array uses IV circuits");
        check(!controllerRecipe.matches(false, inputs, fluids), "array rejects unconfigured IV circuits");
        for (ItemStack variant : FirmwareCircuits.variants(GTValues.IV)) {
            ItemStack configured = FirmwareData.program(variant, array);
            configured.setCount(4);
            inputs.set(circuitSlot, configured);
            check(
                    controllerRecipe.matches(false, inputs, fluids),
                    "array accepts every IV circuit implementation with its own firmware");
        }
    }
}
