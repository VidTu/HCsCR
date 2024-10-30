/*
 * Copyright (c) 2023 Offenderify
 * Copyright (c) 2023-2024 VidTu
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

package ru.vidtu.hcscr.mixins;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Contract;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.vidtu.hcscr.HCsCRFabric;

/**
 * Mixin that speeds up entity removing.
 *
 * @author VidTu
 */
@Mixin(Player.class)
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
    @SuppressWarnings({"NonShortCircuitBooleanExpression", "deprecation"}) // <- Designed that way. (and deprecated method is used in Mojang's code)
    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    public boolean hcscr$attack$hurt(Entity entity, DamageSource source, float amount) {
        return entity.hurtOrSimulate(source, amount) | HCsCRFabric.handleEntityHit(entity, source, amount);
    }
}
