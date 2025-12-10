/*
 * HCsCR is a third-party mod for Minecraft Java Edition
 * that allows removing the end crystals faster.
 *
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
import ru.vidtu.hcscr.platform.HStonecutter;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntFunction;

//? if >=1.20.1 {
import net.minecraft.client.gui.GuiGraphics;
//?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
*///?}

/**
 * HCsCR config screen.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see HConfig
 */
@ApiStatus.Internal
@NullMarked
public final class HScreen extends Screen {
    /**
     * Parent screen, {@code null} if none.
     */
    @Nullable
    private final Screen parent;

    //? if <1.19.4 {
    /*/^*
     * Tooltip to be rendered last pass. (<1.19.4)
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
    public HScreen(@Nullable Screen parent) {
        // Call super.
        super(HStonecutter.translate("hcscr.title"));

        // Assign.
        this.parent = parent;
    }

    /**
     * Adds the config widgets.
     *
     * @apiNote Do not call, called by Minecraft
     */
    @DoNotCall("Called by Minecraft")
    @Override
    protected void init() {
        // Validate.
        final Font font = this.font;
        assert font != null : "HCsCR: Font renderer is not initialized at screen initializing. (screen: " + this + ')';
        final Minecraft minecraft = this.minecraft;
        assert minecraft != null : "HCsCR: Minecraft client instance is not initialized at screen initializing. (screen: " + this + ')';
        assert minecraft.isSameThread() : "HCsCR: Initializing the config screen NOT from the main thread. (thread: " + Thread.currentThread() + ", screen: " + this + ')';

        // Enable.
        /*non-final*/ int index = 0;
        final int centerX = (this.width / 2);
        this.addVersionedWidget(HStonecutter.createCheckbox(font, centerX, calculateWidgetY(index++), HStonecutter.translate("hcscr.enable"), // Implicit NPE for 'font'
                HStonecutter.translate("hcscr.enable.tip"), HConfig.enable(),
                HConfig::enable, this::tooltip));

        // Crystals.
        final int buttonX = (centerX - 100);
        final CrystalMode crystals = HConfig.crystals();
        this.addVersionedWidget(HStonecutter.createButton(font, buttonX, calculateWidgetY(index++), 200, 20, crystals.label(), crystals.tip(), (final Button button, final Consumer<Component> tipSetter) -> {
            // Update the crystals.
            final CrystalMode newCrystals = HConfig.cycleCrystals(/*back=*/HStonecutter.isShiftKeyDown(minecraft));

            // Update the label and tooltip.
            button.setMessage(newCrystals.label());
            tipSetter.accept(newCrystals.tip());
        }, this::tooltip));

        // Crystals Delay.
        final IntFunction<Component> crystalsDelayMessage = (final int delay) -> HStonecutter.translate("options.generic_value",
                HStonecutter.translate("hcscr.crystalsDelay"), (delay > 0) ? HStonecutter.translate(
                        "hcscr.delay.format", delay / 1_000_000) : HStonecutter.translate("hcscr.delay.off"));
        this.addVersionedWidget(HStonecutter.createSlider(font, buttonX, calculateWidgetY(index++), 200, 20, crystalsDelayMessage,
                HStonecutter.translate("hcscr.crystalsDelay.tip"), HConfig.crystalsDelay(), 0, 200_000_000,
                HConfig::crystalsDelay, this::tooltip));

        // Crystals Resync.
        final IntFunction<Component> crystalsResyncMessage = (final int resync) -> HStonecutter.translate("options.generic_value",
                HStonecutter.translate("hcscr.crystalsResync"), (resync > 0) ? HStonecutter.translate(
                        "hcscr.delay.format", resync * 50) : HStonecutter.translate("hcscr.delay.off"));
        this.addVersionedWidget(HStonecutter.createSlider(font, buttonX, calculateWidgetY(index++), 200, 20, crystalsResyncMessage,
                HStonecutter.translate("hcscr.crystalsResync.tip"), HConfig.crystalsResync(), 0, 50,
                HConfig::crystalsResync, this::tooltip));

        // Anchors.
        final BlockMode anchors = HConfig.blocks();
        this.addVersionedWidget(HStonecutter.createButton(font, buttonX, calculateWidgetY(index++), 200, 20, anchors.label(), anchors.tip(), (final Button button, final Consumer<Component> tipSetter) -> {
            // Update the anchors.
            final BlockMode newAnchors = HConfig.cycleBlocks(/*back=*/HStonecutter.isShiftKeyDown(minecraft));

            // Update the label and tooltip.
            button.setMessage(newAnchors.label());
            tipSetter.accept(newAnchors.tip());
        }, this::tooltip));

        // Add done button.
        this.addVersionedWidget(HStonecutter.createButton(font, buttonX, this.height - 24, 200, 20,
                CommonComponents.GUI_DONE, HStonecutter.translate("hcscr.close"),
                (final Button button, final Consumer<Component> tipSetter) -> this.onClose(), this::tooltip));
    }

