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

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NullMarked;
import ru.vidtu.hcscr.HCsCR;
import ru.vidtu.hcscr.compile.Constants;
import ru.vidtu.hcscr.compile.Variables;
import ru.vidtu.hcscr.config.Config;
import ru.vidtu.hcscr.mixin.MinecraftMixin;
import ru.vidtu.hcscr.mixin.crystal.EntityMixin;
import ru.vidtu.hcscr.platform.HStonecutter;

import java.util.Iterator;

//? if >=1.21.4 {
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
//?} else {
/*import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
*///?}

/**
 * Handling logic for the mod's temporarily hidden entities until they are truly removed by
 * the server side. Only does something when {@link Config#crystalsResync()} is non-zero}.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see BlockMode#COLLISION
 * @see Config#blocks()
 */
@ApiStatus.Internal
@NullMarked
public final class HiddenEntities {
    /**
     * Hidden entities mapped to their remaining resync game ticks.
     * <p>
     * These entities won't appear in the world as their
     * hitbox will be removed via {@link EntityMixin}.
     * <p>
     * They are counted down in {@link #tick(Minecraft, ProfilerFiller)}.
     *
     * @see EntityMixin
     * @see #tick(Minecraft, ProfilerFiller)
     * @see #isHidden(Entity)
     * @see #hideForTicks(Entity, int)
     * @see #show(Entity)
     * @see #showAll()
     */
    // This map should be array-backed, but it must support setValue(int) in iterators.
    // fastutil versions before 8.5.12 (shipped before MC1.21.4) don't have this due to a bug:
    // https://github.com/vigna/fastutil/blob/fcac58f7d3df8e7d903fad533f4caada7f4937cf/CHANGES#L4
    // So we're forced to use hash-based map before 1.21.4, even though hashing is useless for us.
    //? if >=1.21.4 {
    private static final Object2IntMap<Entity> HIDDEN = new Object2IntArrayMap<>(0);
    //?} else {
    /*private static final Object2IntMap<Entity> HIDDEN = new Object2IntOpenHashMap<>(0);
    *///?}

    /**
     * Logger for this class.
     */
    @UnknownNullability
    private static final Logger LOGGER = (Variables.DEBUG_LOGS ? LogManager.getLogger("HCsCR/HiddenEntities") : null);

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private HiddenEntities() {
        if (Variables.DEBUG_ASSERTS) {
            throw new AssertionError("HCsCR: No instances.");
        }
    }

