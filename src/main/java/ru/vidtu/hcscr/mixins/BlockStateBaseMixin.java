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

import com.google.errorprone.annotations.DoNotCall;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.vidtu.hcscr.HCsCR;
import ru.vidtu.hcscr.config.AnchorMode;
import ru.vidtu.hcscr.platform.HStonecutter;

/**
 * Mixin that removes client player collision from blocks contained in {@link HCsCR#CLIPPING_ANCHORS}.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see HCsCR#CLIPPING_ANCHORS
 * @see AnchorMode#COLLISION
 */
// @ApiStatus.Internal // Can't annotate this without logging in the console.
@Mixin(BlockBehaviour.BlockStateBase.class)
@NullMarked
public final class BlockStateBaseMixin {
    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    // @ApiStatus.ScheduledForRemoval // Can't annotate this without logging in the console.
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private BlockStateBaseMixin() {
        throw new AssertionError("HCsCR: No instances.");
    }

    /**
     * Injects the empty collision for blocks in {@link HCsCR#CLIPPING_ANCHORS}.
     *
     * @param getter Block container (level/chunk), ignored
     * @param pos    Block position
     * @param ctx    Collision context
     * @param cir    Callback data
     * @apiNote Do not call, called by Mixin
     * @see HCsCR#CLIPPING_ANCHORS
     * @see HStonecutter#collisionContextEntity(EntityCollisionContext)
     */
    @DoNotCall("Called by Mixin")
    @Inject(method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("HEAD"), cancellable = true)
    private void hcscr_getCollisionShape_head(BlockGetter getter, BlockPos pos, CollisionContext ctx, CallbackInfoReturnable<VoxelShape> cir) {
        // Validate.
        assert getter != null : "HCsCR: Parameter 'getter' is null. (pos: " + pos + ", ctx: " + ctx + ", cir: " + cir + ", state: " + this + ')';
        assert pos != null : "HCsCR: Parameter 'pos' is null. (getter: " + getter + ", ctx: " + ctx + ", cir: " + cir + ", state: " + this + ')';
        assert ctx != null : "HCsCR: Parameter 'ctx' is null. (getter: " + getter + ", pos: " + pos + ", cir: " + cir + ", state: " + this + ')';
        assert cir != null : "HCsCR: Parameter 'cir' is null. (getter: " + getter + ", pos: " + pos + ", ctx: " + ctx + ", state: " + this + ')';

        // Do NOT remove collision if any of the following conditions is met:
        // - The current collision context lacks an entity.
        // - The context's entity is not a client-side player. (either non-player entity or a server-side player)
        // - The block position is not contained in HCsCR.CLIPPING_ANCHORS.
        if (!(ctx instanceof EntityCollisionContext) ||
                !(HStonecutter.collisionContextEntity((EntityCollisionContext) ctx) instanceof LocalPlayer) ||
                !HCsCR.CLIPPING_ANCHORS.contains(pos)) return;

        // Spoof collision data to empty.
        cir.setReturnValue(Shapes.empty());
    }
}
