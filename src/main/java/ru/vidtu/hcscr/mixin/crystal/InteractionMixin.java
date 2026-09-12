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
 *      http://www.apache.org/licenses/LICENSE-2.0
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
package ru.vidtu.hcscr.mixin.crystal;

import com.google.errorprone.annotations.DoNotCall;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.vidtu.hcscr.HCsCR;
import ru.vidtu.hcscr.compile.Variables;
import ru.vidtu.hcscr.config.Config;
import ru.vidtu.hcscr.config.CrystalMode;
import ru.vidtu.hcscr.platform.HStonecutter;

/**
 * Mixin that allows interaction entities (1.19.4+) to be hit,
 * if {@link Config#enable()} is {@code true}
 * and {@link Config#crystals()} is {@link CrystalMode#ENVELOPING}.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see Config#enable()
 * @see Config#crystals()
 * @see CrystalMode#ENVELOPING
 */
// @ApiStatus.Internal // Can't annotate this without logging in the console.
@Mixin(Interaction.class)
@NullMarked
public abstract class InteractionMixin extends Entity {
    /**
     * Logger for this class.
     */
    @Unique
    @UnknownNullability
    private static final Logger HCSCR_LOGGER = (Variables.DEBUG_LOGS ? LogManager.getLogger("HCsCR/InteractionMixin") : null);

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
        //noinspection DataFlowIssue // <- Never called. (Mixin)
        super(null, null);
        if (Variables.DEBUG_ASSERTS) {
            throw new AssertionError("HCsCR: No instances.");
        }
    }

    /**
     * Handles the attach skip process evaluation.
     * <p>
     * Forcefully enables attack interaction if the mod is
     * enabled and {@link CrystalMode#ENVELOPING} is active.
     * <p>
     * Does nothing if either:
     * <ul>
     *     <li>The level (world) is not a client level.</li>
     *     <li>The mod is disabled via {@link Config#enable()}.</li>
     *     <li>The {@link Config#crystals()} is not {@link CrystalMode#ENVELOPING}.</li>
     * </ul>
     * <p>
     * Allows further processing of the entity in {@link PlayerMixin_E}, as the attack will succeed.
     *
     * @param source Entity that is attacking this interaction
     * @param cir    Callback data containing the resulting interaction skipping decision (will be modified)
     * @apiNote Do not call, called by Mixin
     * @see Config#enable()
     * @see Config#crystals()
     * @see CrystalMode#ENVELOPING
     */
    @DoNotCall("Called by Mixin")
    @Inject(method = "skipAttackInteraction", at = @At("HEAD"), cancellable = true)
    private void hcscr_skipAttackInteraction_head(final Entity source, final CallbackInfoReturnable<Boolean> cir) {
        // Validate.
        if (Variables.DEBUG_ASSERTS) {
            assert (source != null) : "HCsCR: Parameter 'source' is null. (cir: " + cir + ", interaction: " + this + ')';
            assert (cir != null) : "HCsCR: Parameter 'cir' is null. (source: " + source + ", interaction: " + this + ')';
        }

        // Log. (**TRACE**)
        if (Variables.DEBUG_LOGS) {
            HCSCR_LOGGER.trace(HCsCR.MARKER, "HCsCR: Attacking an interaction. (source: {}, cir: {}, interaction: {})", source, cir, this);
        }

        // Validate.
        //~ if >=1.20.1 '.level' -> '.level()' {
        final Level level = this.level();
        //~}
        if (Variables.DEBUG_ASSERTS) {
            assert (level != null) : "HCsCR: Interaction level is null. (source: " + source + ", cir: " + cir + ", entity: " + this + ')';
        }

        // Do NOT process interactions if any of the following conditions is met:
        // - The current level (world) is not a client one. (e.g., integrated server world)
        // - The mod is fully disabled via the config.
        // - The "Remove Crystals" options is not ENVELOPING.
        if (!level.isClientSide() || !Config.enable() || (Config.crystals() != CrystalMode.ENVELOPING)) { // Implicit NPE for 'level'
            // Log. (**DEBUG**)
            if (Variables.DEBUG_LOGS) {
                HCSCR_LOGGER.debug(HCsCR.MARKER, "HCsCR: Ignored interaction attack. (source: {}, cir: {}, interaction: {})", source, cir, this);
            }

            // Stop.
            return;
        }

        // Forcefully allow the attack.
        // (NOTE: this is called skipAttackInteraction, set to false to allow attacking)
        cir.setReturnValue(false);

        // Log. (**DEBUG**)
        if (Variables.DEBUG_LOGS) {
            HCSCR_LOGGER.debug(HCsCR.MARKER, "HCsCR: Forcefully attacked interactiok. (source: {}, cir: {}, interaction: {})", source, cir, this);
        }
    }
}
//?}
