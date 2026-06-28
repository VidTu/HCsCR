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

package ru.vidtu.hcscr.mixin.crystal;

import com.google.errorprone.annotations.DoNotCall;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.vidtu.hcscr.compile.Variables;
import ru.vidtu.hcscr.handler.HiddenEntities;
import ru.vidtu.hcscr.platform.HStonecutter;

/**
 * Mixin that disables the bounding box for entities
 * marked as {@link HiddenEntities#isHidden(Entity)}.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see HiddenEntities#isHidden(Entity)
 * @see Entity#getBoundingBox()
 * @see Config#crystalsResync()
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
    private static /*shadow-final*/ AABB INITIAL_AABB;

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
        if (Variables.DEBUG_ASSERTS) {
            throw new AssertionError("HCsCR: No instances.");
        }
    }

    /**
     * Sets the bounding box to {@link #INITIAL_AABB} if this entity is {@link HiddenEntities#isHidden(Entity)},
     * removing its bounding box from the world. Does nothing otherwise. Also does nothing if entity's
     * level (world) is not client one. (e.g., an integrated server world)
     *
     * @param cir Callback data containing the resulting bounding box
     * @apiNote Do not call, called by Mixin
     * @see HiddenEntities#isHidden(Entity)
     * @see #INITIAL_AABB
     */
    @DoNotCall("Called by Mixin")
    @Inject(method = "getBoundingBox", at = @At("HEAD"), cancellable = true) // HEAD here for early return.
    private void hcscr_getBoundingBox_head(final CallbackInfoReturnable<AABB> cir) {
        // Validate.
        final Level level = HStonecutter.levelOfEntity((Entity) (Object) this);
        if (Variables.DEBUG_ASSERTS) {
            assert (level != null) : "HCsCR: Level is null. (cir: " + cir + ", entity: " + this + ')';
            // No thread checks, called from either side.
        }

        // Do nothing if either:
        // - The current level (world) is not client-side. (e.g., integrated server world)
        // - The entity is not hidden via HiddenEntities.isHidden(...).
        if (!level.isClientSide() || !HiddenEntities.isHidden((Entity) (Object) this)) return; // Implicit NPE for 'level'

        // Spoof to an empty hitbox.
        cir.setReturnValue(INITIAL_AABB);
    }
}
