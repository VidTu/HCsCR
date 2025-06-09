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

package ru.vidtu.hcscr.mixins;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.vidtu.hcscr.HCsCR;
import ru.vidtu.hcscr.platform.HStonecutter;

/**
 * Mixin that speeds up entity removing via {@link HCsCR#handleEntityHit(Entity, DamageSource, float)}.
 *
 * @author VidTu
 * @apiNote Internal use only
 */
// @ApiStatus.Internal // Can't annotate this without logging in the console.
@Mixin(Player.class)
@NullMarked
public final class PlayerMixin {
    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    // @ApiStatus.ScheduledForRemoval // Can't annotate this without logging in the console.
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private PlayerMixin() {
        throw new AssertionError("HCsCR: No instances.");
    }

    /**
     * Processes the entity attacking. Calls original attack method as well as
     * {@link HCsCR#handleEntityHit(Entity, DamageSource, float)}, returns {@code true} if any succeeded.
     *
     * @param entity Entity being attacked
     * @param source Attack source
     * @param amount Attack amount
     * @return Whether the attack has succeeded
     * @see HStonecutter#hurtEntity(Entity, DamageSource, float)
     * @see HCsCR#handleEntityHit(Entity, DamageSource, float)
     */
    //? if >=1.21.3 {
    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    //?} else
    /*@Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))*/
    private boolean hcscr_attack_hurtOrSimulate(Entity entity, DamageSource source, float amount) {
        // Validate.
        assert entity != null : "HCsCR: Parameter 'entity' is null. (source: " + source + ", amount: " + ", player: " + this + ')';
        assert source != null : "HCsCR: Parameter 'source' is null. (entity: " + entity + ", amount: " + ", player: " + this + ')';
        assert Float.isFinite(amount) : "HCsCR: Parameter 'amount' is not finite. (entity: " + entity + ", source: " + source + ", amount: " + ", player: " + this + ')';

        // Delegate.
        // TODO(VidTu): Use more compatible methods from MixinExtras if it's provided by the platform.
        //noinspection NonShortCircuitBooleanExpression // <- Needs to call both methods.
        return HStonecutter.hurtEntity(entity, source, amount) | HCsCR.handleEntityHit(entity, source, amount);
    }
}
