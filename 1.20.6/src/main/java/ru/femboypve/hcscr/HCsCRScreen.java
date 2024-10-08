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

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
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

    public HCsCRScreen(Screen parent) {
        super(Component.translatable("hcscr.screen.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addRenderableWidget(Checkbox.builder(Component.translatable("hcscr.screen.enabled"), font)
                .pos(width / 2 - 12 - font.width(Component.translatable("hcscr.screen.enabled")) / 2, 40)
                .selected(HCsCR.enabled)
                .onValueChange((checkbox, value) -> HCsCR.enabled = value)
                .build());
        addRenderableWidget(Checkbox.builder(Component.translatable("hcscr.screen.removeCrystals"), font)
                .pos(width / 2 - 12 - font.width(Component.translatable("hcscr.screen.removeCrystals")) / 2, 64)
                .selected(HCsCR.removeCrystals)
                .onValueChange((checkbox, value) -> HCsCR.removeCrystals = value)
                .build());
        addRenderableWidget(Checkbox.builder(Component.translatable("hcscr.screen.removeSlimes"), font)
                .pos(width / 2 - 12 - font.width(Component.translatable("hcscr.screen.removeSlimes")) / 2, 88)
                .selected(HCsCR.removeSlimes)
                .onValueChange((checkbox, value) -> HCsCR.removeSlimes = value)
                .build());
        addRenderableWidget(Checkbox.builder(Component.translatable("hcscr.screen.removeInteractions"), font)
                .pos(width / 2 - 12 - font.width(Component.translatable("hcscr.screen.removeInteractions")) / 2, 112)
                .selected(HCsCR.removeInteractions)
                .onValueChange((checkbox, value) -> HCsCR.removeInteractions = value)
                .build());
        addRenderableWidget(Checkbox.builder(Component.translatable("hcscr.screen.removeAnchors"), font)
                .pos(width / 2 - 12 - font.width(Component.translatable("hcscr.screen.removeAnchors")) / 2, 136)
                .selected(HCsCR.removeAnchors)
                .onValueChange((checkbox, value) -> HCsCR.removeAnchors = value)
                .build());
        addRenderableWidget(new Slider(width / 2 - 100, 160, 200, 20, HCsCR.delay, 0, 200,
                value -> HCsCR.delay = value,
                value -> CommonComponents.optionNameValue(Component.translatable("hcscr.screen.delay"), Component.literal(Integer.toString(value)))));
        addRenderableWidget(Checkbox.builder(Component.translatable("hcscr.screen.absolutePrecision"), font)
                        .pos(width / 2 - 12 - font.width(Component.translatable("hcscr.screen.absolutePrecision")) / 2, 184)
                .selected(HCsCR.absolutePrecision)
                .onValueChange((checkbox, value) -> HCsCR.absolutePrecision = value)
                .build());
        addRenderableWidget(Button.builder(CommonComponents.optionNameValue(
                Component.translatable("hcscr.screen.batching"),
                Component.translatable("hcscr.screen.batching.".concat(HCsCR.batching.id()))
        ), button -> {
            int i = HCsCR.batching.ordinal() + 1;
            if (i >= Batching.values().length) i = 0;
            HCsCR.batching = Batching.values()[i];
            button.setMessage(CommonComponents.optionNameValue(
                    Component.translatable("hcscr.screen.batching"),
                    Component.translatable("hcscr.screen.batching.".concat(HCsCR.batching.id()))
            ));
        }).bounds(width / 2 - 100, 208, 200, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> minecraft.setScreen(parent)).bounds(width / 2 - 75, height - 24, 150, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(font, title, width / 2, 10, -1);
        if (!HCsCR.serverDisabled) return;
        graphics.drawCenteredString(font, Component.translatable("hcscr.screen.serverDisabled"), width / 2, height - 34, 0xFFFF0000);
    }

    @Override
    public void removed() {
        super.removed();
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
