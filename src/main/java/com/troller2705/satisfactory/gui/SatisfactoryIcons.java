package com.troller2705.satisfactory.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.gui.element.ScreenElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.troller2705.satisfactory.Satisfactory;

public class SatisfactoryIcons implements ScreenElement
{

    public static final ResourceLocation ICON_ATLAS = ResourceLocation.fromNamespaceAndPath(Satisfactory.MODID, "textures/gui/icons.png");
    private static final int ICON_SIZE = 16;
    private static final int ATLAS_SIZE = 256;

    // Define your icons by grid coordinates (X, Y) where 0,0 is top-left
    public static final SatisfactoryIcons
            FICSIT_LOGO = new SatisfactoryIcons(0, 0),
            AWESOME_COUPON = new SatisfactoryIcons(1, 0),
            TRASH_CAN = new SatisfactoryIcons(2, 0),
            ARROW_RIGHT = new SatisfactoryIcons(0, 1);

    private final int x;
    private final int y;

    public SatisfactoryIcons(int x, int y) {
        this.x = x * ICON_SIZE;
        this.y = y * ICON_SIZE;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphics graphics, int x, int y) {
        RenderSystem.setShaderTexture(0, ICON_ATLAS);
        graphics.blit(ICON_ATLAS, x, y, 0, this.x, this.y, ICON_SIZE, ICON_SIZE, ATLAS_SIZE, ATLAS_SIZE);
    }
}