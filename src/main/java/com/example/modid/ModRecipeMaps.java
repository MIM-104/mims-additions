package com.example.modid;

import com.nomiceu.nomilabs.LabsSounds;
import com.nomiceu.nomilabs.LabsTextures;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;

/** Recipe map for the Microverse Projector IV. */
public final class ModRecipeMaps {

    public static final RecipeMap<SimpleRecipeBuilder> MICROVERSE_4_RECIPES;

    static {
        // Large miner yields share one counted output slot in JEI.
        MICROVERSE_4_RECIPES =
                new RecipeMap<>("microverse_4", 4, 1, 0, 0, new SimpleRecipeBuilder(), false)
                        .setProgressBar(LabsTextures.PROGRESS_BAR_ROCKET, ProgressWidget.MoveType.HORIZONTAL)
                        .setSound(LabsSounds.MICROVERSE)
                        .setSlotOverlay(false, false, GuiTextures.IMPLOSION_OVERLAY_1);
    }

    private ModRecipeMaps() {}
}
