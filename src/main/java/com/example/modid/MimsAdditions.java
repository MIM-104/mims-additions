package com.example.modid;

import com.example.modid.audit.ProgressionAudit;
import com.example.modid.common.BundledQuestInstaller;
import com.example.modid.common.EntityWalkingMachine;
import com.example.modid.common.ModFluids;
import com.example.modid.common.ModMaterialTweaks;
import com.example.modid.common.PrimitiveSteelRecipes;
import com.example.modid.firmware.FirmwareMachines;
import com.example.modid.firmware.FirmwareMatching;
import com.example.modid.firmware.FirmwareNetwork;
import com.example.modid.firmware.FirmwareRecipes;
import com.example.modid.firmware.FirmwareSelfTest;
import com.example.modid.firmware.FirmwareTargets;
import com.example.modid.firmware.FirmwareWorkbenchTile;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

@Mod(
        modid = Tags.MOD_ID,
        name = Tags.MOD_NAME,
        version = Tags.VERSION,
        dependencies =
                "required-after:gregtech;required-after:draconicevolution;after:avaritia;after:jei;after:nomi-labs")
public class MimsAdditions {

    @Mod.Instance(Tags.MOD_ID)
    public static MimsAdditions INSTANCE;

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    public MimsAdditions() {
        LOGGER.info("Loading {} {}", Tags.MOD_NAME, Tags.VERSION);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        FirmwareNetwork.init(this);
        GameRegistry.registerTileEntity(
                FirmwareWorkbenchTile.class, new ResourceLocation(Tags.MOD_ID, "firmware_workbench"));

        installBundledQuestDatabase(event.getModConfigurationDirectory());
        installBundledCoinScript(event.getModConfigurationDirectory());

        EntityRegistry.registerModEntity(
                new ResourceLocation(Tags.MOD_ID, "walking_machine"),
                EntityWalkingMachine.class,
                "walking_machine",
                0,
                this,
                64,
                1,
                true);

        try {
            ModFluids.init();
            MachineRegistry.init(); // Supreme machines only
            FirmwareMachines.init();
        } catch (Exception e) {
            LOGGER.error("Error initializing MachineRegistry", e);
        }
    }

    private static void installBundledQuestDatabase(File configDirectory) {
        Path directory = new File(configDirectory, "betterquesting").toPath();
        try (InputStream database = MimsAdditions.class.getResourceAsStream("/betterquesting/DefaultQuests.json")) {
            if (database == null)
                throw new IOException("Packaged Better Questing database is missing from the mod JAR");
            BundledQuestInstaller.install(directory, database);
        } catch (IOException e) {
            LOGGER.error("Could not install bundled Better Questing defaults", e);
            throw new IllegalStateException("Failed to install bundled Better Questing defaults", e);
        }
    }

    private static void installBundledCoinScript(File configDirectory) {
        File gameDirectory = configDirectory.getAbsoluteFile().getParentFile();
        if (gameDirectory == null) {
            throw new IllegalStateException(
                    "Cannot locate the Minecraft instance directory for the bundled coin script");
        }

        Path scriptsDirectory = new File(gameDirectory, "scripts").toPath();
        String[][] scripts = {
            {"coins.zs", ".mim-additions-coins-sha256"},
            {"coin_conversions.zs", ".mim-additions-coin-conversions-sha256"},
            {"coins_actuallyadditions.zs", ".mim-additions-coins-actuallyadditions-sha256"},
            {"coins_misc.zs", ".mim-additions-coins-misc-sha256"}
        };
        for (String[] script : scripts) {
            installBundledScript(scriptsDirectory, script[0], script[1]);
        }
    }

    private static void installBundledScript(Path scriptsDirectory, String scriptName, String installedHashName) {
        Path scriptFile = scriptsDirectory.resolve(scriptName);
        Path backupScript = scriptsDirectory.resolve(scriptName + ".mim-additions-backup");
        Path installedHashFile = scriptsDirectory.resolve(installedHashName);
        Path temporaryScript = null;

        try {
            Files.createDirectories(scriptsDirectory);

            if (Files.exists(installedHashFile) && Files.exists(scriptFile)) {
                String installedHash =
                        new String(Files.readAllBytes(installedHashFile), java.nio.charset.StandardCharsets.US_ASCII)
                                .trim();
                if (!installedHash.isEmpty() && !installedHash.equals(sha256(scriptFile))) {
                    LOGGER.warn(
                            "Leaving customized coin script {} untouched; bundled updates were not installed",
                            scriptFile);
                    return;
                }
            } else if (Files.exists(scriptFile) && !Files.exists(backupScript)) {
                Files.copy(scriptFile, backupScript);
                LOGGER.info("Backed up the previous GroovyScript script to {}", backupScript);
            }

            temporaryScript = Files.createTempFile(scriptsDirectory, scriptName + "-", ".tmp");
            try (InputStream script = MimsAdditions.class.getResourceAsStream("/scripts/" + scriptName)) {
                if (script == null) {
                    throw new IOException("Packaged GroovyScript script is missing from the mod JAR: " + scriptName);
                }
                Files.copy(script, temporaryScript, StandardCopyOption.REPLACE_EXISTING);
            }

            try {
                Files.move(
                        temporaryScript,
                        scriptFile,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporaryScript, scriptFile, StandardCopyOption.REPLACE_EXISTING);
            }
            Files.write(
                    installedHashFile,
                    sha256(scriptFile).getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            LOGGER.info("Installed bundled GroovyScript script at {}", scriptFile);
        } catch (IOException e) {
            LOGGER.error("Could not install bundled GroovyScript script {}", scriptName, e);
            throw new IllegalStateException("Failed to install bundled GroovyScript script: " + scriptName, e);
        } finally {
            if (temporaryScript != null) {
                try {
                    Files.deleteIfExists(temporaryScript);
                } catch (IOException e) {
                    LOGGER.warn("Could not remove temporary script {}", temporaryScript, e);
                }
            }
        }
    }

    private static String sha256(Path file) throws IOException {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file));
            StringBuilder result = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                result.append(String.format("%02x", value & 0xff));
            }
            return result.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        // Reapply after the pack's post-init scripts have finished changing materials.
        ModMaterialTweaks.apply();
        FirmwareRecipes.enforceAbsOnly();
        PrimitiveSteelRecipes.enforce();
        FirmwareMatching.enabled = true;
    }

    @Mod.EventHandler
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
        FirmwareMatching.enabled = false;
    }

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        FirmwareRecipes.enforceAbsOnly();
        PrimitiveSteelRecipes.enforce();
        FirmwareTargets.rebuild();
        FirmwareMatching.enabled = true;
        if (Boolean.getBoolean("mim.progression.audit")) {
            ProgressionAudit.dump();
            if (Boolean.getBoolean("mim.progression.auditExit"))
                FMLCommonHandler.instance().getMinecraftServerInstance().initiateShutdown();
        }
        if (Boolean.getBoolean("mim.firmware.selftest")) {
            FirmwareSelfTest.run(FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0));
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        try {
            MachineRegistry.registerMicroverse(); // Microverse only (once)
        } catch (Exception e) {
            LOGGER.error("Error registering Microverse machines", e);
        }

        ModFluids.init();
        RecipeModifier.modifyRecipes();
        DefaultRecipes.registerDefaultRecipes();
        ModMaterialTweaks.apply();
        FirmwareRecipes.register();
        // Apply before JEI captures recipe wrappers; repeat at load-complete for late pack overrides.
        PrimitiveSteelRecipes.enforce();
    }
}
