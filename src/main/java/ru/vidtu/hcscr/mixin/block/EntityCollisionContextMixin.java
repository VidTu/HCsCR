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

//? if <1.17.1 {
/*package ru.vidtu.hcscr.mixin.block;

import com.google.errorprone.annotations.DoNotCall;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.vidtu.hcscr.HEntityCollisionContext;
import ru.vidtu.hcscr.platform.HCompile;
import ru.vidtu.hcscr.platform.HStonecutter;

/^*
 * Mixin that stored the source entity of {@link EntityCollisionContext}. (<1.17.1)
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see HEntityCollisionContext
 * @see BlockStateBaseMixin
 ^/
// @ApiStatus.Internal // Can't annotate this without logging in the console.
@Mixin(EntityCollisionContext.class)
@NullMarked
public final class EntityCollisionContextMixin implements HEntityCollisionContext {
    /^*
     * Entity involving in the context, {@code null} if none.
     ^/
    @Unique
    @Nullable
    private /^non-final^/ Entity hcscr_entity; // Unfortunate it's not final.

    /^*
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     ^/
    // @ApiStatus.ScheduledForRemoval // Can't annotate this without logging in the console.
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private EntityCollisionContextMixin() {
        throw new AssertionError("HCsCR: No instances.");
    }

    /^*
     * Stores the {@link #hcscr_entity} for future use.
     *
     * @param entity The entity to store in the context, {@code null} if none
     * @param ci     Callback data, ignored
     * @apiNote Do not call, called by Mixin
     * @see HStonecutter#collisionContextEntity(EntityCollisionContext)
     * @see #hcscr_entity()
     ^/
    @DoNotCall("Called by Mixin")
    @Inject(method = "<init>(Lnet/minecraft/world/entity/Entity;)V", at = @At("RETURN"))
    private void hcscr_init_return(@Nullable final Entity entity, final CallbackInfo ci) {
        // Store.
        this.hcscr_entity = entity;
    }

    @Contract(pure = true)
    @Override
    @Nullable
    public Entity hcscr_entity() {
        return this.hcscr_entity;
    }
}
*///?}
