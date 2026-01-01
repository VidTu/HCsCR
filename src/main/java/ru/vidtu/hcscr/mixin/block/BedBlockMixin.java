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
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.vidtu.hcscr.HCsCR;
import ru.vidtu.hcscr.config.BlockMode;
import ru.vidtu.hcscr.config.HConfig;
import ru.vidtu.hcscr.platform.HCompile;

//? if >=1.20.6 {
import ru.vidtu.hcscr.platform.HStonecutter;
//?} else {
/*import net.minecraft.world.InteractionHand;
*///?}

/**
 * Mixin that allows beds to be removed via right click.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see BlockMode
 */
// @ApiStatus.Internal // Can't annotate this without logging in the console.
@Mixin(BedBlock.class)
@NullMarked
public final class BedBlockMixin {
    /**
     * Logger for this class.
     */
    @Unique
    private static final Logger HCSCR_LOGGER = LogManager.getLogger("HCsCR/BedBlockMixin");

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    // @ApiStatus.ScheduledForRemoval // Can't annotate this without logging in the console.
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private BedBlockMixin() {
        throw new AssertionError("HCsCR: No instances.");
    }

    //? if >=1.20.6 {

    /**
     * Handles the bed usage.
     *
     * @param state     Bed block state
     * @param level     The level that this bed block is placed in
     * @param pos       Bed block position
     * @param player    Player interacting with the bed, ignored
     * @param hitResult The exact position player used the bed at, ignored
     * @param cir       Callback data containing the bed interaction result, ignored
     * @apiNote Do not call, called by Mixin
     * @see HConfig#enable()
     * @see HConfig#blocks()
     * @see BlockMode
     * @see HCsCR#CLIPPING_BLOCKS
     */
    @DoNotCall("Called by Mixin")
    @Inject(method = "useWithoutItem", at = @At("HEAD"))
    private void hcscr_useWithoutItem_head(final BlockState state, final Level level, final BlockPos pos,
                                           final Player player, final BlockHitResult hitResult,
                                           final CallbackInfoReturnable<InteractionResult> cir) {
        // Validate.
        if (HCompile.DEBUG_ASSERTS) {
            assert (state != null) : "HCsCR: Parameter 'state' is null. (level: " + level + ", pos: " + pos + ", player: " + player + ", hitResult: " + hitResult + ", bed: " + this + ')';
            assert (level != null) : "HCsCR: Parameter 'level' is null. (state: " + state + ", pos: " + pos + ", player: " + player + ", hitResult: " + hitResult + ", bed: " + this + ')';
            assert (pos != null) : "HCsCR: Parameter 'pos' is null. (state: " + state + ", level: " + level + ", player: " + player + ", hitResult: " + hitResult + ", bed: " + this + ')';
            assert (player != null) : "HCsCR: Parameter 'player' is null. (state: " + state + ", level: " + level + ", pos: " + pos + ", hitResult: " + hitResult + ", bed: " + this + ')';
            assert (hitResult != null) : "HCsCR: Parameter 'hitResult' is null. (state: " + state + ", level: " + level + ", pos: " + pos + ", player: " + player + ", bed: " + this + ')';
        }

        // Log. (**TRACE**)
        if (HCompile.DEBUG_LOGS) {
            HCSCR_LOGGER.trace(HCsCR.HCSCR_MARKER, "HCsCR: Detected bed right click. (state: {}, level: {}, pos: {}, player: {}, hitResult: {}, bed: {})", state, level, pos, player, hitResult, this);
        }

        // Do NOT process beds if any of the following conditions is met:
        // - The current level (world) is not client-side. (e.g., integrated server world)
        // - The mod is disabled via config or keybind.
        // - The bed doesn't explode in the current environment/dimension.
        // - The "remove blocks" feature is OFF. (in switch block)
        if (!level.isClientSide() || !HConfig.enable() || !HStonecutter.willBedExplode(level)) { // Implicit NPE for 'level'
            // Log. (**DEBUG**)
            if (HCompile.DEBUG_LOGS) {
                HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Skipped bed right click removing. (state: {}, level: {}, pos: {}, player: {}, hitResult: {}, bed: {})", state, level, pos, player, hitResult, this);
            }

            // Stop.
            return;
        }

        // Validate.
        if (HCompile.DEBUG_ASSERTS) {
            assert (Minecraft.getInstance().isSameThread()) : "HCsCR: Clicking on a bed NOT from the main thread. (thread: " + Thread.currentThread() + ", state: " + state + ", level: " + level + ", pos: " + pos + ", player: " + player + ", hitResult: " + hitResult + ", bed: " + this + ')';
        }

        // Remove or clip.
        switch (HConfig.blocks()) {
            case COLLISION:
                // Get the bed's other part.
                final BlockPos otherPosCollision = pos.relative(BedBlock.getConnectedDirection(state)); // Implicit NPE for 'pos', 'state'
                final BlockState otherStateCollision = level.getBlockState(otherPosCollision);

                // Clip.
                HCsCR.CLIPPING_BLOCKS.put(pos, state);
                if (otherStateCollision.is(BlockTags.BEDS)) {
                    HCsCR.CLIPPING_BLOCKS.put(otherPosCollision, otherStateCollision);
                }

                // Log. (**DEBUG**)
                if (HCompile.DEBUG_LOGS) {
                    HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Clipping bed via right click. (state: {}, level: {}, pos: {}, player: {}, hitResult: {}, bed: {})", state, level, pos, player, hitResult, this);
                }

                // Break.
                break;
            case FULL:
                // Get the bed's other part.
                final BlockPos otherPosFull = pos.relative(BedBlock.getConnectedDirection(state)); // Implicit NPE for 'pos', 'state'
                final BlockState otherStateFull = level.getBlockState(otherPosFull);

                // Remove.
                level.removeBlock(pos, false);
                if (otherStateFull.is(BlockTags.BEDS)) {
                    level.removeBlock(otherPosFull, false);
                }

                // Log. (**DEBUG**)
                if (HCompile.DEBUG_LOGS) {
                    HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Removed bed via right click. (state: {}, level: {}, pos: {}, player: {}, hitResult: {}, bed: {})", state, level, pos, player, hitResult, this);
                }

                // Break.
                break;
            default:
                // Log. (**DEBUG**)
                if (HCompile.DEBUG_LOGS) {
                    HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Ignored bed right click. (state: {}, level: {}, pos: {}, player: {}, hitResult: {}, bed: {})", state, level, pos, player, hitResult, this);
                }
        }
    }

