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

package ru.vidtu.hcscr.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.vidtu.hcscr.HCsCR;
import ru.vidtu.hcscr.platform.HStonecutter;

/**
 * Mixin that clears {@link HCsCR#SCHEDULED_ENTITIES} and {@link HCsCR#HIDDEN_ENTITIES} on world switching.
 *
 * @author VidTu
 * @apiNote Internal use only
 */
// @ApiStatus.Internal // Can't annotate this without logging in the console.
@Mixin(Minecraft.class)
@NullMarked
public final class MinecraftMixin {
    /**
     * Logger for this class.
     */
    @Unique
    private static final Logger HCSCR_LOGGER = LogManager.getLogger("HCsCR/MinecraftMixin");

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    // @ApiStatus.ScheduledForRemoval // Can't annotate this without logging in the console.
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private MinecraftMixin() {
        throw new AssertionError("No instances.");
    }

    /**
     * Clears the {@link HCsCR#SCHEDULED_ENTITIES} and {@link HCsCR#HIDDEN_ENTITIES}.
     *
     * @param level New level, {@code null} if was unloaded, ignored
     * @param ci    Callback data, ignored
     */
    @Inject(method = "updateLevelInEngines", at = @At("RETURN"))
    private void hcscr_updateLevelInEngines_return(@Nullable ClientLevel level, CallbackInfo ci) {
        // Get and push the profiler.
        ProfilerFiller profiler = HStonecutter.profilerOf((Minecraft) (Object) this);
        profiler.push("hcscr:clear_data");

        // Log. (**TRACE**)
        HCSCR_LOGGER.trace("HCsCR: Clearing data... (level: {}, game: {})", level, this);

        // Clear the maps.
        HCsCR.SCHEDULED_ENTITIES.clear();
        HCsCR.HIDDEN_ENTITIES.clear();
        HCsCR.CLIPPING_ANCHORS.clear();

        // Log. (**DEBUG**)
        HCSCR_LOGGER.debug("HCsCR: Cleared data. (level: {}, game: {})", level, this);

        // Pop the profiler.
        profiler.pop();
    }
}
