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

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import ru.vidtu.hcscr.platform.HStonecutter;

import java.util.Locale;

/**
 * Anchors removal mode.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see CrystalMode
 * @see HConfig#anchors()
 */
@ApiStatus.Internal
@NullMarked
public enum AnchorMode {
    /**
     * Anchors won't be instantly removed via right mouse click.
     */
    OFF,

    /**
     * The player collision for anchors will be instantly removed via right mouse click. This is the recommended
     * mode for most situations and is the default/only mode in other anchor optimizers.
     */
    COLLISION,

    /**
     * The anchor will be instantly removed via right mouse click. Will disable the \"air place\" mechanic.
     */
    FULL;

    /**
     * Mode button label.
     */
    private final Component label;

    /**
     * Mode button tip.
     */
    private final Component tip;

    /**
     * Creates a new mode.
     */
    @Contract(pure = true)
    AnchorMode() {
        // Create the translation key.
        String key = ("hcscr.anchors." + this.name().toLowerCase(Locale.ROOT));

        // Create the components.
        this.label = HStonecutter.translate("options.generic_value", HStonecutter.translate("hcscr.anchors"), HStonecutter.translate(key.intern()));
        this.tip = HStonecutter.translate((key + ".tip").intern());
    }

    /**
     * Gets the button label for this mode.
     *
     * @return Mode button label
     * @see #tip()
     * @see HScreen
     */
    @Contract(pure = true)
    Component label() {
        return this.label;
    }

    /**
     * Gets the button tooltip for this mode.
     *
     * @return Mode button tip
     * @see #label()
     * @see HScreen
     */
    @Contract(pure = true)
    Component tip() {
        return this.tip;
    }

    @Contract(pure = true)
    @Override
    public String toString() {
        return "HCsCR/AnchorMode{" +
                "name='" + this.name() + '\'' +
                ", ordinal=" + this.ordinal() +
                ", label='" + this.label + '\'' +
                ", tip='" + this.tip + '\'' +
                '}';
    }
}
