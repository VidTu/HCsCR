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

package ru.vidtu.hcscr.handler;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NullMarked;
import ru.vidtu.hcscr.HCsCR;
import ru.vidtu.hcscr.compile.Variables;
import ru.vidtu.hcscr.config.Config;
import ru.vidtu.hcscr.config.BlockMode;
import ru.vidtu.hcscr.mixin.MinecraftMixin;
import ru.vidtu.hcscr.mixin.block.BedBlockMixin;
import ru.vidtu.hcscr.mixin.block.BlockBehaviour_BlockStateBaseMixin;
import ru.vidtu.hcscr.mixin.block.ClientPacketListenerMixin;
import ru.vidtu.hcscr.mixin.block.RespawnAnchorBlockMixin;

import java.util.Iterator;
import java.util.Map;

/**
 * Handling logic for the mod's clipping through blocks when
 * {@link Config#blocks()} is {@link BlockMode#COLLISION}.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see BlockMode#COLLISION
 * @see Config#blocks()
 */
@ApiStatus.Internal
@NullMarked
public final class BlockClips {
    /**
     * A map of block positions (that the player won't collide with) mapped to expected block states.
     * <p>
     * These blocks won't collide with the player in the world as their hitbox
     * will be removed via {@link BlockBehaviour_BlockStateBaseMixin}.
     * <p>
     * The validity is checked in {@link #tick(Minecraft, ProfilerFiller)}. If a block position's (key)
     * state (value) is not matching the real state (every tick), then it is removed from the map.
     * 
     * @see BlockBehaviour_BlockStateBaseMixin
     * @see #tick(Minecraft, ProfilerFiller)
     * @see #has(BlockPos)
     * @see #add(BlockPos, BlockState)
     * @see #remove(BlockPos)
     * @see #clear()
     */
    // This map is not expected to grow more than a few elements, so it's an array-baked map,
    // not a hash-baked one. Moreover, it's being iterated linearly anyway in tick(...).
    private static final Object2ObjectMap<BlockPos, BlockState> CLIPS = new Object2ObjectArrayMap<>(0);

    /**
     * Logger for this class.
     */
    @UnknownNullability
    private static final Logger LOGGER = (Variables.DEBUG_LOGS ? LogManager.getLogger("HCsCR/BlockClips") : null);

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private BlockClips() {
        if (Variables.DEBUG_ASSERTS) {
            throw new AssertionError("HCsCR: No instances.");
        }
    }

