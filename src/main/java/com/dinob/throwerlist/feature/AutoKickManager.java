package com.dinob.throwerlist.feature;

import com.dinob.throwerlist.config.AutoKickStore;
import com.dinob.throwerlist.config.ThrowerListConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class AutoKickManager {
    public static final AutoKickManager INSTANCE = new AutoKickManager();

    private final Map<UUID, String> nameCache = new LinkedHashMap<>();
    private final Set<UUID> localUuids = ConcurrentHashMap.newKeySet();
    private final Set<UUID> remoteUuids = ConcurrentHashMap.newKeySet();
    private final Map<UUID, PendingKick> pendingKicks = new ConcurrentHashMap<>();

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "ThrowerList-RemoteFetch");
        t.setDaemon(true);
        return t;
    });

    private AutoKickManager() {
        AutoKickStore.loadLocalUuids().forEach(uuid -> addLocal(uuid, null));
    }

    public void load() {
        localUuids.clear();
        localUuids.addAll(AutoKickStore.loadLocalUuids());
        
        if (ThrowerListConfig.autoRefreshRemote) {
            refreshRemoteAsync();
            executor.scheduleAtFixedRate(this::refreshRemoteAsync, 10, 30, TimeUnit.MINUTES);
        }
    }

    public void addLocal(UUID uuid, String name) {
        if (uuid != null) {
            localUuids.add(uuid);
            if (name != null && !name.isBlank()) nameCache.put(uuid, name.trim());
        }
    }

    public void add(UUID uuid, String name) {
        if (uuid == null) return;
        localUuids.add(uuid);
        if (name != null && !name.isBlank()) nameCache.put(uuid, name.trim());
        saveQuietly();
    }

    public void remove(UUID uuid) {
        if (uuid == null) return;
        localUuids.remove(uuid);
        nameCache.remove(uuid);
        saveQuietly();
    }

    public void tick(Minecraft client) {
        if (client.player == null || client.player.connection == null || !ThrowerListConfig.enabled) return;

        if (!pendingKicks.isEmpty()) {
            pendingKicks.entrySet().removeIf(entry -> {
                PendingKick pk = entry.getValue();
                if (--pk.ticksRemaining > 0) return false;
                if (!ThrowerListConfig.enabled) return true;

                String msg = buildKickMessage(pk.name);
                if (!msg.isBlank()) client.player.connection.sendCommand("pc " + msg);
                client.player.connection.sendCommand("p kick " + pk.name);
                return true;
            });
        }
    }

    public void onPlayerJoin(String name, UUID uuid) {
        if (!ThrowerListConfig.enabled || uuid == null) return;
        nameCache.put(uuid, name);

        boolean shouldKick = localUuids.contains(uuid) || remoteUuids.contains(uuid);
        if (shouldKick && !pendingKicks.containsKey(uuid)) {
            pendingKicks.put(uuid, new PendingKick(name, ThrowerListConfig.kickDelayTicks));
        }
    }

    public void refreshRemoteAsync() {
        executor.submit(() -> {
            try {
                URL url = new URL(ThrowerListConfig.remoteUrl);
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                    Set<UUID> fetched = new LinkedHashSet<>();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#")) continue;
                        UUID uuid = parseUuid(line);
                        if (uuid != null) fetched.add(uuid);
                    }
                    synchronized (remoteUuids) {
                        remoteUuids.clear();
                        remoteUuids.addAll(fetched);
                    }
                    Minecraft client = Minecraft.getInstance();
                    if (client.player != null) {
                        client.execute(() -> client.player.sendSystemMessage(
                            Component.literal("[DA] ").withStyle(net.minecraft.ChatFormatting.DARK_GREEN)
                                .append(Component.literal("Fetched " + fetched.size() + " remote UUIDs").withStyle(net.minecraft.ChatFormatting.DARK_GREEN))
                        ));
                    }
                }
            } catch (Exception e) {
                // Silent fail for auto-refresh, log for manual refresh
            }
        });
    }

    public Set<UUID> getLocalUuids() { return new LinkedHashSet<>(localUuids); }
    public Set<UUID> getRemoteUuids() { return new LinkedHashSet<>(remoteUuids); }
    public String getName(UUID uuid) { return nameCache.getOrDefault(uuid, uuid.toString()); }

    public static UUID resolveUuidFromTab(String name) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return null;
        String clean = name.trim().toLowerCase();
        for (PlayerInfo info : client.player.connection.getOnlinePlayers()) {
            if (info.getProfile().name().trim().toLowerCase().equals(clean)) {
                return info.getProfile().id();
            }
        }
        return null;
    }

    private static UUID parseUuid(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String compact = raw.trim().replace("-", "");
        if (compact.length() != 32) return null;
        String dashed = compact.substring(0, 8) + "-" + compact.substring(8, 12) + "-" +
                       compact.substring(12, 16) + "-" + compact.substring(16, 20) + "-" + compact.substring(20);
        try { return UUID.fromString(dashed); } catch (IllegalArgumentException ignored) { return null; }
    }

    private void saveQuietly() {
        try { AutoKickStore.saveLocalUuids(localUuids); } catch (IOException ignored) {}
    }

    private String buildKickMessage(String name) {
        String template = ThrowerListConfig.kickMessage;
        if (template == null || template.isBlank()) return "";
        return template.replace("<name>", name).replace("{name}", name);
    }

    private static class PendingKick {
        final String name;
        int ticksRemaining;
        PendingKick(String name, int ticks) { this.name = name; this.ticksRemaining = ticks; }
    }
}