    /**
     * Saves the config and closes the config screen to {@link #parent} screen.
     */
    @Override
    public void onClose() {
        // Validate.
        final Minecraft minecraft = this.minecraft;
        assert minecraft != null : "HCsCR: Minecraft client instance is not initialized at screen closing. (screen: " + this + ')';
        assert minecraft.isSameThread() : "HCsCR: Closing the config screen NOT from the main thread. (thread: " + Thread.currentThread() + ", screen: " + this + ')';

        // Save.
        HConfig.save();

        // Close.
        minecraft.setScreen(this.parent); // Implicit NPE for 'minecraft'
    }

    /**
     * Renders this screen. Called by the implementation.
     *
     * @param graphics  Current graphics handler
     * @param mouseX    Scaled mouse X position
     * @param mouseY    Scaled mouse Y position
     * @param tickDelta Current tick delta (not to be confused with the partial tick)
     * @apiNote Do not call, called by Minecraft
     */
    @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter") // <- Parameter names are not provided by Mojmap.
    @DoNotCall("Called by Minecraft")
    @Override
    //? if >=1.20.1 {
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float tickDelta) {
    //?} else {
    /*public void render(final PoseStack graphics, final int mouseX, final int mouseY, final float tickDelta) {*/
    //?}
        // Validate.
        assert graphics != null : "HCsCR: Parameter 'graphics' is null. (mouseX: " + mouseX + ", mouseY: " + mouseY + ", tickDelta: " + tickDelta + ", screen:" + this + ')';
        assert (tickDelta >= 0.0F) && (tickDelta < Float.POSITIVE_INFINITY) : "HCsCR: Parameter 'tickDelta' is not in the [0..INF) range. (graphics: " + graphics + ", mouseX: " + mouseX + ", mouseY: " + mouseY + ", tickDelta: " + tickDelta + ", screen:" + this + ')';
        final Font font = this.font;
        assert font != null : "HCsCR: Font renderer is not initialized at screen rendering. (graphics: " + graphics + ", mouseX: " + mouseX + ", mouseY: " + mouseY + ", tickDelta: " + tickDelta + ", screen: " + this + ')';
        final Minecraft minecraft = this.minecraft;
        assert minecraft != null : "HCsCR: Minecraft client instance is not initialized at screen rendering. (graphics: " + graphics + ", mouseX: " + mouseX + ", mouseY: " + mouseY + ", tickDelta: " + tickDelta + ", screen: " + this + ')';
        assert minecraft.isSameThread() : "HCsCR: Rendering the config screen NOT from the main thread. (thread: " + Thread.currentThread() + ", graphics: " + graphics + ", mouseX: " + mouseX + ", mouseY: " + mouseY + ", tickDelta: " + tickDelta + ", screen: " + this + ')';

        // Render background and widgets.
        //? if <1.20.2 {
        /*this.renderBackground(graphics); // Implicit NPE for 'graphics'
        *///?}
        super.render(graphics, mouseX, mouseY, tickDelta); // Implicit NPE for 'graphics'

        // Render title.
        //? if >=1.20.1 {
        graphics.drawCenteredString(font, this.title, this.width / 2, 5, 0xFF_FF_FF_FF);
        //?} else {
        /*drawCenteredString(graphics, font, this.title, this.width / 2, 5, 0xFF_FF_FF_FF);*/
        //?}

        // Render the last pass tooltip.
        //? if <1.19.4 {
        /*final List<FormattedCharSequence> tooltip = this.tooltip;
        if (tooltip == null) return;
        this.renderTooltip(graphics, tooltip, mouseX, mouseY);
        this.tooltip = null;
        *///?}
    }

    /**
     * Adds the widget to this screen using the appropriate method in the implementation.
     *
     * @param widget Widget to add
     */
    private void addVersionedWidget(final AbstractWidget widget) {
        // Validate.
        assert widget != null : "HCsCR: Parameter 'widget' is null. (screen: " + this + ')';

        // Delegate.
        //? if >=1.17.1 {
        this.addRenderableWidget(widget); // Implicit NPE for 'widget'
        //?} else {
        /*this.addButton(widget); // Implicit NPE for 'widget'
        *///?}
    }

    /**
     * Sets the tooltip. (<1.19.4)
     *
     * @param tooltip Tooltip to be rendered last pass
     */
    private void tooltip(final List<FormattedCharSequence> tooltip) {
        // Validate.
        assert tooltip != null : "HCsCR: Parameter 'tooltip' is null. (screen: " + this + ')';

        // Assign.
        //? if <1.19.4 {
        /*this.tooltip = tooltip;*/
        //?}
    }

    @Contract(pure = true)
    @Override
    public String toString() {
        return "HCsCR/HScreen{" +
                "parent=" + this.parent +
                //? if <1.19.4 {
                /*", tooltip=" + this.tooltip +*/
                //?}
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
        return (20 + (index * 24));
    }
}
