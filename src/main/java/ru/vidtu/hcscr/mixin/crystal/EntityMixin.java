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

/**
 * Mixin that disables the bounding box for entities
 * marked as {@link HiddenEntities#isHidden(Entity)}.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see HiddenEntities#isHidden(Entity)
 * @see Config#crystalsResync()
 */
// @ApiStatus.Internal // Can't annotate this without logging in the console.
@Mixin(Entity.class)
@NullMarked
public final class EntityMixin {
    /**
     * Empty singleton bounding box.
     * <p>
     * Provided by the implementation. ({@link Shadow})
     *
     * @see HiddenEntities#isHidden(Entity)
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
     * Handles the bounding box obtaining process.
     * <p>
     * Sets the bounding box to {@link #INITIAL_AABB} if this entity is
     * {@link HiddenEntities#isHidden(Entity)}, removing its bounding box.
     * <p>
     * Does nothing if either:
     * <ul>
     *     <li>The level (world) is not a client level.</li>
     *     <li>An entity returns {@code false} in {@link HiddenEntities#isHidden(Entity)}.</li>
     * </ul>
     *
     * @param cir Callback data containing the resulting bounding box (will be modified)
     * @apiNote Do not call, called by Mixin
     * @see HiddenEntities#isHidden(Entity)
     * @see #INITIAL_AABB
     */
    @DoNotCall("Called by Mixin")
    @Inject(method = "getBoundingBox", at = @At("HEAD"), cancellable = true) // HEAD here for early return.
    private void hcscr_getBoundingBox_head(final CallbackInfoReturnable<AABB> cir) {
        // Validate.
        //~ if >=1.20.1 '.level' -> '.level()' {
        final Level level = ((Entity) (Object) this).level();
        //~}
        if (Variables.DEBUG_ASSERTS) {
            assert (level != null) : "HCsCR: Level is null. (cir: " + cir + ", entity: " + this + ')';
            // No thread checks, called from either side.
        }

        // Do nothing if either:
        // - The current level (world) is not a client one. (e.g., integrated server world)
        // - The entity is not marked hidden via HiddenEntities.isHidden(...).
        if (!level.isClientSide() || !HiddenEntities.isHidden((Entity) (Object) this)) return; // Implicit NPE for 'level'

        // Spoof to an empty hitbox.
        cir.setReturnValue(INITIAL_AABB);
    }
}
