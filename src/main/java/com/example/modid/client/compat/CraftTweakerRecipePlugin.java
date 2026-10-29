package com.example.modid.client.compat;

import com.example.modid.MachineRegistry;
import com.example.modid.firmware.FirmwareDisplayIngredients;

import gregtech.common.items.MetaItems;

import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JEIPlugin
public class CraftTweakerRecipePlugin implements IModPlugin {
    private IIngredientRegistry ingredientRegistry;
    private List<ItemStack> searchableUhvItems;

    @Override
    public void register(IModRegistry registry) {
        List<ItemStack> uhvComponents =
                Arrays.asList(
                        MetaItems.ELECTRIC_MOTOR_UHV.getStackForm(),
                        MetaItems.ELECTRIC_PUMP_UHV.getStackForm(),
                        MetaItems.ELECTRIC_PISTON_UHV.getStackForm(),
                        MetaItems.CONVEYOR_MODULE_UHV.getStackForm(),
                        MetaItems.ROBOT_ARM_UHV.getStackForm(),
                        MetaItems.SENSOR_UHV.getStackForm(),
                        MetaItems.EMITTER_UHV.getStackForm(),
                        MetaItems.FIELD_GENERATOR_UHV.getStackForm());
        searchableUhvItems = new ArrayList<>(uhvComponents);
        if (MachineRegistry.SUPREME_CIRCUIT_ASSEMBLER != null) {
            searchableUhvItems.add(MachineRegistry.SUPREME_CIRCUIT_ASSEMBLER.getStackForm());
        }
        ingredientRegistry = registry.getIngredientRegistry();

        List<IRecipeWrapper> wrappers = new ArrayList<>();
        for (IRecipe recipe : ForgeRegistries.RECIPES.getValuesCollection()) {
            if (!isCraftTweakerRecipe(recipe) || !isVisible(recipe)) {
                continue;
            }

            if (recipe instanceof IShapedRecipe) {
                wrappers.add(new ShapedRecipeWrapper(recipe, (IShapedRecipe) recipe));
            } else {
                wrappers.add(new CraftTweakerRecipeWrapper(recipe));
            }
        }

        registry.addRecipes(wrappers, VanillaRecipeCategoryUid.CRAFTING);
    }

    private static boolean isCraftTweakerRecipe(IRecipe recipe) {
        return recipe.getClass().getName().startsWith("crafttweaker.mc1120.recipes.MCRecipe");
    }

    private static boolean isVisible(IRecipe recipe) {
        try {
            return Boolean.TRUE.equals(recipe.getClass().getMethod("isVisible").invoke(recipe));
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                    "Could not determine visibility for CraftTweaker recipe " + recipe.getRegistryName(), exception);
        }
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {}

    @Override
    public void registerSubtypes(ISubtypeRegistry subtypeRegistry) {}

    @Override
    public void registerIngredients(IModIngredientRegistration registry) {}

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {}

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        if (ingredientRegistry == null || searchableUhvItems == null) {
            throw new IllegalStateException("JEI ingredient registration was not initialized");
        }
        ingredientRegistry.addIngredientsAtRuntime(VanillaTypes.ITEM, searchableUhvItems);
    }

    private static class CraftTweakerRecipeWrapper implements ICraftingRecipeWrapper {
        private final IRecipe recipe;

        private CraftTweakerRecipeWrapper(IRecipe recipe) {
            this.recipe = recipe;
        }

        @Override
        public void getIngredients(IIngredients ingredients) {
            List<List<ItemStack>> inputs = new ArrayList<>();
            NonNullList<Ingredient> recipeIngredients = recipe.getIngredients();
            for (Ingredient ingredient : recipeIngredients) {
                List<ItemStack> matchingStacks = new ArrayList<>();
                if (ingredient != null) {
                    for (ItemStack stack : ingredient.getMatchingStacks()) {
                        if (!stack.isEmpty()) {
                            matchingStacks.add(stack);
                        }
                    }
                }
                inputs.add(matchingStacks);
            }

            ingredients.setInputLists(
                    VanillaTypes.ITEM, FirmwareDisplayIngredients.forOutput(recipe.getRecipeOutput(), inputs));
            ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
        }

        @Override
        public ResourceLocation getRegistryName() {
            return recipe.getRegistryName();
        }
    }

    private static final class ShapedRecipeWrapper extends CraftTweakerRecipeWrapper
            implements IShapedCraftingRecipeWrapper {
        private final IShapedRecipe recipe;

        private ShapedRecipeWrapper(IRecipe recipe, IShapedRecipe shapedRecipe) {
            super(recipe);
            this.recipe = shapedRecipe;
        }

        @Override
        public int getWidth() {
            return recipe.getRecipeWidth();
        }

        @Override
        public int getHeight() {
            return recipe.getRecipeHeight();
        }
    }
}
