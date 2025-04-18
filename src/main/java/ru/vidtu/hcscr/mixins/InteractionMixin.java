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
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.vidtu.hcscr.config.CrystalMode;
import ru.vidtu.hcscr.config.HConfig;
import ru.vidtu.hcscr.platform.HStonecutter;

/**
 * Mixin that allows {@link Interaction} entities to be hit if {@link HConfig#enable()} is {@code true}
 * and {@link HConfig#crystals()} is {@link CrystalMode#ENVELOPING}. (1.19.4+)
 *
 * @author VidTu
 * @apiNote Internal use only
 */
// @ApiStatus.Internal // Can't annotate this without logging in the console.
@Mixin(Interaction.class)
@NullMarked
public abstract class InteractionMixin extends Entity {
    /**
     * Logger for this class.
     */
    @Unique
    private static final Logger HCSCR_LOGGER = LogManager.getLogger("HCsCR/InteractionMixin");

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
        super(null, null);
        throw new AssertionError("No instances.");
    }

    /**
     * Forcefully enables attack interaction, if mod is enabled and {@link CrystalMode#ENVELOPING} is active.
     *
     * @param attacker Attacking entity
     * @param cir      Callback data
     */
    @Inject(method = "skipAttackInteraction", at = @At("HEAD"), cancellable = true)
    private void hcscr_skipAttackInteraction_head(Entity attacker, CallbackInfoReturnable<Boolean> cir) {
        // Log. (**TRACE**)
        HCSCR_LOGGER.trace("HCsCR: Received attack in Interaction entity. (attacker: {}, cir: {}, interaction: {})", attacker, cir, this);

        // Validate.
        Level level = HStonecutter.levelOf(this);
        assert level != null : "HCsCR: Interaction entity has null level. (attacker: " + attacker + ", cir: " + cir + ", entity: " + this + ')';

        // Do NOT process interactions if any of the following conditions is met:
        // - The current level (world) is not client-side. (e.g. integrated server world)
        // - The mod is disabled via config/keybind.
        // - The current crystal removal mode is not ENVELOPING.
        if (!level.isClientSide() || !HConfig.enable() || (HConfig.crystals() != CrystalMode.ENVELOPING)) {
            // Log, stop. (**DEBUG**)
            HCSCR_LOGGER.debug("HCsCR: Ignored Interaction entity attack overriding. (attacker: {}, cir: {}, interaction: {})", attacker, cir, this);
            return;
        }

        // Forcefully allow interactions.
        cir.setReturnValue(false);

        // Log. (**DEBUG**)
        HCSCR_LOGGER.debug("HCsCR: Forcefully allowed Interaction to be attacked. (attacker: {}, cir: {}, interaction: {})", attacker, cir, this);
    }
}
//?}
