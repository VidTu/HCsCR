/*
 * Copyright (c) 2023 Offenderify
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

package ru.offenderify.hcscr.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.offenderify.hcscr.HCsCR;

/**
 * Mixin that overwrites end crystal hit removing.
 *
 * @author Offenderify
 */
@Mixin(EndCrystal.class)
public abstract class EndCrystalMixin extends Entity {
    private EndCrystalMixin() {
        super(null, null);
        throw new AssertionError("The life is hard, but initializing @Mixin is harder.");
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    public void hcscr$hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (isInvulnerableTo(source) || source.getEntity() instanceof EnderDragon || isRemoved() || !level.isClientSide || HCsCR.disabledByCurrentServer) return;
        remove(RemovalReason.KILLED);
        cir.setReturnValue(true);
    }
}
