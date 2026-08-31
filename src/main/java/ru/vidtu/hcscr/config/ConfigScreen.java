/*
 * HCsCR is a third-party mod for Minecraft Java Edition
 * that allows removing the end crystals faster.
 *
 * Copyright (c) 2023 Offenderify
 * Copyright (c) 2023-2026 VidTu
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
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.vidtu.hcscr.config;

import com.google.errorprone.annotations.DoNotCall;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
//? if >=26.1.2 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} elif >=1.20.1 {
/*import net.minecraft.client.gui.GuiGraphics;
*///?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
*///?}
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
//? if >=1.19.4 {
import net.minecraft.client.gui.components.Tooltip;
//?}
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
//? if <1.19.4 {
/*import org.apache.commons.lang3.mutable.MutableObject;
*///?}
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import ru.vidtu.hcscr.compile.Variables;
import ru.vidtu.hcscr.platform.HStonecutter;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;

/**
 * HCsCR config screen.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see Config
 */
@ApiStatus.Internal
@NullMarked
public final class ConfigScreen extends Screen {
    /**
     * A duration for tooltips in version-dependant units. Currently {@code 250} milliseconds.
     */
    //? if >=1.20.6 {
    private static final java.time.Duration TOOLTIP_DURATION = java.time.Duration.ofMillis(250L);
    //?} elif >=1.19.4 {
    /*private static final int TOOLTIP_DURATION = 250; // Millis.
    *///?} else {
    /*private static final long TOOLTIP_DURATION = 250_000_000L; // Nanos.
    *///?}

    /**
     * Parent screen, {@code null} if none.
     */
    @Nullable
    private final Screen parent;

    //? if <1.19.4 {
    /*/^*
     * Tooltip to be rendered. (pre-1.19.4)
     ^/
    @Nullable
    private /^non-final^/ List<FormattedCharSequence> tooltip;
    *///?}

    /**
     * Creates a new config screen.
     *
     * @param parent Parent screen, {@code null} if none
     */
    @Contract(pure = true)
    public ConfigScreen(@Nullable final Screen parent) {
        // Call super.
        super(HStonecutter.translate("hcscr.title"));

        // Assign.
        this.parent = parent;
    }

