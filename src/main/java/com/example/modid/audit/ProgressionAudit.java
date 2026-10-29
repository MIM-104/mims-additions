package com.example.modid.audit;

import com.example.modid.MachineRegistry;
import com.example.modid.MimsAdditions;
import com.example.modid.firmware.*;
import com.google.gson.*;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.common.metatileentities.MetaTileEntities;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Opt-in snapshot of the effective, post-script recipe registry. Never runs during normal play. */
public final class ProgressionAudit {
    public static volatile boolean finished;
    private static final JsonObject items = new JsonObject();
    private static final JsonArray recipes = new JsonArray(), errors = new JsonArray(), providers = new JsonArray();

    private ProgressionAudit() {}

    private static String key(ItemStack stack) {
        if (stack.isEmpty()) return "EMPTY";
        String base =
                stack.getItem().getRegistryName() + "@" + (stack.isItemStackDamageable() ? 0 : stack.getMetadata());
        if (FirmwareData.configured(stack)) base += "#fw:" + FirmwareData.target(stack);
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("Configuration"))
            base += "#selector:" + stack.getTagCompound().getInteger("Configuration");
        if (!items.has(base)) {
            JsonObject info = new JsonObject();
            info.addProperty("name", stack.getDisplayName());
            info.addProperty("tier", FirmwareData.tier(stack));
            if (stack.hasTagCompound()) info.addProperty("exampleNBT", stack.getTagCompound().toString());
            JsonArray ores = new JsonArray();
            for (int ore : OreDictionary.getOreIDs(stack)) ores.add(OreDictionary.getOreName(ore));
            info.add("ores", ores);
            items.add(base, info);
        }
        return base;
    }

    private static JsonObject output(ItemStack stack) {
        JsonObject out = new JsonObject();
        out.addProperty("key", key(stack));
        out.addProperty("count", stack.getCount());
        return out;
    }

    private static JsonObject fluid(FluidStack stack) {
        JsonObject out = new JsonObject();
        out.addProperty("key", "fluid:" + stack.getFluid().getName());
        out.addProperty("count", stack.amount);
        return out;
    }

    private static JsonObject input(Collection<ItemStack> choices, int count, boolean catalyst, ItemStack result) {
        JsonObject in = new JsonObject();
        JsonArray options = new JsonArray();
        Set<String> seen = new LinkedHashSet<>();
        boolean firmware = !catalyst && FirmwareData.requiresFirmware(result);
        for (ItemStack stack : choices) {
            if (stack.isEmpty()) continue;
            if (firmware && FirmwareData.isCircuit(stack))
                stack = FirmwareData.program(stack, FirmwareData.describe(result));
            String name = key(stack);
            if (seen.add(name)) options.add(name);
        }
        in.add("options", options);
        in.addProperty("count", count);
        in.addProperty("catalyst", catalyst);
        return in;
    }

    private static JsonObject row(String id, String type, JsonArray inputs, JsonArray outputs) {
        JsonObject row = new JsonObject();
        row.addProperty("id", id);
        row.addProperty("type", type);
        row.add("inputs", inputs);
        row.add("outputs", outputs);
        return row;
    }

    private static void crafting(IRecipe recipe, String type, String id) {
        ItemStack result = recipe.getRecipeOutput();
        if (result.isEmpty()) return;
        JsonArray inputs = new JsonArray(), outputs = new JsonArray();
        for (Ingredient ingredient : recipe.getIngredients())
            if (ingredient != Ingredient.EMPTY)
                inputs.add(input(Arrays.asList(ingredient.getMatchingStacks()), 1, false, result));
        outputs.add(output(result));
        JsonObject row = row(id, type, inputs, outputs);
        row.addProperty("class", recipe.getClass().getName());
        row.addProperty("dynamic", recipe.isDynamic());
        recipes.add(row);
    }

    private static void gt(RecipeMap<?> map, Recipe recipe, String id) {
        JsonArray inputs = new JsonArray(), outputs = new JsonArray();
        ItemStack result = recipe.getOutputs().isEmpty() ? ItemStack.EMPTY : recipe.getOutputs().get(0);
        for (GTRecipeInput ingredient : recipe.getInputs())
            inputs.add(
                    input(
                            Arrays.asList(ingredient.getInputStacks()),
                            ingredient.getAmount(),
                            ingredient.isNonConsumable(),
                            result));
        for (GTRecipeInput ingredient : recipe.getFluidInputs()) {
            JsonObject in = new JsonObject();
            JsonArray options = new JsonArray();
            FluidStack stack = ingredient.getInputFluidStack();
            if (stack != null) options.add("fluid:" + stack.getFluid().getName());
            in.add("options", options);
            in.addProperty("count", ingredient.getAmount());
            in.addProperty("catalyst", ingredient.isNonConsumable());
            inputs.add(in);
        }
        for (ItemStack stack : recipe.getOutputs()) outputs.add(output(stack));
        for (FluidStack stack : recipe.getFluidOutputs()) outputs.add(fluid(stack));
        recipe.getChancedOutputs()
                .getChancedEntries()
                .forEach(
                        chance -> {
                            JsonObject out = output(chance.getIngredient());
                            out.addProperty("chance", chance.getChance());
                            outputs.add(out);
                        });
        recipe.getChancedFluidOutputs()
                .getChancedEntries()
                .forEach(
                        chance -> {
                            JsonObject out = fluid(chance.getIngredient());
                            out.addProperty("chance", chance.getChance());
                            outputs.add(out);
                        });
        JsonObject row = row(id, "gt:" + map.unlocalizedName, inputs, outputs);
        row.addProperty("eut", recipe.getEUt());
        row.addProperty("ticks", recipe.getDuration());
        row.addProperty("eu", (long) recipe.getEUt() * recipe.getDuration());
        JsonObject props = new JsonObject();
        for (String property : recipe.getPropertyKeys())
            props.addProperty(property, String.valueOf(recipe.getPropertyRaw(property)));
        row.add("properties", props);
        recipes.add(row);
    }

    private static void providers(Class<?> owner) {
        Set<MetaTileEntity> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Field field : owner.getFields())
            if (Modifier.isStatic(field.getModifiers()))
                try {
                    Object value = field.get(null);
                    List<Object> entries = new ArrayList<>();
                    if (value instanceof Object[]) entries.addAll(Arrays.asList((Object[]) value));
                    else entries.add(value);
                    for (Object entry : entries)
                        if (entry instanceof MetaTileEntity && seen.add((MetaTileEntity) entry)) {
                            MetaTileEntity machine = (MetaTileEntity) entry;
                            JsonObject p = new JsonObject();
                            p.addProperty("item", key(machine.getStackForm()));
                            p.addProperty("machine", machine.metaTileEntityId.toString());
                            p.addProperty("class", entry.getClass().getName());
                            try {
                                p.addProperty(
                                        "tier",
                                        ((Number) entry.getClass().getMethod("getTier").invoke(entry)).intValue());
                            } catch (Exception ignored) {
                            }
                            JsonArray maps = new JsonArray();
                            try {
                                Object map = entry.getClass().getMethod("getRecipeMap").invoke(entry);
                                if (map instanceof RecipeMap) maps.add(((RecipeMap<?>) map).unlocalizedName);
                            } catch (Exception ignored) {
                            }
                            try {
                                Object[] all = (Object[]) entry.getClass().getMethod("getRecipeMaps").invoke(entry);
                                for (Object map : all)
                                    if (map instanceof RecipeMap) maps.add(((RecipeMap<?>) map).unlocalizedName);
                            } catch (Exception ignored) {
                            }
                            p.add("maps", maps);
                            providers.add(p);
                        }
                } catch (Exception error) {
                    errors.add(owner.getName() + "." + field.getName() + ": " + error);
                }
    }

    private static void avaritia() throws Exception {
        Class<?> manager = Class.forName("morph.avaritia.recipe.AvaritiaRecipeManager");
        for (String field : new String[] {"EXTREME_RECIPES", "COMPRESSOR_RECIPES"}) {
            Map<?, ?> registry = (Map<?, ?>) manager.getField(field).get(null);
            for (Map.Entry<?, ?> entry : registry.entrySet()) {
                Object recipe = entry.getValue();
                Class<?> type = recipe.getClass();
                boolean compressor = field.equals("COMPRESSOR_RECIPES");
                ItemStack result =
                        (ItemStack) type.getMethod(compressor ? "getResult" : "getRecipeOutput").invoke(recipe);
                List<?> ingredients = (List<?>) type.getMethod("getIngredients").invoke(recipe);
                JsonArray ins = new JsonArray(), outs = new JsonArray();
                List<ItemStack> compressorOptions = new ArrayList<>();
                for (Object ingredient : ingredients)
                    if (ingredient instanceof Ingredient && ingredient != Ingredient.EMPTY) {
                        List<ItemStack> choices = Arrays.asList(((Ingredient) ingredient).getMatchingStacks());
                        if (compressor) compressorOptions.addAll(choices);
                        else ins.add(input(choices, 1, false, result));
                    }
                if (compressor)
                    ins.add(
                            input(
                                    compressorOptions,
                                    ((Number) type.getMethod("getCost").invoke(recipe)).intValue(),
                                    false,
                                    result));
                outs.add(output(result));
                recipes.add(
                        row(
                                String.valueOf(entry.getKey()),
                                compressor ? "avaritia_compressor" : "avaritia_extreme",
                                ins,
                                outs));
            }
        }
    }

    public static void dump() {
        try {
            for (String name : OreDictionary.getOreNames())
                for (ItemStack stack : OreDictionary.getOres(name)) key(stack);
            for (IRecipe recipe : ForgeRegistries.RECIPES)
                try {
                    crafting(recipe, "crafting", String.valueOf(recipe.getRegistryName()));
                } catch (Exception error) {
                    errors.add("crafting " + recipe.getRegistryName() + ": " + error);
                }
            for (RecipeMap<?> map : RecipeMap.getRecipeMaps()) {
                int i = 0;
                for (Recipe recipe : map.getRecipeList())
                    try {
                        gt(map, recipe, map.unlocalizedName + "/" + i++);
                    } catch (Exception error) {
                        errors.add("GT " + map.unlocalizedName + ": " + error);
                    }
            }
            int i = 0;
            for (Map.Entry<ItemStack, ItemStack> recipe : FurnaceRecipes.instance().getSmeltingList().entrySet()) {
                JsonArray ins = new JsonArray(), outs = new JsonArray();
                ins.add(input(Collections.singletonList(recipe.getKey()), 1, false, ItemStack.EMPTY));
                outs.add(output(recipe.getValue()));
                recipes.add(row("smelting/" + i++, "smelting", ins, outs));
            }
            for (Object recipe :
                    com.blakebr0.extendedcrafting.crafting.table.TableRecipeManager.getInstance().getRecipes())
                if (recipe instanceof IRecipe) crafting((IRecipe) recipe, "extended_crafting", "extended/" + i++);
            providers(MetaTileEntities.class);
            providers(MachineRegistry.class);
            providers(FirmwareMachines.class);
            for (String owner :
                    new String[] {
                        "gregicality.multiblocks.common.metatileentities.GCYMMetaTileEntities",
                        "com.nomiceu.nomilabs.gregtech.metatileentity.registry.LabsMetaTileEntities"
                    })
                try {
                    providers(Class.forName(owner));
                } catch (Exception error) {
                    errors.add(owner + ": " + error);
                }
            if (Loader.isModLoaded("avaritia"))
                try {
                    avaritia();
                } catch (Exception error) {
                    errors.add("Avaritia: " + error);
                }
            // Dynamic table recipes: only applications and tiers which are actually requested by recipes.
            for (FirmwareTarget target : FirmwareTargets.all())
                for (int tier : target.circuitTiers.keySet())
                    for (ItemStack raw : FirmwareCircuits.variants(tier)) {
                        JsonArray ins = new JsonArray(), outs = new JsonArray();
                        ins.add(input(Collections.singletonList(raw), 1, false, ItemStack.EMPTY));
                        for (ItemStack catalyst :
                                new ItemStack[] {
                                    new ItemStack(FirmwareContent.WORKBENCH),
                                    new ItemStack(FirmwareContent.SOLDERING_IRON),
                                    new ItemStack(FirmwareContent.WIRE_STRIPPERS)
                                }) ins.add(input(Collections.singletonList(catalyst), 1, true, ItemStack.EMPTY));
                        ins.add(
                                input(
                                        Collections.singletonList(FirmwareMaterials.solder()),
                                        1,
                                        false,
                                        ItemStack.EMPTY));
                        ins.add(
                                input(
                                        Collections.singletonList(FirmwareMaterials.tinWire()),
                                        FirmwarePuzzle.branches(tier),
                                        false,
                                        ItemStack.EMPTY));
                        for (int branch = 0; branch < FirmwarePuzzle.branches(tier); branch++)
                            ins.add(
                                    input(
                                            Collections.singletonList(FirmwareMaterials.branch(branch, tier)),
                                            1,
                                            false,
                                            ItemStack.EMPTY));
                        outs.add(output(FirmwareData.program(raw, target)));
                        recipes.add(row("manual/" + target.key + "/" + key(raw), "manual_firmware", ins, outs));
                    }
            for (FirmwareSelectors.Selection selection : FirmwareSelectors.all())
                for (ItemStack raw : FirmwareCircuits.variants(selection.tier))
                    gt(
                            FirmwareSynthesisArray.RECIPES,
                            FirmwareSynthesisArray.RECIPES.makeRecipe(raw, selection),
                            "firmware/" + selection.target.key + "/" + key(raw));
            JsonObject result = new JsonObject(), mods = new JsonObject();
            Loader.instance().getActiveModList().forEach(mod -> mods.addProperty(mod.getModId(), mod.getVersion()));
            result.add("mods", mods);
            result.add("items", items);
            result.add("recipes", recipes);
            result.add("providers", providers);
            result.add("errors", errors);
            result.addProperty(
                    "note",
                    "Export after server scripts; graph analysis must also account for world sources, machine structures, research, voltage, tool wear, inventory and NBT predicates.");
            Path path = Paths.get(System.getProperty("mim.audit.output", "progression-audit.json")).toAbsolutePath();
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                new GsonBuilder().disableHtmlEscaping().create().toJson(result, writer);
            }
            MimsAdditions.LOGGER.info(
                    "PROGRESSION AUDIT EXPORTED: {} recipes, {} item states, {} providers, {} export errors to {}",
                    recipes.size(),
                    items.size(),
                    providers.size(),
                    errors.size(),
                    path);
        } catch (Throwable error) {
            MimsAdditions.LOGGER.error("PROGRESSION AUDIT EXPORT FAILED", error);
        } finally {
            finished = true;
        }
    }
}
