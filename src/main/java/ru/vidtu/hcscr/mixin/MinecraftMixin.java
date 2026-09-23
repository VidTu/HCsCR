/*
 * HCsCR is a third-party mod for Minecraft Java Edition
 * that allows removing the end crystals faster.
 *
 * Copyright (c) 2023 Offenderify
 * Copyright (c) 2023-2026 VidTu
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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.vidtu.hcscr.HCsCR;
import ru.vidtu.hcscr.compile.Variables;

/**
 * Mixin that:
 * <ul>
 *     <li>Calls {@link HCsCR#respawn(Minecraft)} on level change.</li>
 *     <li>Calls {@link HCsCR#loop(Minecraft)} on game loops (frames). (Fabric only)</li>
 * </ul>
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see HCsCR#respawn(Minecraft)
 * @see HCsCR#loop(Minecraft)
 */
// @ApiStatus.Internal // Can't annotate this without logging in the console.
@Mixin(Minecraft.class)
@NullMarked
public final class MinecraftMixin {
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
        if (Variables.DEBUG_ASSERTS) {
            throw new AssertionError("HCsCR: No instances.");
        }
    }

    /**
     * Handles game updating data on level load, change, or unload.
     * <p>
     * Calls {@link HCsCR#respawn(Minecraft)}.
     *
     * @param level     New level, {@code null} if was unloaded, ignored
     * @param stopSound Whether the sound engine should be halted and all sounds stopped, ignored
     * @param ci        Callback data, ignored
     * @apiNote Do not call, called by Mixin
     * @see HCsCR#respawn(Minecraft)
     */
    @DoNotCall("Called by Mixin")
    //? if >=1.21.11 {
    @Inject(method = "updateLevelInEngines(Lnet/minecraft/client/multiplayer/ClientLevel;Z)V", at = @At("RETURN"))
    private void hcscr_updateLevelInEngines_return(final @Nullable ClientLevel level, final boolean stopSound,
                                                   final CallbackInfo ci) {
    //?} else {
    /*@Inject(method = "updateLevelInEngines", at = @At("RETURN"))
    private void hcscr_updateLevelInEngines_return(final @Nullable ClientLevel level, final CallbackInfo ci) {
    *///?}
        // Respawn.
        HCsCR.respawn((Minecraft) (Object) this);
    }

    //? if fabric {
    /**
     * Handles every game loop iteration (every game frame).
     * <p>
     * Calls the {@link HCsCR#loop(Minecraft)} if the game ticking is advancing.
     *
     * @param advanceGameTime Whether the game should be ticked or just looped (game loop without game ticks), no logic is being run by the mod unless game is ticked (this set to {@code true})
     * @param ci              Callback data, ignored
     * @apiNote Do not call, called by Mixin
     * @see HCsCR#loop(Minecraft)
     */
    @DoNotCall("Called by Mixin")
    @Inject(method = "runTick", at = @At("RETURN"))
    private void hcscr_runTick_return(final boolean advanceGameTime, final CallbackInfo ci) {
        // Do nothing if game is not ticking. This happens when the integrated
        // server is initializing, shutting down, or the game is crashing.
        if (!advanceGameTime) return;

        // Loop.
        HCsCR.loop((Minecraft) (Object) this);
    }
    //?}
}
