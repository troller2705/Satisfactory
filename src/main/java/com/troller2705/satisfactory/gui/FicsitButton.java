package com.troller2705.satisfactory.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class FicsitButton extends Button {

    private final ResourceLocation spritesheet;
    private final int u, v;             // X, Y on the spritesheet (Normal state)
    private final int hoverV;           // Y on the spritesheet (Hovered state)
    private final int disabledV;        // Y on the spritesheet (Disabled state)
    private final int texWidth, texHeight; // Total size of the spritesheet file

    public FicsitButton(int x, int y, int width, int height, Component text,
                        ResourceLocation spritesheet, int u, int v, int hoverV, int disabledV,
                        int texWidth, int texHeight, OnPress onPress) {
        super(x, y, width, height, text, onPress, Button.DEFAULT_NARRATION);
        this.spritesheet = spritesheet;
        this.u = u;
        this.v = v;
        this.hoverV = hoverV;
        this.disabledV = disabledV;
        this.texWidth = texWidth;
        this.texHeight = texHeight;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Determine which Y-coordinate to use based on the button's state
        int currentV = this.v;
        if (!this.active) {
            currentV = this.disabledV;
        } else if (this.isHovered) {
            currentV = this.hoverV;
        }

        // Draw the FICSIT button texture
        graphics.blit(this.spritesheet, this.getX(), this.getY(), this.u, currentV, this.width, this.height, this.texWidth, this.texHeight);

        // Draw the text inside the button
        int textColor = !this.active ? 0x555555 : (this.isHovered ? 0xFFFFFF : 0x222222);
        graphics.drawCenteredString(Minecraft.getInstance().font, this.getMessage(),
                this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, textColor);
    }
}