    /**
     * Cleans the block clips. Removes redundant entries
     * from {@link #CLIPS}. Should be called every tick.
     *
     * @param client   Client game instance
     * @param profiler Client profiler, {@code null} if {@link Variables#DEBUG_PROFILER} is {@code false}
     * @see HCsCR#tick(Minecraft)
     * @see #CLIPS
     */
    public static void tick(final Minecraft client, final @UnknownNullability ProfilerFiller profiler) {
        // Validate.
        if (Variables.DEBUG_ASSERTS) {
            assert (client != null) : "HCsCR: Parameter 'client' is null. (profiler: " + profiler + ')';
            if (Variables.DEBUG_PROFILER) {
                assert (profiler != null) : "HCsCR: Parameter 'profiler' is null. (client: " + client + ')';
            }
            assert (client.isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", client: " + client + ", profiler: " + profiler + ')';
        }

        // Push the profiler.
        if (Variables.DEBUG_PROFILER) {
            profiler.push("hcscr:block_clips"); // Implicit NPE for 'profiler'
        }

        // Do nothing if there are no clips.
        if (CLIPS.isEmpty()) {
            // Pop the profiler.
            if (Variables.DEBUG_PROFILER) {
                profiler.pop();
            }

            // Stop.
            return;
        }

        // Clear all clips, if level is null.
        final ClientLevel level = client.level; // Implicit NPE for 'client'
        if (level == null) {
            // Log. (**TRACE**)
            if (Variables.DEBUG_LOGS) {
                LOGGER.trace(HCsCR.MARKER, "HCsCR: Null level, clearing block clips...");
            }

            // Clear.
            CLIPS.clear();

            // Log. (**DEBUG**)
            if (Variables.DEBUG_LOGS) {
                LOGGER.debug(HCsCR.MARKER, "HCsCR: Null level, cleared block clips.");
            }

            // Pop the profiler.
            if (Variables.DEBUG_PROFILER) {
                profiler.pop();
            }

            // Stop.
            return;
        }

        // Log. (**TRACE**)
        if (Variables.DEBUG_LOGS) {
            LOGGER.trace(HCsCR.MARKER, "HCsCR: Checking block clips... (clips: {})", CLIPS);
        }

        // Iterate.
        final Iterator<Map.Entry<BlockPos, BlockState>> iterator = CLIPS.entrySet().iterator();
        while (iterator.hasNext()) {
            // Extract.
            final Map.Entry<BlockPos, BlockState> entry = iterator.next();
            final BlockPos pos = entry.getKey();
            final BlockState expectedState = entry.getValue();

            // Log. (**TRACE**)
            if (Variables.DEBUG_LOGS) {
                LOGGER.trace(HCsCR.MARKER, "HCsCR: Checking block clip... (pos: {}, expectedState: {})", pos, expectedState);
            }

            // Do nothing if the block is still there.
            final BlockState actualState = level.getBlockState(pos);
            if (actualState.equals(expectedState)) {
                // Log. (**TRACE**)
                if (Variables.DEBUG_LOGS) {
                    LOGGER.trace(HCsCR.MARKER, "HCsCR: Block clip is fine. (pos: {}, expectedState: {}, actualState: {})", pos, expectedState, actualState);
                }

                // Continue.
                continue;
            }

            // Remove.
            iterator.remove();

            // Log. (**DEBUG**)
            if (Variables.DEBUG_LOGS) {
                LOGGER.debug(HCsCR.MARKER, "HCsCR: Removed the block clip. (pos: {}, expectedState: {}, actualState: {})", pos, expectedState, actualState);
            }
        }

        // Log. (**TRACE**)
        if (Variables.DEBUG_LOGS) {
            LOGGER.trace(HCsCR.MARKER, "HCsCR: Checked block clips. (clips: {})", CLIPS);
        }

        // Pop the profiler.
        if (Variables.DEBUG_PROFILER) {
            profiler.pop();
        }
    }

    /**
     * Checks if a clip exists at the location. Should be called on
     * collision from {@link BlockBehaviour_BlockStateBaseMixin}.
     *
     * @param pos Block position to check a clip at
     * @return {@code true} if a clip exists in {@link #CLIPS} for that location, {@code false} if not
     * @see #CLIPS
     * @see #add(BlockPos, BlockState)
     * @see #remove(BlockPos)
     * @see #clear()
     */
    @Contract(pure = true)
    public static boolean has(final BlockPos pos) {
        // Validate.
        if (Variables.DEBUG_ASSERTS) {
            assert (pos != null) : "HCsCR: Parameter 'pos' is null.";
            assert (Minecraft.getInstance().isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", pos: " + pos + ')';
        }

        // Check.
        return CLIPS.containsKey(pos);
    }

    /**
     * Adds a clip into {@link #CLIPS}. Should be called when on right click
     * from {@link BedBlockMixin} or {@link RespawnAnchorBlockMixin}.
     *
     * @param pos   Block position to create a clip for
     * @param state Expected block state at the clip for it to be effective
     * @see #CLIPS
     * @see #has(BlockPos)
     * @see #remove(BlockPos)
     * @see #clear()
     */
    public static void add(final BlockPos pos, final BlockState state) {
        // Validate.
        if (Variables.DEBUG_ASSERTS) {
            assert (pos != null) : "HCsCR: Parameter 'pos' is null. (state: " + state + ')';
            assert (state != null) : "HCsCR: Parameter 'state' is null. (pos: " + pos + ')';
            assert (Minecraft.getInstance().isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", pos: " + pos + ", state: " + state + ')';
        }

        // Put.
        CLIPS.put(pos, state);
    }

    /**
     * Removes a clip by its position from {@link #CLIPS}. Does nothing if there's no clip.
     * Should be called when a block is re-synchronized in {@link ClientPacketListenerMixin}.
     *
     * @param pos Block position to remove a clip at
     * @see #CLIPS
     * @see #has(BlockPos)
     * @see #add(BlockPos, BlockState)
     * @see #clear()
     */
    public static void remove(final BlockPos pos) {
        // Validate.
        if (Variables.DEBUG_ASSERTS) {
            assert (pos != null) : "HCsCR: Parameter 'pos' is null.";
            assert (Minecraft.getInstance().isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", pos: " + pos + ')';
        }

        // Remove.
        CLIPS.remove(pos);
    }

    /**
     * Clears all clips from {@link #CLIPS}. Does nothing if there are no clips.
     * Should be called when a world is unloaded in {@link MinecraftMixin}.
     *
     * @see #CLIPS
     * @see #has(BlockPos)
     * @see #add(BlockPos, BlockState)
     * @see #remove(BlockPos)
     */
    public static void clear() {
        // Validate.
        if (Variables.DEBUG_ASSERTS) {
            assert (Minecraft.getInstance().isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ')';
        }

        // Clear.
        CLIPS.clear();
    }
}
