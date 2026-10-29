package com.example.modid.firmware;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Apply the same output-specific requirement to crafting-grid recipe displays as to matching. */
public final class FirmwareDisplayIngredients {
    private FirmwareDisplayIngredients() {}

    public static List<List<ItemStack>> forOutput(ItemStack output, List<List<ItemStack>> inputs) {
        if (!FirmwareData.requiresFirmware(output)) return inputs;
        FirmwareTarget target = FirmwareData.describe(output);
        List<List<ItemStack>> displayed = new ArrayList<>(inputs.size());
        for (List<ItemStack> options : inputs) {
            List<ItemStack> copies = new ArrayList<>(options.size());
            for (ItemStack stack : options) {
                if (FirmwareData.isCircuit(stack)) {
                    ItemStack circuit = FirmwareData.program(stack, target);
                    circuit.setCount(stack.getCount());
                    copies.add(circuit);
                } else copies.add(stack);
            }
            displayed.add(copies);
        }
        return displayed;
    }
}
