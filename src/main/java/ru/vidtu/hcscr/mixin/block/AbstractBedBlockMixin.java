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

//? if >=26.3 {
import net.minecraft.world.level.block.AbstractBedBlock;
//?}

//~ if >=26.3 'BedBlock.' -> 'AbstractBedBlock.' {
/**
 * Mixin that allows beds to be removed (or clipped) via clicking
 * if {@link Config#blocks()} is not {@link BlockMode#OFF}.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see BlockMode
 * @see BlockClips#addClip(BlockPos, BlockState)
 */
// @ApiStatus.Internal // Can't annotate this without logging in the console.
@Mixin(AbstractBedBlock.class)
@NullMarked
public final class AbstractBedBlockMixin {
    /**
     * Logger for this class.
     */
    @Unique
    @UnknownNullability
    private static final Logger HCSCR_LOGGER = (Variables.DEBUG_LOGS ? LogManager.getLogger("HCsCR/AbstractBedBlockMixin") : null);

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    // @ApiStatus.ScheduledForRemoval // Can't annotate this without logging in the console.
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private AbstractBedBlockMixin() {
        if (Variables.DEBUG_ASSERTS) {
            throw new AssertionError("HCsCR: No instances.");
        }
    }

    /**
     * Handles the bed click. Removes the bed if {@link Config#blocks()} is {@link BlockMode#FULL}, adds the
     * bed to {@link BlockClips#addClip(BlockPos, BlockState)} if {@link BlockMode#COLLISION}. Does nothing
     * otherwise. Also does nothing if the level (world) is from the server, the mod is globally disabled
     * via {@link Config#enable()}, or the bed doesn't explode in the current dimension. (e.g., overworld)
     *
     * @param state     Bed block state
     * @param level     The level that this bed block is placed in
     * @param pos       Bed block position
     * @param player    Player interacting with the bed, ignored
     * @param hand      The hand the player uses to interact with the bed (pre-1.20.6), ignored
     * @param hitResult The exact position player used the bed at, ignored
     * @param cir       Callback data containing the bed interaction result, ignored
     * @apiNote Do not call, called by Mixin
     * @see Config#enable()
     * @see Config#blocks()
     * @see BlockMode
     * @see BlockClips#addClip(BlockPos, BlockState)
     */
    //? if >=1.20.6 {
    @DoNotCall("Called by Mixin")
    @Inject(method = "useWithoutItem", at = @At("HEAD")) // HEAD here to avoid early returns from other Mixins an game code.
    private void hcscr_useWithoutItem_head(final BlockState state, final Level level, final BlockPos pos,
                                           final Player player, final BlockHitResult hitResult,
                                           final CallbackInfoReturnable<InteractionResult> cir) {
    //?} else {
    /*@DoNotCall("Called by Mixin")
    @Inject(method = "use", at = @At("HEAD")) // HEAD here to avoid early returns from other Mixins an game code.
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
            // No thread checks, called from either side.
        }

        // Log. (**TRACE**)
        if (Variables.DEBUG_LOGS) {
            //? if >=1.20.6 {
            HCSCR_LOGGER.trace(HCsCR.MARKER, "HCsCR: Handling bed click... (state: {}, level: {}, pos: {}, player: {}, hitResult: {}, bed: {})", state, level, pos, player, hitResult, this);
            //?} else {
            /*HCSCR_LOGGER.trace(HCsCR.MARKER, "HCsCR: Handling bed click... (state: {}, level: {}, pos: {}, player: {}, hand: {}, hitResult: {}, bed: {})", state, level, pos, player, hand, hitResult, this);
            *///?}
        }

        // Do nothing if either:
        // - The bed is not a wool bed. (e.g., a straw bed from 26.3+)
        // - The current level (world) is not client's. (e.g., integrated server world)
        // - The mod is fully disabled via config.
        // - The bed doesn't explode in the current environment/dimension. (heuristical in 1.21.11+)
        // - The "remove blocks" feature is OFF. (in switch block below)
        //? if >=26.3 {
        // Environmental attributes from 25w42a for BED_WORKS are NOT synced to the client,
        // so we just guess and check by comparing if the dimension doesn't have an OVERWORLD skybox.
        if (!((Object) this instanceof BedBlock) || !level.isClientSide() || !Config.enable() || (level.dimensionType().skybox() == DimensionType.Skybox.OVERWORLD)) { // Implicit NPE for 'level'
        //?} elif >=1.21.11 {
        /*// Environmental attributes from 25w42a for BED_WORKS are NOT synced to the client,
        // so we just guess and check by comparing if the dimension doesn't have an OVERWORLD skybox.
        if (!level.isClientSide() || !Config.enable() || (level.dimensionType().skybox() == DimensionType.Skybox.OVERWORLD)) { // Implicit NPE for 'level'
        *///?} else {
        /*if (!level.isClientSide() || !Config.enable() || AbstractBedBlock.canSetSpawn(level)) { // Implicit NPE for 'level'
        *///?}
            // Log. (**DEBUG**)
            if (Variables.DEBUG_LOGS) {
                //? if >=1.20.6 {
                HCSCR_LOGGER.debug(HCsCR.MARKER, "HCsCR: Ignored bed click. (state: {}, level: {}, pos: {}, player: {}, hitResult: {}, bed: {})", state, level, pos, player, hitResult, this);
                //?} else {
                /*HCSCR_LOGGER.debug(HCsCR.MARKER, "HCsCR: Ignored bed click. (state: {}, level: {}, pos: {}, player: {}, hand: {}, hitResult: {}, bed: {})", state, level, pos, player, hand, hitResult, this);
                *///?}
            }

            // Stop.
            return;
        }

