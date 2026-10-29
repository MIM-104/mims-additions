package com.example.modid.common;

import com.example.modid.Tags;

import gregtech.api.GTValues;
import gregtech.common.items.MetaItems;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class CircuitProgression {

    private static volatile MinecraftServer cachedServer;
    private static volatile int highestCircuitTier = -1;
    private static List<TieredCircuit> tieredCircuits;

    private CircuitProgression() {}

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote || event.world.provider.getDimension() != 0) {
            return;
        }

        MinecraftServer server = event.world.getMinecraftServer();
        if (server == null) {
            return;
        }

        FactoryDecayWorldData data = FactoryDecayWorldData.get(event.world);
        int previousUnlockedTier = data.getHighestUnlockedCircuitTier();
        int carriedTier = findHighestCircuitTier(server.getPlayerList().getPlayers());
        int unlockedTier = data.unlockCircuitTier(carriedTier);
        announceNewStages(server, previousUnlockedTier, unlockedTier);
        highestCircuitTier = unlockedTier;
        cachedServer = server;

        if (unlockedTier >= GTValues.ZPM) {
            data.startLateGame(event.world.getTotalWorldTime());
        }
    }

    private static void announceNewStages(MinecraftServer server, int previousTier, int unlockedTier) {
        announceNewStage(
                server,
                previousTier,
                unlockedTier,
                GTValues.ULV,
                "Early game penalties activated.",
                TextFormatting.YELLOW);
        announceNewStage(
                server, previousTier, unlockedTier, GTValues.EV, "Mid game penalties activated.", TextFormatting.GOLD);
        announceNewStage(
                server,
                previousTier,
                unlockedTier,
                GTValues.ZPM,
                "Late game penalties activated.",
                TextFormatting.DARK_RED);
    }

    private static void announceNewStage(
            MinecraftServer server,
            int previousTier,
            int unlockedTier,
            int requiredTier,
            String message,
            TextFormatting color) {
        if (previousTier >= requiredTier || unlockedTier < requiredTier) {
            return;
        }

        server.getPlayerList().sendMessage(new TextComponentString(color + message + TextFormatting.RESET));
    }

    public static int getHighestCircuitTier(World world) {
        if (world == null || world.isRemote) {
            return -1;
        }

        MinecraftServer server = world.getMinecraftServer();
        if (server == null) {
            return -1;
        }

        if (cachedServer == server) {
            return highestCircuitTier;
        }

        int carriedTier = findHighestCircuitTier(server.getPlayerList().getPlayers());
        return FactoryDecayWorldData.get(world).unlockCircuitTier(carriedTier);
    }

    private static int findHighestCircuitTier(Iterable<? extends EntityPlayer> players) {
        int highestTier = -1;
        List<TieredCircuit> circuits = getTieredCircuits();
        for (EntityPlayer player : players) {
            highestTier = Math.max(highestTier, findHighestCircuitTier(player, circuits));
        }
        return highestTier;
    }

    private static int findHighestCircuitTier(EntityPlayer player, List<TieredCircuit> circuits) {
        int highestTier = -1;
        highestTier = Math.max(highestTier, findHighestCircuitTier(player.inventory.mainInventory, circuits));
        highestTier = Math.max(highestTier, findHighestCircuitTier(player.inventory.armorInventory, circuits));
        highestTier = Math.max(highestTier, findHighestCircuitTier(player.inventory.offHandInventory, circuits));
        return highestTier;
    }

    private static int findHighestCircuitTier(Iterable<ItemStack> stacks, List<TieredCircuit> circuits) {
        int highestTier = -1;
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            for (TieredCircuit circuit : circuits) {
                if (stack.isItemEqual(circuit.stack)) {
                    highestTier = Math.max(highestTier, circuit.tier);
                }
            }
        }
        return highestTier;
    }

    private static synchronized List<TieredCircuit> getTieredCircuits() {
        if (tieredCircuits == null) {
            List<TieredCircuit> circuits = new ArrayList<>();

            add(circuits, MetaItems.INTEGRATED_LOGIC_CIRCUIT.getStackForm(), GTValues.ULV);
            add(circuits, MetaItems.ULTRA_LOW_POWER_INTEGRATED_CIRCUIT.getStackForm(), GTValues.ULV);
            add(circuits, MetaItems.LOW_POWER_INTEGRATED_CIRCUIT.getStackForm(), GTValues.LV);
            add(circuits, MetaItems.ELECTRONIC_CIRCUIT_LV.getStackForm(), GTValues.LV);
            add(circuits, MetaItems.INTEGRATED_CIRCUIT_LV.getStackForm(), GTValues.LV);
            add(circuits, MetaItems.MICROPROCESSOR_LV.getStackForm(), GTValues.LV);
            add(circuits, MetaItems.POWER_INTEGRATED_CIRCUIT.getStackForm(), GTValues.MV);
            add(circuits, MetaItems.ELECTRONIC_CIRCUIT_MV.getStackForm(), GTValues.MV);
            add(circuits, MetaItems.INTEGRATED_CIRCUIT_MV.getStackForm(), GTValues.MV);
            add(circuits, MetaItems.PROCESSOR_MV.getStackForm(), GTValues.MV);
            add(circuits, MetaItems.HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(), GTValues.HV);
            add(circuits, MetaItems.PROCESSOR_ASSEMBLY_HV.getStackForm(), GTValues.HV);
            add(circuits, MetaItems.NANO_PROCESSOR_HV.getStackForm(), GTValues.HV);
            add(circuits, MetaItems.NANO_PROCESSOR_ASSEMBLY_EV.getStackForm(), GTValues.EV);
            add(circuits, MetaItems.QUANTUM_PROCESSOR_EV.getStackForm(), GTValues.EV);
            add(circuits, MetaItems.NANO_COMPUTER_IV.getStackForm(), GTValues.IV);
            add(circuits, MetaItems.QUANTUM_ASSEMBLY_IV.getStackForm(), GTValues.IV);
            add(circuits, MetaItems.CRYSTAL_PROCESSOR_IV.getStackForm(), GTValues.IV);
            add(circuits, MetaItems.MAINFRAME_IV.getStackForm(), GTValues.IV);
            add(circuits, MetaItems.NANO_MAINFRAME_LUV.getStackForm(), GTValues.LuV);
            add(circuits, MetaItems.QUANTUM_COMPUTER_LUV.getStackForm(), GTValues.LuV);
            add(circuits, MetaItems.CRYSTAL_ASSEMBLY_LUV.getStackForm(), GTValues.LuV);
            add(circuits, MetaItems.WETWARE_PROCESSOR_LUV.getStackForm(), GTValues.LuV);
            add(circuits, MetaItems.QUANTUM_MAINFRAME_ZPM.getStackForm(), GTValues.ZPM);
            add(circuits, MetaItems.CRYSTAL_COMPUTER_ZPM.getStackForm(), GTValues.ZPM);
            add(circuits, MetaItems.WETWARE_PROCESSOR_ASSEMBLY_ZPM.getStackForm(), GTValues.ZPM);
            add(circuits, MetaItems.CRYSTAL_MAINFRAME_UV.getStackForm(), GTValues.UV);
            add(circuits, MetaItems.WETWARE_SUPER_COMPUTER_UV.getStackForm(), GTValues.UV);
            add(circuits, MetaItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(), GTValues.UHV);
            add(circuits, MetaItems.WETWARE_MAINFRAME_UHV.getStackForm(), GTValues.UHV);

            if (ModItems.epsteiniumInfusedWetwareMainframe == null) {
                throw new IllegalStateException("The UEV wetware mainframe item is not registered");
            }
            add(circuits, new ItemStack(ModItems.epsteiniumInfusedWetwareMainframe), GTValues.UEV);

            tieredCircuits = circuits;
        }
        return tieredCircuits;
    }

    private static void add(List<TieredCircuit> circuits, ItemStack stack, int tier) {
        if (stack.isEmpty()) {
            throw new IllegalStateException("A GregTech tiered circuit item is not registered");
        }
        circuits.add(new TieredCircuit(stack, tier));
    }

    private static final class TieredCircuit {
        private final ItemStack stack;
        private final int tier;

        private TieredCircuit(ItemStack stack, int tier) {
            this.stack = stack;
            this.tier = tier;
        }
    }
}
