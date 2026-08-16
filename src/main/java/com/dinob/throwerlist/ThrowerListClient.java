package com.dinob.throwerlist;

import com.dinob.throwerlist.config.ThrowerListConfig;
import com.dinob.throwerlist.config.AutoKickStore;
import com.dinob.throwerlist.feature.AutoKickManager;
import com.dinob.throwerlist.gui.ThrowerListScreen;
import com.dinob.throwerlist.util.KeyBindingTokenUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;

public final class ThrowerListClient implements ClientModInitializer {
    private static final KeyMapping OPEN_GUI_KEY = new KeyMapping(
        "key.throwerlist.open_gui",
        InputConstants.Type.KEYSYM,
        KeyBindingTokenUtil.parseKeyCode("M"),
        KeyMapping.Category.register(Identifier.fromNamespaceAndPath("throwerlist", "general"))
    );

    @Override
    public void onInitializeClient() {
        ThrowerListConfig.load();
        AutoKickStore.loadLocalUuids().forEach(uuid -> AutoKickManager.INSTANCE.addLocal(uuid, null));
        AutoKickManager.INSTANCE.load();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                AutoKickManager.INSTANCE.tick(client);
            }
            while (OPEN_GUI_KEY.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new ThrowerListScreen(null));
                } else {
                    client.setScreen(null);
                }
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("throwerlist")
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
                    .executes(ctx -> refreshRemote(ctx)))
            );
        });
    }

    private static int showHelp(com.mojang.brigadier.context.CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> ctx) {
        sendFeedback(ctx.getSource(), "§e§lThrowerList Commands:");
        sendFeedback(ctx.getSource(), "§7/throwerlist §r- Open GUI");
        sendFeedback(ctx.getSource(), "§7/throwerlist help §r- Show this help");
        sendFeedback(ctx.getSource(), "§7/throwerlist add <player> §r- Add player to kick list");
        sendFeedback(ctx.getSource(), "§7/throwerlist remove <player> §r- Remove player from kick list");
        sendFeedback(ctx.getSource(), "§7/throwerlist list §r- Show all players in list");
        sendFeedback(ctx.getSource(), "§7/throwerlist toggle §r- Enable/disable auto-kick");
        sendFeedback(ctx.getSource(), "§7/throwerlist message <msg> §r- Set kick message (use <name>)");
        sendFeedback(ctx.getSource(), "§7/throwerlist refresh §r- Fetch remote UUIDs from GitHub");
        return 1;
    }

    private static int addPlayer(com.mojang.brigadier.context.CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> ctx, String name) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && client.level != null) {
            var uuid = AutoKickManager.resolveUuidFromTab(name);
            if (uuid != null) {
                AutoKickManager.INSTANCE.add(uuid, name);
                sendFeedback(ctx.getSource(), "§aAdded " + name + " (" + uuid + ") to ThrowerList");
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
        var uuids = AutoKickManager.INSTANCE.getLocalUuids();
        if (uuids.isEmpty()) {
            sendFeedback(ctx.getSource(), "§7ThrowerList is empty");
        } else {
            sendFeedback(ctx.getSource(), "§eThrowerList (" + uuids.size() + "):");
            for (var uuid : uuids) {
                String name = AutoKickManager.INSTANCE.getName(uuid);
                sendFeedback(ctx.getSource(), "§7 - " + name + " (" + uuid + ")");
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