package com.direwolf20.buildinggadgets2.client.screen.widgets;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;
import java.util.function.Predicate;

/**
 * A one stop shop for all your icon gui related needs. We support colors,
 * icons, selected and deselected states, sound and loads more. Come on
 * down!
 */
public class GuiIconActionable extends Button {
    private Predicate<Boolean> action;
    private boolean selected;
    private boolean isSelectable;

    private final Color selectedColor = new Color(0, 255, 0, 50);
    private final Color deselectedColor = new Color(255, 255, 255, 50);
    private Color activeColor;

    private final ResourceLocation selectedTexture;
    private final ResourceLocation deselectedTexture;

    public GuiIconActionable(int x, int y, String texture, Component message, boolean isSelectable, Predicate<Boolean> action) {
        //super(builder(message, (button) -> {}).pos(x, y).size(25, 25));
        super(x, y, 25, 25, message, (button) -> {
        }, Button.DEFAULT_NARRATION);
        this.activeColor = deselectedColor;
        this.isSelectable = isSelectable;
        this.action = action;

        this.setSelected(action.test(false));

        // Set the selected and deselected textures.
        String assetLocation = "textures/gui/setting/%s.png";

        this.deselectedTexture = new ResourceLocation(BuildingGadgets2.MODID, String.format(assetLocation, texture));
        this.selectedTexture = !isSelectable ? this.deselectedTexture : new ResourceLocation(BuildingGadgets2.MODID, String.format(assetLocation, texture + "_selected"));
    }

    /**
     * If yo do not need to be able to select / toggle something then use this constructor as
     * you'll hit missing texture issues if you don't have an active (_selected) texture.
     */
    public GuiIconActionable(int x, int y, String texture, Component message, Predicate<Boolean> action) {
        this(x, y, texture, message, false, action);
    }

    public void setFaded(boolean faded) {
        alpha = faded ? .6f : 1f;
    }

    /**
     * This should be used when ever-changing select.
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
        this.activeColor = selected ? selectedColor : deselectedColor;
    }

    @Override
    public void playDownSound(SoundManager soundHandler) {
        soundHandler.play(SimpleSoundInstance.forUI(Registration.BEEP.get(), selected ? .6F : 1F));
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        this.action.test(true);

        if (!this.isSelectable)
            return;

        this.setSelected(!this.selected);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!visible)
            return;

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, activeColor.getRGB());

//        RenderSystem.setShaderColor(activeColor.getRGB().getRed() / 255f, activeColor.getGreen() / 255f, activeColor.getBlue() / 255f, .15f);
//        fill(matrices, this.x, this.y, this.x + this.width, this.y + this.height, -1873784752);

        RenderSystem.setShaderTexture(0, selected ? selectedTexture : deselectedTexture);
        RenderSystem.setShaderColor(activeColor.getRed() / 255f, activeColor.getGreen() / 255f, activeColor.getBlue() / 255f, alpha);

        ResourceLocation texture = selected ? selectedTexture : deselectedTexture;
        guiGraphics.blit(texture, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();

        if (mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height)
            guiGraphics.drawString(Minecraft.getInstance().font, this.getMessage().getString(), mouseX > (Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2) ? mouseX + 2 : mouseX - Minecraft.getInstance().font.width(getMessage().getString()), mouseY - 10, activeColor.getRGB() | 0xFF000000);

    }
}