    /**
     * Cleans the hidden entities. Removes redundant entities from {@link #HIDDEN}. A redundant
     * entry is one for which {@code int} value reached zero. If not reached zero, decrements
     * by one every method call. Should be called every tick from {@link HCsCR#tick(Minecraft)}.
     *
     * @param client   Client game instance
     * @param profiler Client profiler, {@code null} if {@link Variables#DEBUG_PROFILER} is {@code false}
     * @see HCsCR#tick(Minecraft)
     * @see #HIDDEN
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
            profiler.push("hcscr:hidden_entities"); // Implicit NPE for 'profiler'
        }

        // Do nothing if there are no hidden entities.
        if (HIDDEN.isEmpty()) {
            // Pop the profiler.
            if (Variables.DEBUG_PROFILER) {
                profiler.pop();
            }

            // Stop.
            return;
        }

        // Clear all entities, if level is null.
        if (client.level == null) { // Implicit NPE for 'client'
            // Log. (**TRACE**)
            if (Variables.DEBUG_LOGS) {
                LOGGER.trace(HCsCR.MARKER, "HCsCR: Null level, clearing hidden entities... (hidden: {})", HIDDEN);
            }

            // Clear.
            HIDDEN.clear();

            // Log. (**DEBUG**)
            if (Variables.DEBUG_LOGS) {
                LOGGER.debug(HCsCR.MARKER, "HCsCR: Null level, cleared hidden entities. (hidden: {})", HIDDEN);
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
            LOGGER.trace(HCsCR.MARKER, "HCsCR: Checking hidden entities... (hidden: {})", HIDDEN);
        }

        // Iterate.
        final Iterator<Object2IntMap.Entry<Entity>> iterator = HIDDEN.object2IntEntrySet().iterator();
        while (iterator.hasNext()) {
            // Extract.
            final Object2IntMap.Entry<Entity> entry = iterator.next();
            final Entity entity = entry.getKey();
            final int ticksBeforeResync = entry.getIntValue();

            // Log. (**TRACE**)
            if (Variables.DEBUG_LOGS && LOGGER.isTraceEnabled(HCsCR.MARKER)) {
                LOGGER.trace(HCsCR.MARKER, "HCsCR: Ticking hidden entity... (entity: {}, ticksBeforeResync: {})", entity, ticksBeforeResync);
            }

            // Entity has been removed.
            if (HStonecutter.isEntityRemoved(entity)) {
                // Remove.
                iterator.remove();

                // Log. (**DEBUG**)
                if (Variables.DEBUG_LOGS && LOGGER.isDebugEnabled(HCsCR.MARKER)) {
                    LOGGER.debug(HCsCR.MARKER, "HCsCR: Removed hidden entity. (entity: {}, ticksBeforeResync: {})", entity, ticksBeforeResync);
                }

                // Continue.
                continue;
            }

            // Entity should be resynced.
            if (ticksBeforeResync <= 0) {
                // Remove.
                iterator.remove();

                // Log. (**DEBUG**)
                if (Variables.DEBUG_LOGS && LOGGER.isDebugEnabled(HCsCR.MARKER)) {
                    LOGGER.debug(HCsCR.MARKER, "HCsCR: Resynced hidden entity. (entity: {}, ticksBeforeResync: {})", entity, ticksBeforeResync);
                }

                // Continue.
                continue;
            }

            // Decrement the remaining ticks.
            entry.setValue(ticksBeforeResync - 1);
        }

        // Log. (**TRACE**)
        if (Variables.DEBUG_LOGS) {
            LOGGER.trace(HCsCR.MARKER, "HCsCR: Checked hidden entities. (hidden: {})", HIDDEN);
        }

        // Pop the profiler.
        if (Variables.DEBUG_PROFILER) {
            profiler.pop();
        }
    }

    /**
     * Checks if an entity should be hidden exists at the location.
     * Should be called on hitbox retrieval from {@link EntityMixin}.
     *
     * @param entity Entity to check the hidden status of
     * @return {@code true} if an entity is currently hidden, {@code false} if not
     * @see #HIDDEN
     * @see #hideForTicks(Entity, int)
     * @see #show(Entity)
     * @see #showAll()
     */
    @Contract(pure = true)
    public static boolean isHidden(final Entity entity) {
        // Validate.
        if (Variables.DEBUG_ASSERTS) {
            assert (entity != null) : "HCsCR: Parameter 'entity' is null. (entity: " + entity + ')';
            assert (Minecraft.getInstance().isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", entity: " + entity + ')';
        }

        // Check.
        return HIDDEN.containsKey(entity);
    }

    /**
     * Adds (hides) an entity into {@link #HIDDEN}. Should be called in TODO.
     *
     * @param entity Entity to hide
     * @param ticks  Amount of ticks to hide the entity for (in the range of {@link Constants#MIN_HIDE_TICKS} inclusive to {@link Constants#MAX_HIDE_TICKS} inclusive)
     * @see #HIDDEN
     * @see #isHidden(Entity)
     * @see #show(Entity)
     * @see #showAll()
     * @see Constants#MIN_HIDE_TICKS
     * @see Constants#DEFAULT_HIDE_TICKS
     * @see Constants#MAX_HIDE_TICKS
     */
    public static void hideForTicks(final Entity entity, final @Range(from = Constants.MIN_HIDE_TICKS, to = Constants.MAX_HIDE_TICKS) int ticks) {
        // Validate.
        if (Variables.DEBUG_ASSERTS) {
            assert (entity != null) : "HCsCR: Parameter 'entity' is null. (ticks: " + ticks + ')';
            assert ((ticks >= Constants.MIN_HIDE_TICKS) && (ticks <= Constants.MAX_HIDE_TICKS)) : "HCsCR: Parameter 'ticks' is not in the [" + Constants.MIN_HIDE_TICKS + ".." + Constants.MAX_HIDE_TICKS + "] range. (entity: " + entity + ", ticks: " + ticks + ')';
            assert (Minecraft.getInstance().isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", entity: " + entity + ", ticks: " + ticks + ')';
            // TODO(VidTu): Check if remove.
        }

        // TODO(VidTu): Should we clamp here?

        // Split debug logic.
        if (Variables.DEBUG_LOGS && (LOGGER.isDebugEnabled(HCsCR.MARKER) || LOGGER.isTraceEnabled(HCsCR.MARKER))) {
            // Log. (**TRACE**)
            LOGGER.trace(HCsCR.MARKER, "HCsCR: Hiding an entity... (entity: {}, ticks: {}, hidden: {})", entity, ticks, HIDDEN);

            // Put. (store previous)
            final int previous = HIDDEN.put(entity, ticks);

            // Log. (**DEBUG**)
            LOGGER.debug(HCsCR.MARKER, "HCsCR: Hid an entity. (entity: {}, ticks: {}, previous: {}, hidden: {})", entity, ticks, previous, HIDDEN);
        } else {
            // Put.
            HIDDEN.put(entity, ticks);
        }
    }

    /**
     * Removes (shows) an entity from {@link #HIDDEN}. Does nothing if it wasn't hidden.
     * Should be called when an entity is removed in {@link ClientPacketListenerMixin}.
     *
     * @param Entity to show
     * @see #HIDDEN
     * @see #isHidden(Entity)
     * @see #hideForTicks(Entity, int)
     * @see #showAll()
     */
    public static void show(final Entity entity) { // TODO(VidTu): Implement.
        // Validate.
        if (Variables.DEBUG_ASSERTS) {
            assert (entity != null) : "HCsCR: Parameter 'entity' is null. (entity: " + entity + ')';
            assert (Minecraft.getInstance().isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", entity: " + entity + ')';
        }

        // Split debug logic.
        if (Variables.DEBUG_LOGS) {
            // Log. (**TRACE**)
            LOGGER.trace(HCsCR.MARKER, "HCsCR: Showing a hidden entity... (entity: {}, hidden: {})", entity, HIDDEN);

            // Remove. (store remaining)
            final int remaining = HIDDEN.remove(entity);

            // Log. (**DEBUG**)
            LOGGER.debug(HCsCR.MARKER, "HCsCR: Shown a hidden entity. (entity: {}, remaining: {}, hidden: {})", entity, remaining, HIDDEN);
        } else {
            // Remove.
            HIDDEN.remove(entity);
        }
    }

    /**
     * Clears all entities from {@link #HIDDEN}. Does nothing if there are no entities.
     * Should be called when a world is unloaded in {@link MinecraftMixin}.
     *
     * @see #HIDDEN
     * @see #isHidden(Entity)
     * @see #hideForTicks(Entity, int)
     * @see #show(Entity)
     */
    public static void showAll() {
        // Validate.
        if (Variables.DEBUG_ASSERTS) {
            assert (Minecraft.getInstance().isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ')';
        }

        // Log. (**TRACE**)
        if (Variables.DEBUG_LOGS) {
            LOGGER.trace(HCsCR.MARKER, "HCsCR: Showing hidden entities... (hidden: {})", HIDDEN);
        }

        // Clear.
        HIDDEN.clear();

        // Log. (**DEBUG**)
        if (Variables.DEBUG_LOGS) {
            LOGGER.debug(HCsCR.MARKER, "HCsCR: Shown hidden entities. (hidden: {})", HIDDEN);
        }
    }
}
