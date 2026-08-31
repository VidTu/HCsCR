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

import it.unimi.dsi.fastutil.objects.Reference2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2LongMap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NullMarked;
import ru.vidtu.hcscr.HCsCR;
import ru.vidtu.hcscr.compile.Variables;
import ru.vidtu.hcscr.config.Config;

import java.util.Iterator;

/**
 * Handling logic for the mod's entities scheduled for removal (or hiding via {@link HiddenEntities}),
 * right after hitting them. Only does something when {@link Config#crystalsDelay()} is non-zero.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see Config#crystalsDelay()
 */
@ApiStatus.Internal
@NullMarked
public final class ScheduledEntities {
    /**
     * Hit entities mapped to their time of removal/hiding time in units of {@link System#nanoTime()}.
     * <p>
     * As soon as current time will reach the removal time, {@link #loop(Minecraft, ProfilerFiller)}
     * will either remove them or mark them as hidden entities into {@link HiddenEntities}.
     *
     * @see #loop(Minecraft, ProfilerFiller)
     * @see #scheduleAt(Entity, long)
     * @see #unschedule(Entity)
     * @see #unscheduleAll()
     */
    // This map is not expected to grow more than a few elements, so it's an array-baked map, not a hash-baked one.
    // Moreover, it's being iterated linearly anyway in handleFrame(...).
    private static final Reference2LongMap<Entity> SCHEDULED = new Reference2LongArrayMap<>(0);

    /**
     * Logger for this class.
     */
    @UnknownNullability
    private static final Logger LOGGER = (Variables.DEBUG_LOGS ? LogManager.getLogger("HCsCR/ScheduledEntities") : null);

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private ScheduledEntities() {
        if (Variables.DEBUG_ASSERTS) {
            throw new AssertionError("HCsCR: No instances.");
        }
    }

    /**
     * Cleans the scheduled entities. Removes redundant entities from {@link #SCHEDULED}.
     * A redundant entry is one for which {@code long} value exceeds {@link System#nanoTime()}.
     * Should be called every tick from {@link HCsCR#loop(Minecraft)}.
     *
     * @param client   Client game instance
     * @param profiler Client profiler, {@code null} if {@link Variables#DEBUG_PROFILER} is {@code false}
     * @see HCsCR#loop(Minecraft)
     * @see #SCHEDULED
     */
    public static void loop(final Minecraft client, final @UnknownNullability ProfilerFiller profiler) {
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
            profiler.push("hcscr:scheduled_entities"); // Implicit NPE for 'profiler'
        }

        // Do nothing if there are no scheduled entities.
        if (SCHEDULED.isEmpty()) {
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
                LOGGER.trace(HCsCR.MARKER, "HCsCR: Null level, clearing scheduled entities... (scheduled: {})", SCHEDULED);
            }

            // Clear.
            SCHEDULED.clear();

            // Log. (**DEBUG**)
            if (Variables.DEBUG_LOGS) {
                LOGGER.debug(HCsCR.MARKER, "HCsCR: Null level, cleared scheduled entities. (scheduled: {})", SCHEDULED);
            }

            // Pop the profiler.
            if (Variables.DEBUG_PROFILER) {
                profiler.pop();
            }

