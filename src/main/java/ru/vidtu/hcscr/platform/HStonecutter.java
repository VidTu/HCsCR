/*
 * Copyright (c) 2023 Offenderify
 * Copyright (c) 2023-2025 VidTu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.vidtu.hcscr.platform;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import ru.vidtu.hcscr.config.ConfigScreen;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;

/**
 * A helper class that contains methods that depend on Stonecutter, a Java source code preprocessor.
 *
 * @author VidTu
 * @apiNote Internal use only
 */
@ApiStatus.Internal
@NullMarked
public final class HStonecutter {
    /**
     * A duration for tooltips in version-dependant units. Currently {@code 250} milliseconds.
     */
    //? if >=1.20.6 {
    public static final java.time.Duration TOOLTIP_DURATION = java.time.Duration.ofMillis(250L);
    //?} else if >=1.19.4 {
    /*public static final int TOOLTIP_DURATION = 250; // Millis.*/
    //?} else
    /*public static final long TOOLTIP_DURATION = 250_000_000L;*/ // Nanos.

    /**
     * A channel identifier for servers to know that this mod is installed.
     */
    //? if >=1.21.1 {
    public static final ResourceLocation CHANNEL_IDENTIFIER = ResourceLocation.fromNamespaceAndPath("hcscr", "imhere");
    //?} else
    /*public static final ResourceLocation CHANNEL_IDENTIFIER = new ResourceLocation("hcscr", "imhere");*/

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    public HStonecutter() {
        throw new AssertionError("No instances.");
    }

    /**
     * Creates a new translatable component.
     *
     * @param key Translation key
     * @return A new translatable component
     */
    @Contract(value = "_ -> new", pure = true)
    public static MutableComponent translate(String key) {
        //? if >=1.19.2 {
        return Component.translatable(key);
         //?} else
        /*return new net.minecraft.network.chat.TranslatableComponent(key);*/
    }

