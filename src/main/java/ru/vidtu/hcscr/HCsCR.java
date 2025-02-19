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

package ru.vidtu.hcscr;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
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
    private static final Logger LOGGER = LogManager.getLogger("HCsCR");

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
        HConfig.saveOrLog();
        return newState;
    }
}
