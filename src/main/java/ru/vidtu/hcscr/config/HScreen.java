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
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.vidtu.hcscr.config;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import ru.vidtu.hcscr.platform.HStonecutter;

import java.util.List;
import java.util.function.IntFunction;

/**
 * HCsCR config screen.
 *
 * @author VidTu
 * @apiNote Internal use only
 */
@ApiStatus.Internal
@NullMarked
public final class HScreen extends Screen {
    /**
     * Parent screen, {@code null} if none.
     */
    @Nullable
    private final Screen parent;

    //? if < 1.19.4 {
    /*/^*
     * Tooltip to be rendered last pass. (<1.19.4)
     ^/
    @Nullable
    private List<FormattedCharSequence> tooltip;
    *///?}

    /**
     * Creates a new config screen.
     *
     * @param parent Parent screen, {@code null} if none
     */
    @Contract(pure = true)
    public HScreen(@Nullable Screen parent) {
        super(HStonecutter.translate("hcscr.title"));
        this.parent = parent;
    }

    /**
     * Adds the widgets.
     */
    @Override
    protected void init() {
        // Enable.
        int centerX = (this.width / 2);
        this.add(HStonecutter.guiCheckbox(this.font, centerX, 20, HStonecutter.translate("hcscr.enable"),
                HStonecutter.translate("hcscr.enable.tip"), HConfig.enable(),
                HConfig::enable, this::tooltip));

        // Crystals.
        int buttonX = (centerX - 100);
        CrystalMode crystals = HConfig.crystals();
        this.add(HStonecutter.guiButton(this.font, buttonX, 20 + 24, 200, 20, crystals.buttonLabel(), crystals.buttonTip(), (button, tipSetter) -> {
            // Update the crystals.
            CrystalMode newCrystals = HConfig.cycleCrystals(/*back=*/hasShiftDown());

            // Update the label and tooltip.
            button.setMessage(newCrystals.buttonLabel());
            tipSetter.accept(newCrystals.buttonTip());
        }, this::tooltip));

        // Crystals Delay.
        IntFunction<Component> crystalsDelayMessage = delay -> HStonecutter.translate("options.generic_value",
                HStonecutter.translate("hcscr.crystalsDelay"), delay > 0 ? HStonecutter.translate(
                        "hcscr.delay.format", delay) : HStonecutter.translate("hcscr.delay.off"));
        this.add(HStonecutter.guiSlider(this.font, buttonX, 20 + (24 * 2), 200, 20, crystalsDelayMessage,
                HStonecutter.translate("hcscr.crystalsDelay.tip"), HConfig.crystalsDelay(), 0, 200,
                HConfig::crystalsDelay, this::tooltip));

        // Crystals Resync.
        IntFunction<Component> crystalsResyncMessage = resync -> HStonecutter.translate("options.generic_value",
                HStonecutter.translate("hcscr.crystalsResync"), resync > 0 ? HStonecutter.translate(
                        "hcscr.delay.format", resync * 50) : HStonecutter.translate("hcscr.delay.off"));
        this.add(HStonecutter.guiSlider(this.font, buttonX, 20 + (24 * 3), 200, 20, crystalsResyncMessage,
                HStonecutter.translate("hcscr.crystalsResync.tip"), HConfig.crystalsResync(), 0, 50,
                HConfig::crystalsResync, this::tooltip));

        // Anchors.
        AnchorMode anchors = HConfig.anchors();
        this.add(HStonecutter.guiButton(this.font, buttonX, 20 + (24 * 4), 200, 20, anchors.buttonLabel(), anchors.buttonTip(), (button, tipSetter) -> {
            // Update the anchors.
            AnchorMode newAnchors = HConfig.cycleAnchors(/*back=*/hasShiftDown());

            // Update the label and tooltip.
            button.setMessage(newAnchors.buttonLabel());
            tipSetter.accept(newAnchors.buttonTip());
        }, this::tooltip));

        // Anchors Delay.
        IntFunction<Component> anchorsDelayMessage = delay -> HStonecutter.translate("options.generic_value",
                HStonecutter.translate("hcscr.anchorsDelay"), delay > 0 ? HStonecutter.translate(
                        "hcscr.delay.format", delay) : HStonecutter.translate("hcscr.delay.off"));
        this.add(HStonecutter.guiSlider(this.font, buttonX, 20 + (24 * 5), 200, 20, anchorsDelayMessage,
                HStonecutter.translate("hcscr.anchorsDelay.tip"), HConfig.anchorsDelay(), 0, 200,
                HConfig::anchorsDelay, this::tooltip));

        // Add done button.
        this.add(HStonecutter.guiButton(this.font, buttonX, this.height - 24, 98, 20,
                CommonComponents.GUI_DONE, HStonecutter.translate("hcscr.save.tip"),
                (btn, tipSetter) -> this.onClose(), this::tooltip));
    }

    /**
     * Saves the config and closes the config screen to {@link #parent} screen.
     */
    @Override
    public void onClose() {
        // Save.
        HConfig.save();

        // Close.
        this.minecraft.setScreen(this.parent);
    }

    @Override
    //? if >=1.20.1 {
    public void render(@NotNull net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float delta) {
    //?} else
    /*public void render(@NotNull com.mojang.blaze3d.vertex.PoseStack graphics, int mouseX, int mouseY, float delta) {*/
        // Render background and widgets.
        //? if <1.20.2
        /*this.renderBackground(graphics);*/
        super.render(graphics, mouseX, mouseY, delta);

        // Render title.
        //? if >=1.20.1 {
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 5, -1);
        //?} else
        /*drawCenteredString(graphics, this.font, this.title, this.width / 2, 5, -1);*/

        // Render the last pass tooltip.
        //? if < 1.19.4 {
        /*if (this.sc_tooltip == null) return;
        renderTooltip(graphics, this.sc_tooltip, mouseX, mouseY);
        this.sc_tooltip = null;
        *///?}
    }

    /**
     * Adds the widget to this screen using the appropriate method in the implementation.
     *
     * @param widget Widget to add
     */
    private void add(AbstractWidget widget) {
        //? if >=1.17.1 {
        this.addRenderableWidget(widget);
        //?} else
        /*this.addButton(widget);*/
    }

    /**
     * Sets the tooltip. (<1.19.4)
     *
     * @param tooltip Tooltip to be rendered last pass
     */
    private void tooltip(List<FormattedCharSequence> tooltip) {
        //? if <1.19.4
        /*this.tooltip = tooltip;*/
    }

    @Contract(pure = true)
    @Override
    public String toString() {
        return "HCsCR/HScreen{" +
                "parent=" + this.parent +
                '}';
    }
}
