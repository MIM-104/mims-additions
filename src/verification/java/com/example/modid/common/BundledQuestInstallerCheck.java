package com.example.modid.common;

import com.google.gson.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/** Exercises real file installation and three-way updates without starting Minecraft. */
public final class BundledQuestInstallerCheck {
    private static int checks;

    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }

    private static byte[] bytes(JsonObject book) {
        return book.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static JsonObject read(Path dir) throws IOException {
        return new JsonParser()
                .parse(new String(Files.readAllBytes(dir.resolve("DefaultQuests.json")), StandardCharsets.UTF_8))
                .getAsJsonObject();
    }

    private static void install(Path dir, JsonObject book) throws IOException {
        BundledQuestInstaller.install(dir, new ByteArrayInputStream(bytes(book)));
    }

    private static JsonObject book(int version, String title, String desc) {
        JsonObject out = new JsonObject(),
                quests = new JsonObject(),
                quest = new JsonObject(),
                settings = new JsonObject(),
                bq = new JsonObject();
        quest.addProperty("name:8", title);
        quest.addProperty("desc:8", desc);
        quest.addProperty("questID:3", 1);
        quests.add("1:10", quest);
        out.add("questDatabase:9", quests);
        bq.addProperty("pack_version:3", version);
        settings.add("betterquesting:10", bq);
        out.add("questSettings:10", settings);
        return out;
    }

    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory(Paths.get(args[0]), "quest-install-");
        Path legacy = root.resolve("legacy");
        Files.createDirectories(legacy);
        JsonObject old = book(13, "Old quest", "Old description");
        Files.write(legacy.resolve("DefaultQuests.json"), bytes(old));
        Files.write(
                legacy.resolve(".mim-additions-questbook-sha256"), "legacy-marker".getBytes(StandardCharsets.UTF_8));
        Files.write(legacy.resolve("QuestProgress.json"), "keep progress".getBytes(StandardCharsets.UTF_8));
        JsonObject first = book(19, "Updated quest", "Updated description");
        first.getAsJsonObject("questDatabase:9")
                .add(
                        "2:10",
                        book(19, "Firmware Workbench", "Program circuits")
                                .getAsJsonObject("questDatabase:9")
                                .get("1:10"));
        install(legacy, first);
        check(read(legacy).equals(first), "legacy marker must not block the new quests");
        check(Files.exists(legacy.resolve(".mim-additions-questbook-base.json")), "baseline saved");
        try (java.util.stream.Stream<Path> files = Files.list(legacy)) {
            Path backup =
                    files.filter(p -> p.getFileName().toString().startsWith("DefaultQuests.json.mim-additions-backup-"))
                            .findFirst()
                            .get();
            check(
                    new JsonParser().parse(new String(Files.readAllBytes(backup), StandardCharsets.UTF_8)).equals(old),
                    "old definitions backed up intact");
        }
        JsonObject edited = read(legacy);
        edited.getAsJsonObject("questDatabase:9")
                .getAsJsonObject("1:10")
                .addProperty("desc:8", "My in-game note\nSecond line");
        edited.getAsJsonObject("questDatabase:9").remove("2:10");
        Files.write(legacy.resolve("DefaultQuests.json"), bytes(edited));
        install(legacy, first);
        check(read(legacy).equals(edited), "same bundle preserves in-game edits and deletions");
        JsonObject second = book(20, "New title", "New bundled description");
        second.getAsJsonObject("questDatabase:9").add("2:10", first.getAsJsonObject("questDatabase:9").get("2:10"));
        JsonObject added = new JsonObject();
        added.addProperty("name:8", "New penalty guide");
        second.getAsJsonObject("questDatabase:9").add("3:10", added);
        install(legacy, second);
        JsonObject merged = read(legacy), quests = merged.getAsJsonObject("questDatabase:9");
        check(
                quests.getAsJsonObject("1:10").get("name:8").getAsString().equals("New title"),
                "untouched title updated");
        check(
                quests.getAsJsonObject("1:10").get("desc:8").getAsString().equals("My in-game note\nSecond line"),
                "conflicting local description preserved");
        check(!quests.has("2:10"), "local quest deletion preserved");
        check(quests.has("3:10"), "new bundled quest added");
        check(
                merged.getAsJsonObject("questSettings:10")
                                .getAsJsonObject("betterquesting:10")
                                .get("pack_version:3")
                                .getAsInt()
                        == 20,
                "version advances for BQ reload");
        check(
                new String(Files.readAllBytes(legacy.resolve("QuestProgress.json")), StandardCharsets.UTF_8)
                        .equals("keep progress"),
                "progress file untouched");
        install(legacy, second);
        check(read(legacy).equals(merged), "repeated startup is idempotent");
        Path fresh = root.resolve("fresh");
        install(fresh, first);
        check(read(fresh).equals(first), "fresh installation gets all bundled quests");
        System.out.println("QUEST INSTALLER TEST PASSED: " + checks + " checks");
    }
}
