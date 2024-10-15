/*
 * Copyright (c) 2023 Offenderify
 * Copyright (c) 2023-2024 VidTu
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

package ru.vidtu.hcscr;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vidtu.hcscr.config.HConfig;

/**
 * Main HCsCR class.
 *
 * @author Offenderify
 * @author VidTu
 */
public final class HCsCR {
    /**
     * Logger for this class.
     */
    @NotNull
    private static final Logger LOGGER = LoggerFactory.getLogger("HCsCR");

    /***
     * Whether the mod is enabled by the current server, {@code true} by default.
     */
    private static boolean serverEnabled = true;

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     */
    @Contract(value = "-> fail", pure = true)
    private HCsCR() {
        throw new AssertionError("No instances.");
    }

    /**
     * Toggles the mod.
     *
     * @return The new mod state
     */
    @CanIgnoreReturnValue
    public static boolean toggle() {
        boolean newState = (HConfig.enabled = !HConfig.enabled);
        HConfig.saveOrThrow();
        return newState;
    }

    /**
     * Gets the server enabled state.
     *
     * @return Whether the mod is enabled by the current server, {@code true} by default
     */
    @Contract(pure = true)
    public static boolean serverEnabled() {
        return serverEnabled;
    }

    /**
     * Sets the server enabled state.
     *
     * @param serverEnabled Whether the mod should be enabled by the current server, {@code true} by default
     * @return Whether the state has been changed
     */
    @CanIgnoreReturnValue
    public static boolean serverEnabled(boolean serverEnabled) {
        if (HCsCR.serverEnabled == serverEnabled) return false;
        HCsCR.serverEnabled = serverEnabled;
        LOGGER.info("HCsCR: The current server has {} the mod.", serverEnabled ? "enabled" : "disabled");
        return true;
    }

    /**
     * Resets the server enabled state to {@code true}.
     *
     * @return Whether the state has been changed
     */
    @CanIgnoreReturnValue
    public static boolean serverEnabledReset() {
        if (serverEnabled) return false;
        serverEnabled = true;
        LOGGER.info("HCsCR: The current server has been changed. The mod is now enabled.");
        return true;
    }
}
