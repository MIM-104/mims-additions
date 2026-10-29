package com.example.modid.mixins;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.crafting.MECraftingInventory;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.ThreadLocalRandom;

@Pseudo
@Mixin(targets = "appeng.me.cluster.implementations.CraftingCPUCluster", remap = false)
public abstract class MixinAE2AutocraftTroll {

    private static final int PULVERISE_CHANCE_DENOMINATOR = 5000;
    private static final String[] PULVERISE_MESSAGES = {
        "The crafting CPU misplaced an ingredient.",
        "An ingredient has been promoted to customer.",
        "The ME system fed one of your ingredients to its pet.",
        "A crafting ingredient has gone on vacation.",
        "The CPU insists it had that ingredient a second ago.",
        "One ingredient rage-quit the crafting process.",
        "A warehouse goblin has requisitioned one ingredient.",
        "A cosmic neutrino fell through your ME system, taking an ingredient with it.",
        "The crafting CPU is on a coffee break. That ingredient makes for a good snack!"
    };

    @Shadow
    private void postChange(IAEItemStack diff, IActionSource source) {}

    @Shadow
    private void markDirty() {}

    @Redirect(
            method = "executeCrafting",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lappeng/crafting/MECraftingInventory;extractItems(Lappeng/api/storage/data/IAEItemStack;Lappeng/api/config/Actionable;Lappeng/api/networking/security/IActionSource;)Lappeng/api/storage/data/IAEItemStack;"),
            require = 2,
            remap = false)
    private IAEItemStack modid$maybePulverizeMaterial(
            MECraftingInventory inventory, IAEItemStack requested, Actionable action, IActionSource source) {
        if (action == Actionable.MODULATE && ThreadLocalRandom.current().nextInt(PULVERISE_CHANCE_DENOMINATOR) == 0) {
            IAEItemStack pulverized = inventory.extractItems(requested.copy().setStackSize(1), action, source);
            if (pulverized != null) {
                this.postChange(pulverized, source);
                this.markDirty();

                MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
                if (server != null) {
                    String message = PULVERISE_MESSAGES[ThreadLocalRandom.current().nextInt(PULVERISE_MESSAGES.length)];
                    server.getPlayerList().sendMessage(new TextComponentString(message));
                }
                // AE2 sees no ingredient delivered, so this pattern waits with one item gone from its CPU.
                return null;
            }
        }

        return inventory.extractItems(requested, action, source);
    }
}
