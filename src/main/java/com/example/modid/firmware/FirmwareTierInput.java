package com.example.modid.firmware;

import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;

import net.minecraft.item.ItemStack;

import java.util.*;

/** An ore-tier choice with an explicit raw or application-specific firmware requirement. */
public final class FirmwareTierInput extends GTRecipeItemInput {
    private final int tier;
    private final String purpose;

    public FirmwareTierInput(int tier, String purpose, int count) {
        super(examples(tier, purpose), count);
        this.tier = tier;
        this.purpose = purpose;
    }

    private static ItemStack[] examples(int tier, String purpose) {
        List<ItemStack> examples = FirmwareCircuits.variants(tier);
        if (!purpose.isEmpty()) {
            FirmwareTarget target = FirmwareTargets.get(purpose);
            if (target == null) throw new IllegalArgumentException("Unknown firmware purpose " + purpose);
            examples.replaceAll(stack -> FirmwareData.program(stack, target));
        }
        return examples.toArray(new ItemStack[0]);
    }

    @Override
    public boolean acceptsStack(ItemStack stack) {
        return FirmwareData.tier(stack) == tier && purpose.equals(FirmwareData.target(stack));
    }

    @Override
    protected FirmwareTierInput copy() {
        return (FirmwareTierInput) copyWithAmount(amount);
    }

    @Override
    public GTRecipeInput copyWithAmount(int count) {
        FirmwareTierInput copy = new FirmwareTierInput(tier, purpose, count);
        copy.isConsumable = isConsumable;
        return copy;
    }

    @Override
    protected int computeHash() {
        return Objects.hash(getClass(), tier, purpose, amount, isConsumable);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof FirmwareTierInput
                && equalIgnoreAmount((FirmwareTierInput) other)
                && amount == ((FirmwareTierInput) other).amount;
    }

    @Override
    public boolean equalIgnoreAmount(GTRecipeInput other) {
        return other instanceof FirmwareTierInput
                && tier == ((FirmwareTierInput) other).tier
                && Objects.equals(purpose, ((FirmwareTierInput) other).purpose)
                && isConsumable == ((FirmwareTierInput) other).isConsumable;
    }
}
