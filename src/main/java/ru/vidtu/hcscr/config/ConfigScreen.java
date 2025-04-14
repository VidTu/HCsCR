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

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntFunction;

//? if >=1.20.1 {
import net.minecraft.client.gui.GuiGraphics;
//?} else
/*import com.mojang.blaze3d.vertex.PoseStack;*/
import ru.vidtu.hcscr.platform.HStonecutter;

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
        super(HStonecutter.translate("hcscr.config"));
        this.parent = parent;
    }

    @ApiStatus.Internal
    @Override
    protected void init() {
        // Enabled.
        int centerX = (this.width / 2);
        this.sc_add(HStonecutter.guiCheckbox(this.font, centerX, 20, HStonecutter.translate("hcscr.config.enabled"),
                HStonecutter.translate("hcscr.config.enabled.tip"), HConfig.enabled,
                value -> HConfig.enabled = value, this));

        // Remove Crystals.
        this.sc_add(HStonecutter.guiCheckbox(this.font, centerX, 20 + 24, HStonecutter.translate("hcscr.config.removeCrystals"),
                HStonecutter.translate("hcscr.config.removeCrystals.tip"), HConfig.removeCrystals,
                value -> HConfig.removeCrystals = value, this));

        // Remove Slimes.
        this.sc_add(HStonecutter.guiCheckbox(this.font, centerX, 20 + (24 * 2), HStonecutter.translate("hcscr.config.removeSlimes"),
                HStonecutter.translate("hcscr.config.removeSlimes.tip"), HConfig.removeSlimes,
                value -> HConfig.removeSlimes = value, this));

        // Remove Interactions.
        this.sc_add(HStonecutter.guiCheckbox(this.font, centerX, 20 + (24 * 3), HStonecutter.translate("hcscr.config.removeInteractions"),
                HStonecutter.translate("hcscr.config.removeInteractions.tip"), HConfig.removeInteractions,
                value -> HConfig.removeInteractions = value, this));

        // Remove Anchors.
        this.sc_add(HStonecutter.guiCheckbox(this.font, centerX, 20 + (24 * 4),HStonecutter.translate("hcscr.config.removeAnchors"),
                HStonecutter.translate("hcscr.config.removeAnchors.tip"), HConfig.removeAnchors,
                value -> HConfig.removeAnchors = value, this));

        // Delay.
        int buttonX = (this.width / 2) - 100;
        IntFunction<Component> message = delay -> HStonecutter.translate("options.generic_value", HStonecutter.translate("hcscr.config.delay"),
                delay > 0 ? HStonecutter.translate("hcscr.config.delay.format", delay) : HStonecutter.translate("hcscr.config.delay.false"));
        this.sc_add(HStonecutter.guiSlider(this.font, buttonX, 20 + (24 * 5), 200, 20, message,
                HStonecutter.translate("hcscr.config.delay.tip"), HConfig.delay, 0, 200,
                value -> HConfig.delay = value, this));

        // Batching.
        Batching batching = HConfig.batching == null ? Batching.DISABLED : HConfig.batching;
        this.sc_add(HStonecutter.guiButton(this.font, buttonX, 20 + (24 * 6), 200, 20, HStonecutter.translate("options.generic_value", HStonecutter.translate("hcscr.config.batching"), HStonecutter.translate(batching.toString())), HStonecutter.translate(batching + ".tip"), btn -> {
            // Update the value.
            switch (HConfig.batching) {
                case DISABLED:
                    HConfig.batching = Batching.CONTAINING;
                    break;
                case CONTAINING:
                    HConfig.batching = Batching.CONTAINING_CONTAINED;
                    break;
                case CONTAINING_CONTAINED:
                    HConfig.batching = Batching.INTERSECTING;
                    break;
                default:
                    HConfig.batching = Batching.DISABLED;
                    break;
            }

            // Set the message and tooltip.
            this.minecraft.setScreen(this);
        }, this));

        // Add done button.
        this.sc_add(HStonecutter.guiButton(this.font, buttonX, this.height - 24, 200, 20,
                CommonComponents.GUI_DONE, null, btn -> this.onClose(), this));
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
    //? if >=1.20.1 {
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
    //?} else
    /*public void render(@NotNull PoseStack graphics, int mouseX, int mouseY, float delta) {*/
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
