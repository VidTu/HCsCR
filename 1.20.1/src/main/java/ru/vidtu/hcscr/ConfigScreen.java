/*
 * Copyright (c) 2023 Offenderify
 * Copyright (c) 2023-2025 VidTu
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

package ru.vidtu.hcscr;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.vidtu.hcscr.config.Batching;
import ru.vidtu.hcscr.config.HConfig;

import java.util.Objects;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;

/**
 * HCsCR config screen.
 *
 * @author Offenderify
 * @author VidTu
 */
final class ConfigScreen extends Screen {
    /**
     * Parent screen, {@code null} if none.
     */
    @Nullable
    private final Screen parent;

    /**
     * Disabled by server.
     */
    @NotNull
    private final Component disabled = Component.translatable("hcscr.config.disabled");

    /**
     * Creates a new screen.
     *
     * @param parent Parent screen, {@code null} if none
     */
    @Contract(pure = true)
    ConfigScreen(@Nullable Screen parent) {
        super(Component.translatable("hcscr.config"));
        this.parent = parent;
    }

    @ApiStatus.Internal
    @Override
    protected void init() {
        // Enabled.
        Checkbox box = new CallbackCheckbox(this.font, (this.width - this.font.width(Component.translatable("hcscr.config.enabled")) - 24) / 2, 20,
                Component.translatable("hcscr.config.enabled"), HConfig.enabled, value -> HConfig.enabled = value);
        box.setTooltip(Tooltip.create(Component.translatable("hcscr.config.enabled.tip")));
        box.setTooltipDelay(250);
        this.addRenderableWidget(box);

        // Remove Crystals.
        box = new CallbackCheckbox(this.font, (this.width - this.font.width(Component.translatable("hcscr.config.removeCrystals")) - 24) / 2, 44,
                Component.translatable("hcscr.config.removeCrystals"), HConfig.removeCrystals, value -> HConfig.removeCrystals = value);
        box.setTooltip(Tooltip.create(Component.translatable("hcscr.config.removeCrystals.tip")));
        box.setTooltipDelay(250);
        this.addRenderableWidget(box);

        // Remove Slimes.
        box = new CallbackCheckbox(this.font, (this.width - this.font.width(Component.translatable("hcscr.config.removeSlimes")) - 24) / 2, 68,
                Component.translatable("hcscr.config.removeSlimes"), HConfig.removeSlimes, value -> HConfig.removeSlimes = value);
        box.setTooltip(Tooltip.create(Component.translatable("hcscr.config.removeSlimes.tip")));
        box.setTooltipDelay(250);
        this.addRenderableWidget(box);

        // Remove Interactions.
        box = new CallbackCheckbox(this.font, (this.width - this.font.width(Component.translatable("hcscr.config.removeInteractions")) - 24) / 2, 92,
                Component.translatable("hcscr.config.removeInteractions"), HConfig.removeInteractions, value -> HConfig.removeInteractions = value);
        box.setTooltip(Tooltip.create(Component.translatable("hcscr.config.removeInteractions.tip")));
        box.setTooltipDelay(250);
        this.addRenderableWidget(box);

        // Remove Anchors.
        box = new CallbackCheckbox(this.font, (this.width - this.font.width(Component.translatable("hcscr.config.removeAnchors")) - 24) / 2, 116,
                Component.translatable("hcscr.config.removeAnchors"), HConfig.removeAnchors, value -> HConfig.removeAnchors = value);
        box.setTooltip(Tooltip.create(Component.translatable("hcscr.config.removeAnchors.tip")));
        box.setTooltipDelay(250);
        this.addRenderableWidget(box);

        // Delay.
        int delay = Math.max(0, Math.min(200, HConfig.delay));
        CallbackSlider slider = new CallbackSlider(this.width / 2 - 100, 140, 200, 20, delay, 0, 200,
                value -> HConfig.delay = value,
                value -> CommonComponents.optionNameValue(Component.translatable("hcscr.config.delay"), value > 0 ? Component.translatable("hcscr.config.delay.format", value) : Component.translatable("hcscr.config.delay.false")));
        slider.setTooltip(Tooltip.create(Component.translatable("hcscr.config.delay.tip")));
        slider.setTooltipDelay(250);
        this.addRenderableWidget(slider);

        // Batching.
        Batching batching = Objects.requireNonNullElse(HConfig.batching, Batching.DISABLED);
        Button button = Button.builder(CommonComponents.optionNameValue(Component.translatable("hcscr.config.batching"), Component.translatable(batching.toString())), btn -> {
            // Update the value.
            Batching newBatching = HConfig.batching = switch (HConfig.batching) {
                case DISABLED -> Batching.CONTAINING;
                case CONTAINING -> Batching.CONTAINING_CONTAINED;
                case CONTAINING_CONTAINED -> Batching.INTERSECTING;
                default -> Batching.DISABLED;
            };

            // Set the message and tooltip.
            btn.setMessage(CommonComponents.optionNameValue(Component.translatable("hcscr.config.batching"), Component.translatable(newBatching.toString())));
            btn.setTooltip(Tooltip.create(Component.translatable(newBatching + ".tip")));
            btn.setTooltipDelay(250);
        }).bounds(this.width / 2 - 100, 164, 200, 20).tooltip(Tooltip.create(Component.translatable(batching + ".tip"))).build();
        button.setTooltipDelay(250);
        this.addRenderableWidget(button);

        // Add done button.
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, btn -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 24, 200, 20)
                .build());
    }

    @ApiStatus.Internal
    @Override
    public void onClose() {
        // Bruh.
        assert this.minecraft != null;

        // Save config.
        HConfig.saveOrLog();

        // Close to parent.
        this.minecraft.setScreen(this.parent);
    }

    @ApiStatus.Internal
    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Render background and widgets.
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, delta);

        // Render title.
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 5, 0xFF_FF_FF_FF);

        // Render the "disabled" label.
        if (HCsCR.serverEnabled()) return;
        graphics.drawCenteredString(this.font, this.disabled, this.width / 2, this.height - 34, 0xFFFF0000);
    }

    /**
     * Dynamic slider implementation with handler and message provider.
     *
     * @author VidTu
     */
    static final class CallbackSlider extends AbstractSliderButton {
        /**
         * Minimum slider value.
         */
        private final int min;

        /**
         * Maximum slider value.
         */
        private final int max;

        /**
         * Slider change handler.
         */
        @NotNull
        private final IntConsumer handler;

        /**
         * Slider message provider.
         */
        @NotNull
        private final IntFunction<Component> provider;

        /**
         * Creates a new slider.
         *
         * @param x        Slider X position
         * @param y        Slider Y position
         * @param width    Slider width
         * @param height   Slider height
         * @param value    Slider value
         * @param min      Minimum slider value
         * @param max      Maximum slider value
         * @param handler  Slider change handler
         * @param provider Slider message provider
         */
        @Contract(pure = true)
        CallbackSlider(int x, int y, int width, int height, int value, int min, int max,
                       @NotNull IntConsumer handler, @NotNull IntFunction<Component> provider) {
            // Call.
            super(x, y, width, height, provider.apply(value), (Math.max(min, Math.min(max, value)) - min) / (double) (max - min));

            // Assign.
            this.min = min;
            this.max = max;
            this.handler = handler;
            this.provider = provider;
        }

        @ApiStatus.Internal
        @Override
        protected void updateMessage() {
            // Calculate the value.
            int value = (int) (this.min + this.value * (this.max - this.min));
            int clamped = Math.max(this.min, Math.min(this.max, value));

            // Generate and apply the message.
            Component message = this.provider.apply(clamped);
            this.setMessage(message);
        }

        @ApiStatus.Internal
        @Override
        protected void applyValue() {
            // Calculate the value.
            int value = (int) (this.min + this.value * (this.max - this.min));
            int clamped = Math.max(this.min, Math.min(this.max, value));

            // Apply the value.
            this.handler.accept(clamped);
        }
    }

    /**
     * Dynamic checkbox implementation with handler.
     *
     * @author VidTu
     */
    static final class CallbackCheckbox extends Checkbox {
        /**
         * Checkbox value handler.
         */
        @NotNull
        private final BooleanConsumer handler;

        /**
         * Creates a new checkbox.
         *
         * @param font    Checkbox font
         * @param x       Checkbox X position
         * @param y       Checkbox Y position
         * @param message Checkbox label message
         * @param check   Checkbox status
         * @param handler Checkbox value handler
         */
        @Contract(pure = true)
        CallbackCheckbox(@NotNull Font font, int x, int y, @NotNull Component message, boolean check,
                         @NotNull BooleanConsumer handler) {
            super(x, y, font.width(message) + 24, 20, message, check);
            this.handler = handler;
        }

        @ApiStatus.Internal
        @Override
        public void onPress() {
            super.onPress();
            this.handler.accept(this.selected());
        }
    }
}
