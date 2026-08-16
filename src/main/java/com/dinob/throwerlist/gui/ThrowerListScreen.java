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
    private static final int PANEL_WIDTH = 420;
    private static final int PANEL_HEIGHT = 380;
    private static final int HEADER_HEIGHT = 40;
    private static final int GUI_BTN_HEIGHT = 22;
    private static final int GUI_FIELD_HEIGHT = 18;
    private static final int GUI_ROW_STEP = 34;
    private static final int GUI_LABEL_GAP = 14;
    private static final int GUI_SECTION_GAP = 8;

    private static final int COLOR_BACKDROP_TOP = 0xE3070B10;
    private static final int COLOR_BACKDROP_BOTTOM = 0xF111171E;
    private static final int COLOR_PANEL = 0xF1151C24;
    private static final int COLOR_PANEL_BORDER = 0xFF3A4A5C;
    private static final int COLOR_PANEL_INNER = 0xF119222B;
    private static final int COLOR_TITLE = 0xFFF4F8FB;
    private static final int COLOR_TEXT = 0xFFDBE7F1;
    private static final int COLOR_MUTED = 0xFF8FA7BC;

    private final Screen parent;
    private EditBox kickMessageField;
    private EditBox delayField;
    private EditBox addPlayerField;
    private int panelX, panelY, panelW, panelH, contentX, contentW, contentY;

    public ThrowerListScreen(Screen parent) {
        super(Component.literal("ThrowerList"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.panelW = Math.min(PANEL_WIDTH, Math.max(420, this.width - 24));
        this.panelH = Math.min(PANEL_HEIGHT, Math.max(380, this.height - 24));
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;
        this.contentX = panelX + 16;
        this.contentW = panelW - 32;
        this.contentY = panelY + HEADER_HEIGHT + 8;

        int y = contentY;
        int fieldWidth = contentW;

        // Kick Message
        kickMessageField = textField(contentX, y, fieldWidth, 100, ThrowerListConfig.kickMessage, s -> ThrowerListConfig.kickMessage = s);
        y += GUI_ROW_STEP;
        this.addRenderableWidget(Button.builder(Component.literal("Reset to Default"), b -> resetMessage())
            .bounds(contentX, y, 120, GUI_BTN_HEIGHT).build());

        y += GUI_ROW_STEP + GUI_SECTION_GAP;

        // Kick Delay
        delayField = textField(contentX, y, 120, 5, Integer.toString(ThrowerListConfig.kickDelayTicks), s -> { try { ThrowerListConfig.kickDelayTicks = Integer.parseInt(s); } catch (NumberFormatException ignored) {} });
        y += GUI_ROW_STEP;
        this.addRenderableWidget(Button.builder(Component.literal("Reset to Default"), b -> resetDelay())
            .bounds(contentX, y, 120, GUI_BTN_HEIGHT).build());

        y += GUI_ROW_STEP + GUI_SECTION_GAP;

        // Refresh Button
        this.addRenderableWidget(Button.builder(Component.literal("Refresh from GitHub"), b -> refreshRemote())
            .bounds(contentX, y, contentW, GUI_BTN_HEIGHT).build());

        y += GUI_ROW_STEP + GUI_SECTION_GAP;

        // Add Player
        addPlayerField = textField(contentX, y, contentW - 70, 32, "", s -> {});
        y += GUI_ROW_STEP;
        this.addRenderableWidget(Button.builder(Component.literal("Add"), b -> addPlayer())
            .bounds(contentX + contentW - 65, y, 60, GUI_BTN_HEIGHT).build());

        y += GUI_ROW_STEP + GUI_SECTION_GAP;

        // Toggle + Save
        this.addRenderableWidget(Button.builder(
            Component.literal(ThrowerListConfig.enabled ? "Enabled" : "Disabled"),
            b -> toggleEnabled())
            .bounds(contentX, y, 110, GUI_BTN_HEIGHT).build());

        this.addRenderableWidget(Button.builder(Component.literal("Save & Close"), b -> onClose())
            .bounds(contentX + contentW - 100, y, 100, GUI_BTN_HEIGHT).build());
    }

    private EditBox textField(int x, int y, int w, int maxLen, String initial, java.util.function.Consumer<String> onChange) {
        EditBox field = this.addRenderableWidget(new EditBox(this.font, x, y, w, 18, Component.empty()));
        field.setMaxLength(maxLen);
        field.setValue(initial == null ? "" : initial);
        field.setResponder(onChange);
        return field;
    }

    private void addPlayer() {
        String name = addPlayerField.getValue().trim();
        if (name.isEmpty()) return;
        var uuid = AutoKickManager.resolveUuidFromTab(name);
        if (uuid != null) {
            AutoKickManager.INSTANCE.add(uuid, name);
            addPlayerField.setValue("");
        }
    }

    private void toggleEnabled() {
        ThrowerListConfig.enabled = !ThrowerListConfig.enabled;
        ThrowerListConfig.save();
        init();
    }

    private void resetMessage() {
        ThrowerListConfig.kickMessage = "autokicked <name>";
        if (kickMessageField != null) kickMessageField.setValue(ThrowerListConfig.kickMessage);
    }

    private void resetDelay() {
        ThrowerListConfig.kickDelayTicks = 20;
        if (delayField != null) delayField.setValue("20");
    }

    private void refreshRemote() {
        AutoKickManager.INSTANCE.refreshRemoteAsync();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        // Backdrop
        context.fillGradient(0, 0, this.width, this.height, 0xE3070B10, 0xF111171E);

        // Panel
        int panelX = this.panelX;
        int panelY = this.panelY;
        int panelW = this.panelW;
        int panelH = this.panelH;

        context.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xF1151C24);
        context.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + panelH - 1, 0xF119222B);
        context.fill(panelX, panelY, panelX + panelW, panelY + 1, 0xFF3A4A5C);
        context.fill(panelX, panelY + panelH - 1, panelX + panelW, panelY + panelH, 0xFF3A4A5C);
        context.fill(panelX, panelY, panelX + 1, panelY + panelH, 0xFF3A4A5C);
        context.fill(panelX + panelW - 1, panelY, panelX + panelW, panelY + panelH, 0xFF3A4A5C);

        // Header
        context.fill(panelX, panelY, panelX + panelW, panelY + 40, 0xF1151C24);
        context.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + 39, 0xF119222B);
        context.centeredText(this.font, this.title, this.width / 2, panelY + 12, 0xFFF4F8FB);

        // Labels above fields
        if (kickMessageField != null) {
            int x = kickMessageField.getX();
            int y = kickMessageField.getY();
            context.text(this.font, Component.literal("Kick Message").withStyle(net.minecraft.ChatFormatting.DARK_GREEN), x, y - 14, 0xFF8FA7BC);
            context.text(this.font, Component.literal("Use <name> or {name> for player name").withStyle(net.minecraft.ChatFormatting.DARK_GRAY), x, y + 18 + 4, 0xFF888888);
        }
        if (delayField != null) {
            int x = delayField.getX();
            int y = delayField.getY();
            context.text(this.font, Component.literal("Kick Delay (ticks)").withStyle(net.minecraft.ChatFormatting.DARK_GREEN), x, y - 14, 0xFF8FA7BC);
        }
        if (addPlayerField != null) {
            int x = addPlayerField.getX();
            int y = addPlayerField.getY();
            context.text(this.font, Component.literal("Add Player (exact name)").withStyle(net.minecraft.ChatFormatting.DARK_GREEN), x, y - 14, 0xFF8FA7BC);
        }

        // Status
        String status = ThrowerListConfig.enabled ? "§aEnabled" : "§cDisabled";
        context.text(this.font, Component.literal("Status: " + status), this.contentX, panelY + panelH - 28, 0xFF8FA7BC);

        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        ThrowerListConfig.save();
        if (this.minecraft != null) this.minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}