    /**
     * Creates a new translatable component.
     *
     * @param key  Translation key
     * @param args Translation args
     * @return A new translatable component
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static MutableComponent translate(String key, Object... args) {
        //? if >=1.19.2 {
        return Component.translatable(key, args);
         //?} else
        /*return new net.minecraft.network.chat.TranslatableComponent(key, args);*/
    }

    /**
     * Gets the profiler of the game.
     *
     * @param game Current game instance
     * @return Game profiler
     */
    @CheckReturnValue
    public static ProfilerFiller profilerOf(@SuppressWarnings("unused") Minecraft game) { // <- Used only before 1.21.3.
        //? if >=1.21.3 {
        return net.minecraft.util.profiling.Profiler.get();
        //?} else
        /*return game.getProfiler();*/
    }

    /**
     * Gets the level of the entity.
     *
     * @param entity Target entity
     * @return The level (world) in which the entity is currently located or was last located
     */
    @Contract(pure = true)
    public static Level levelOf(Entity entity) {
        //? if >=1.20.1 {
        return entity.level();
        //?} else
        /*return entity.level;*/
    }

    /**
     * Checks whether the entity has been removed from the world or marked for removal from the world.
     *
     * @param entity Target entity to check
     * @return Whether the entity has been removed
     */
    @Contract(pure = true)
    public static boolean isEntityRemoved(Entity entity) {
        //? if >=1.17.1 {
        return entity.isRemoved();
        //?} else
        /*return entity.removed;*/
    }

    /**
     * Marks the entity as to be removed from the world.
     *
     * @param entity Target entity to remove
     */
    public static void removeEntity(Entity entity) {
        //? if >=1.17.1 {
        entity.discard();
        //?} else
        /*entity.remove();*/
    }

    /**
     * Hurts (damages) the entity the specified amount.
     *
     * @param entity Target entity to hurt
     * @param source Hurt source
     * @param amount Amount of damage
     * @return The result of the hurting
     */
    @SuppressWarnings({"deprecation", "RedundantSuppression"}) // <- Used in vanilla code. (1.21.3+)
    @CheckReturnValue
    public static boolean hurt(Entity entity, DamageSource source, float amount) {
        //? if >=1.21.3 {
        return entity.hurtOrSimulate(source, amount);
        //?} else
        /*return entity.hurt(source, amount);*/
    }

    /**
     * Creates a new GUI button instance.
     *
     * @param font            Font renderer used by the GUI
     * @param x               Button X position
     * @param y               Button Y position
     * @param width           Button width in scaled pixels
     * @param height          Button height in scaled pixels
     * @param message         Button label
     * @param tooltip         Button tooltip
     * @param handler         Button click handler (button itself and tooltip setter)
     * @param tooltipRenderer Last pass tooltip renderer, typically {@link ConfigScreen}
     * @return A new button instance
     */
    @Contract(value = "_, _, _, _, _, _, _, _, _ -> new", pure = true)
    public static Button guiButton(@SuppressWarnings("unused") Font font, int x, int y, int width, int height, // <- Used before 1.19.4.
                                   Component message, Component tooltip, BiConsumer<Button, Consumer<Component>> handler,
                                   @SuppressWarnings("unused") Consumer<List<FormattedCharSequence>> tooltipRenderer) { // <- Used before 1.19.4.
        //? if >=1.19.4 {
        Button button = Button.builder(message, btn -> handler.accept(btn, tip -> {
            btn.setTooltip(net.minecraft.client.gui.components.Tooltip.create(tip));
            btn.setTooltipDelay(TOOLTIP_DURATION);
        })).tooltip(net.minecraft.client.gui.components.Tooltip.create(tooltip)).bounds(x, y, width, height).build();
        button.setTooltipDelay(TOOLTIP_DURATION);
        return button;
        //?} else {
        /*org.apache.commons.lang3.mutable.Mutable<List<FormattedCharSequence>> tipHolder = new org.apache.commons.lang3.mutable.MutableObject<>(font.split(tooltip, 170));
        return new Button(x, y, width, height, message, btn -> handler.accept(btn, tip -> tipHolder.setValue(font.split(tip, 170)))) {
            /^*
             * Last time when the mouse was NOT over this element in units of {@link System#nanoTime()}.
             ^/
            private long lastAway = System.nanoTime();

            @Override
            public void renderButton(com.mojang.blaze3d.vertex.PoseStack graphics, int mouseX, int mouseY, float delta) {
                // Render the element itself.
                super.renderButton(graphics, mouseX, mouseY, delta);

                // Button is not hovered, update the state.
                if (!this.isHovered) {
                    this.lastAway = System.nanoTime();
                    return;
                }

                // Button is not hovered for enough time.
                if ((System.nanoTime() - this.lastAway) < TOOLTIP_DURATION) return;

                // Render the tooltip.
                tooltipRenderer.accept(tipHolder.getValue());
            }
        };
        *///?}
    }

    /**
     * Creates a new GUI checkbox instance.
     *
     * @param font            Font renderer used by the GUI
     * @param x               Checkbox X position
     * @param y               Checkbox Y position
     * @param message         Checkbox label
     * @param tooltip         Checkbox tooltip
     * @param check           Whether the checkbox is checked
     * @param handler         Checkbox click handler
     * @param tooltipRenderer Last pass tooltip renderer, typically {@link ConfigScreen}
     * @return A new checkbox instance
     */
    @Contract(value = "_, _, _, _, _, _, _, _ -> new", pure = true)
    public static Checkbox guiCheckbox(Font font, int x, int y, Component message, Component tooltip,
                                       boolean check, BooleanConsumer handler,
                                       @SuppressWarnings("unused") Consumer<List<FormattedCharSequence>> tooltipRenderer) { // <- Used before 1.19.4.
        //? if >=1.20.4 {
        Checkbox box = Checkbox.builder(message, font)
                .pos(x - ((font.width(message) + 24) / 2), y)
                .selected(check)
                .onValueChange((checkbox, value) -> handler.accept(value))
                .build();
        //?} else {
        /*int width = font.width(message) + 24;
        Checkbox box = new Checkbox(x - (width / 2), y, width, 20, message, check) {
            @Override
            public void onPress() {
                // Toggle the checkbox.
                super.onPress();

                // Invoke the handler.
                handler.accept(this.selected());
            }

            //? if <1.19.4 {
            /^/^¹*
             * A tooltip split to {@code 170} scaled pixels wide, a value used in modern versions
             ¹^/
            private final List<FormattedCharSequence> tip = font.split(tooltip, 170);

            /^¹*
             * Last time when the mouse was NOT over this element in units of {@link System#nanoTime()}.
             ¹^/
            private long lastAway = System.nanoTime();

            @Override
            public void renderButton(com.mojang.blaze3d.vertex.PoseStack graphics, int mouseX, int mouseY, float delta) {
                // Render the element itself.
                super.renderButton(graphics, mouseX, mouseY, delta);

                // Button is not hovered, update the state.
                if (!this.isHovered) {
                    this.lastAway = System.nanoTime();
                    return;
                }

                // Button is not hovered for enough time.
                if ((System.nanoTime() - this.lastAway) < TOOLTIP_DURATION) return;

                // Render the tooltip.
                tooltipRenderer.accept(this.tip);
            }
            ^///?}
        };
        *///?}
        //? if >=1.19.4 {
        box.setTooltip(net.minecraft.client.gui.components.Tooltip.create(tooltip));
        box.setTooltipDelay(HStonecutter.TOOLTIP_DURATION);
        //?}
        return box;
    }

    /**
     * Creates a new GUI slider instance.
     *
     * @param font            Font renderer used by the GUI
     * @param x               Slider X position
     * @param y               Slider Y position
     * @param width           Slider width in scaled pixels
     * @param height          Slider height in scaled pixels
     * @param message         Slider label provider by value
     * @param tooltip         Slider tooltip
     * @param value           Slider value
     * @param min             Slider minimum allowed value
     * @param max             Slider maximum value
     * @param handler         Slider move handler
     * @param tooltipRenderer Last pass tooltip renderer, typically {@link ConfigScreen}
     * @return A new slider instance
     */
    public static AbstractSliderButton guiSlider(@SuppressWarnings("unused") Font font, int x, int y, int width, int height, // <- Used before 1.19.4.
                                                 IntFunction<Component> message, Component tooltip,
                                                 int value, int min, int max, IntConsumer handler,
                                                 @SuppressWarnings("unused") Consumer<List<FormattedCharSequence>> tooltipRenderer) { // <- Used before 1.19.4.
        int clamped = Mth.clamp(value, min, max);
        double normalized = (double) (clamped - min) / (max - min);
        AbstractSliderButton slider = new AbstractSliderButton(x, y, width, height, message.apply(clamped), normalized) {
            /**
             * A denormalized value, i.e. back in its original range.
             */
            private int denormalized = clamped;

            @Override
            protected void updateMessage() {
                this.setMessage(message.apply(this.denormalized));
            }

            @Override
            protected void applyValue() {
                int denormalized = this.denormalized = (int) Mth.lerp(this.value, min, max);
                handler.accept(denormalized);
            }

            //? if <1.19.4 {
            /*/^*
             * A tooltip split to {@code 170} scaled pixels wide, a value used in modern versions
             ^/
            private final List<FormattedCharSequence> tip = font.split(tooltip, 170);

            /^*
             * Last time when the mouse was NOT over this element in units of {@link System#nanoTime()}.
             ^/
            private long lastAway = System.nanoTime();

            @Override
            public void renderButton(com.mojang.blaze3d.vertex.PoseStack graphics, int mouseX, int mouseY, float delta) {
                // Render the element itself.
                super.renderButton(graphics, mouseX, mouseY, delta);

                // Button is not hovered, update the state.
                if (!this.isHovered) {
                    this.lastAway = System.nanoTime();
                    return;
                }

                // Button is not hovered for enough time.
                if ((System.nanoTime() - this.lastAway) < TOOLTIP_DURATION) return;

                // Render the tooltip.
                tooltipRenderer.accept(this.tip);
            }
            *///?}
        };
        //? if >=1.19.4 {
        slider.setTooltip(net.minecraft.client.gui.components.Tooltip.create(tooltip));
        slider.setTooltipDelay(HStonecutter.TOOLTIP_DURATION);
        //?}
        return slider;
    }
}
