package com.example.modid.firmware;

import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;

import net.minecraft.item.ItemStack;

import java.util.Objects;

/** Separate template and blank predicates prevent the array from consuming its master circuit. */
public final class FirmwareRecipeInput extends GTRecipeItemInput {
    private final ItemStack example;
    private final boolean template;
    private final String target;

    public FirmwareRecipeInput(ItemStack example, boolean template, int amount) {
        super(example, amount);
        this.example = example.copy();
        this.template = template;
        this.target = template ? FirmwareData.target(example) : "";
    }

    @Override
    public boolean acceptsStack(ItemStack stack) {
        return stack != null
                && !stack.isEmpty()
                && stack.isItemEqual(example)
                && FirmwareData.isCircuit(stack)
                && (template ? target.equals(FirmwareData.target(stack)) : !FirmwareData.configured(stack));
    }

    @Override
    protected FirmwareRecipeInput copy() {
        FirmwareRecipeInput result = new FirmwareRecipeInput(example, template, amount);
        result.isConsumable = isConsumable;
        return result;
    }

    @Override
    public GTRecipeInput copyWithAmount(int amount) {
        FirmwareRecipeInput result = new FirmwareRecipeInput(example, template, amount);
        result.isConsumable = isConsumable;
        return result;
    }

    @Override
    protected int computeHash() {
        return Objects.hash(super.computeHash(), template, target);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof FirmwareRecipeInput
                && template == ((FirmwareRecipeInput) other).template
                && Objects.equals(target, ((FirmwareRecipeInput) other).target)
                && super.equals(other);
    }

    @Override
    public boolean equalIgnoreAmount(GTRecipeInput other) {
        return other instanceof FirmwareRecipeInput
                && template == ((FirmwareRecipeInput) other).template
                && Objects.equals(target, ((FirmwareRecipeInput) other).target)
                && super.equalIgnoreAmount(other);
    }
}
