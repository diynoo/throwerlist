package com.dinob.throwerlist.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public final class AutoKickStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "throwerlist_uuids.json";
    private static final String REMOTE_FILE_NAME = "remoteuuids.json";
    private static final String BLACKLIST_FILE_NAME = "throwerlist_blacklist.json";

    private AutoKickStore() {}

    public static Set<UUID> loadLocalUuids() {
        return loadUuids(listPath());
    }

    public static void saveLocalUuids(Set<UUID> uuids) throws IOException {
        saveUuids(listPath(), uuids);
    }

    public static Set<UUID> loadRemoteUuids() {
        return loadUuids(remoteListPath());
    }

    public static void saveRemoteUuids(Set<UUID> uuids) throws IOException {
        saveUuids(remoteListPath(), uuids);
    }

    public static Set<UUID> loadBlacklistUuids() {
        return loadUuids(blacklistPath());
    }

    public static void saveBlacklistUuids(Set<UUID> uuids) throws IOException {
        saveUuids(blacklistPath(), uuids);
    }

    private static Set<UUID> loadUuids(Path file) {
        Set<UUID> out = new LinkedHashSet<>();
        if (!Files.exists(file)) {
            return out;
        }
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonArray()) {
                return out;
            }
            for (JsonElement element : root.getAsJsonArray()) {
                if (!element.isJsonPrimitive()) continue;
                UUID uuid = parseUuid(element.getAsString());
                if (uuid != null) out.add(uuid);
            }
        } catch (IOException ignored) {}
        return out;
    }

    private static void saveUuids(Path file, Set<UUID> uuids) throws IOException {
        Files.createDirectories(file.getParent());
        JsonArray array = new JsonArray();
        for (UUID uuid : uuids) {
            if (uuid == null) continue;
            array.add(uuid.toString());
        }
        try (Writer writer = Files.newBufferedWriter(file)) {
            GSON.toJson(array, writer);
        }
    }

    private static UUID parseUuid(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String compact = raw.trim().replace("-", "");
        if (compact.length() != 32) return null;
        String dashed = compact.substring(0, 8) + "-" + compact.substring(8, 12) + "-" +
                       compact.substring(12, 16) + "-" + compact.substring(16, 20) + "-" + compact.substring(20);
        try { return UUID.fromString(dashed); } catch (IllegalArgumentException ignored) { return null; }
    }

    private static Path listPath() {
        return ThrowerListConfig.getConfigDir().resolve(FILE_NAME);
    }

    private static Path remoteListPath() {
        return ThrowerListConfig.getConfigDir().resolve(REMOTE_FILE_NAME);
    }

    private static Path blacklistPath() {
        return ThrowerListConfig.getConfigDir().resolve(BLACKLIST_FILE_NAME);
    }
}