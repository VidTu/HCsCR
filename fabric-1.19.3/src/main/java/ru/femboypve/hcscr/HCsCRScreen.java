/*
 * Copyright (c) 2023 Offenderify, VidTu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.femboypve.hcscr;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;
import java.util.function.IntFunction;

/**
 * ModMenu config screen class.
 *
 * @author Offenderify
 * @author VidTu
 */
public final class HCsCRScreen extends Screen {
    private final Screen parent;
    private Checkbox enabled;
    private Checkbox removeCrystals;
    private Checkbox removeSlimes;
    private Checkbox removeInteractions;
    private Checkbox removeAnchors;
    private Checkbox absolutePrecision;

    public HCsCRScreen(Screen parent) {
        super(Component.translatable("hcscr.screen.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addRenderableWidget(enabled = new Checkbox(
                width / 2 - 12 - font.width(Component.translatable("hcscr.screen.enabled")) / 2, 40,
                24 + font.width(Component.translatable("hcscr.screen.enabled")), 20,
                Component.translatable("hcscr.screen.enabled"), HCsCR.enabled));
        addRenderableWidget(removeCrystals = new Checkbox(
                width / 2 - 12 - font.width(Component.translatable("hcscr.screen.removeCrystals")) / 2, 64,
                24 + font.width(Component.translatable("hcscr.screen.removeCrystals")), 20,
                Component.translatable("hcscr.screen.removeCrystals"), HCsCR.removeCrystals));
        addRenderableWidget(removeSlimes = new Checkbox(
                width / 2 - 12 - font.width(Component.translatable("hcscr.screen.removeSlimes")) / 2, 88,
                24 + font.width(Component.translatable("hcscr.screen.removeSlimes")), 20,
                Component.translatable("hcscr.screen.removeSlimes"), HCsCR.removeSlimes));
        addRenderableWidget(removeInteractions = new Checkbox(
                width / 2 - 12 - font.width(Component.translatable("hcscr.screen.removeInteractions")) / 2, 112,
                24 + font.width(Component.translatable("hcscr.screen.removeInteractions")), 20,
                Component.translatable("hcscr.screen.removeInteractions"), HCsCR.removeInteractions));
        addRenderableWidget(removeAnchors = new Checkbox(
                width / 2 - 12 - font.width(Component.translatable("hcscr.screen.removeAnchors")) / 2, 136,
                24 + font.width(Component.translatable("hcscr.screen.removeAnchors")), 20,
                Component.translatable("hcscr.screen.removeAnchors"), HCsCR.removeAnchors));
        addRenderableWidget(new Slider(width / 2 - 100, 160, 200, 20,
                HCsCR.delay, 0, 200, value -> HCsCR.delay = value,
                value -> CommonComponents.optionNameValue(Component.translatable("hcscr.screen.delay"),
                        Component.literal(Integer.toString(value)))));
        addRenderableWidget(absolutePrecision = new Checkbox(
                width / 2 - 12 - font.width(Component.translatable("hcscr.screen.absolutePrecision")) / 2, 184,
                24 + font.width(Component.translatable("hcscr.screen.absolutePrecision")), 20,
                Component.translatable("hcscr.screen.absolutePrecision"), HCsCR.absolutePrecision));
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> minecraft.setScreen(parent)).bounds(width / 2 - 75, height - 24, 150, 20).build());
        removeInteractions.setTooltip(Tooltip.create(Component.translatable("hcscr.screen.removeInteractions.version")));
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        renderBackground(stack);
        super.render(stack, mouseX, mouseY, delta);
        drawCenteredString(stack, font, title, width / 2, 10, -1);
        if (!HCsCR.serverDisabled) return;
        drawCenteredString(stack, font, Component.translatable("hcscr.screen.serverDisabled"), width / 2, height - 34, 0xFFFF0000);
    }

    @Override
    public void removed() {
        super.removed();
        HCsCR.enabled = enabled.selected();
        HCsCR.removeCrystals = removeCrystals.selected();
        HCsCR.removeSlimes = removeSlimes.selected();
        HCsCR.removeInteractions = removeInteractions.selected();
        HCsCR.removeAnchors = removeAnchors.selected();
        HCsCR.absolutePrecision = absolutePrecision.selected();
        HCsCR.saveConfig(FabricLoader.getInstance().getConfigDir());
    }

    private static final class Slider extends AbstractSliderButton {
        private final int min;
        private final int max;
        private final IntConsumer handler;
        private final IntFunction<Component> messageProvider;

        public Slider(int x, int y, int width, int height, int value, int min, int max,
                      IntConsumer handler, IntFunction<Component> messageProvider) {
            super(x, y, width, height, messageProvider.apply(value), (value - min) / (double) (max - min));
            this.min = min;
            this.max = max;
            this.handler = handler;
            this.messageProvider = messageProvider;
        }

        @Override
        protected void updateMessage() {
            int clamped = (int) (min + value * (max - min));
            if (clamped < min) clamped = min;
            if (clamped > max) clamped = max;
            setMessage(messageProvider.apply(clamped));
        }

        @Override
        protected void applyValue() {
            int clamped = (int) (min + value * (max - min));
            if (clamped < min) clamped = min;
            if (clamped > max) clamped = max;
            handler.accept(clamped);
        }
    }
}
