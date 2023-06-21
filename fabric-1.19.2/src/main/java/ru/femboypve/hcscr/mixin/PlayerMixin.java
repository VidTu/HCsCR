/*
 * Copyright (c) 2023 Offenderify, VidTu
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

package ru.femboypve.hcscr.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.femboypve.hcscr.HCsCRFabric;

import java.util.HashSet;

/**
 * Mixin that speeds up entity removing.
 *
 * @author VidTu
 */
@Mixin(Player.class)
public abstract class PlayerMixin {
    private PlayerMixin() {
        throw new AssertionError("The life is hard, but initializing @Mixin is harder.");
    }

    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    public boolean hcscr$attack$hurt(Entity entity, DamageSource source, float amount) {
        return entity.hurt(source, amount) | HCsCRFabric.removeClientSide(entity, source, amount, new HashSet<>());
    }
}