        // Small implementation note about bed "explosiveness" detection: It is not possible to detect whether the bed
        // will explode in the current dimension definitevely. Therefore, we trust the server's "bedExplodes"
        // before 1.21.11 value and use the heuristical approach to dimensions with checking whether the skybox
        // looks like OVERWORLD's one (doesn't exist in the nether, is a pixelated pattern in the end),
        // this will (of course) screw up the custom dimensions. However, there is no better alternative.
        // Servers can (and probably) will break this using custom dimensions or anti-cheat measures.
        // However, if you are a server owner, you can block the mod via other measures, see README docs.

        // Validate.
        if (Variables.DEBUG_ASSERTS) {
            //? if >=1.20.6 {
            assert (Minecraft.getInstance().isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", state: " + state + ", level: " + level + ", pos: " + pos + ", player: " + player + ", hitResult: " + hitResult + ", bed: " + this + ')';
            //?} else {
            /*assert (Minecraft.getInstance().isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", state: " + state + ", level: " + level + ", pos: " + pos + ", player: " + player + ", hand: " + hand + ", hitResult: " + hitResult + ", bed: " + this + ')';
            *///?}
        }

        // Remove, clip or ignore.
        switch (Config.blocks()) {
            // Clip. (for BlockMode.COLLISION)
            case COLLISION: {
                // Add the bed to block clips.
                BlockClips.addClip(pos, state);

                // Find the bed's connected part.
                final BlockPos connectedPos = pos.relative(AbstractBedBlock.getConnectedDirection(state)); // Implicit NPE for 'pos', 'state'
                final BlockState connectedState = level.getBlockState(connectedPos);

                // Add the bed's connected part to block clips. (only if it's really a part of the bed)
                if (connectedState.is(BlockTags.BEDS)) {
                    BlockClips.addClip(connectedPos, connectedState);
                }

                // Log. (**DEBUG**)
                if (Variables.DEBUG_LOGS && HCSCR_LOGGER.isDebugEnabled(HCsCR.MARKER)) {
                    //? if >=1.20.6 {
                    HCSCR_LOGGER.debug(HCsCR.MARKER, "HCsCR: Clipped the bed. (state: {}, level: {}, pos: {}, player: {}, hitResult: {}, connectedPos: {}, connectedState: {}, bed: {})", state, level, pos, player, hitResult, connectedPos, connectedState, this);
                    //?} else {
                    /*HCSCR_LOGGER.debug(HCsCR.MARKER, "HCsCR: Clipped the bed. (state: {}, level: {}, pos: {}, player: {}, hand: {}, hitResult: {}, connectedPos: {}, connectedState: {}, bed: {})", state, level, pos, player, hand, hitResult, connectedPos, connectedState, this);
                    *///?}
                }

                // Break.
                break;
            }

            // Remove. (for BlockMode.FULL)
            case FULL: {
                // Remove the bed.
                level.removeBlock(pos, false); // Implicit NPE for 'pos'

                // Find the bed's connected part.
                final BlockPos connectedPos = pos.relative(AbstractBedBlock.getConnectedDirection(state)); // Implicit NPE for 'state'
                final BlockState connectedState = level.getBlockState(connectedPos);

                // Remove the bed's connected part. (only if it's really a part of the bed)
                if (connectedState.is(BlockTags.BEDS)) {
                    level.removeBlock(connectedPos, false);
                }

                // Log. (**DEBUG**)
                if (Variables.DEBUG_LOGS && HCSCR_LOGGER.isDebugEnabled(HCsCR.MARKER)) {
                    //? if >=1.20.6 {
                    HCSCR_LOGGER.debug(HCsCR.MARKER, "HCsCR: Removed the bed. (state: {}, level: {}, pos: {}, player: {}, hitResult: {}, connectedPos: {}, connectedState: {}, bed: {})", state, level, pos, player, hitResult, connectedPos, connectedState, this);
                    //?} else {
                    /*HCSCR_LOGGER.debug(HCsCR.MARKER, "HCsCR: Removed the bed. (state: {}, level: {}, pos: {}, player: {}, hand: {}, hitResult: {}, connectedPos: {}, connectedState: {}, bed: {})", state, level, pos, player, hand, hitResult, connectedPos, connectedState, this);
                    *///?}
                }

                // Break.
                break;
            }

            // Do nothing. (for BlockMode.OFF or any unexpected value)
            default: {
                // Log. (**DEBUG**)
                if (Variables.DEBUG_LOGS) {
                    //? if >=1.20.6 {
                    HCSCR_LOGGER.debug(HCsCR.MARKER, "HCsCR: Ignored the bed. (state: {}, level: {}, pos: {}, player: {}, hitResult: {}, bed: {})", state, level, pos, player, hitResult, this);
                    //?} else {
                    /*HCSCR_LOGGER.debug(HCsCR.MARKER, "HCsCR: Ignored the bed. (state: {}, level: {}, pos: {}, player: {}, hand: {}, hitResult: {}, bed: {})", state, level, pos, player, hand, hitResult, this);
                    *///?}
                }
            }
        }
    }
}
//~}
