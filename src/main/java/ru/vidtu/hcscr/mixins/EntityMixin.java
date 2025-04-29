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

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.vidtu.hcscr.HCsCR;
import ru.vidtu.hcscr.platform.HStonecutter;

/**
 * Mixin that disables the bounding box for entities marked as {@link HCsCR#HIDDEN_ENTITIES}.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see HCsCR#HIDDEN_ENTITIES
 * @see Entity#getBoundingBox()
 */
// @ApiStatus.Internal // Can't annotate this without logging in the console.
@Mixin(Entity.class)
@NullMarked
public final class EntityMixin {
    /**
     * Empty bounding box, provided by the implementation.
     */
    @SuppressWarnings("NonConstantFieldWithUpperCaseName") // <- Shadow.
    @Shadow
    @Final
    private static AABB INITIAL_AABB;

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    // @ApiStatus.ScheduledForRemoval // Can't annotate this without logging in the console.
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private EntityMixin() {
        throw new AssertionError("HCsCR: No instances.");
    }

    /**
     * Sets the bounding box to {@link #INITIAL_AABB} if this entity is {@link HCsCR#HIDDEN_ENTITIES}.
     *
     * @param cir Callback data
     * @see HCsCR#HIDDEN_ENTITIES
     */
    @Inject(method = "getBoundingBox", at = @At("HEAD"), cancellable = true)
    private void hcscr_getBoundingBox_head(CallbackInfoReturnable<AABB> cir) {
        // Validate.
        Level level = this.hcscr_level();
        assert level != null : "HCsCR: Getting entity bounding box with null level. (cir: " + cir + ", entity: " + this + ')';

        // Do NOT hide entity if any of the following conditions is met:
        // - The current level (world) is not client-side. (e.g. integrated server world)
        // - The entity is not actually hidden.
        //noinspection SuspiciousMethodCalls // <- Mixin.
        if (!level.isClientSide() || !HCsCR.HIDDEN_ENTITIES.containsKey(this)) return;

        // Set to empty hitbox.
        cir.setReturnValue(INITIAL_AABB);
    }

    /**
     * A hacky method to call the {@link HStonecutter#levelOf(Entity)}
     * with IntelliJ not marking it as unreachable code.
     *
     * @return Game profiler
     */
    @Contract(pure = true)
    @Unique
    private Level hcscr_level() {
        return HStonecutter.levelOf((Entity) (Object) this);
    }
}
