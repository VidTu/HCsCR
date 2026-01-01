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
import ru.vidtu.hcscr.config.BlockMode;
import ru.vidtu.hcscr.config.HConfig;
import ru.vidtu.hcscr.platform.HCompile;

//? if >=1.20.6 {
import ru.vidtu.hcscr.platform.HStonecutter;
//?} else {
/*import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Shadow;
*///?}

/**
 * Mixin that allows respawn anchors to be removed via right click.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see BlockMode
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
     * @param state     Anchor block state
     * @param level     The level that this anchor block is placed in
     * @param pos       Anchor block position
     * @param player    Player interacting with the anchor, ignored
     * @param hitResult The exact position player used the anchor at, ignored
     * @param cir       Callback data containing the anchor interaction result, ignored
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
            assert (state != null) : "HCsCR: Parameter 'state' is null. (level: " + level + ", pos: " + pos + ", player: " + player + ", hitResult: " + hitResult + ", anchor: " + this + ')';
            assert (level != null) : "HCsCR: Parameter 'level' is null. (state: " + state + ", pos: " + pos + ", player: " + player + ", hitResult: " + hitResult + ", anchor: " + this + ')';
            assert (pos != null) : "HCsCR: Parameter 'pos' is null. (state: " + state + ", level: " + level + ", player: " + player + ", hitResult: " + hitResult + ", anchor: " + this + ')';
            assert (player != null) : "HCsCR: Parameter 'player' is null. (state: " + state + ", level: " + level + ", pos: " + pos + ", hitResult: " + hitResult + ", anchor: " + this + ')';
            assert (hitResult != null) : "HCsCR: Parameter 'hitResult' is null. (state: " + state + ", level: " + level + ", pos: " + pos + ", player: " + player + ", anchor: " + this + ')';
        }

        // Log. (**TRACE**)
        if (HCompile.DEBUG_LOGS) {
            HCSCR_LOGGER.trace(HCsCR.HCSCR_MARKER, "HCsCR: Detected anchor right click. (state: {}, level: {}, pos: {}, player: {}, hitResult: {}, anchor: {})", state, level, pos, player, hitResult, this);
        }

        // Do NOT process anchors if any of the following conditions is met:
        // - The current level (world) is not client-side. (e.g., integrated server world)
        // - The anchor doesn't have any charges.
        // - The mod is disabled via config or keybind.
        // - The anchor doesn't explode in the current environment/dimension.
        // - The "remove blocks" feature is OFF. (in switch block)
        if (!level.isClientSide() || (state.getValue(RespawnAnchorBlock.CHARGE) == 0) || // Implicit NPE for 'level', 'state'
                !HConfig.enable() || !HStonecutter.willAnchorExplode(level)) {
            // Log. (**DEBUG**)
            if (HCompile.DEBUG_LOGS) {
                HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Skipped anchor right click removing. (state: {}, level: {}, pos: {}, player: {}, hitResult: {}, anchor: {})", state, level, pos, player, hitResult, this);
            }

            // Stop.
            return;
        }

        // Validate.
        if (HCompile.DEBUG_ASSERTS) {
            assert (Minecraft.getInstance().isSameThread()) : "HCsCR: Clicking on an anchor NOT from the main thread. (thread: " + Thread.currentThread() + ", state: " + state + ", level: " + level + ", pos: " + pos + ", player: " + player + ", hitResult: " + hitResult + ", anchor: " + this + ')';
        }

        // Remove or clip.
        switch (HConfig.blocks()) {
            case COLLISION:
                // Clip.
                HCsCR.CLIPPING_BLOCKS.put(pos, state);

                // Log. (**DEBUG**)
                if (HCompile.DEBUG_LOGS) {
                    HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Clipping anchor via right click. (state: {}, level: {}, pos: {}, player: {}, hitResult: {}, anchor: {})", state, level, pos, player, hitResult, this);
                }

                // Break.
                break;
            case FULL:
                // Remove.
                level.removeBlock(pos, false); // Implicit NPE for 'pos'

                // Log. (**DEBUG**)
                if (HCompile.DEBUG_LOGS) {
                    HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Removed anchor via right click. (state: {}, level: {}, pos: {}, player: {}, hitResult: {}, anchor: {})", state, level, pos, player, hitResult, this);
                }

                // Break.
                break;
            default:
                // Log. (**DEBUG**)
                if (HCompile.DEBUG_LOGS) {
                    HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Ignored anchor right click. (state: {}, level: {}, pos: {}, player: {}, hitResult: {}, anchor: {})", state, level, pos, player, hitResult, this);
                }
        }
    }

    //?} else {

    /*/^*
     * Handles the anchor usage.
     *
     * @param state     Anchor block state
     * @param level     The level that this anchor block is placed in
     * @param pos       Anchor block position
     * @param player    Player interacting with the anchor
     * @param hand      The hand the player uses to interact with the anchor
     * @param hitResult The exact position player used the anchor at, ignored
     * @param cir       Callback data containing the anchor interaction result, ignored
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
            assert (state != null) : "HCsCR: Parameter 'state' is null. (level: " + level + ", pos: " + pos + ", player: " + player + ", hand: " + hand + ", hitResult: " + hitResult + ", anchor: " + this + ')';
            assert (level != null) : "HCsCR: Parameter 'level' is null. (state: " + state + ", pos: " + pos + ", player: " + player + ", hand: " + hand + ", hitResult: " + hitResult + ", anchor: " + this + ')';
            assert (pos != null) : "HCsCR: Parameter 'pos' is null. (state: " + state + ", level: " + level + ", player: " + player + ", hand: " + hand + ", hitResult: " + hitResult + ", anchor: " + this + ')';
            assert (player != null) : "HCsCR: Parameter 'player' is null. (state: " + state + ", level: " + level + ", pos: " + pos + ", hand: " + hand + ", hitResult: " + hitResult + ", anchor: " + this + ')';
            assert (hand != null) : "HCsCR: Parameter 'hand' is null. (state: " + state + ", level: " + level + ", pos: " + pos + ", player: " + player + ", hitResult: " + hitResult + ", anchor: " + this + ')';
            assert (hitResult != null) : "HCsCR: Parameter 'hitResult' is null. (state: " + state + ", level: " + level + ", pos: " + pos + ", player: " + player + ", hand: " + hand + ", anchor: " + this + ')';
        }

        // Do NOT process anchors if any of the following conditions is met:
        // - The current level (world) is not client-side. (e.g., integrated server world)
        // - The anchor doesn't have any charges.
        // - The anchor doesn't explode in the current dimension.
        // - The mod is disabled via config or keybind.
        // - The anchor is currently being charged.
        // - The "remove blocks" feature is OFF. (in switch block)
        if (!level.isClientSide() || (state.getValue(RespawnAnchorBlock.CHARGE) == 0) || // Implicit NPE for 'level', 'state'
                RespawnAnchorBlock.canSetSpawn(level) || !HConfig.enable()) {
            // Log. (**DEBUG**)
            if (HCompile.DEBUG_LOGS) {
                HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Skipped anchor right click removing. (state: {}, level: {}, pos: {}, player: {}, hand: {}, hitResult: {}, anchor: {})", state, level, pos, player, hand, hitResult, this);
            }

            // Stop.
            return;
        }

        // Validate.
        if (HCompile.DEBUG_ASSERTS) {
            assert (Minecraft.getInstance().isSameThread()) : "HCsCR: Clicking on an anchor NOT from the main thread. (thread: " + Thread.currentThread() + ", state: " + state + ", level: " + level + ", pos: " + pos + ", player: " + player + ", hand: " + hand + ", hitResult: " + hitResult + ", anchor: " + this + ')';
        }

        // Skip if charging.
        final ItemStack itemInHand = player.getItemInHand(hand); // Implicit NPE for 'player'
        if (((hand == InteractionHand.MAIN_HAND) && !isRespawnFuel(itemInHand) && // Implicit NPE for 'itemInHand'
                isRespawnFuel(player.getItemInHand(InteractionHand.OFF_HAND))) ||
                (isRespawnFuel(itemInHand) && canBeCharged(state))) {
            // Log. (**DEBUG**)
            if (HCompile.DEBUG_LOGS) {
                HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Skipped anchor right click removing. (state: {}, level: {}, pos: {}, player: {}, hand: {}, hitResult: {}, itemInHand: {}, anchor: {})", state, level, pos, player, hand, hitResult, itemInHand, this);
            }

            // Stop.
            return;
        }

        // Remove or clip.
        switch (HConfig.blocks()) {
            case COLLISION:
                // Clip.
                HCsCR.CLIPPING_BLOCKS.put(pos, state);

                // Log. (**DEBUG**)
                if (HCompile.DEBUG_LOGS) {
                    HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Clipping anchor via right click. (state: {}, level: {}, pos: {}, player: {}, hitResult: {}, itemInHand: {}, anchor: {})", state, level, pos, player, hitResult, itemInHand, this);
                }

                // Break.
                break;
            case FULL:
                // Remove.
                level.removeBlock(pos, false); // Implicit NPE for 'pos'

                // Log. (**DEBUG**)
                if (HCompile.DEBUG_LOGS) {
                    HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Removed anchor via right click. (state: {}, level: {}, pos: {}, player: {}, hitResult: {}, itemInHand: {}, anchor: {})", state, level, pos, player, hitResult, itemInHand, this);
                }

                // Break.
                break;
            default:
                // Log. (**DEBUG**)
                if (HCompile.DEBUG_LOGS) {
                    HCSCR_LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Ignored anchor right click. (state: {}, level: {}, pos: {}, player: {}, hitResult: {}, itemInHand: {}, anchor: {})", state, level, pos, player, hitResult, itemInHand, this);
                }
        }
    }

    @Contract(pure = true)
    @Shadow
    private static boolean isRespawnFuel(final ItemStack itemInHand) {
        throw new AssertionError("HCsCR: Unreachable code statement.");
    }

    @Contract(pure = true)
    @Shadow
    private static boolean canBeCharged(final BlockState state) {
        throw new AssertionError("HCsCR: Unreachable code statement.");
    }
    *///?}
}
