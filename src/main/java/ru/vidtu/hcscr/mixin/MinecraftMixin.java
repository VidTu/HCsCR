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

package ru.vidtu.hcscr.mixin;

import com.google.errorprone.annotations.DoNotCall;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
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
import ru.vidtu.hcscr.platform.HCompile;
import ru.vidtu.hcscr.platform.HStonecutter;

/**
 * Mixin that clears {@link HCsCR#SCHEDULED_ENTITIES} and {@link HCsCR#HIDDEN_ENTITIES} on world switching.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see HCsCR
 */
// @ApiStatus.Internal // Can't annotate this without logging in the console.
@Mixin(Minecraft.class)
@NullMarked
public abstract class MinecraftMixin extends ReentrantBlockableEventLoop<Runnable> {
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
    @SuppressWarnings({"RedundantSuppression", "DataFlowIssue"}) // <- Never called. (Mixin)
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private MinecraftMixin() {
        super(null);
        throw new AssertionError("HCsCR: No instances.");
    }

    /**
     * Clears the {@link HCsCR#SCHEDULED_ENTITIES}, {@link HCsCR#HIDDEN_ENTITIES},
     * and {@link HCsCR#CLIPPING_BLOCKS} on level load, change, or unload.
     *
     * @param level     New level, {@code null} if was unloaded, ignored
     * @param stopSound Whether the sound engine should be halted and all sounds stopped, ignored
     * @param ci        Callback data, ignored
     * @apiNote Do not call, called by Mixin
     * @see HCsCR#SCHEDULED_ENTITIES
     * @see HCsCR#HIDDEN_ENTITIES
     * @see HCsCR#CLIPPING_BLOCKS
     */
    @DoNotCall("Called by Mixin")
    //? if >=1.21.11 {
    @Inject(method = "updateLevelInEngines(Lnet/minecraft/client/multiplayer/ClientLevel;Z)V", at = @At("RETURN"))
    private void hcscr_updateLevelInEngines_return(@Nullable final ClientLevel level, final boolean stopSound,
                                                   final CallbackInfo ci) {
    //?} else {
    /*@Inject(method = "updateLevelInEngines", at = @At("RETURN"))
    private void hcscr_updateLevelInEngines_return(@Nullable final ClientLevel level, final CallbackInfo ci) {
    *///?}
        // Validate.
        if (HCompile.DEBUG_ASSERTS) {
            assert (this.isSameThread()) : "HCsCR: Updating level in engines NOT from the main thread. (thread: " + Thread.currentThread() + ", level: " + level + ", client: " + this + ')';
        }

        // Get and push the profiler.
        final ProfilerFiller profiler;
        if (HCompile.DEBUG_PROFILER) {
            profiler = HStonecutter.profilerOfClient((Minecraft) (Object) this);
            profiler.push("hcscr:clear_data");
        } else {
            profiler = null;
        }

        // Log. (**TRACE**)
        if (HCompile.DEBUG_LOGS) {
            HCSCR_LOGGER.trace(HCsCR.HCSCR_MARKER, "HCsCR: Clearing data... (level: {}, client: {})", level, this);
        }

        // Clear the maps.
        HCsCR.SCHEDULED_ENTITIES.clear();
        HCsCR.HIDDEN_ENTITIES.clear();
        HCsCR.CLIPPING_BLOCKS.clear();

        // Log. (**DEBUG**)
        if (HCompile.DEBUG_LOGS) {
            HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Cleared data. (level: {}, client: {})", level, this);
        }

        // Pop the profiler.
        if (HCompile.DEBUG_PROFILER) {
            profiler.pop();
        }
    }

    //? if fabric {
    /**
     * Calls the {@link HCsCR#handleClientMainLoop(Minecraft)} if the game is ticking.
     *
     * @param advanceGameTime Whether the game should be explicitly ticked or just updated, no logic is being run by the mod unless set to {@code true}
     * @param ci              Callback data, ignored
     * @apiNote Do not call, called by Mixin
     * @see HCsCR#handleClientMainLoop(Minecraft)
     */
    @DoNotCall("Called by Mixin")
    @Inject(method = "runTick", at = @At("RETURN"))
    private void hcscr_runTick_return(final boolean advanceGameTime, final CallbackInfo ci) {
        // Skip if game is not ticking. This happens when the integrated
        // server is loading, unloading, or the game is crashing.
        if (!advanceGameTime) return;

        // Tick.
        HCsCR.handleClientMainLoop((Minecraft) (Object) this);
    }
    //?}
}
