package com.troller2705.satisfactory.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.troller2705.satisfactory.Satisfactory;
import com.troller2705.satisfactory.integration.FTBQuestsBridge;
import com.troller2705.satisfactory.menu.HubTerminalMenu;
import com.troller2705.satisfactory.network.SubmitMilestonePayload;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class HubTerminalScreen extends AbstractContainerScreen<HubTerminalMenu> {

    private static final ResourceLocation TERMINAL_BG = ResourceLocation.fromNamespaceAndPath(Satisfactory.MODID, "textures/gui/hub_terminal.png");

    private static final ResourceLocation SPRITESHEET = ResourceLocation.fromNamespaceAndPath(Satisfactory.MODID, "textures/gui/ficsit_spritesheet.png");

    private static final int TEX_WIDTH = 1000;
    private static final int TEX_HEIGHT = 954;
    private static final int FICSIT_ORANGE = 0xFA9549;
    private static final int TEXT_WHITE = 0xFFFFFF;

    private enum Tab { SELECT, ACTIVE }
    private Tab currentTab = Tab.SELECT;

    private List<Chapter> availableTiers;
    private Chapter activeTier;
    private List<Quest> activeMilestones;
    private Quest activeMilestone;
    private Quest selectedMilestone;

    private boolean hasLoadedSavedData = false;

    public HubTerminalScreen(HubTerminalMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        // Lock the logical GUI size so slot clicks perfectly match the visuals
        this.imageWidth = 250;
        this.imageHeight = 238;
        this.inventoryLabelY = 10000; // Hides the default "Inventory" text
        this.titleLabelY = 10000;     // Hides the default title text
    }

    @Override
    protected void init() {
        super.init(); // This automatically calculates this.leftPos and this.topPos

        if (!this.hasLoadedSavedData) {
            // 1. Load the browsing data
            this.availableTiers = FTBQuestsBridge.getHubTiers();
            if (this.availableTiers != null && !this.availableTiers.isEmpty()) {
                this.activeTier = this.availableTiers.get(0);
                this.activeMilestones = FTBQuestsBridge.getMilestonesForTier(this.activeTier);
                if (this.activeMilestones != null && !this.activeMilestones.isEmpty()) {
                    this.selectedMilestone = this.activeMilestones.get(0);
                }
            }

            // 2. Restore the saved milestone from the Block Entity
            long savedQuestId = this.menu.blockEntity.activeQuestId;
            if (savedQuestId != 0L) {
                this.activeMilestone = FTBQuestsBridge.getQuestById(savedQuestId);
                // If they already have one locked in, open straight to the Active tab!
                if (this.activeMilestone != null) {
                    this.currentTab = Tab.ACTIVE;
                }
            }

            this.hasLoadedSavedData = true;
        }

        // Global UI Widgets
        this.addRenderableWidget(new FicsitButton(
                this.leftPos + this.imageWidth - 25, this.topPos + 5, 20, 20, Component.literal("X"),
                SPRITESHEET,
                0, 0,   // U, V for normal red button
                20, 0,  // U, V for hovered button
                40, 0,  // U, V for disabled button
                btn -> this.onClose()
        ));

        // Tab Buttons
        this.addRenderableWidget(new FicsitButton(
                this.leftPos + 10, this.topPos + 10, 110, 20, Component.literal("Select Milestone"),
                SPRITESHEET, 0, 50, 0, 75, 0, 100,
                btn -> switchTab(Tab.SELECT)
        )).active = (currentTab != Tab.SELECT);

        this.addRenderableWidget(new FicsitButton(
                this.leftPos + 125, this.topPos + 10, 110, 20, Component.literal("Active Milestone"),
                SPRITESHEET, 0, 50, 0, 75, 0, 100,
                btn -> switchTab(Tab.ACTIVE)
        )).active = (currentTab != Tab.ACTIVE);

        int btnWidth = (int) (this.imageWidth * 0.4);
        int btnX = this.leftPos + (this.imageWidth / 2) - (btnWidth / 2);

        // Action Buttons
        if (currentTab == Tab.SELECT) {
            this.addRenderableWidget(new FicsitButton(
                    btnX, this.topPos + 205, btnWidth, 20, Component.literal("LOCK MILESTONE"),
                    SPRITESHEET, 0, 150, 0, 175, 0, 200,
                    btn -> {
                        // ... your packet sending logic
                        switchTab(Tab.ACTIVE);
                    }
            )).active = (this.selectedMilestone != null);
        } else {
            // The massive LAUNCH POD button
            this.addRenderableWidget(new FicsitButton(
                    btnX, this.topPos + 95, btnWidth, 30, Component.literal("LAUNCH POD"),
                    SPRITESHEET, 150, 150, 150, 185, 150, 220,
                    btn -> submitMilestone()
            ));
        }

        updateSlotVisibility();
    }

    private void switchTab(Tab newTab) {
        this.currentTab = newTab;
        this.clearWidgets();
        this.init();
    }
    // Replaces the old method that caused the 'final' variable crash
    private void updateSlotVisibility() {
        // Simply flips the switch on the Menu class based on the active tab!
        this.menu.slotsActive = (currentTab == Tab.ACTIVE);
    }

    private void submitMilestone() {
        if (this.selectedMilestone != null) {
            // Grab the block position directly from the Menu's BlockEntity reference
            BlockPos pos = this.menu.blockEntity.getBlockPos();

            // Send the updated payload with the location and the Quest ID
            PacketDistributor.sendToServer(new SubmitMilestonePayload(pos, this.selectedMilestone.id));

            Minecraft.getInstance().player.displayClientMessage(Component.literal("Pod Launched!"), true);
        }
    }

    // 1.21.1 requires overriding renderBackground to kill the blur
    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0x99000000);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TERMINAL_BG);
        graphics.blit(TERMINAL_BG, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, 0, 0, TEX_WIDTH, TEX_HEIGHT, TEX_WIDTH, TEX_HEIGHT);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick); // Draws background, buttons, and SLOTS
        this.renderTooltip(graphics, mouseX, mouseY);        // Draws item tooltips when hovering
    }

    // Draws text layered on top of everything
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.selectedMilestone == null) return;

        int centerX = this.imageWidth / 2;
        Component title = this.selectedMilestone.getTitle();

        if (currentTab == Tab.SELECT) {
            graphics.drawCenteredString(this.font, title, centerX, 40, FICSIT_ORANGE);
            drawQuestPreview(graphics, this.selectedMilestone, 60);
        } else {
            graphics.drawCenteredString(this.font, title, centerX, 40, FICSIT_ORANGE);
            drawQuestPreview(graphics, this.selectedMilestone, 60);
            graphics.drawString(this.font, "Deposit Requirements:", 44, 105, TEXT_WHITE, false);
        }
    }

    private void drawQuestPreview(GuiGraphics graphics, Quest questToDraw, int startY) {
        graphics.drawString(this.font, "Costs:", 44, startY, TEXT_WHITE, false);
        int cx = 80;
        for (dev.ftb.mods.ftbquests.quest.task.Task task : questToDraw.getTasks()) {
            task.getIcon().draw(graphics, cx, startY - 4, 16, 16);
            String amount = String.valueOf(task.getMaxProgress());
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 200);
            graphics.drawString(this.font, amount, cx + 17 - this.font.width(amount), startY + 5, TEXT_WHITE, true);
            graphics.pose().popPose();
            cx += 24;
        }

        graphics.drawString(this.font, "Rewards:", 150, startY, TEXT_WHITE, false);
        int rx = 195;
        for (dev.ftb.mods.ftbquests.quest.reward.Reward reward : questToDraw.getRewards()) {
            reward.getIcon().draw(graphics, rx, startY - 4, 16, 16);
            rx += 24;
        }
    }
}