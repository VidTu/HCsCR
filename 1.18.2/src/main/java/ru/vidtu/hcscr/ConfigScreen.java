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

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import ru.vidtu.hcscr.config.Batching;
import ru.vidtu.hcscr.config.HConfig;
import ru.vidtu.hcscr.mixins.AbstractWidgetAccessor;

import java.util.List;
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
    private final Component disabled = new TranslatableComponent("hcscr.config.disabled");

    /**
     * Tooltip to render at the last pass.
     */
    @Nullable
    Tooltip tooltip;

    /**
     * Creates a new screen.
     *
     * @param parent Parent screen, {@code null} if none
     */
    @Contract(pure = true)
    ConfigScreen(@Nullable Screen parent) {
        super(new TranslatableComponent("hcscr.config"));
        this.parent = parent;
    }

    @ApiStatus.Internal
    @Override
    protected void init() {
        // Enabled.
        Checkbox box = new CallbackCheckbox(this.font, (this.width - this.font.width(new TranslatableComponent("hcscr.config.enabled")) - 24) / 2, 20,
                new TranslatableComponent("hcscr.config.enabled"), new Tooltip(this, this.font, new TranslatableComponent("hcscr.config.enabled.tip")),
                HConfig.enabled, value -> HConfig.enabled = value);
        this.addRenderableWidget(box);

        // Remove Crystals.
        box = new CallbackCheckbox(this.font, (this.width - this.font.width(new TranslatableComponent("hcscr.config.removeCrystals")) - 24) / 2, 44,
                new TranslatableComponent("hcscr.config.removeCrystals"), new Tooltip(this, this.font, new TranslatableComponent("hcscr.config.removeCrystals.tip")),
                HConfig.removeCrystals, value -> HConfig.removeCrystals = value);
        this.addRenderableWidget(box);

        // Remove Slimes.
        box = new CallbackCheckbox(this.font, (this.width - this.font.width(new TranslatableComponent("hcscr.config.removeSlimes")) - 24) / 2, 68,
                new TranslatableComponent("hcscr.config.removeSlimes"), new Tooltip(this, this.font, new TranslatableComponent("hcscr.config.removeSlimes.tip")),
                HConfig.removeSlimes, value -> HConfig.removeSlimes = value);
        this.addRenderableWidget(box);

        // Remove Interactions.
        box = new CallbackCheckbox(this.font, (this.width - this.font.width(new TranslatableComponent("hcscr.config.removeInteractions")) - 24) / 2, 92,
                new TranslatableComponent("hcscr.config.removeInteractions"), new Tooltip(this, this.font, new TranslatableComponent("hcscr.config.removeInteractions.tip")),
                HConfig.removeInteractions, value -> HConfig.removeInteractions = value);
        this.addRenderableWidget(box);

        // Remove Anchors.
        box = new CallbackCheckbox(this.font, (this.width - this.font.width(new TranslatableComponent("hcscr.config.removeAnchors")) - 24) / 2, 116,
                new TranslatableComponent("hcscr.config.removeAnchors"), new Tooltip(this, this.font, new TranslatableComponent("hcscr.config.removeAnchors.tip")),
                HConfig.removeAnchors, value -> HConfig.removeAnchors = value);
        this.addRenderableWidget(box);

        // Delay.
        int delay = Math.max(0, Math.min(200, HConfig.delay));
        CallbackSlider slider = new CallbackSlider(this.width / 2 - 100, 140, 200, 20, delay, 0, 200,
                new Tooltip(this, this.font, new TranslatableComponent("hcscr.config.delay.tip")),
                value -> HConfig.delay = value,
                value -> CommonComponents.optionNameValue(new TranslatableComponent("hcscr.config.delay"), value > 0 ? new TranslatableComponent("hcscr.config.delay.format", value) : new TranslatableComponent("hcscr.config.delay.false")));
        this.addRenderableWidget(slider);

        // Batching.
        Batching batching = Objects.requireNonNullElse(HConfig.batching, Batching.DISABLED);
        Tooltip tooltip = new Tooltip(this, this.font, new TranslatableComponent(batching + ".tip"));
        Button button = new Button(this.width / 2 - 100, 164, 200, 20, CommonComponents.optionNameValue(new TranslatableComponent("hcscr.config.batching"), new TranslatableComponent(batching.toString())), btn -> {
            // Update the value.
            Batching newBatching = HConfig.batching = switch (HConfig.batching) {
                case DISABLED -> Batching.CONTAINING;
                case CONTAINING -> Batching.CONTAINING_CONTAINED;
                case CONTAINING_CONTAINED -> Batching.INTERSECTING;
                default -> Batching.DISABLED;
            };

            // Set the message and tooltip.
            btn.setMessage(CommonComponents.optionNameValue(new TranslatableComponent("hcscr.config.batching"), new TranslatableComponent(newBatching.toString())));
            tooltip.tooltip(new TranslatableComponent(newBatching + ".tip"));
        }, tooltip);
        this.addRenderableWidget(button);

        // Add done button.
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 24, 200, 20,
                CommonComponents.GUI_DONE, btn -> this.onClose()));
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
    public void render(@NotNull PoseStack pose, int mouseX, int mouseY, float delta) {
        // Render background and widgets.
        this.renderBackground(pose);
        super.render(pose, mouseX, mouseY, delta);

        // Render title.
        drawCenteredString(pose, this.font, this.title, this.width / 2, 5, 0xFF_FF_FF_FF);

        // Render the "disabled" label.
        if (!HCsCR.serverEnabled()) {
            drawCenteredString(pose, this.font, this.disabled, this.width / 2, this.height - 34, 0xFFFF0000);
        }

        // Last pass tooltip.
        if (this.tooltip == null) return;
        this.renderTooltip(pose, this.tooltip.tooltip, mouseX, mouseY);
        this.tooltip = null;
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
         * Slider tooltip.
         */
        @NotNull
        private final Tooltip tooltip;

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
         * @param tooltip  Slider tooltip
         * @param handler  Slider change handler
         * @param provider Slider message provider
         */
        @Contract(pure = true)
        CallbackSlider(int x, int y, int width, int height, int value, int min, int max, @NotNull Tooltip tooltip,
                       @NotNull IntConsumer handler, @NotNull IntFunction<Component> provider) {
            // Call.
            super(x, y, width, height, provider.apply(value), (Math.max(min, Math.min(max, value)) - min) / (double) (max - min));

            // Assign.
            this.min = min;
            this.max = max;
            this.tooltip = tooltip;
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

        @ApiStatus.Internal
        @Override
        public void renderButton(PoseStack pose, int mouseX, int mouseY, float delta) {
            super.renderButton(pose, mouseX, mouseY, delta);
            this.tooltip.render(this);
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
         * Checkbox tooltip.
         */
        @NotNull
        private final Tooltip tooltip;

        /**
         * Creates a new checkbox.
         *
         * @param font    Checkbox font
         * @param x       Checkbox X position
         * @param y       Checkbox Y position
         * @param message Checkbox label message
         * @param tooltip Checkbox tooltip
         * @param check   Checkbox status
         * @param handler Checkbox value handler
         */
        @Contract(pure = true)
        CallbackCheckbox(@NotNull Font font, int x, int y, @NotNull Component message, @NotNull Tooltip tooltip,
                         boolean check, @NotNull BooleanConsumer handler) {
            super(x, y, font.width(message) + 24, 20, message, check);
            this.tooltip = tooltip;
            this.handler = handler;
        }

        @ApiStatus.Internal
        @Override
        public void onPress() {
            super.onPress();
            this.handler.accept(this.selected());
        }

        @ApiStatus.Internal
        @Override
        public void renderButton(PoseStack pose, int mouseX, int mouseY, float delta) {
            super.renderButton(pose, mouseX, mouseY, delta);
            this.tooltip.render(this);
        }
    }

    /**
     * An emulation of delayed tooltips from newer versions.
     *
     * @author VidTu
     */
    static final class Tooltip implements Button.OnTooltip {
        /**
         * Parent screen.
         */
        @NotNull
        private final ConfigScreen screen;

        /**
         * Parent font.
         */
        @NotNull
        private final Font font;

        /**
         * Tooltip component.
         */
        @NotNull
        @Unmodifiable
        List<FormattedCharSequence> tooltip;

        /**
         * Last mouse move.
         */
        private long lastFree = System.nanoTime();

        /**
         * Creates a new tooltip.
         *
         * @param screen  Parent screen
         * @param font    Parent font
         * @param tooltip Tooltip component
         */
        @Contract(pure = true)
        Tooltip(@NotNull ConfigScreen screen, @NotNull Font font, @NotNull Component tooltip) {
            this.screen = screen;
            this.font = font;
            this.tooltip = font.split(tooltip, screen.width / 2);
        }

        @ApiStatus.Internal
        @Override
        public void onTooltip(Button button, PoseStack pose, int mouseX, int mouseY) {
            this.render(button);
        }

        /**
         * Schedules the tooltip rendering, if hovered.
         *
         * @param widget Parent widget
         */
        void render(@NotNull AbstractWidget widget) {
            // Skip if not hovered.
            if (!((AbstractWidgetAccessor) widget).hcscr$isHovered()) {
                this.lastFree = System.nanoTime();
                return;
            }

            // Render if hovered long enough.
            if ((System.nanoTime() - this.lastFree) < 250_000_000L) return;
            this.screen.tooltip = this;
        }

        /**
         * Sets the tooltip.
         *
         * @param tooltip New tooltip
         */
        void tooltip(@NotNull Component tooltip) {
            this.tooltip = this.font.split(tooltip, this.screen.width / 2);
        }
    }
}
