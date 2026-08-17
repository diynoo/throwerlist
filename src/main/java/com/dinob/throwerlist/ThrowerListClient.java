package com.dinob.throwerlist;

import com.dinob.throwerlist.config.ThrowerListConfig;
import com.dinob.throwerlist.config.AutoKickStore;
import com.dinob.throwerlist.feature.AutoKickManager;
import com.dinob.throwerlist.gui.ThrowerListScreen;
import com.dinob.throwerlist.util.KeyBindingTokenUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;

public final class ThrowerListClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ThrowerListConfig.load();
        AutoKickStore.loadLocalUuids().forEach(uuid -> AutoKickManager.INSTANCE.addLocal(uuid, null));
        AutoKickManager.INSTANCE.load();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                AutoKickManager.INSTANCE.tick(client);
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            var root = literal("throwerlist")
                .executes(ctx -> {
                    Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(new ThrowerListScreen(null)));
                    return 1;
                })
                .then(literal("help")
                    .executes(ctx -> showHelp(ctx)))
                .then(literal("add")
                    .then(argument("player", StringArgumentType.word())
                        .executes(ctx -> addPlayer(ctx, StringArgumentType.getString(ctx, "player")))))
                .then(literal("remove")
                    .then(argument("player", StringArgumentType.word())
                        .executes(ctx -> removePlayer(ctx, StringArgumentType.getString(ctx, "player")))))
                .then(literal("list")
                    .executes(ctx -> listPlayers(ctx)))
                .then(literal("toggle")
                    .executes(ctx -> toggleEnabled(ctx)))
                .then(literal("message")
                    .then(argument("msg", StringArgumentType.greedyString())
                        .executes(ctx -> setMessage(ctx, StringArgumentType.getString(ctx, "msg")))))
                .then(literal("refresh")
                    .executes(ctx -> refreshRemote(ctx)));

            var rootNode = dispatcher.register(root);
            dispatcher.register(literal("tl").redirect(rootNode));
        });
    }

    private static int showHelp(com.mojang.brigadier.context.CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> ctx) {
        sendFeedback(ctx.getSource(), "§e§lThrowerList Commands:");
        sendFeedback(ctx.getSource(), "§7/throwerlist or /tl §r- Open GUI");
        sendFeedback(ctx.getSource(), "§7/tl help §r- Show this help");
        sendFeedback(ctx.getSource(), "§7/tl add <player> §r- Add player to kick list");
        sendFeedback(ctx.getSource(), "§7/tl remove <player> §r- Remove player from kick list");
        sendFeedback(ctx.getSource(), "§7/tl list §r- Show all players in list");
        sendFeedback(ctx.getSource(), "§7/tl toggle §r- Enable/disable auto-kick");
        sendFeedback(ctx.getSource(), "§7/tl message <msg> §r- Set kick message (use <name>)");
        sendFeedback(ctx.getSource(), "§7/tl refresh §r- Fetch remote UUIDs from GitHub");
        return 1;
    }

    private static int addPlayer(com.mojang.brigadier.context.CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> ctx, String name) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && client.level != null) {
            var uuid = AutoKickManager.resolveUuidFromTab(name);
            if (uuid != null) {
                AutoKickManager.INSTANCE.add(uuid, name);
                sendFeedback(ctx.getSource(), "§aAdded " + name + " to ThrowerList");
            } else {
                sendError(ctx.getSource(), "§cPlayer " + name + " not found in tab list");
            }
        }
        return 1;
    }

    private static int removePlayer(com.mojang.brigadier.context.CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> ctx, String name) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && client.level != null) {
            var uuid = AutoKickManager.resolveUuidFromTab(name);
            if (uuid != null) {
                AutoKickManager.INSTANCE.remove(uuid);
                sendFeedback(ctx.getSource(), "§aRemoved " + name + " from ThrowerList");
            } else {
                sendError(ctx.getSource(), "§cPlayer " + name + " not found in tab list");
            }
        }
        return 1;
    }

    private static int listPlayers(com.mojang.brigadier.context.CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> ctx) {
        var localUuids = AutoKickManager.INSTANCE.getLocalUuids();
        var remoteUuids = AutoKickManager.INSTANCE.getRemoteUuids();
        
        if (localUuids.isEmpty() && remoteUuids.isEmpty()) {
            sendFeedback(ctx.getSource(), "§7ThrowerList is empty");
        } else {
            sendFeedback(ctx.getSource(), "§eThrowerList (Local: " + localUuids.size() + ", Remote: " + remoteUuids.size() + "):");
            
            if (!localUuids.isEmpty()) {
                sendFeedback(ctx.getSource(), "§a§lLocal:");
                for (var uuid : localUuids) {
                    String name = AutoKickManager.INSTANCE.getName(uuid);
                    sendFeedback(ctx.getSource(), "§7 - " + name);
                }
            }
            
            if (!remoteUuids.isEmpty()) {
                sendFeedback(ctx.getSource(), "§b§lRemote:");
                for (var uuid : remoteUuids) {
                    String name = AutoKickManager.INSTANCE.getName(uuid);
                    sendFeedback(ctx.getSource(), "§7 - " + name);
                }
            }
        }
        return 1;
    }

    private static int toggleEnabled(com.mojang.brigadier.context.CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> ctx) {
        boolean enabled = !ThrowerListConfig.enabled;
        ThrowerListConfig.enabled = enabled;
        ThrowerListConfig.save();
        sendFeedback(ctx.getSource(), "§aThrowerList " + (enabled ? "enabled" : "disabled"));
        return 1;
    }

    private static int setMessage(com.mojang.brigadier.context.CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> ctx, String msg) {
        ThrowerListConfig.kickMessage = msg;
        ThrowerListConfig.save();
        sendFeedback(ctx.getSource(), "§aKick message set to: " + msg);
        return 1;
    }

    private static int refreshRemote(com.mojang.brigadier.context.CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> ctx) {
        sendFeedback(ctx.getSource(), "§eFetching remote UUIDs from GitHub...");
        AutoKickManager.INSTANCE.refreshRemoteAsync();
        return 1;
    }

    private static void sendFeedback(net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource source, String message) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            client.player.sendSystemMessage(Component.literal("[TL] ").withStyle(net.minecraft.ChatFormatting.DARK_GREEN)
                .append(Component.literal(message).withStyle(net.minecraft.ChatFormatting.DARK_GREEN)));
        } else {
            source.sendFeedback(Component.literal("[TL] ").withStyle(net.minecraft.ChatFormatting.DARK_GREEN)
                .append(Component.literal(message).withStyle(net.minecraft.ChatFormatting.DARK_GREEN)));
        }
    }

    private static void sendError(net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource source, String message) {
        source.sendError(Component.literal("[TL] ").withStyle(net.minecraft.ChatFormatting.DARK_GREEN)
            .append(Component.literal(message).withStyle(net.minecraft.ChatFormatting.RED)));
    }
}