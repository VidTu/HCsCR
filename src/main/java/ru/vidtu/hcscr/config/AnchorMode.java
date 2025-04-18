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
     * Mode translation key.
     */
    private final String key;

    /**
     * Mode tip translation key.
     */
    private final String tip;

    /**
     * Creates a new mode.
     */
    @Contract(pure = true)
    AnchorMode() {
        // Create the translation keys.
        this.key = ("hcscr.anchors." + this.name().toLowerCase(Locale.ROOT)).intern();
        this.tip = (this.key + ".tip").intern();
    }

    /**
     * Creates a button label for this mode.
     *
     * @return A new button label for this mode
     */
    @Contract(value = "-> new", pure = true)
    Component buttonLabel() {
        return HStonecutter.translate("options.generic_value", HStonecutter.translate("hcscr.anchors"), HStonecutter.translate(this.key));
    }

    /**
     * Creates a button tooltip for this mode.
     *
     * @return A new button tip for this mode
     */
    @Contract(value = "-> new", pure = true)
    Component buttonTip() {
        return HStonecutter.translate(this.tip);
    }

    @Contract(pure = true)
    @Override
    public String toString() {
        return "HCsCR/AnchorMode{" +
                "name='" + this.name() + '\'' +
                ", ordinal=" + this.ordinal() +
                ", key='" + this.key + '\'' +
                ", tip='" + this.tip + '\'' +
                '}';
    }
}
