/*
 * HCsCR is a third-party mod for Minecraft Java Edition to remove your end crystals faster.
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

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RespawnAnchorBlock;
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
import ru.vidtu.hcscr.config.AnchorMode;
import ru.vidtu.hcscr.config.HConfig;

/**
 * Mixin that allows respawn anchors to be removed.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see AnchorMode
 */
// @ApiStatus.Internal // Can't annotate this without logging in the console.
@Mixin(RespawnAnchorBlock.class)
@NullMarked
public final class RespawnAnchorBlockMixin {
    /**
     * Logger for this class.
     */
    @Unique
    private static final Logger HCSCR_LOGGER = LogManager.getLogger("HCsCR/RespawnBlockMixin");

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    // @ApiStatus.ScheduledForRemoval // Can't annotate this without logging in the console.
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private RespawnAnchorBlockMixin() {
        throw new AssertionError("HCsCR: No instances.");
    }

    //? if >=1.20.6 {

    /**
     * Handles the anchor usage.
     *
     * @param state  Anchor state
     * @param level  Anchor level
     * @param pos    Anchor position
     * @param player Interacting player, ignored
     * @param result Hit vector, ignored
     * @param cir    Callback data, ignored
     * @see HConfig#enable()
     * @see HConfig#anchors()
     * @see AnchorMode
     * @see HCsCR#CLIPPING_ANCHORS
     */
    @Inject(method = "useWithoutItem", at = @At("HEAD"))
    private void hcscr_useWithoutItem_head(BlockState state, Level level, BlockPos pos, Player player,
                                           BlockHitResult result, CallbackInfoReturnable<InteractionResult> cir) {
        // Validate.
        assert state != null : "HCsCR: Parameter 'state' is null. (level: " + level + ", pos: " + pos + ", player: " + player + ", result: " + result + ", anchor: " + this + ')';
        assert level != null : "HCsCR: Parameter 'level' is null. (state: " + state + ", pos: " + pos + ", player: " + player + ", result: " + result + ", anchor: " + this + ')';
        assert pos != null : "HCsCR: Parameter 'pos' is null. (state: " + state + ", level: " + level + ", player: " + player + ", result: " + result + ", anchor: " + this + ')';
        assert player != null : "HCsCR: Parameter 'player' is null. (state: " + state + ", level: " + level + ", pos: " + pos + ", result: " + result + ", anchor: " + this + ')';
        assert result != null : "HCsCR: Parameter 'result' is null. (state: " + state + ", level: " + level + ", pos: " + pos + ", player: " + player + ", anchor: " + this + ')';

        // Log. (**TRACE**)
        HCSCR_LOGGER.trace(HCsCR.HCSCR_MARKER, "HCsCR: Detected anchor right click. (state: {}, level: {}, pos: {}, player: {}, result: {}, anchor: {})", state, level, pos, player, result, this);

        // Do NOT process anchors if any of the following conditions is met:
        // - The current level (world) is not client-side. (e.g. integrated server world)
        // - The anchor doesn't have any charges.
        // - The anchor doesn't explode in the current dimension.
        // - The mod is disabled via config or keybind.
        // - The remove anchors feature is OFF. (in switch block)
        if (!level.isClientSide() || (state.getValue(RespawnAnchorBlock.CHARGE) == 0) ||
                RespawnAnchorBlock.canSetSpawn(level) || !HConfig.enable()) {
            // Log, stop. (**DEBUG**)
            HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Skipped anchor right click removing. (state: {}, level: {}, pos: {}, player: {}, result: {}, anchor: {})", state, level, pos, player, result, this);
            return;
        }

        // Remove or clip.
        switch (HConfig.anchors()) {
            case COLLISION:
                // Clip.
                HCsCR.CLIPPING_ANCHORS.add(pos);

                // Log, break. (**DEBUG**)
                HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Clipping anchor via right click. (state: {}, level: {}, pos: {}, player: {}, result: {}, anchor: {})", state, level, pos, player, result, this);
                break;
            case FULL:
                // Remove.
                level.removeBlock(pos, false);

                // Log, break. (**DEBUG**)
                HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Removed anchor via right click. (state: {}, level: {}, pos: {}, player: {}, result: {}, anchor: {})", state, level, pos, player, result, this);
                break;
            default:
                // Log. (**DEBUG**)
                HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Ignored anchor right click. (state: {}, level: {}, pos: {}, player: {}, result: {}, anchor: {})", state, level, pos, player, result, this);
        }
    }
    //?} else {
    /*/^*
     * Handles the anchor usage.
     *
     * @param state  Anchor state
     * @param level  Anchor level
     * @param pos    Anchor position
     * @param player Interacting player
     * @param hand   Interaction hand
     * @param result Hit vector, ignored
     * @param cir    Callback data, ignored
     * @see HConfig#enable()
     * @see HConfig#anchors()
     * @see AnchorMode
     * @see HCsCR#CLIPPING_ANCHORS
     ^/
    @Inject(method = "use", at = @At("HEAD"))
    private void hcscr_use_head(BlockState state, Level level, BlockPos pos, Player player,
                                net.minecraft.world.InteractionHand hand, BlockHitResult result,
                                CallbackInfoReturnable<InteractionResult> cir) {
        // Validate.
        assert state != null : "HCsCR: Parameter 'state' is null. (level: " + level + ", pos: " + pos + ", player: " + player + ", hand: " + hand + ", result: " + result + ", anchor: " + this + ')';
        assert level != null : "HCsCR: Parameter 'level' is null. (state: " + state + ", pos: " + pos + ", player: " + player + ", hand: " + hand + ", result: " + result + ", anchor: " + this + ')';
        assert pos != null : "HCsCR: Parameter 'pos' is null. (state: " + state + ", level: " + level + ", player: " + player + ", hand: " + hand + ", result: " + result + ", anchor: " + this + ')';
        assert player != null : "HCsCR: Parameter 'player' is null. (state: " + state + ", level: " + level + ", pos: " + pos + ", hand: " + hand + ", result: " + result + ", anchor: " + this + ')';
        assert hand != null : "HCsCR: Parameter 'hand' is null. (state: " + state + ", level: " + level + ", pos: " + pos + ", player: " + player + ", result: " + result + ", anchor: " + this + ')';
        assert result != null : "HCsCR: Parameter 'result' is null. (state: " + state + ", level: " + level + ", pos: " + pos + ", player: " + player + ", hand: " + hand + ", anchor: " + this + ')';

        // Do NOT process anchors if any of the following conditions is met:
        // - The current level (world) is not client-side. (e.g. integrated server world)
        // - The anchor doesn't have any charges.
        // - The anchor doesn't explode in the current dimension.
        // - The mod is disabled via config or keybind.
        // - The anchor is currently being charged.
        // - The remove anchors feature is OFF. (in switch block)
        if (!level.isClientSide() || (state.getValue(RespawnAnchorBlock.CHARGE) == 0) ||
                RespawnAnchorBlock.canSetSpawn(level) || !HConfig.enable()) {
            // Log, stop. (**DEBUG**)
            HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Skipped anchor right click removing. (state: {}, level: {}, pos: {}, player: {}, hand: {}, result: {}, anchor: {})", state, level, pos, player, hand, result, this);
            return;
        }

        // Skip if charging.
        net.minecraft.world.item.ItemStack stack = player.getItemInHand(hand);
        if (((hand == net.minecraft.world.InteractionHand.MAIN_HAND) && !isRespawnFuel(stack) &&
                isRespawnFuel(player.getItemInHand(net.minecraft.world.InteractionHand.OFF_HAND))) ||
                (isRespawnFuel(stack) && canBeCharged(state))) {
            // Log, stop. (**DEBUG**)
            HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Skipped anchor right click removing. (state: {}, level: {}, pos: {}, player: {}, hand: {}, result: {}, stack: {}, anchor: {})", state, level, pos, player, hand, result, stack, this);
            return;
        }

        // Remove or clip.
        switch (HConfig.anchors()) {
            case COLLISION:
                // Clip.
                HCsCR.CLIPPING_ANCHORS.add(pos);

                // Log, break. (**DEBUG**)
                HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Clipping anchor via right click. (state: {}, level: {}, pos: {}, player: {}, result: {}, anchor: {})", state, level, pos, player, result, this);
                break;
            case FULL:
                // Remove.
                level.removeBlock(pos, false);

                // Log, break. (**DEBUG**)
                HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Removed anchor via right click. (state: {}, level: {}, pos: {}, player: {}, result: {}, anchor: {})", state, level, pos, player, result, this);
                break;
            default:
                // Log. (**DEBUG**)
                HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Ignored anchor right click. (state: {}, level: {}, pos: {}, player: {}, result: {}, anchor: {})", state, level, pos, player, result, this);
        }
    }

    @Contract(pure = true)
    @org.spongepowered.asm.mixin.Shadow
    private static boolean isRespawnFuel(net.minecraft.world.item.ItemStack stack) {
        throw new AssertionError("HCsCR: Unreachable code statement.");
    }

    @Contract(pure = true)
    @org.spongepowered.asm.mixin.Shadow
    private static boolean canBeCharged(BlockState state) {
        throw new AssertionError("HCsCR: Unreachable code statement.");
    }
    *///?}
}
