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

//? if >=1.19.4 {
package ru.vidtu.hcscr.mixins;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.vidtu.hcscr.config.CrystalMode;
import ru.vidtu.hcscr.config.HConfig;
import ru.vidtu.hcscr.platform.HStonecutter;

/**
 * Mixin that allows {@link Interaction} entities to be hit if {@link HConfig#allowHittingInteractions()} is allowed.
 *
 * @author VidTu
 * @apiNote Internal use only
 */
// @ApiStatus.Internal // Can't annotate this without logging in the console.
@Mixin(Interaction.class)
@NullMarked
public final class InteractionMixin {
    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    // @ApiStatus.ScheduledForRemoval // Can't annotate this without logging in the console.
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private InteractionMixin() {
        throw new AssertionError("No instances.");
    }

    /**
     * Enables attack interaction, if mod is enabled and {@link CrystalMode#ENVELOPING} is active.
     *
     * @param entity Interacting entity
     * @param cir    Callback data
     */
    @Inject(method = "skipAttackInteraction", at = @At("HEAD"), cancellable = true)
    private void hcscr_skipAttackInteraction_head(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        // Do NOT process interactions if any of the following conditions is met:
        // - The mod is disabled via config or keybind.
        // - The current crystal removal mode is not ENVELOPING.
        // - The current level (world) is not client-side. (e.g. integrated server world)
        if (!HConfig.allowHittingInteractions() && !HStonecutter.levelOf(entity).isClientSide()) return;

        // Forcefully allow interactions.
        cir.setReturnValue(false);
    }
}
//?}
