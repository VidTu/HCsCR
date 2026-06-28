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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import ru.vidtu.hcscr.compile.Variables;
import ru.vidtu.hcscr.platform.HStonecutter;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntFunction;

//? if >=26.1.2 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} elif >=1.20.1 {
/*import net.minecraft.client.gui.GuiGraphics;
*///?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
*///?}

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
        //~ if >=1.17.1 'this.addButton' -> 'this.addRenderableWidget' {
            //~ if >=1.21.10 'Screen.hasShiftDown()' -> 'minecraft.hasShiftDown()' {
        // Validate.
        final Font font = this.font;
        final Minecraft minecraft = this.minecraft;
        if (Variables.DEBUG_ASSERTS) {
            assert (font != null) : "HCsCR: Font is null. (screen: " + this + ')';
            assert (minecraft != null) : "HCsCR: Client is null. (screen: " + this + ')';
            assert (minecraft.isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", screen: " + this + ')';
        }

        // Enable.
        /*non-final*/ int index = 0;
        final int centerX = (this.width / 2);
        this.addRenderableWidget(HStonecutter.createCheckbox(font, centerX, calculateWidgetY(index++), HStonecutter.translate("hcscr.enable"), // Implicit NPE for 'font'
                HStonecutter.translate("hcscr.enable.tip"), Config.enable(),
                Config::enable, this::tooltip));

        // Crystals.
        final int buttonX = (centerX - 100);
        final CrystalMode crystals = Config.crystals();
        this.addRenderableWidget(HStonecutter.createButton(font, buttonX, calculateWidgetY(index++), 200, 20, crystals.label(), crystals.tip(), (final Button button, final Consumer<Component> tipSetter) -> {
            // Update the crystals.
            final CrystalMode newCrystals = Config.cycleCrystals(/*back=*/minecraft.hasShiftDown());

            // Update the label and tooltip.
            button.setMessage(newCrystals.label());
            tipSetter.accept(newCrystals.tip());
        }, this::tooltip));

        // Crystals Delay.
        final IntFunction<Component> crystalsDelayMessage = (final int delay) -> HStonecutter.translate("options.generic_value",
                HStonecutter.translate("hcscr.crystalsDelay"), (delay > 0) ? HStonecutter.translate(
                        "hcscr.delay.format", delay / 1_000_000) : HStonecutter.translate("hcscr.delay.off"));
        this.addRenderableWidget(HStonecutter.createSlider(font, buttonX, calculateWidgetY(index++), 200, 20, crystalsDelayMessage,
                HStonecutter.translate("hcscr.crystalsDelay.tip"), Config.crystalsDelay(), 0, 200_000_000,
                Config::crystalsDelay, this::tooltip));

        // Crystals Resync.
        final IntFunction<Component> crystalsResyncMessage = (final int resync) -> HStonecutter.translate("options.generic_value",
                HStonecutter.translate("hcscr.crystalsResync"), (resync > 0) ? HStonecutter.translate(
                        "hcscr.delay.format", resync * 50) : HStonecutter.translate("hcscr.delay.off"));
        this.addRenderableWidget(HStonecutter.createSlider(font, buttonX, calculateWidgetY(index++), 200, 20, crystalsResyncMessage,
                HStonecutter.translate("hcscr.crystalsResync.tip"), Config.crystalsResync(), 0, 50,
                Config::crystalsResync, this::tooltip));

        // Blocks.
        final BlockMode blocks = Config.blocks();
        this.addRenderableWidget(HStonecutter.createButton(font, buttonX, calculateWidgetY(index++), 200, 20, blocks.label(), blocks.tip(), (final Button button, final Consumer<Component> tipSetter) -> {
            // Update the blocks.
            final BlockMode newBlocks = Config.cycleBlocks(/*back=*/minecraft.hasShiftDown());

            // Update the label and tooltip.
            button.setMessage(newBlocks.label());
            tipSetter.accept(newBlocks.tip());
        }, this::tooltip));

        // Add done button.
        this.addRenderableWidget(HStonecutter.createButton(font, buttonX, this.height - 28, 200, 20,
                CommonComponents.GUI_DONE, HStonecutter.translate("hcscr.close"),
                (final Button ignoredButton, final Consumer<Component> ignoredTipSetter) -> this.onClose(),
                this::tooltip));
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
        // Validate.
        final Minecraft minecraft = this.minecraft;
        if (Variables.DEBUG_ASSERTS) {
            assert (minecraft != null) : "HCsCR: Client is null. (screen: " + this + ')';
            assert (minecraft.isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", screen: " + this + ')';
        }

        // Save.
        Config.save();

        // Close.
        //$ set_screen minecraft 'this.parent'
        minecraft.gui.setScreen(this.parent);// Implicit NPE for 'minecraft'
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
        // Validate.
        final Font font = this.font;
        final Minecraft minecraft = this.minecraft;
        if (Variables.DEBUG_ASSERTS) {
            assert (graphics != null) : "HCsCR: Parameter 'graphics' is null. (mouseX: " + mouseX + ", mouseY: " + mouseY + ", tickDelta: " + tickDelta + ", screen:" + this + ')';
            assert ((tickDelta >= 0.0f) && (tickDelta < Float.POSITIVE_INFINITY)) : "HCsCR: Parameter 'tickDelta' is not in the [0..+INF) range. (graphics: " + graphics + ", mouseX: " + mouseX + ", mouseY: " + mouseY + ", tickDelta: " + tickDelta + ", screen:" + this + ')';
            assert (font != null) : "HCsCR: Font is null. (graphics: " + graphics + ", mouseX: " + mouseX + ", mouseY: " + mouseY + ", tickDelta: " + tickDelta + ", screen: " + this + ')';
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
        graphics.centeredText(font, this.title, this.width / 2, 12, 0xFF_FF_FF_FF);
        //?} elif >=1.20.1 {
        /*graphics.drawCenteredString(font, this.title, this.width / 2, 12, 0xFF_FF_FF_FF);
        *///?} else {
        /*drawCenteredString(graphics, font, this.title, this.width / 2, 12, 0xFF_FF_FF_FF);
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
     * Sets the tooltip. (pre-1.19.4)
     *
     * @param tooltip Tooltip to be rendered last pass
     */
    private void tooltip(final List<FormattedCharSequence> tooltip) {
        // Validate.
        if (Variables.DEBUG_ASSERTS) {
            assert (tooltip != null) : "HCsCR: Parameter 'tooltip' is null. (screen: " + this + ')';
        }

        // Assign.
        //? if <1.19.4 {
        /*this.tooltip = tooltip;
        *///?}
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
            assert ((index >= 0) && (index <= Byte.MAX_VALUE)) : "HCsCR: Parameter 'index' is not in the [0.." + Byte.MAX_VALUE + "] range. (index: " + index + ')';
        }

        // Calculate.
        return (36 + (index * 24));
    }
}
