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
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.vidtu.hcscr.HCsCR;
import ru.vidtu.hcscr.compile.Variables;
import ru.vidtu.hcscr.config.BlockMode;
import ru.vidtu.hcscr.config.Config;
import ru.vidtu.hcscr.handler.BlockClips;

//? if >=1.21.11 {
import net.minecraft.world.level.dimension.DimensionType;
//?} elif <1.20.6 {
/*import net.minecraft.world.InteractionHand;
*///?}

/**
 * Mixin that allows beds to be removed via right click.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see BlockMode
 * @see BlockClips
 */
// @ApiStatus.Internal // Can't annotate this without logging in the console.
@Mixin(BedBlock.class)
@NullMarked
public final class BedBlockMixin {
    /**
     * Logger for this class.
     */
    @Unique
    @UnknownNullability
    private static final Logger HCSCR_LOGGER = (Variables.DEBUG_LOGS ? LogManager.getLogger("HCsCR/BedBlockMixin") : null);

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
        if (Variables.DEBUG_ASSERTS) {
            throw new AssertionError("HCsCR: No instances.");
        }
    }

    /**
     * Handles the bed usage.
     *
     * @param state     Bed block state
     * @param level     The level that this bed block is placed in
     * @param pos       Bed block position
     * @param player    Player interacting with the bed, ignored
     * @param hand      The hand the player uses to interact with the bed (before 1.20.6), ignored
     * @param hitResult The exact position player used the bed at, ignored
     * @param cir       Callback data containing the bed interaction result, ignored
     * @apiNote Do not call, called by Mixin
     * @see Config#enable()
     * @see Config#blocks()
     * @see BlockMode
     * @see BlockClips
     */
    //? if >=1.20.6 {
    @DoNotCall("Called by Mixin")
    @Inject(method = "useWithoutItem", at = @At("HEAD"))
    private void hcscr_useWithoutItem_head(final BlockState state, final Level level, final BlockPos pos,
                                           final Player player, final BlockHitResult hitResult,
                                           final CallbackInfoReturnable<InteractionResult> cir) {
    //?} else {
    /*@DoNotCall("Called by Mixin")
    @Inject(method = "use", at = @At("HEAD"))
    private void hcscr_use_head(final BlockState state, final Level level, final BlockPos pos, final Player player,
                                final InteractionHand hand, final BlockHitResult hitResult,
                                final CallbackInfoReturnable<InteractionResult> cir) {
    *///?}
        // Validate.
        if (Variables.DEBUG_ASSERTS) {
            //? if >=1.20.6 {
            assert (state != null) : "HCsCR: Parameter 'state' is null. (level: " + level + ", pos: " + pos + ", player: " + player + ", hitResult: " + hitResult + ", bed: " + this + ')';
            assert (level != null) : "HCsCR: Parameter 'level' is null. (state: " + state + ", pos: " + pos + ", player: " + player + ", hitResult: " + hitResult + ", bed: " + this + ')';
            assert (pos != null) : "HCsCR: Parameter 'pos' is null. (state: " + state + ", level: " + level + ", player: " + player + ", hitResult: " + hitResult + ", bed: " + this + ')';
            assert (player != null) : "HCsCR: Parameter 'player' is null. (state: " + state + ", level: " + level + ", pos: " + pos + ", hitResult: " + hitResult + ", bed: " + this + ')';
            assert (hitResult != null) : "HCsCR: Parameter 'hitResult' is null. (state: " + state + ", level: " + level + ", pos: " + pos + ", player: " + player + ", bed: " + this + ')';
            //?} else {
            /*assert (state != null) : "HCsCR: Parameter 'state' is null. (level: " + level + ", pos: " + pos + ", player: " + player + ", hand: " + hand + ", hitResult: " + hitResult + ", bed: " + this + ')';
            assert (level != null) : "HCsCR: Parameter 'level' is null. (state: " + state + ", pos: " + pos + ", player: " + player + ", hand: " + hand + ", hitResult: " + hitResult + ", bed: " + this + ')';
            assert (pos != null) : "HCsCR: Parameter 'pos' is null. (state: " + state + ", level: " + level + ", player: " + player + ", hand: " + hand + ", hitResult: " + hitResult + ", bed: " + this + ')';
            assert (player != null) : "HCsCR: Parameter 'player' is null. (state: " + state + ", level: " + level + ", pos: " + pos + ", hand: " + hand + ", hitResult: " + hitResult + ", bed: " + this + ')';
            assert (hand != null) : "HCsCR: Parameter 'hand' is null. (state: " + state + ", level: " + level + ", pos: " + pos + ", player: " + player + ", hitResult: " + hitResult + ", bed: " + this + ')';
            assert (hitResult != null) : "HCsCR: Parameter 'hitResult' is null. (state: " + state + ", level: " + level + ", pos: " + pos + ", player: " + player + ", hand: " + hand + ", bed: " + this + ')';
            *///?}
        }

        // Log. (**TRACE**)
        if (Variables.DEBUG_LOGS) {
            //? if >=1.20.6 {
            HCSCR_LOGGER.trace(HCsCR.MARKER, "HCsCR: Detected bed right click. (state: {}, level: {}, pos: {}, player: {}, hitResult: {}, bed: {})", state, level, pos, player, hitResult, this);
            //?} else {
            /*HCSCR_LOGGER.trace(HCsCR.MARKER, "HCsCR: Detected bed right click. (state: {}, level: {}, pos: {}, player: {}, hand: {}, hitResult: {}, bed: {})", state, level, pos, player, hand, hitResult, this);
            *///?}
        }

        // Do NOT process beds if any of the following conditions is met:
        // - The current level (world) is not client-side. (e.g., integrated server world)
        // - The mod is disabled via config or keybind.
        // - The bed doesn't explode in the current environment/dimension. (heuristical in 1.21.11+)
        // - The "remove blocks" feature is OFF. (in switch block)
        //? if >=1.21.11 {
        // Environmental attributes from 25w42a for BED_WORKS are NOT synced to the client,
        // so we just guess and check by comparing if the dimension doesn't have an OVERWORLD skybox.
        if (!level.isClientSide() || !Config.enable() || (level.dimensionType().skybox() == DimensionType.Skybox.OVERWORLD)) { // Implicit NPE for 'level'
        //?} else {
        /*if (!level.isClientSide() || !Config.enable() || BedBlock.canSetSpawn(level)) { // Implicit NPE for 'level'
        *///?}
            // Log. (**DEBUG**)
            if (Variables.DEBUG_LOGS) {
                //? if >=1.20.6 {
                HCSCR_LOGGER.debug(HCsCR.MARKER, "HCsCR: Skipped bed right click removing. (state: {}, level: {}, pos: {}, player: {}, hitResult: {}, bed: {})", state, level, pos, player, hitResult, this);
                //?} else {
                /*HCSCR_LOGGER.debug(HCsCR.MARKER, "HCsCR: Skipped bed right click removing. (state: {}, level: {}, pos: {}, player: {}, hand: {}, hitResult: {}, bed: {})", state, level, pos, player, hand, hitResult, this);
                *///?}
            }

            // Stop.
            return;
        }

        // Validate.
        if (Variables.DEBUG_ASSERTS) {
            //? if >=1.20.6 {
            assert (Minecraft.getInstance().isSameThread()) : "HCsCR: Clicking on a bed NOT from the main thread. (thread: " + Thread.currentThread() + ", state: " + state + ", level: " + level + ", pos: " + pos + ", player: " + player + ", hitResult: " + hitResult + ", bed: " + this + ')';
            //?} else {
            /*assert (Minecraft.getInstance().isSameThread()) : "HCsCR: Clicking on a bed NOT from the main thread. (thread: " + Thread.currentThread() + ", state: " + state + ", level: " + level + ", pos: " + pos + ", player: " + player + ", hand: " + hand + ", hitResult: " + hitResult + ", bed: " + this + ')';
            *///?}
        }

        // Remove or clip.
        switch (Config.blocks()) {
            case COLLISION:
                // Get the bed's other part.
                final BlockPos connectedPosCollision = pos.relative(BedBlock.getConnectedDirection(state)); // Implicit NPE for 'pos', 'state'
                final BlockState connectedStateCollision = level.getBlockState(connectedPosCollision);

                // Clip.
                BlockClips.add(pos, state);
                if (connectedStateCollision.is(BlockTags.BEDS)) {
                    BlockClips.add(connectedPosCollision, connectedStateCollision);
                }

                // Log. (**DEBUG**)
                if (Variables.DEBUG_LOGS) {
                    //? if >=1.20.6 {
                    HCSCR_LOGGER.debug(HCsCR.MARKER, "HCsCR: Clipping bed via right click. (state: {}, level: {}, pos: {}, player: {}, hitResult: {}, bed: {})", state, level, pos, player, hitResult, this);
                    //?} else {
                    /*HCSCR_LOGGER.debug(HCsCR.MARKER, "HCsCR: Clipping bed via right click. (state: {}, level: {}, pos: {}, player: {}, hand: {}, hitResult: {}, bed: {})", state, level, pos, player, hand, hitResult, this);
                    *///?}
                }

                // Break.
                break;
            case FULL:
                // Get the bed's connected part.
                final BlockPos connectedPosFull = pos.relative(BedBlock.getConnectedDirection(state)); // Implicit NPE for 'pos', 'state'
                final BlockState connectedStateFull = level.getBlockState(connectedPosFull);

                // Remove.
                level.removeBlock(pos, false);
                if (connectedStateFull.is(BlockTags.BEDS)) {
                    level.removeBlock(connectedPosFull, false);
                }

                // Log. (**DEBUG**)
                if (Variables.DEBUG_LOGS) {
                    //? if >=1.20.6 {
                    HCSCR_LOGGER.debug(HCsCR.MARKER, "HCsCR: Removed bed via right click. (state: {}, level: {}, pos: {}, player: {}, hitResult: {}, bed: {})", state, level, pos, player, hitResult, this);
                    //?} else {
                    /*HCSCR_LOGGER.debug(HCsCR.MARKER, "HCsCR: Removed bed via right click. (state: {}, level: {}, pos: {}, player: {}, hand: {}, hitResult: {}, bed: {})", state, level, pos, player, hand, hitResult, this);
                    *///?}
                }

                // Break.
                break;
            default:
                // Log. (**DEBUG**)
                if (Variables.DEBUG_LOGS) {
                    //? if >=1.20.6 {
                    HCSCR_LOGGER.debug(HCsCR.MARKER, "HCsCR: Ignored bed right click. (state: {}, level: {}, pos: {}, player: {}, hitResult: {}, bed: {})", state, level, pos, player, hitResult, this);
                    //?} else {
                    /*HCSCR_LOGGER.debug(HCsCR.MARKER, "HCsCR: Ignored bed right click. (state: {}, level: {}, pos: {}, player: {}, hand: {}, hitResult: {}, bed: {})", state, level, pos, player, hand, hitResult, this);
                    *///?}
                }
        }
    }
}
