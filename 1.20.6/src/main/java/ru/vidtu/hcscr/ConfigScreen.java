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

import java.time.Duration;
import java.util.Objects;
import java.util.function.Supplier;

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
        Checkbox box = Checkbox.builder(Component.translatable("hcscr.config.enabled"), this.font)
                .pos((this.width - this.font.width(Component.translatable("hcscr.config.enabled")) - 24) / 2, 20)
                .selected(HConfig.enabled)
                .onValueChange((cb, value) -> HConfig.enabled = value)
                .tooltip(Tooltip.create(Component.translatable("hcscr.config.enabled.tip")))
                .build();
        box.setTooltipDelay(Duration.ofMillis(250L));
        this.addRenderableWidget(box);

        // Remove Crystals.
        box = Checkbox.builder(Component.translatable("hcscr.config.removeCrystals"), this.font)
                .pos((this.width - this.font.width(Component.translatable("hcscr.config.removeCrystals")) - 24) / 2, 44)
                .selected(HConfig.removeCrystals)
                .onValueChange((cb, value) -> HConfig.removeCrystals = value)
                .tooltip(Tooltip.create(Component.translatable("hcscr.config.removeCrystals.tip")))
                .build();
        box.setTooltipDelay(Duration.ofMillis(250L));
        this.addRenderableWidget(box);

        // Remove Slimes.
        box = Checkbox.builder(Component.translatable("hcscr.config.removeSlimes"), this.font)
                .pos((this.width - this.font.width(Component.translatable("hcscr.config.removeSlimes")) - 24) / 2, 68)
                .selected(HConfig.removeSlimes)
                .onValueChange((cb, value) -> HConfig.removeSlimes = value)
                .tooltip(Tooltip.create(Component.translatable("hcscr.config.removeSlimes.tip")))
                .build();
        box.setTooltipDelay(Duration.ofMillis(250L));
        this.addRenderableWidget(box);

        // Remove Interactions.
        box = Checkbox.builder(Component.translatable("hcscr.config.removeInteractions"), this.font)
                .pos((this.width - this.font.width(Component.translatable("hcscr.config.removeInteractions")) - 24) / 2, 92)
                .selected(HConfig.removeInteractions)
                .onValueChange((cb, value) -> HConfig.removeInteractions = value)
                .tooltip(Tooltip.create(Component.translatable("hcscr.config.removeInteractions.tip")))
                .build();
        box.setTooltipDelay(Duration.ofMillis(250L));
        this.addRenderableWidget(box);

        // Remove Anchors.
        box = Checkbox.builder(Component.translatable("hcscr.config.removeAnchors"), this.font)
                .pos((this.width - this.font.width(Component.translatable("hcscr.config.removeAnchors")) - 24) / 2, 116)
                .selected(HConfig.removeAnchors)
                .onValueChange((cb, value) -> HConfig.removeAnchors = value)
                .tooltip(Tooltip.create(Component.translatable("hcscr.config.removeAnchors.tip")))
                .build();
        box.setTooltipDelay(Duration.ofMillis(250L));
        this.addRenderableWidget(box);

        // Delay.
        Supplier<Component> message = () -> {
            int delay = HConfig.delay;
            return CommonComponents.optionNameValue(Component.translatable("hcscr.config.delay"), delay > 0 ? Component.translatable("hcscr.config.delay.format", delay) : Component.translatable("hcscr.config.delay.false"));
        };
        double delay = Math.clamp(HConfig.delay / 200.0D, 0, 1);
        AbstractSliderButton slider = new AbstractSliderButton(this.width / 2 - 100, 140, 200, 20, message.get(), delay) {
            @Override
            protected void updateMessage() {
                this.setMessage(message.get());
            }

            @Override
            protected void applyValue() {
                // Apply the value.
                double value = Math.clamp(this.value, 0.0D, 1.0D);
                HConfig.delay = (int) (value * 200);
            }
        };
        slider.setTooltip(Tooltip.create(Component.translatable("hcscr.config.delay.tip")));
        slider.setTooltipDelay(Duration.ofMillis(250L));
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
            btn.setTooltipDelay(Duration.ofMillis(250L));
        }).bounds(this.width / 2 - 100, 164, 200, 20).tooltip(Tooltip.create(Component.translatable(batching + ".tip"))).build();
        button.setTooltipDelay(Duration.ofMillis(250L));
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
        super.render(graphics, mouseX, mouseY, delta);

        // Render title.
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 5, 0xFF_FF_FF_FF);
    }
}
