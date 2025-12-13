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

package ru.vidtu.hcscr.mixin.crystal;

import com.google.errorprone.annotations.DoNotCall;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.vidtu.hcscr.HCsCR;
import ru.vidtu.hcscr.platform.HCompile;
import ru.vidtu.hcscr.platform.HPlugin;
import ru.vidtu.hcscr.platform.HStonecutter;

/**
 * Mixin that speeds up entity removing via {@link HCsCR#handlePlayerHittingEntity(Entity, DamageSource, float)}
 * in absence of MixinExtras via {@link Redirect} hook. See {@link PlayerMixin_M} for an alternative.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see HPlugin
 * @see PlayerMixin_M
 */
// @ApiStatus.Internal // Can't annotate this without logging in the console.
@Mixin(Player.class)
@NullMarked
public final class PlayerMixin_M {
    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    // @ApiStatus.ScheduledForRemoval // Can't annotate this without logging in the console.
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private PlayerMixin_M() {
        throw new AssertionError("HCsCR: No instances.");
    }

    /**
     * Processes the entity attacking. Calls original attack method as well as
     * {@link HCsCR#handlePlayerHittingEntity(Entity, DamageSource, float)}, returns {@code true} if any succeeded.
     *
     * @param target       Entity being attacked by this player
     * @param damageSource Attack source (inaccurate if invoked on the client)
     * @param totalDamage  Total amount of damage done to the entity (inaccurate if invoked on the client)
     * @return Whether the attack has succeeded
     * @apiNote Do not call, called by Mixin
     * @see HStonecutter#hurtEntity(Entity, DamageSource, float)
     * @see HCsCR#handlePlayerHittingEntity(Entity, DamageSource, float)
     */
    @DoNotCall("Called by Mixin")
    //? if >=1.21.3 {
    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    //?} else {
    /*@Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    *///?}
    private boolean hcscr_attack_hurtOrSimulate(final Entity target, final DamageSource damageSource,
                                                final float totalDamage) {
        // Validate.
        if (HCompile.DEBUG_ASSERTS) {
            assert (target != null) : "HCsCR: Parameter 'target' is null. (damageSource: " + damageSource + ", totalDamage: " + totalDamage + ", player: " + this + ')';
            assert (damageSource != null) : "HCsCR: Parameter 'damageSource' is null. (target: " + target + ", totalDamage: " + totalDamage + ", player: " + this + ')';
            assert (Float.isFinite(totalDamage)) : "HCsCR: Parameter 'totalDamage' is not finite. (target: " + target + ", damageSource: " + damageSource + ", totalDamage: " + totalDamage + ", player: " + this + ')';
        }

        // Delegate.
        //noinspection NonShortCircuitBooleanExpression // <- Needs to call both methods.
        return (HStonecutter.hurtEntity(target, damageSource, totalDamage) | HCsCR.handlePlayerHittingEntity(target, damageSource, totalDamage));
    }
}
