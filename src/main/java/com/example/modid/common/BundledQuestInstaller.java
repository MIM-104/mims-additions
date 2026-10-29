package com.example.modid.common;

import com.google.gson.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.Set;

/** Installs quest definitions, keeping a baseline so local edits survive subsequent updates. */
public final class BundledQuestInstaller {
    private static final Logger LOG = LogManager.getLogger("MIM's Additions/Quests");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private BundledQuestInstaller() {}

    public static void install(Path directory, InputStream bundled) throws IOException {
        Files.createDirectories(directory);
        byte[] incomingBytes = readAll(bundled);
        JsonObject incoming = parse(incomingBytes);
        Path defaults = directory.resolve("DefaultQuests.json");
        Path marker = directory.resolve(".mim-additions-questbook-sha256");
        Path baseline = directory.resolve(".mim-additions-questbook-base.json");
        String incomingHash = hash(incomingBytes);
        String installedHash =
                Files.exists(marker) ? new String(Files.readAllBytes(marker), StandardCharsets.UTF_8).trim() : "";
        if (incomingHash.equals(installedHash) && Files.exists(defaults)) {
            // Same addon version: leave in-game edits alone, but establish the missing legacy baseline.
            if (!Files.exists(baseline)) atomicWrite(baseline, incomingBytes);
            return;
        }

        byte[] result = incomingBytes;
        if (Files.exists(defaults)) {
            byte[] currentBytes = Files.readAllBytes(defaults);
            String currentHash = hash(currentBytes);
            if (!currentHash.equals(incomingHash)) {
                Path backup = directory.resolve("DefaultQuests.json.mim-additions-backup-" + currentHash + ".json");
                if (!Files.exists(backup)) Files.write(backup, currentBytes, StandardOpenOption.CREATE_NEW);
                LOG.info("Backed up the previous quest definitions to {}", backup);
                if (Files.exists(baseline)) {
                    JsonObject current = parse(currentBytes);
                    JsonObject previous = parse(Files.readAllBytes(baseline));
                    JsonObject merged = merge(previous, current, incoming).getAsJsonObject();
                    setVersion(merged, Math.max(version(incoming), version(current) + 1));
                    result = (GSON.toJson(merged) + "\n").getBytes(StandardCharsets.UTF_8);
                    LOG.info("Merged updated bundled quests with local edits at {}", defaults);
                } else if (!installedHash.isEmpty()) {
                    // Older addons only wrote a hash. They cannot distinguish edits from normal BQ reformatting.
                    // Migrate the definitions once, with a complete backup, rather than refusing updates forever.
                    LOG.info("Migrating the legacy questbook marker to the bundled quest definitions at {}", defaults);
                }
            }
        }
        atomicWrite(defaults, result);
        // Store the incoming bundle, not the merged result: this is the ancestor for the next update.
        atomicWrite(baseline, incomingBytes);
        atomicWrite(marker, incomingHash.getBytes(StandardCharsets.UTF_8));
        LOG.info(
                "Installed bundled Better Questing defaults at {} (pack version {})", defaults, version(parse(result)));
    }

    private static JsonElement merge(JsonElement base, JsonElement current, JsonElement incoming) {
        if (same(current, base)) return copy(incoming);
        if (same(incoming, base) || same(current, incoming)) return copy(current);
        if (base != null
                && current != null
                && incoming != null
                && base.isJsonObject()
                && current.isJsonObject()
                && incoming.isJsonObject()) {
            JsonObject result = new JsonObject();
            Set<String> keys = new LinkedHashSet<>();
            for (java.util.Map.Entry<String, JsonElement> entry : current.getAsJsonObject().entrySet())
                keys.add(entry.getKey());
            for (java.util.Map.Entry<String, JsonElement> entry : incoming.getAsJsonObject().entrySet())
                keys.add(entry.getKey());
            for (java.util.Map.Entry<String, JsonElement> entry : base.getAsJsonObject().entrySet())
                keys.add(entry.getKey());
            for (String key : keys) {
                JsonElement value =
                        merge(
                                base.getAsJsonObject().get(key),
                                current.getAsJsonObject().get(key),
                                incoming.getAsJsonObject().get(key));
                if (value != null) result.add(key, value);
            }
            return result;
        }
        // Conflicting text, lists, additions and explicit deletions retain the user's value.
        return copy(current);
    }

    private static boolean same(JsonElement a, JsonElement b) {
        return a == null ? b == null : a.equals(b);
    }

    private static JsonElement copy(JsonElement value) {
        return value == null ? null : new JsonParser().parse(value.toString());
    }

    private static JsonObject parse(byte[] bytes) throws IOException {
        try {
            return new JsonParser().parse(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (RuntimeException error) {
            throw new IOException("Invalid Better Questing database", error);
        }
    }

    private static int version(JsonObject book) {
        JsonObject settings = book.has("questSettings:10") ? book.getAsJsonObject("questSettings:10") : null;
        JsonObject bq =
                settings != null && settings.has("betterquesting:10")
                        ? settings.getAsJsonObject("betterquesting:10")
                        : null;
        return bq != null && bq.has("pack_version:3") ? bq.get("pack_version:3").getAsInt() : 0;
    }

    private static void setVersion(JsonObject book, int version) {
        if (!book.has("questSettings:10")) book.add("questSettings:10", new JsonObject());
        JsonObject settings = book.getAsJsonObject("questSettings:10");
        if (!settings.has("betterquesting:10")) settings.add("betterquesting:10", new JsonObject());
        settings.getAsJsonObject("betterquesting:10").addProperty("pack_version:3", version);
    }

    private static byte[] readAll(InputStream input) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int count;
        while ((count = input.read(buffer)) != -1) out.write(buffer, 0, count);
        return out.toByteArray();
    }

    private static String hash(byte[] bytes) throws IOException {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            StringBuilder out = new StringBuilder();
            for (byte value : digest) out.append(String.format("%02x", value & 255));
            return out.toString();
        } catch (NoSuchAlgorithmException error) {
            throw new IOException(error);
        }
    }

    private static void atomicWrite(Path target, byte[] bytes) throws IOException {
        Path temporary = Files.createTempFile(target.getParent(), target.getFileName().toString(), ".tmp");
        try {
            Files.write(temporary, bytes);
            try {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException error) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