    //?} else {

    /*/^*
     * Handles the bed usage.
     *
     * @param state     Bed block state
     * @param level     The level that this bed block is placed in
     * @param pos       Bed block position
     * @param player    Player interacting with the bed, ignored
     * @param hand      The hand the player uses to interact with the bed, ignored
     * @param hitResult The exact position player used the bed at, ignored
     * @param cir       Callback data containing the bed interaction result, ignored
     * @apiNote Do not call, called by Mixin
     * @see HConfig#enable()
     * @see HConfig#blocks()
     * @see BlockMode
     * @see HCsCR#CLIPPING_BLOCKS
     ^/
    @DoNotCall("Called by Mixin")
    @Inject(method = "use", at = @At("HEAD"))
    private void hcscr_use_head(final BlockState state, final Level level, final BlockPos pos, final Player player,
                                final InteractionHand hand, final BlockHitResult hitResult,
                                final CallbackInfoReturnable<InteractionResult> cir) {
        // Validate.
        if (HCompile.DEBUG_ASSERTS) {
            assert (state != null) : "HCsCR: Parameter 'state' is null. (level: " + level + ", pos: " + pos + ", player: " + player + ", hand: " + hand + ", hitResult: " + hitResult + ", bed: " + this + ')';
            assert (level != null) : "HCsCR: Parameter 'level' is null. (state: " + state + ", pos: " + pos + ", player: " + player + ", hand: " + hand + ", hitResult: " + hitResult + ", bed: " + this + ')';
            assert (pos != null) : "HCsCR: Parameter 'pos' is null. (state: " + state + ", level: " + level + ", player: " + player + ", hand: " + hand + ", hitResult: " + hitResult + ", bed: " + this + ')';
            assert (player != null) : "HCsCR: Parameter 'player' is null. (state: " + state + ", level: " + level + ", pos: " + pos + ", hand: " + hand + ", hitResult: " + hitResult + ", bed: " + this + ')';
            assert (hand != null) : "HCsCR: Parameter 'hand' is null. (state: " + state + ", level: " + level + ", pos: " + pos + ", player: " + player + ", hitResult: " + hitResult + ", bed: " + this + ')';
            assert (hitResult != null) : "HCsCR: Parameter 'hitResult' is null. (state: " + state + ", level: " + level + ", pos: " + pos + ", player: " + player + ", hand: " + hand + ", bed: " + this + ')';
        }

        // Do NOT process beds if any of the following conditions is met:
        // - The current level (world) is not client-side. (e.g., integrated server world)
        // - The bed doesn't explode in the current dimension.
        // - The mod is disabled via config or keybind.
        // - The "remove blocks" feature is OFF. (in switch block)
        if (!level.isClientSide() || BedBlock.canSetSpawn(level) || !HConfig.enable()) { // Implicit NPE for 'level'
            // Log. (**DEBUG**)
            if (HCompile.DEBUG_LOGS) {
                HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Skipped bed right click removing. (state: {}, level: {}, pos: {}, player: {}, hand: {}, hitResult: {}, bed: {})", state, level, pos, player, hand, hitResult, this);
            }

            // Stop.
            return;
        }

        // Validate.
        if (HCompile.DEBUG_ASSERTS) {
            assert (Minecraft.getInstance().isSameThread()) : "HCsCR: Clicking on a bed NOT from the main thread. (thread: " + Thread.currentThread() + ", state: " + state + ", level: " + level + ", pos: " + pos + ", player: " + player + ", hand: " + hand + ", hitResult: " + hitResult + ", bed: " + this + ')';
        }

        // Remove or clip.
        switch (HConfig.blocks()) {
            case COLLISION:
                // Get the bed's other part.
                final BlockPos otherPosCollision = pos.relative(BedBlock.getConnectedDirection(state)); // Implicit NPE for 'pos', 'state'
                final BlockState otherStateCollision = level.getBlockState(otherPosCollision);

                // Clip.
                HCsCR.CLIPPING_BLOCKS.put(pos, state);
                if (otherStateCollision.is(BlockTags.BEDS)) {
                    HCsCR.CLIPPING_BLOCKS.put(otherPosCollision, otherStateCollision);
                }

                // Log. (**DEBUG**)
                if (HCompile.DEBUG_LOGS) {
                    HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Clipping bed via right click. (state: {}, level: {}, pos: {}, player: {}, hitResult: {}, bed: {})", state, level, pos, player, hitResult, this);
                }

                // Break.
                break;
            case FULL:
                // Get the bed's other part.
                final BlockPos otherPosFull = pos.relative(BedBlock.getConnectedDirection(state)); // Implicit NPE for 'pos', 'state'
                final BlockState otherStateFull = level.getBlockState(otherPosFull);

                // Remove.
                level.removeBlock(pos, false);
                if (otherStateFull.is(BlockTags.BEDS)) {
                    level.removeBlock(otherPosFull, false);
                }

                // Log. (**DEBUG**)
                if (HCompile.DEBUG_LOGS) {
                    HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Removed bed via right click. (state: {}, level: {}, pos: {}, player: {}, hitResult: {}, bed: {})", state, level, pos, player, hitResult, this);
                }

                // Break.
                break;
            default:
                // Log. (**DEBUG**)
                if (HCompile.DEBUG_LOGS) {
                    HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Ignored bed right click. (state: {}, level: {}, pos: {}, player: {}, hitResult: {}, bed: {})", state, level, pos, player, hitResult, this);
                }
        }
    }
    *///?}
}