            // Stop.
            return;
        }

        // Iterate.
        final int resync = Config.crystalsResync();
        final boolean noResync = (resync == 0);
        final long now = System.nanoTime();
        final Iterator<Reference2LongMap.Entry<Entity>> iterator = SCHEDULED.reference2LongEntrySet().iterator();
        while (iterator.hasNext()) {
            // Extract.
            final Reference2LongMap.Entry<Entity> entry = iterator.next();
            final Entity entity = entry.getKey();
            final long deadline = entry.getLongValue();

            // Entity has been removed.
            //~ if >=1.17.1 'removed' -> 'isRemoved()' {
            if (entity.isRemoved()) {
            //~}
                // Remove.
                iterator.remove();

                // Log. (**DEBUG**)
                if (Variables.DEBUG_LOGS && LOGGER.isDebugEnabled(HCsCR.MARKER)) {
                    LOGGER.debug(HCsCR.MARKER, "HCsCR: Forgot scheduled entity. (now: {}, entity: {}, deadline: {})", now, entity, deadline);
                }

                // Continue.
                continue;
            }

            // Skip if entry is still in the world and hasn't reached the deadline.
            if ((deadline - now) >= 0L) continue;

            // Remove.
            iterator.remove();

            // Hide or remove the entity.
            if (noResync) {
                //$ remove_entity entity
                entity.discard();
            } else {
                HiddenEntities.hideForTicks(entity, resync);
            }

            // Log. (**DEBUG**)
            if (Variables.DEBUG_LOGS && LOGGER.isDebugEnabled(HCsCR.MARKER)) {
                LOGGER.debug(HCsCR.MARKER, "HCsCR: Removed/hidden scheduled entity. (now: {}, entity: {}, deadline: {})", now, entity, deadline);
            }
        }

        // Pop the profiler.
        if (Variables.DEBUG_PROFILER) {
            profiler.pop();
        }
    }

    /**
     * Adds (schedules) an entity into {@link #SCHEDULED}. Should be called in TODO.
     *
     * @param entity   Entity to schedule
     * @param deadline Time in units of {@link System#nanoTime()} to remove or hide the entity
     * @see #SCHEDULED
     * @see #unschedule(Entity)
     * @see #unscheduleAll()
     */
    public static void scheduleAt(final Entity entity, final long deadline) {
        // Validate.
        if (Variables.DEBUG_ASSERTS) {
            assert (entity != null) : "HCsCR: Parameter 'entity' is null. (deadline: " + deadline + ')';
            final long diff = (System.nanoTime() - deadline);
            assert (diff >= -10_000_000_000L && diff <= 10_000_000_000L) : "HCsCR: Parameter 'deadline' differs from current time for more than 10 seconds. (entity: " + entity + ", deadline: " + deadline + ", diff: " + diff + ')';
            assert (Minecraft.getInstance().isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", entity: " + entity + ", deadline: " + deadline + ')';
            //~ if >=1.17.1 'removed' -> 'isRemoved()' {
            assert (!entity.isRemoved()) : "HCsCR: Invalid entity. (entity: " + entity + ", deadline: " + deadline + ')';
            //~}
        }

        // Split debug logic.
        if (Variables.DEBUG_LOGS && (LOGGER.isDebugEnabled(HCsCR.MARKER) || LOGGER.isTraceEnabled(HCsCR.MARKER))) {
            // Log. (**TRACE**)
            LOGGER.trace(HCsCR.MARKER, "HCsCR: Scheduling an entity removal... (entity: {}, deadline: {}, scheduled: {})", entity, deadline, SCHEDULED);

            // Put. (store previous)
            final long previous = SCHEDULED.put(entity, deadline);

            // Log. (**DEBUG**)
            LOGGER.debug(HCsCR.MARKER, "HCsCR: Scheduled an entity removal. (entity: {}, deadline: {}, previous: {}, scheduled: {})", entity, deadline, previous, SCHEDULED);
        } else {
            // Put.
            SCHEDULED.put(entity, deadline);
        }
    }

    /**
     * Removes (unschedules) an entity from {@link #SCHEDULED}. Does nothing if it wasn't scjedules.
     * Should be called when an entity is removed in {@link ClientPacketListenerMixin}.
     *
     * @param Entity to unschedule
     * @see #SCHEDULED
     * @see #scheduleAt(Entity, long)
     * @see #unscheduleAll()
     */
    public static void unschedule(final Entity entity) { // TODO(VidTu): Implement.
        // Validate.
        if (Variables.DEBUG_ASSERTS) {
            assert (entity != null) : "HCsCR: Parameter 'entity' is null. (entity: " + entity + ')';
            assert (Minecraft.getInstance().isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", entity: " + entity + ')';
        }

        // Split debug logic.
        if (Variables.DEBUG_LOGS) {
            // Log. (**TRACE**)
            LOGGER.trace(HCsCR.MARKER, "HCsCR: Unscheduling a scheduled entity... (entity: {}, scheduled: {})", entity, SCHEDULED);

            // Remove. (store deadline)
            final long deadline = SCHEDULED.remove(entity);

            // Log. (**DEBUG**)
            LOGGER.debug(HCsCR.MARKER, "HCsCR: Unscheduled a scheduled entity. (entity: {}, deadline: {}, scheduled: {})", entity, deadline, SCHEDULED);
        } else {
            // Remove.
            SCHEDULED.remove(entity);
        }
    }

    /**
     * Clears all entities from {@link #SCHEDULED}. Does nothing if there are no entities.
     * Should be called when a world is unloaded in {@link MinecraftMixin}.
     *
     * @see #SCHEDULED
     * @see #scheduleAt(Entity, long)
     * @see #unschedule(Entity)
     */
    public static void unscheduleAll() {
        // Validate.
        if (Variables.DEBUG_ASSERTS) {
            assert (Minecraft.getInstance().isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ')';
        }

        // Log. (**TRACE**)
        if (Variables.DEBUG_LOGS) {
            LOGGER.trace(HCsCR.MARKER, "HCsCR: Unscheduling scheduled entities... (scheduled: {})", SCHEDULED);
        }

        // Clear.
        SCHEDULED.clear();

        // Log. (**DEBUG**)
        if (Variables.DEBUG_LOGS) {
            LOGGER.debug(HCsCR.MARKER, "HCsCR: Unscheduled scheduled entities. (scheduled: {})", SCHEDULED);
        }
    }
}
