/*
 * HCsCR is a third-party mod for Minecraft Java Edition to remove your end crystals faster.
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

//? if fabric {
package ru.vidtu.hcscr.platform;

import com.google.errorprone.annotations.DoNotCall;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import ru.vidtu.hcscr.config.HConfig;
import ru.vidtu.hcscr.config.HScreen;

/**
 * HCsCR entrypoint for the ModMenu API.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see HFabric
 * @see HConfig
 */
@ApiStatus.Internal
@NullMarked
public final class HModMenu implements ModMenuApi {
    /**
     * Creates a new entrypoint.
     *
     * @apiNote Do not call, called by ModMenu
     */
    @Contract(pure = true)
    public HModMenu() {
        // Empty
    }

    /**
     * Gets the ModMenu config screen factory.
     *
     * @return Config screen factory for ModMenu
     * @apiNote Do not call, called by ModMenu
     */
    @DoNotCall("Called by ModMenu")
    @Contract(pure = true)
    @Override
    public ConfigScreenFactory<HScreen> getModConfigScreenFactory() {
        return HScreen::new;
    }

    @Contract(pure = true)
    @Override
    public String toString() {
        return "HCsCR/HModMenu{}";
    }
}
//?}
