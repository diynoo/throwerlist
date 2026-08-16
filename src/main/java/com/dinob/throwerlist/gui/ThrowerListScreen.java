package com.dinob.throwerlist.gui;

import com.dinob.throwerlist.config.ThrowerListConfig;
import com.dinob.throwerlist.feature.AutoKickManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class ThrowerListScreen extends Screen {
    private static final int WIDTH = 320;
    private static final int HEIGHT = 240;

    private final Screen parent;
    private EditBox kickMessageField;
    private EditBox delayField;
    private EditBox addPlayerField;
    private final AutoKickManager manager = AutoKickManager.INSTANCE;
    private int listScroll = 0;

    public ThrowerListScreen(Screen parent) {
        super(Component.literal("ThrowerList"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = (this.width - WIDTH) / 2;
        int y = (this.height - HEIGHT) / 2;

        kickMessageField = new EditBox(this.font, x + 10, y + 30, 300, 18, Component.literal("Kick Message"));
        kickMessageField.setValue(ThrowerListConfig.kickMessage);
        kickMessageField.setResponder(s -> ThrowerListConfig.kickMessage = s);
        this.addRenderableWidget(kickMessageField);

        delayField = new EditBox(this.font, x + 10, y + 55, 80, 18, Component.literal("Delay"));
        delayField.setValue(Integer.toString(ThrowerListConfig.kickDelayTicks));
        delayField.setResponder(s -> { try { ThrowerListConfig.kickDelayTicks = Integer.parseInt(s); } catch (NumberFormatException ignored) {} });
        this.addRenderableWidget(delayField);

        addPlayerField = new EditBox(this.font, x + 10, y + 180, 230, 18, Component.literal("Add player"));
        this.addRenderableWidget(addPlayerField);

        this.addRenderableWidget(Button.builder(Component.literal("Add"), b -> addPlayer())
            .bounds(x + 250, y + 180, 60, 18).build());

        this.addRenderableWidget(Button.builder(
            Component.literal(ThrowerListConfig.enabled ? "Enabled" : "Disabled"),
            b -> toggleEnabled())
            .bounds(x + 10, y + 205, 100, 18).build());

        this.addRenderableWidget(Button.builder(Component.literal("Save & Close"), b -> onClose())
            .bounds(x + 220, y + 205, 90, 18).build());
    }

    private void addPlayer() {
        String name = addPlayerField.getValue().trim();
        if (name.isEmpty()) return;
        var uuid = AutoKickManager.resolveUuidFromTab(name);
        if (uuid != null) {
            manager.add(uuid, name);
            addPlayerField.setValue("");
        }
    }

    private void toggleEnabled() {
        ThrowerListConfig.enabled = !ThrowerListConfig.enabled;
        ThrowerListConfig.save();
        init();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        int x = (this.width - WIDTH) / 2;
        int y = (this.height - HEIGHT) / 2;

        context.fill(x, y, x + WIDTH, y + HEIGHT, 0xFF1A1A2E);
        context.fill(x, y, x + WIDTH, y + 24, 0xFF16213E);
        context.text(this.font, this.title, x + WIDTH / 2 - this.font.width(this.title.getString()) / 2, y + 6, 0xFFFFFFFF);

        context.text(this.font, Component.literal("Kick Message"), x + 10, y + 20, 0xFFAAAAAA);
        context.text(this.font, Component.literal("Use <name> or {name> for player"), x + 10, y + 78, 0xFF888888);
        context.text(this.font, Component.literal("Kick Delay (ticks)"), x + 10, y + 50, 0xFFAAAAAA);

        int listY = y + 100;
        int listHeight = 100;
        context.fill(x + 10, listY, x + 310, listY + listHeight, 0xFF0F0F1A);

        var uuids = manager.getLocalUuids();
        int i = 0;
        for (var uuid : uuids) {
            if (i < listScroll) { i++; continue; }
            int rowY = listY + 2 + (i - listScroll) * 18;
            if (rowY > listY + listHeight - 18) break;

            String name = manager.getName(uuid);
            context.text(this.font, Component.literal("§e" + name + " §7(" + uuid + ")"), x + 14, rowY, 0xFFFFFFFF);

            int btnX = x + 266;
            context.fill(btnX - 2, rowY - 1, btnX + 40, rowY + 15, 0xFF333344);
            context.text(this.font, Component.literal("Remove"), btnX, rowY + 2, 0xFFFF5555);

            i++;
        }

        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (super.mouseClicked(click, doubled)) return true;

        int x = (this.width - WIDTH) / 2;
        int y = (this.height - HEIGHT) / 2;
        var uuids = manager.getLocalUuids();
        int i = 0;
        int listY = y + 100;
        for (var uuid : uuids) {
            if (i < listScroll) { i++; continue; }
            int rowY = listY + 2 + (i - listScroll) * 18;
            int btnX = x + 266 - 2;
            if (click.x() >= btnX && click.x() <= btnX + 40 && click.y() >= rowY - 1 && click.y() <= rowY + 15) {
                manager.remove(uuid);
                return true;
            }
            if (rowY > listY + 120) break;
            i++;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        int maxScroll = Math.max(0, manager.getLocalUuids().size() - 5);
        if (vertical < 0) listScroll = Math.min(listScroll + 1, maxScroll);
        else if (vertical > 0) listScroll = Math.max(listScroll - 1, 0);
        return true;
    }

    @Override
    public void onClose() {
        ThrowerListConfig.save();
        if (this.minecraft != null) this.minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}