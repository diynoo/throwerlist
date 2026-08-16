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
    private static final int WIDTH = 380;
    private static final int HEIGHT = 320;
    private static final int MARGIN = 16;
    private static final int ROW_HEIGHT = 22;
    private static final int SECTION_GAP = 12;

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
        int cx = (this.width - WIDTH) / 2;
        int cy = (this.height - HEIGHT) / 2;
        int x = cx + MARGIN;
        int y = cy + MARGIN;
        int fieldWidth = WIDTH - 2 * MARGIN;

        // Title handled in render

        // Section 1: Kick Message
        y += 28;
        kickMessageField = new EditBox(this.font, x, y, fieldWidth, 20, Component.literal("Kick Message"));
        kickMessageField.setValue(ThrowerListConfig.kickMessage);
        kickMessageField.setResponder(s -> ThrowerListConfig.kickMessage = s);
        this.addRenderableWidget(kickMessageField);

        y += ROW_HEIGHT + 4;
        this.addRenderableWidget(Button.builder(Component.literal("Reset to Default"), b -> resetMessage())
            .bounds(x, y, fieldWidth, 18).build());

        y += ROW_HEIGHT + SECTION_GAP;

        // Section 2: Kick Delay
        delayField = new EditBox(this.font, x, y, 100, 20, Component.literal("Delay"));
        delayField.setValue(Integer.toString(ThrowerListConfig.kickDelayTicks));
        delayField.setResponder(s -> { try { ThrowerListConfig.kickDelayTicks = Integer.parseInt(s); } catch (NumberFormatException ignored) {} });
        this.addRenderableWidget(delayField);

        y += ROW_HEIGHT + 4;
        this.addRenderableWidget(Button.builder(Component.literal("Reset to Default"), b -> resetDelay())
            .bounds(x, y, 100, 18).build());

        y += ROW_HEIGHT + SECTION_GAP;

        // Section 3: Remote URL
        this.addRenderableWidget(Button.builder(Component.literal("Refresh from GitHub"), b -> refreshRemote())
            .bounds(x, y, fieldWidth, 20).build());

        y += ROW_HEIGHT + SECTION_GAP;

        // Section 4: Add Player
        addPlayerField = new EditBox(this.font, x, y, fieldWidth - 70, 20, Component.literal("Add player"));
        this.addRenderableWidget(addPlayerField);

        this.addRenderableWidget(Button.builder(Component.literal("Add"), b -> addPlayer())
            .bounds(x + fieldWidth - 65, y, 60, 20).build());

        y += ROW_HEIGHT + SECTION_GAP;

        // Section 5: Toggle + Save
        this.addRenderableWidget(Button.builder(
            Component.literal(ThrowerListConfig.enabled ? "Enabled" : "Disabled"),
            b -> toggleEnabled())
            .bounds(x, y, 110, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Save & Close"), b -> onClose())
            .bounds(x + fieldWidth - 100, y, 100, 20).build());
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
        int cx = (this.width - WIDTH) / 2;
        int cy = (this.height - HEIGHT) / 2;
        int x = cx + MARGIN;
        int y = cy + MARGIN;

        // Background
        context.fill(cx, cy, cx + WIDTH, cy + HEIGHT, 0xFF181824);
        context.fill(cx, cy, cx + WIDTH, cy + 28, 0xFF0F141E);

        // Title
        String titleText = "ThrowerList";
        int titleWidth = this.font.width(titleText);
        context.fill(cx + WIDTH / 2 - titleWidth / 2 - 8, cy + 6, cx + WIDTH / 2 + titleWidth / 2 + 8, cy + 22, 0x88000000);
        context.text(this.font, Component.literal("ThrowerList"), cx + WIDTH / 2 - titleWidth / 2, cy + 10, 0xFF4FC3F7);

        // Subtitle
        context.text(this.font, Component.literal("Auto-Kick List Manager"), cx + WIDTH / 2 - this.font.width("Auto-Kick List Manager") / 2, cy + HEIGHT - 10, 0xFF888888);

        // Section headers and fields are rendered by widgets
        // Just render section labels
        int labelY = cy + MARGIN + 28 - 18;
        context.text(this.font, Component.literal("§7Kick Message"), x, labelY, 0xFFBBBBBB);

        labelY = cy + MARGIN + 28 + ROW_HEIGHT + 4 + 20 + SECTION_GAP - 18;
        context.text(this.font, Component.literal("§7Kick Delay (ticks)"), x, labelY, 0xFFBBBBBB);

        labelY = labelY + ROW_HEIGHT + SECTION_GAP + 20 + SECTION_GAP - 18;
        context.text(this.font, Component.literal("§7Remote UUIDs"), x, labelY, 0xFFBBBBBB);

        labelY = labelY + ROW_HEIGHT + SECTION_GAP + 20 + SECTION_GAP - 18;
        context.text(this.font, Component.literal("§7Add Player"), x, labelY, 0xFFBBBBBB);

        // Hint for kick message
        context.text(this.font, Component.literal("§8Use <name> or {name} for player name"), x, cy + MARGIN + 28 + 20 + 4, 0xFF777777);

        // Status indicator
        String status = ThrowerListConfig.enabled ? "§aEnabled" : "§cDisabled";
        context.text(this.font, Component.literal("Status: " + status), x, cy + HEIGHT - MARGIN - 30, 0xFFBBBBBB);

        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (super.mouseClicked(click, doubled)) return true;
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        return false;
    }

    @Override
    public void onClose() {
        ThrowerListConfig.save();
        if (this.minecraft != null) this.minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}