    /**
     * Adds the config widgets. Should be called by the implementation.
     *
     * @apiNote Do not call, called by Minecraft
     */
    @DoNotCall("Called by Minecraft")
    @Override
    protected void init() {
        //~ if >=1.17.1 'addButton' -> 'addRenderableWidget' {
            //~ if >=1.21.10 'Screen.hasShiftDown' -> 'minecraft.hasShiftDown' {
        // Extract and validate.
        final Minecraft minecraft = this.minecraft;
        if (Variables.DEBUG_ASSERTS) {
            assert (minecraft != null) : "HCsCR: Client is null. (screen: " + this + ')';
            assert (minecraft.isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", screen: " + this + ')';
        }

        // "Enable" checkbox.
        /*non-final*/ int index = 0;
        final int centerX = (this.width / 2);
        this.addRenderableWidget(this.createCheckbox(centerX, calculateWidgetY(index++), HStonecutter.translate("hcscr.enable"), // Implicit NPE for 'font'
                HStonecutter.translate("hcscr.enable.tip"), Config.enable(), Config::enable));

        // "Crystals" cycle-button.
        final int buttonX = (centerX - 100);
        final CrystalMode crystals = Config.crystals();
        this.addRenderableWidget(this.createButton(buttonX, calculateWidgetY(index++), 200, 20, crystals.label(), crystals.tip(), (final Button button, final Consumer<Component> tipSetter) -> {
            // Update the config.
            final CrystalMode newCrystals = Config.cycleCrystals(/*back=*/minecraft.hasShiftDown()); // Implicit NPE for 'minecraft'

            // Update the label/tooltip.
            button.setMessage(newCrystals.label());
            tipSetter.accept(newCrystals.tip());
        }));

        // "Crystals Delay" slider.
        final IntFunction<Component> crystalsDelayMessage = (final int delay) -> HStonecutter.translate("options.generic_value",
                HStonecutter.translate("hcscr.crystalsDelay"), (delay > 0) ? HStonecutter.translate(
                        "hcscr.delay.format", delay / 1_000_000) : HStonecutter.translate("hcscr.delay.off"));
        this.addRenderableWidget(this.createSlider(buttonX, calculateWidgetY(index++), 200, 20,
                crystalsDelayMessage, HStonecutter.translate("hcscr.crystalsDelay.tip"),
                Config.crystalsDelay(), 0, 200_000_000, Config::crystalsDelay));

        // "Crystals Resync" slider.
        final IntFunction<Component> crystalsResyncMessage = (final int resync) -> HStonecutter.translate("options.generic_value",
                HStonecutter.translate("hcscr.crystalsResync"), (resync > 0) ? HStonecutter.translate(
                        "hcscr.delay.format", resync * 50) : HStonecutter.translate("hcscr.delay.off"));
        this.addRenderableWidget(this.createSlider(buttonX, calculateWidgetY(index++), 200, 20,
                crystalsResyncMessage, HStonecutter.translate("hcscr.crystalsResync.tip"),
                Config.crystalsResync(), 0, 50, Config::crystalsResync));

        // "Blocks" button.
        final BlockMode blocks = Config.blocks();
        this.addRenderableWidget(this.createButton(buttonX, calculateWidgetY(index++), 200, 20, blocks.label(), blocks.tip(), (final Button button, final Consumer<Component> tipSetter) -> {
            // Update the blocks.
            final BlockMode newBlocks = Config.cycleBlocks(/*back=*/minecraft.hasShiftDown()); // Implicit NPE for 'minecraft'

            // Update the label and tooltip.
            button.setMessage(newBlocks.label());
            tipSetter.accept(newBlocks.tip());
        }));

        // "Done" button.
        this.addRenderableWidget(this.createButton(buttonX, this.height - 28, 200, 20, CommonComponents.GUI_DONE, null, 
                (final Button ignoredButton, final Consumer<Component> ignoredTipSetter) -> this.onClose()));
            //~}
        //~}
    }

    /**
     * Saves the config and closes the config screen to {@link #parent} screen. Called either
     * by the implementation (e.g., via ESC key) or by this class (e.g., via "Done" button).
     *
     * @see Config#save()
     */
    @Override
    public void onClose() {
        // Extract and validate.
        final Minecraft minecraft = this.minecraft;
        if (Variables.DEBUG_ASSERTS) {
            assert (minecraft != null) : "HCsCR: Client is null. (screen: " + this + ')';
            assert (minecraft.isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", screen: " + this + ')';
        }

        // Save the config.
        Config.save();

        // Close the screen.
        //$ set_screen minecraft 'this.parent'
        minecraft.gui.setScreen(this.parent); // Implicit NPE for 'minecraft'
    }

    /**
     * Renders or extracts the data for rendering of this
     * screen. Should be called by the implementation.
     *
     * @param graphics  Current graphics handler (rendering context)
     * @param mouseX    Scaled mouse X position
     * @param mouseY    Scaled mouse Y position
     * @param tickDelta Current tick delta (not to be confused with the partial tick)
     * @apiNote Do not call, called by Minecraft
     */
    @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter") // <- >=26.1: I refuse to rename 'tickDelta' to 'a'; <26.1: Mojmap didn't provide parameters.
    @DoNotCall("Called by Minecraft")
    @Override
    //? if >=26.1.2 {
    public void extractRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float tickDelta) {
    //?} elif >=1.20.1 {
    /*public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float tickDelta) {
    *///?} else {
    /*public void render(final PoseStack graphics, final int mouseX, final int mouseY, final float tickDelta) {
    *///?}
        // Extract and validate.
        final Font font = this.font;
        if (Variables.DEBUG_ASSERTS) {
            assert (graphics != null) : "HCsCR: Parameter 'graphics' is null. (mouseX: " + mouseX + ", mouseY: " + mouseY + ", tickDelta: " + tickDelta + ", screen:" + this + ')';
            assert ((tickDelta >= 0.0f) && (tickDelta < Float.POSITIVE_INFINITY)) : "HCsCR: Parameter 'tickDelta' is not in the [0..+INF) range. (graphics: " + graphics + ", mouseX: " + mouseX + ", mouseY: " + mouseY + ", tickDelta: " + tickDelta + ", screen:" + this + ')';
            assert (font != null) : "HCsCR: Font is null. (graphics: " + graphics + ", mouseX: " + mouseX + ", mouseY: " + mouseY + ", tickDelta: " + tickDelta + ", screen: " + this + ')';
            final Minecraft minecraft = this.minecraft;
            assert (minecraft != null) : "HCsCR: Client is null. (graphics: " + graphics + ", mouseX: " + mouseX + ", mouseY: " + mouseY + ", tickDelta: " + tickDelta + ", screen: " + this + ')';
            assert (minecraft.isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", graphics: " + graphics + ", mouseX: " + mouseX + ", mouseY: " + mouseY + ", tickDelta: " + tickDelta + ", screen: " + this + ')';
        }

        // Render the background. (pre-1.20.2)
        // Newer versions (1.20.2+) do this automatically in super.render(...) below.
        //? if <1.20.2 {
        /*this.renderBackground(graphics); // Implicit NPE for 'graphics'
        *///?}

        // Render widgets.
        //? if >=26.1.2 {
        super.extractRenderState(graphics, mouseX, mouseY, tickDelta); // Implicit NPE for 'graphics'
        //?} else {
        /*super.render(graphics, mouseX, mouseY, tickDelta); // Implicit NPE for 'graphics'
        *///?}

        // Render the title.
        //? if >=26.1.2 {
        graphics.centeredText(font, this.title, this.width / 2, 12, 0xFF_FF_FF_FF); // Implicit NPE for 'font'
        //?} elif >=1.20.1 {
        /*graphics.drawCenteredString(font, this.title, this.width / 2, 12, 0xFF_FF_FF_FF); // Implicit NPE for 'font'
        *///?} else {
        /*drawCenteredString(graphics, font, this.title, this.width / 2, 12, 0xFF_FF_FF_FF); // Implicit NPE for 'font'
        *///?}

        // Render the tooltip. (pre-1.19.4)
        // Newer versions (1.19.4+) support native last-pass tooltips.
        //? if <1.19.4 {
        /*final List<FormattedCharSequence> tooltip = this.tooltip;
        if (tooltip == null) return;
        this.renderTooltip(graphics, tooltip, mouseX, mouseY);
        this.tooltip = null;
        *///?}
    }

    /**
     * Creates a new GUI button instance.
     *
     * @param x       Button X position in scaled pixels
     * @param y       Button Y position in scaled pixels
     * @param width   Button width in scaled pixels
     * @param height  Button height in scaled pixels
     * @param message Button label
     * @param tooltip Button tooltip ({@code null} if none)
     * @param handler Button click handler (button itself and tooltip setter)
     * @return A new button instance
     */
    @Contract(value = "_, _, _, _, _, _, _ -> new", pure = true)
    private Button createButton(final int x, final int y, final int width, final int height,
                                final Component message, final @Nullable Component tooltip,
                                final BiConsumer<Button, Consumer<Component>> handler) {
        // Extract and validate.
        final Font font = this.font;
        final Minecraft minecraft = this.minecraft;
        if (Variables.DEBUG_ASSERTS) {
            final int screenWidth = Math.max(this.width, 320);
            assert ((x >= -320) && (x <= screenWidth)) : "HCsCR: Parameter 'x' is not in the [" + -320 + ".." + screenWidth + "] range. (x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", message: " + message + ", tooltip: " + tooltip + ", handler: " + handler + ", screen: " + this + ')';
            final int screenHeight = Math.max(this.height, 240);
            assert ((y >= -240) && (y <= screenHeight)) : "HCsCR: Parameter 'y' is not in the [" + -240 + ".." + screenHeight + "] range. (x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", message: " + message + ", tooltip: " + tooltip + ", handler: " + handler + ", screen: " + this + ')';
            assert (width == 200) : "HCsCR: Parameter 'width' is not 200. (x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", message: " + message + ", tooltip: " + tooltip + ", handler: " + handler + ", screen: " + this + ')';
            assert (height == 20) : "HCsCR: Parameter 'height' is 20. (x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", message: " + message + ", tooltip: " + tooltip + ", handler: " + handler + ", screen: " + this + ')';
            assert (message != null) : "HCsCR: Parameter 'message' is null. (x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", tooltip: " + tooltip + ", handler: " + handler + ", screen: " + this + ')';
            assert (handler != null) : "HCsCR: Parameter 'handler' is null. (x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", message: " + message + ", tooltip: " + tooltip + ", screen: " + this + ')';
            assert (font != null) : "HCsCR: Font is null. (x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", message: " + message + ", tooltip: " + tooltip + ", handler: " + handler + ", screen: " + this + ')';
            assert (minecraft != null) : "HCsCR: Client is null. (x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", message: " + message + ", tooltip: " + tooltip + ", handler: " + handler + ", screen: " + this + ')';
            assert (minecraft.isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", message: " + message + ", tooltip: " + tooltip + ", handler: " + handler + ", screen: " + this + ')';
        }

        // Create a button.
        //? if >=1.19.4 {
        final Button button = Button.builder(message, (final Button innerButton) -> { // Implicit NPE for 'message'
            // Validate.
            if (Variables.DEBUG_ASSERTS) {
                assert (innerButton != null) : "HCsCR: Parameter 'innerButton' is null.";
                assert (minecraft.isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", innerButton: " + innerButton + ')';
            }

            // Handle a click.
            handler.accept(innerButton, (final Component newTip) -> { // Implicit NPE for 'handler', 'innerButton'
                // Validate.
                if (Variables.DEBUG_ASSERTS) {
                    assert (minecraft.isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", newTip: " + newTip + ", innerButton: " + innerButton + ')';
                }

                // Set the tooltip.
                if (newTip == null) {
                    innerButton.setTooltip(null);
                    return;
                }
                innerButton.setTooltip(Tooltip.create(newTip));
                innerButton.setTooltipDelay(TOOLTIP_DURATION);
            });
        }).bounds(x, y, width, height).build();
        if (tooltip == null) return button;
        button.setTooltip(Tooltip.create(tooltip));
        button.setTooltipDelay(TOOLTIP_DURATION);
        return button;
        //?} else {
        /*final MutableObject<List<FormattedCharSequence>> tipHolder = new MutableObject<>((tooltip == null) ? null : font.split(tooltip, 170)); // Implicit NPE for 'font'
        return new Button(x, y, width, height, message, (final Button innerButton) -> {
            // Validate.
            if (Variables.DEBUG_ASSERTS) {
                assert (innerButton != null) : "HCsCR: Parameter 'innerButton' is null.";
                assert (minecraft.isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", innerButton: " + innerButton + ')';
            }

            // Handle a click.
            handler.accept(innerButton, (final Component newTip) -> { // Implicit NPE for 'handler', 'innerButton'
                // Validate.
                if (Variables.DEBUG_ASSERTS) {
                    assert (minecraft.isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", newTip: " + newTip + ", innerButton: " + innerButton + ')';
                }

                // Set the tooltip.
                tipHolder.setValue((newTip == null) ? null : font.split(newTip, 170));
            });
        }) {
            /^*
             * Last time when the mouse was NOT over this button in units of {@link System#nanoTime()}.
             ^/
            private /^non-final^/ long lastAway = System.nanoTime();

            @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter") // <- Parameter names are not provided by Mojmap.
            @Override
            public void renderButton(final PoseStack graphics, final int mouseX, final int mouseY, final float delta) {
                // Validate.
                if (Variables.DEBUG_ASSERTS) {
                    assert (graphics != null) : "HCsCR: Parameter 'graphics' is null. (mouseX: " + mouseX + ", mouseY: " + mouseY + ", tickDelta: " + tickDelta + ", button: " + this + ", screen:" + ConfigScreen.this + ')';
                    assert ((tickDelta >= 0.0f) && (tickDelta < Float.POSITIVE_INFINITY)) : "HCsCR: Parameter 'tickDelta' is not in the [0..+INF) range. (graphics: " + graphics + ", mouseX: " + mouseX + ", mouseY: " + mouseY + ", tickDelta: " + tickDelta + ", button: " + this + ", screen:" + ConfigScreen.this + ')';
                    assert (minecraft.isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", graphics: " + graphics + ", mouseX: " + mouseX + ", mouseY: " + mouseY + ", tickDelta: " + tickDelta + ", button: " + this + ", screen":  + ConfigScreen.this + ')';
                }

                // Render the button.
                super.renderButton(graphics, mouseX, mouseY, delta);

                // Button is not hovered, update the state.
                if (!this.isHovered) {
                    this.lastAway = System.nanoTime();
                    return;
                }

                // Button is not hovered for enough time.
                if ((System.nanoTime() - this.lastAway) < TOOLTIP_DURATION) return;

                // Render (defer) the tooltip.
                ConfigScreen.this.tooltip = tipHolder.getValue();
            }
        };
        *///?}
    }

    /**
     * Creates a new GUI checkbox instance.
     *
     * @param x       Checkbox X position in scaled pixels
     * @param y       Checkbox Y position in scaled pixels
     * @param message Checkbox label
     * @param tooltip Checkbox tooltip
     * @param check   Whether the checkbox is checked
     * @param handler Checkbox click handler
     * @return A new checkbox instance
     */
    @Contract(value = "_, _, _, _, _, _ -> new", pure = true)
    private Checkbox createCheckbox(final int x, final int y, final Component message, final Component tooltip,
                                    final boolean check, final BooleanConsumer handler) {
        // Extract and validate.
        final Font font = this.font;
        final Minecraft minecraft = this.minecraft;
        if (Variables.DEBUG_ASSERTS) {
            final int screenWidth = Math.max(this.width, 320);
            assert ((x >= -320) && (x <= screenWidth)) : "HCsCR: Parameter 'x' is not in the [" + -320 + ".." + screenWidth + "] range. (x: " + x + ", y: " + y + ", message: " + message + ", tooltip: " + tooltip + ", check: " + check + ", handler: " + handler + ", screen: " + this + ')';
            final int screenHeight = Math.max(this.height, 240);
            assert ((y >= -240) && (y <= screenHeight)) : "HCsCR: Parameter 'y' is not in the [" + -240 + ".." + screenHeight + "] range. (x: " + x + ", y: " + y + ", message: " + message + ", tooltip: " + tooltip + ", check: " + check + ", handler: " + handler + ", screen: " + this + ')';
            assert (message != null) : "HCsCR: Parameter 'message' is null. (x: " + x + ", y: " + y + ", tooltip: " + tooltip + ", check: " + check + ", handler: " + handler + ", screen: " + this + ')';
            assert (tooltip != null) : "HCsCR: Parameter 'tooltip' is null. (x: " + x + ", y: " + y + ", message: " + message + ", check: " + check + ", handler: " + handler + ", screen: " + this + ')';
            assert (handler != null) : "HCsCR: Parameter 'handler' is null. (x: " + x + ", y: " + y + ", message: " + message + ", tooltip: " + tooltip + ", check: " + check + ", screen: " + this + ')';
            assert (font != null) : "HCsCR: Font is null. (x: " + x + ", y: " + y + ", message: " + message + ", tooltip: " + tooltip + ", check: " + check + ", handler: " + handler + ", screen: " + this + ')';
            assert (minecraft != null) : "HCsCR: Client is null. (x: " + x + ", y: " + y + ", message: " + message + ", tooltip: " + tooltip + ", check: " + check + ", handler: " + handler + ", screen: " + this + ')';
            assert (minecraft.isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", x: " + x + ", y: " + y + ", message: " + message + ", tooltip: " + tooltip + ", check: " + check + ", handler: " + handler + ", screen: " + this + ')';
        }

        // Create.
        //? if >=1.20.4 {
        final Checkbox box = Checkbox.builder(message, font).pos(x - ((font.width(message) + 24) / 2), y).selected(check).onValueChange((final Checkbox innerBox, final boolean value) -> {  // Implicit NPE for 'message', 'font'
            // Validate.
            if (Variables.DEBUG_ASSERTS) {
                assert (innerBox != null) : "HCsCR: Parameter 'innerBox' is null. (innerBox: " + innerBox + ", value: " + value + ')';
                assert (minecraft.isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", innerBox: " + innerBox + ", value: " + value + ')';
            }

            // Set the value.
            handler.accept(value); // Implicit NPE for 'handler'
        }).build();
        //?} else {
        /*final int width = font.width(message) + 24; // Implicit NPE for 'font', 'message'
        final Checkbox box = new Checkbox(x - (width / 2), y, width, 20, message, check) {
            @Override
            public void onPress() {
                // Validate.
                if (Variables.DEBUG_ASSERTS) {
                    assert (minecraft.isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", box: " + this + ')';
                }

                // Toggle the checkbox.
                super.onPress();

                // Set the value.
                handler.accept(this.selected()); // Implicit NPE for 'handler'
            }

            //? if <1.19.4 {
            /^/^¹*
             * A tooltip split to {@code 170} scaled pixels wide, a value used in modern versions.
             ¹^/
            private final List<FormattedCharSequence> tip = font.split(tooltip, 170); // Implicit NPE for 'tooltip'

            /^¹*
             * Last time when the mouse was NOT over this checkbox in units of {@link System#nanoTime()}.
             ¹^/
            private /^¹non-final¹^/ long lastAway = System.nanoTime();

            @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter") // <- Parameter names are not provided by Mojmap.
            @Override
            public void renderButton(final PoseStack graphics, final int mouseX, final int mouseY, final float delta) {
                // Validate.
                if (Variables.DEBUG_ASSERTS) {
                    assert (graphics != null) : "HCsCR: Parameter 'graphics' is null. (mouseX: " + mouseX + ", mouseY: " + mouseY + ", tickDelta: " + tickDelta + ", checkbox: " + this + ", screen:" + ConfigScreen.this + ')';
                    assert ((tickDelta >= 0.0f) && (tickDelta < Float.POSITIVE_INFINITY)) : "HCsCR: Parameter 'tickDelta' is not in the [0..+INF) range. (graphics: " + graphics + ", mouseX: " + mouseX + ", mouseY: " + mouseY + ", tickDelta: " + tickDelta + ", checkbox: " + this + ", screen:" + ConfigScreen.this + ')';
                    assert (minecraft.isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", graphics: " + graphics + ", mouseX: " + mouseX + ", mouseY: " + mouseY + ", tickDelta: " + tickDelta + ", checkbox: " + this + ", screen":  + ConfigScreen.this + ')';
                }

                // Render the checkbox itself.
                super.renderButton(graphics, mouseX, mouseY, delta);

                // Checkbox is not hovered, update the state.
                if (!this.isHovered) {
                    this.lastAway = System.nanoTime();
                    return;
                }

                // Checkbox is not hovered for enough time.
                if ((System.nanoTime() - this.lastAway) < TOOLTIP_DURATION) return;

                // Render (defer) the tooltip.
                ConfigScreen.this.tooltip = this.tip;
            }
            ^///?}
        };
        *///?}
        //? if >=1.19.4 {
        box.setTooltip(Tooltip.create(tooltip)); // Implicit NPE for 'tooltip'
        box.setTooltipDelay(TOOLTIP_DURATION);
        //?}
        return box;
    }

    /**
     * Creates a new GUI slider instance.
     *
     * @param x        Slider X position in scaled pixels
     * @param y        Slider Y position in scaled pixels
     * @param width    Slider width in scaled pixels
     * @param height   Slider height in scaled pixels
     * @param provider Slider label provider by value
     * @param tooltip  Slider tooltip
     * @param value    Slider value
     * @param min      Slider minimum allowed value
     * @param max      Slider maximum value
     * @param handler  Slider move handler
     * @return A new slider instance
     */
    @Contract(value = "_, _, _, _, _, _, _, _, _, _, _, _ -> new", pure = true)
    private AbstractSliderButton createSlider(final int x, final int y, final int width, final int height,
                                              final IntFunction<Component> provider, final Component tooltip,
                                              final int value, final int min, final int max, final IntConsumer handler) {
        // Extract and validate.
        final Font font = this.font;
        final Minecraft minecraft = this.minecraft;
        if (Variables.DEBUG_ASSERTS) {
            final int screenWidth = Math.max(this.width, 320);
            assert ((x >= -320) && (x <= screenWidth)) : "HCsCR: Parameter 'x' is not in the [" + -320 + ".." + screenWidth + "] range. (x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", screen: " + this + ')';
            final int screenHeight = Math.max(this.height, 240);
            assert ((y >= -240) && (y <= screenHeight)) : "HCsCR: Parameter 'y' is not in the [" + -240 + ".." + screenHeight + "] range. (x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", screen: " + this + ')';
            assert (width == 200) : "HCsCR: Parameter 'width' is not 200. (x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", screen: " + this + ')';
            assert (height == 20) : "HCsCR: Parameter 'height' is 20. (x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", screen: " + this + ')';
            assert (provider != null) : "HCsCR: Parameter 'provider' is null. (x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", screen: " + this + ')';
            assert (tooltip != null) : "HCsCR: Parameter 'tooltip' is null. (x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", screen: " + this + ')';
            assert (max > min) : "HCsCR: Parameter 'min' <= 'max'. (x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", screen: " + this + ')';
            assert (value >= min) : "HCsCR: Parameter 'value' < 'min'. (x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", screen: " + this + ')';
            assert (value <= max) : "HCsCR: Parameter 'value' > 'max'. (x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", screen: " + this + ')';
            assert (handler != null) : "HCsCR: Parameter 'handler' is null. (x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", screen: " + this + ')';
            assert (font != null) : "HCsCR: Font is null. (x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", screen: " + this + ')';
            assert (minecraft != null) : "HCsCR: Client is null. (x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", screen: " + this + ')';
            assert (minecraft.isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", screen: " + this + ')';
        }

        // Create the slider.
        final int clamped = Mth.clamp(value, min, max);
        final double normalized = ((double) (clamped - min) / (max - min));
        final Component message = provider.apply(clamped); // Implicit NPE for 'provider'
        if (Variables.DEBUG_ASSERTS) {
            assert (message != null) : "HCsCR: Message is null. (x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", screen: " + this + ')';
        }
        final AbstractSliderButton slider = new AbstractSliderButton(x, y, width, height, message, normalized) { // Implicit NPE for 'message'
            /**
             * A denormalized value, that is, back in its original range. (in the range
             * of {@code min} to {@code max}, instead of {@code 0.0} to {@code 1.0})
             */
            private /*non-final*/ int denormalized = clamped;

            @Override
            protected void updateMessage() {
                final Component message = provider.apply(this.denormalized);
                if (Variables.DEBUG_ASSERTS) {
                    assert (message != null) : "HCsCR: Message is null. (provider: " + provider + ", slider: " + this + ')';
                }
                this.setMessage(message); // Implicit NPE for 'message'
            }

            @Override
            protected void applyValue() {
                final int denormalized = this.denormalized = (int) Math.round(Mth.lerp(this.value, min, max));
                handler.accept(denormalized); // Implicit NPE for 'handler'
            }

            //? if <1.19.4 {
            /*/^*
             * A tooltip split to {@code 170} scaled pixels wide, a value used in modern versions.
             ^/
            private final List<FormattedCharSequence> tip = font.split(tooltip, 170); // Implicit NPE for 'font', 'tooltip'

            /^*
             * Last time when the mouse was NOT over this slider in units of {@link System#nanoTime()}.
             ^/
            private /^non-final^/ long lastAway = System.nanoTime();

            @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter") // <- Parameter names are not provided by Mojmap.
            @Override
            public void renderButton(final PoseStack graphics, final int mouseX, final int mouseY, final float delta) {
                // Validate.
                if (Variables.DEBUG_ASSERTS) {
                    assert (graphics != null) : "HCsCR: Parameter 'graphics' is null. (mouseX: " + mouseX + ", mouseY: " + mouseY + ", tickDelta: " + tickDelta + ", slider: " + this + ", screen:" + ConfigScreen.this + ')';
                    assert ((tickDelta >= 0.0f) && (tickDelta < Float.POSITIVE_INFINITY)) : "HCsCR: Parameter 'tickDelta' is not in the [0..+INF) range. (graphics: " + graphics + ", mouseX: " + mouseX + ", mouseY: " + mouseY + ", tickDelta: " + tickDelta + ", slider: " + this + ", screen:" + ConfigScreen.this + ')';
                    assert (minecraft.isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", graphics: " + graphics + ", mouseX: " + mouseX + ", mouseY: " + mouseY + ", tickDelta: " + tickDelta + ", slider: " + this + ", screen":  + ConfigScreen.this + ')';
                }

                // Render the slider itself.
                super.renderButton(graphics, mouseX, mouseY, delta);

                // Slider is not hovered, update the state.
                if (!this.isHovered) {
                    this.lastAway = System.nanoTime();
                    return;
                }

                // Slider is not hovered for enough time.
                if ((System.nanoTime() - this.lastAway) < TOOLTIP_DURATION) return;

                // Render (defer) the tooltip.
                ConfigScreen.this.tooltip = this.tip;
            }
            *///?}
        };
        //? if >=1.19.4 {
        slider.setTooltip(Tooltip.create(tooltip)); // Implicit NPE for 'tooltip'
        slider.setTooltipDelay(TOOLTIP_DURATION);
        //?}
        return slider;
    }

    @Contract(pure = true)
    @Override
    public String toString() {
        return "HCsCR/ConfigScreen{" +
                "parent=" + this.parent +
                //? if <1.19.4 {
                /*", tooltip=" + this.tooltip +
                *///?}
                '}';
    }

    /**
     * Calculates and returns the widget Y position based on its vertical index.
     *
     * @param index Widget index
     * @return Calculated widget Y position
     */
    @Contract(pure = true)
    private static int calculateWidgetY(final int index) {
        // Validate.
        if (Variables.DEBUG_ASSERTS) {
            assert ((index >= 0) && (index <= 8)) : "HCsCR: Parameter 'index' is not in the [0..8] range. (index: " + index + ')';
        }

        // Calculate.
        return (36 + (index * 24));
    }
}
