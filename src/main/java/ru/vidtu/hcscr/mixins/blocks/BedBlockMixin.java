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

package ru.vidtu.hcscr.mixins.blocks;

import com.google.errorprone.annotations.DoNotCall;
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
     * @param state  Bed state
     * @param level  Bed level
     * @param pos    Bed position
     * @param player Interacting player, ignored
     * @param result Hit vector, ignored
     * @param cir    Callback data, ignored
     * @apiNote Do not call, called by Mixin
     * @see HConfig#enable()
     * @see HConfig#blocks()
     * @see BlockMode
     * @see HCsCR#CLIPPING_BLOCKS
     */
    @DoNotCall("Called by Mixin")
    @Inject(method = "useWithoutItem", at = @At("HEAD"))
    private void hcscr_useWithoutItem_head(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult result, CallbackInfoReturnable<InteractionResult> cir) {
        // Validate.
        assert state != null : "HCsCR: Parameter 'state' is null. (level: " + level + ", pos: " + pos + ", player: " + player + ", result: " + result + ", bed: " + this + ')';
        assert level != null : "HCsCR: Parameter 'level' is null. (state: " + state + ", pos: " + pos + ", player: " + player + ", result: " + result + ", bed: " + this + ')';
        assert pos != null : "HCsCR: Parameter 'pos' is null. (state: " + state + ", level: " + level + ", player: " + player + ", result: " + result + ", bed: " + this + ')';
        assert player != null : "HCsCR: Parameter 'player' is null. (state: " + state + ", level: " + level + ", pos: " + pos + ", result: " + result + ", bed: " + this + ')';
        assert result != null : "HCsCR: Parameter 'result' is null. (state: " + state + ", level: " + level + ", pos: " + pos + ", player: " + player + ", bed: " + this + ')';

        // Log. (**TRACE**)
        HCSCR_LOGGER.trace(HCsCR.HCSCR_MARKER, "HCsCR: Detected bed right click. (state: {}, level: {}, pos: {}, player: {}, result: {}, bed: {})", state, level, pos, player, result, this);

        // Do NOT process beds if any of the following conditions is met:
        // - The current level (world) is not client-side. (e.g., integrated server world)
        // - The bed doesn't explode in the current dimension.
        // - The mod is disabled via config or keybind.
        // - The "remove blocks" feature is OFF. (in switch block)
        if (!level.isClientSide() || BedBlock.canSetSpawn(level) || !HConfig.enable()) { // Implicit NPE for 'level'
            // Log, stop. (**DEBUG**)
            HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Skipped bed right click removing. (state: {}, level: {}, pos: {}, player: {}, result: {}, bed: {})", state, level, pos, player, result, this);
            return;
        }

        // Remove or clip.
        switch (HConfig.blocks()) {
            case COLLISION:
                // Get the bed's other part.
                BlockPos otherPos1 = pos.relative(BedBlock.getConnectedDirection(state)); // Implicit NPE for 'pos', 'state'
                BlockState otherState1 = level.getBlockState(otherPos1);

                // Clip.
                HCsCR.CLIPPING_BLOCKS.put(pos, state);
                if (otherState1.is(BlockTags.BEDS)) {
                    HCsCR.CLIPPING_BLOCKS.put(otherPos1, otherState1);
                }

                // Log, break. (**DEBUG**)
                HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Clipping bed via right click. (state: {}, level: {}, pos: {}, player: {}, result: {}, bed: {})", state, level, pos, player, result, this);
                break;
            case FULL:
                // Get the bed's other part.
                BlockPos otherPos2 = pos.relative(BedBlock.getConnectedDirection(state)); // Implicit NPE for 'pos', 'state'
                BlockState otherState2 = level.getBlockState(otherPos2);

                // Remove.
                level.removeBlock(pos, false);
                if (otherState2.is(BlockTags.BEDS)) {
                    level.removeBlock(otherPos2, false);
                }

                // Log, break. (**DEBUG**)
                HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Removed bed via right click. (state: {}, level: {}, pos: {}, player: {}, result: {}, bed: {})", state, level, pos, player, result, this);
                break;
            default:
                // Log. (**DEBUG**)
                HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Ignored bed right click. (state: {}, level: {}, pos: {}, player: {}, result: {}, bed: {})", state, level, pos, player, result, this);
        }
    }

    //?} else {

    /*/^*
     * Handles the bed usage.
     *
     * @param state  Bed state
     * @param level  Bed level
     * @param pos    Bed position
     * @param player Interacting player
     * @param hand   Interaction hand
     * @param result Hit vector, ignored
     * @param cir    Callback data, ignored
     * @apiNote Do not call, called by Mixin
     * @see HConfig#enable()
     * @see HConfig#blocks()
     * @see BlockMode
     * @see HCsCR#CLIPPING_BLOCKS
     ^/
    @DoNotCall("Called by Mixin")
    @Inject(method = "use", at = @At("HEAD"))
    private void hcscr_use_head(BlockState state, Level level, BlockPos pos, Player player,
                                net.minecraft.world.InteractionHand hand, BlockHitResult result,
                                CallbackInfoReturnable<InteractionResult> cir) {
        // Validate.
        assert state != null : "HCsCR: Parameter 'state' is null. (level: " + level + ", pos: " + pos + ", player: " + player + ", hand: " + hand + ", result: " + result + ", bed: " + this + ')';
        assert level != null : "HCsCR: Parameter 'level' is null. (state: " + state + ", pos: " + pos + ", player: " + player + ", hand: " + hand + ", result: " + result + ", bed: " + this + ')';
        assert pos != null : "HCsCR: Parameter 'pos' is null. (state: " + state + ", level: " + level + ", player: " + player + ", hand: " + hand + ", result: " + result + ", bed: " + this + ')';
        assert player != null : "HCsCR: Parameter 'player' is null. (state: " + state + ", level: " + level + ", pos: " + pos + ", hand: " + hand + ", result: " + result + ", bed: " + this + ')';
        assert hand != null : "HCsCR: Parameter 'hand' is null. (state: " + state + ", level: " + level + ", pos: " + pos + ", player: " + player + ", result: " + result + ", bed: " + this + ')';
        assert result != null : "HCsCR: Parameter 'result' is null. (state: " + state + ", level: " + level + ", pos: " + pos + ", player: " + player + ", hand: " + hand + ", bed: " + this + ')';

        // Do NOT process beds if any of the following conditions is met:
        // - The current level (world) is not client-side. (e.g., integrated server world)
        // - The bed doesn't explode in the current dimension.
        // - The mod is disabled via config or keybind.
        // - The "remove blocks" feature is OFF. (in switch block)
        if (!level.isClientSide() || BedBlock.canSetSpawn(level) || !HConfig.enable()) { // Implicit NPE for 'level'
            // Log, stop. (**DEBUG**)
            HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Skipped bed right click removing. (state: {}, level: {}, pos: {}, player: {}, hand: {}, result: {}, bed: {})", state, level, pos, player, hand, result, this);
            return;
        }

        // Remove or clip.
        switch (HConfig.blocks()) {
            case COLLISION:
                // Get the bed's other part.
                BlockPos otherPos1 = pos.relative(BedBlock.getConnectedDirection(state)); // Implicit NPE for 'pos', 'state'
                BlockState otherState1 = level.getBlockState(otherPos1);

                // Clip.
                HCsCR.CLIPPING_BLOCKS.put(pos, state);
                if (otherState1.is(BlockTags.BEDS)) {
                    HCsCR.CLIPPING_BLOCKS.put(otherPos1, otherState1);
                }

                // Log, break. (**DEBUG**)
                HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Clipping bed via right click. (state: {}, level: {}, pos: {}, player: {}, result: {}, bed: {})", state, level, pos, player, result, this);
                break;
            case FULL:
                // Get the bed's other part.
                BlockPos otherPos2 = pos.relative(BedBlock.getConnectedDirection(state)); // Implicit NPE for 'pos', 'state'
                BlockState otherState2 = level.getBlockState(otherPos2);

                // Remove.
                level.removeBlock(pos, false);
                if (otherState2.is(BlockTags.BEDS)) {
                    level.removeBlock(otherPos2, false);
                }

                // Log, break. (**DEBUG**)
                HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Removed bed via right click. (state: {}, level: {}, pos: {}, player: {}, result: {}, bed: {})", state, level, pos, player, result, this);
                break;
            default:
                // Log. (**DEBUG**)
                HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Ignored bed right click. (state: {}, level: {}, pos: {}, player: {}, result: {}, bed: {})", state, level, pos, player, result, this);
        }
    }
    *///?}
}
