package ru.offenderify.hcscr;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class HCsCRScreen extends Screen {
    private final Screen parent;
    private Checkbox enabled;

    public HCsCRScreen(Screen parent) {
        super(Component.translatable("hcscr.screen.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addRenderableWidget(enabled = new Checkbox(width / 2 - font.width(Component.translatable("hcscr.screen.enabled")), 40, 24 + font.width(Component.translatable("hcscr.screen.enabled")), 20, Component.translatable("hcscr.screen.enabled"), HCsCR.enabled));
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> minecraft.setScreen(parent)).bounds(width / 2 - 75, height - 24, 150, 20).build());
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        renderBackground(stack);
        super.render(stack, mouseX, mouseY, delta);
        drawCenteredString(stack, font, title, width / 2, 10, -1);
        if (HCsCR.disabledByCurrentServer) {
            drawCenteredString(stack, font, Component.translatable("hcscr.screen.serverDisabled"), width / 2, 70, 0xFFFF0000);
        }
    }

    @Override
    public void removed() {
        super.removed();
        HCsCR.enabled = enabled.isActive();
        HCsCR.saveConfig();
    }
}
