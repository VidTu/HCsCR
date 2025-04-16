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

package ru.vidtu.hcscr.config;

import com.google.common.base.MoreObjects;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.vidtu.hcscr.platform.HStonecutter;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntFunction;

/**
 * HCsCR config screen.
 *
 * @author Offenderify
 * @author VidTu
 */
public final class ConfigScreen extends Screen implements Consumer<List<FormattedCharSequence>> {
    /**
     * Parent screen, {@code null} if none.
     */
    @Nullable
    private final Screen parent;

    //? if < 1.19.4 {
    /*/^*
     * Tooltip to be rendered last pass.
     ^/
    @Nullable
    private List<FormattedCharSequence> sc_tooltip;
    *///?}

    /**
     * Creates a new screen.
     *
     * @param parent Parent screen, {@code null} if none
     */
    @Contract(pure = true)
    public ConfigScreen(@Nullable Screen parent) {
        super(HStonecutter.translate("hcscr.title"));
        this.parent = parent;
    }

    @ApiStatus.Internal
    @Override
    protected void init() {
        // Enable.
        int centerX = (this.width / 2);
        this.sc_add(HStonecutter.guiCheckbox(this.font, centerX, 20, HStonecutter.translate("hcscr.enable"),
                HStonecutter.translate("hcscr.enable.tip"), HConfig.enable,
                value -> HConfig.enable = value, this));

        // Crystals.
        int buttonX = (centerX - 100);
        CrystalMode crystals = MoreObjects.firstNonNull(HConfig.crystals, CrystalMode.OFF);
        this.sc_add(HStonecutter.guiButton(this.font, buttonX, 20 + 24, 200, 20, crystals.buttonLabel(), crystals.buttonTip(), (button, tipSetter) -> {
            // Update the crystals.
            CrystalMode newCrystals;
            switch (crystals) {
                case OFF:
                    HConfig.crystals = newCrystals = CrystalMode.DIRECT;
                    break;
                case DIRECT:
                    HConfig.crystals = newCrystals = CrystalMode.ENVELOPING;
                    break;
                default:
                    HConfig.crystals = newCrystals = CrystalMode.OFF;
            }

            // Update the label and tooltip.
            button.setMessage(newCrystals.buttonLabel());
            tipSetter.accept(newCrystals.buttonTip());
        }, this));

        // Crystals Delay.
        IntFunction<Component> crystalsDelayMessage = delay -> HStonecutter.translate("options.generic_value",
                HStonecutter.translate("hcscr.crystalsDelay"), delay > 0 ? HStonecutter.translate(
                        "hcscr.delay.format", delay) : HStonecutter.translate("hcscr.delay.off"));
        this.sc_add(HStonecutter.guiSlider(this.font, buttonX, 20 + (24 * 2), 200, 20, crystalsDelayMessage,
                HStonecutter.translate("hcscr.crystalsDelay.tip"), HConfig.crystalsDelay, 0, 200,
                value -> HConfig.crystalsDelay = value, this));

        // Anchors.
        AnchorMode anchors = MoreObjects.firstNonNull(HConfig.anchors, AnchorMode.OFF);
        this.sc_add(HStonecutter.guiButton(this.font, buttonX, 20 + (24 * 3), 200, 20, anchors.buttonLabel(), anchors.buttonTip(), (button, tipSetter) -> {
            // Update the crystals.
            AnchorMode newAnchors;
            switch (anchors) {
                case OFF:
                    newAnchors = HConfig.anchors = AnchorMode.COLLISION;
                    break;
                case COLLISION:
                    newAnchors = HConfig.anchors = AnchorMode.FULL;
                    break;
                default:
                    newAnchors = HConfig.anchors = AnchorMode.OFF;
            }

            // Update the label and tooltip.
            button.setMessage(newAnchors.buttonLabel());
            tipSetter.accept(newAnchors.buttonTip());
        }, this));

        // Anchors Delay.
        IntFunction<Component> anchorsDelayMessage = delay -> HStonecutter.translate("options.generic_value",
                HStonecutter.translate("hcscr.anchorsDelay"), delay > 0 ? HStonecutter.translate(
                        "hcscr.delay.format", delay) : HStonecutter.translate("hcscr.delay.off"));
        this.sc_add(HStonecutter.guiSlider(this.font, buttonX, 20 + (24 * 4), 200, 20, anchorsDelayMessage,
                HStonecutter.translate("hcscr.anchorsDelay.tip"), HConfig.anchorsDelay, 0, 200,
                value -> HConfig.anchorsDelay = value, this));

        // Add done button.
        this.sc_add(HStonecutter.guiButton(this.font, buttonX, this.height - 24, 200, 20,
                CommonComponents.GUI_DONE, null, (btn, tipSetter) -> this.onClose(), this));
    }

    @ApiStatus.Internal
    @Override
    public void onClose() {
        // Bruh.
        assert this.minecraft != null;

        // Save config.
        HConfig.saveOrLog(FabricLoader.getInstance().getConfigDir());

        // Close to parent.
        this.minecraft.setScreen(this.parent);
    }

    @ApiStatus.Internal
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

    private void sc_add(AbstractWidget widget) {
        //? if >=1.17.1 {
        this.addRenderableWidget(widget);
        //?} else
        /*this.addButton(widget);*/
    }

    @Override
    public void accept(List<FormattedCharSequence> tooltip) {
        //? if <1.19.4
        /*this.sc_tooltip = tooltip;*/
    }
}
