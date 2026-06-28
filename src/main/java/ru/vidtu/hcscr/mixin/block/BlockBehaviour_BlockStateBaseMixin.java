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

package ru.vidtu.hcscr.mixin.block;

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
import ru.vidtu.hcscr.compile.Variables;
import ru.vidtu.hcscr.config.BlockMode;
import ru.vidtu.hcscr.config.Config;
import ru.vidtu.hcscr.handler.BlockClips;

//? if <1.17.1 {
/*import ru.vidtu.hcscr.extension.EntityCollisionContextExtension;
*///?}

/**
 * Mixin that removes client player collision from blocks
 * that are {@link BlockClips#shouldClip(BlockPos)}.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see BlockClips#has(BlockPos)
 * @see BlockMode#COLLISION
 * @see Config#blocks()
 */
// @ApiStatus.Internal // Can't annotate this without logging in the console.
@Mixin(BlockBehaviour.BlockStateBase.class)
@NullMarked
public final class BlockBehaviour_BlockStateBaseMixin {
    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    // @ApiStatus.ScheduledForRemoval // Can't annotate this without logging in the console.
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private BlockBehaviour_BlockStateBaseMixin() {
        if (Variables.DEBUG_ASSERTS) {
            throw new AssertionError("HCsCR: No instances.");
        }
    }

    /**
     * Injects the empty collision for blocks in {@link BlockClips#shouldClip(BlockPos)} for the
     * client player entity context only. Does nothing for other contexts, including if the level
     * (world) is from the server, context lacks an entity, or the entity is not the client player.
     *
     * @param level   The level that this block is placed in, ignored
     * @param pos     Block position
     * @param context Current collision context to infer the collision type
     * @param cir     Callback data containing the resulting collision shape
     * @apiNote Do not call, called by Mixin
     * @see BlockClips#shouldClip(BlockPos)
     */
    @DoNotCall("Called by Mixin")
    @Inject(method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("HEAD"), cancellable = true) // HEAD here for early return.
    private void hcscr_getCollisionShape_head(final BlockGetter level, final BlockPos pos, final CollisionContext context,
                                              final CallbackInfoReturnable<VoxelShape> cir) {
        // Validate.
        if (Variables.DEBUG_ASSERTS) {
            assert (level != null) : "HCsCR: Parameter 'level' is null. (pos: " + pos + ", context: " + context + ", cir: " + cir + ", state: " + this + ')';
            assert (pos != null) : "HCsCR: Parameter 'pos' is null. (level: " + level + ", context: " + context + ", cir: " + cir + ", state: " + this + ')';
            assert (context != null) : "HCsCR: Parameter 'context' is null. (level: " + level + ", pos: " + pos + ", cir: " + cir + ", state: " + this + ')';
            assert (cir != null) : "HCsCR: Parameter 'cir' is null. (level: " + level + ", pos: " + pos + ", context: " + context + ", state: " + this + ')';
            // No thread checks, called from either side.
        }

        // Do nothing if either:
        // - The current collision context lacks an entity. (i.e., it's NOT an EntityCollisionContext)
        // - The context's entity is not a client player. (e.g., a non-player entity, a server player, a null)
        // - The block position doesn't match BlockClips.shouldClip(...).
        //? if >=1.18.2 {
        if (!(context instanceof EntityCollisionContext ctx) || !(ctx.getEntity() instanceof LocalPlayer) || !BlockClips.shouldClip(pos)) return;
        //?} elif >=1.17.1 {
        /*if (!(context instanceof EntityCollisionContext ctx) || !(ctx.getEntity().orElse(null) instanceof LocalPlayer) || !BlockClips.shouldClip(pos)) return;
        *///?} else {
        /*//noinspection CastToIncompatibleInterface // <- Mixin Accessor.
        if (!(context instanceof EntityCollisionContext) || !(((EntityCollisionContextExtension) context).hcscr_entity() instanceof LocalPlayer) || !BlockClips.shouldClip(pos)) return;
        *///?}

        // Spoof collision data to an empty shape.
        cir.setReturnValue(Shapes.empty()); // PERF: Singleton.
    }
}
