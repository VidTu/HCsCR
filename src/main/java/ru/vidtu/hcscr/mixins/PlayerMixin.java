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
 * Mixin that speeds up entity removing.
 *
 * @author VidTu
 */
// @ApiStatus.Internal // Can't annotate this without logging in the console.
@Mixin(Player.class)
@NullMarked
public final class PlayerMixin {
    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     */
    @Contract(value = "-> fail", pure = true)
    private PlayerMixin() {
        throw new AssertionError("No instances.");
    }

    // Removes the entities on hit.
    @SuppressWarnings("NonShortCircuitBooleanExpression") // <- Designed that way.
    //? if >=1.21.3 {
    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    //?} else
    /*@Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))*/
    public boolean hcscr_attack_hurt(Entity entity, DamageSource source, float amount) {
        return HStonecutter.hurt(entity, source, amount) | HCsCR.handleEntityHit(entity, source, amount);
    }
}
