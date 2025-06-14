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

package ru.vidtu.hcscr;

import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import ru.vidtu.hcscr.config.CrystalMode;
import ru.vidtu.hcscr.config.HConfig;
import ru.vidtu.hcscr.config.HScreen;
import ru.vidtu.hcscr.mixins.blocks.BlockStateBaseMixin;
import ru.vidtu.hcscr.mixins.crystals.EntityMixin;
import ru.vidtu.hcscr.platform.HStonecutter;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Main HCsCR class.
 *
 * @author VidTu
 * @apiNote Internal use only
 */
@ApiStatus.Internal
@NullMarked
public final class HCsCR {
    /**
     * Marker for all logs generated by the mod.
     */
    public static final Marker HCSCR_MARKER = MarkerManager.getMarker("MOD_HCSCR");

    /**
     * Open config screen keybind. Not bound by default.
     *
     * @see #handleConfigBind(Minecraft, ProfilerFiller)
     */
    public static final KeyMapping CONFIG_BIND = new KeyMapping("hcscr.key.config", InputConstants.UNKNOWN.getValue(), "hcscr.key");

    /**
     * Toggle the mod keybind. Not bound by default.
     *
     * @see #handleToggleBind(Minecraft, ProfilerFiller)
     */
    public static final KeyMapping TOGGLE_BIND = new KeyMapping("hcscr.key.toggle", InputConstants.UNKNOWN.getValue(), "hcscr.key");

    /**
     * Hit entities mapped to their time of removal/hiding time in units of {@link System#nanoTime()}.
     * <p>
     * As soon as current time will reach the removal time, {@link #handleFrameTick(ProfilerFiller)}
     * will either remove them via {@link HStonecutter#removeEntity(Entity)} or mark them
     * as hidden entities into {@link #HIDDEN_ENTITIES}.
     *
     * @see #handleFrameTick(ProfilerFiller)
     * @see HStonecutter#removeEntity(Entity)
     * @see #HIDDEN_ENTITIES
     */
    // This map is not expected to grow more than a few elements, so it's an array-baked map, not a hash-baked one.
    // Moreover, it's being iterated linearly anyway in handleFrame(...).
    public static final Object2LongMap<Entity> SCHEDULED_ENTITIES = new Object2LongArrayMap<>(0);

    /**
     * Hidden entities mapped to their remaining resync game ticks.
     * <p>
     * These entities won't appear in the world as their
     * hitbox will be removed via {@link EntityMixin}.
     * <p>
     * They are counted down in {@link #handleHiddenEntities(Minecraft, ProfilerFiller)}.
     *
     * @see EntityMixin
     * @see #handleHiddenEntities(Minecraft, ProfilerFiller)
     */
    // Ideally, an array-baked map should be always used here too. Due to a bug in fastutil, setValue(int) is not
    // supported until 8.5.12: https://github.com/vigna/fastutil/blob/fcac58f7d3df8e7d903fad533f4caada7f4937cf/CHANGES#L41
    //? if >=1.21.4 {
    public static final Object2IntMap<Entity> HIDDEN_ENTITIES = new it.unimi.dsi.fastutil.objects.Object2IntArrayMap<>(0);
    //?} else
    /*public static final Object2IntMap<Entity> HIDDEN_ENTITIES = new it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap<>(0);*/

    /**
     * A map of block positions that the player won't
     * collide with mapped to expected block states.
     * <p>
     * These blocks won't collide with the player in the world as
     * their hitbox will be removed via {@link BlockStateBaseMixin}.
     * <p>
     * The validity is checked in {@link #handleClippingBlocks(Minecraft, ProfilerFiller)}.
     *
     * @see BlockStateBaseMixin
     * @see #handleClippingBlocks(Minecraft, ProfilerFiller)
     */
    // This map is not expected to grow more than a few elements, so it's an array-baked map, not a hash-baked one.
    // Moreover, it's being iterated linearly anyway in handleTick(...).
    public static final Object2ObjectMap<BlockPos, BlockState> CLIPPING_BLOCKS = new Object2ObjectArrayMap<>(0);

    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = LogManager.getLogger("HCsCR");

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private HCsCR() {
        throw new AssertionError("HCsCR: No instances.");
    }

    /**
     * Handles the client tick end.
     *
     * @param game Current game instance
     * @see #handleConfigBind(Minecraft, ProfilerFiller)
     * @see #handleToggleBind(Minecraft, ProfilerFiller)
     * @see #handleHiddenEntities(Minecraft, ProfilerFiller)
     * @see #handleClippingBlocks(Minecraft, ProfilerFiller)
     */
    public static void handleGameTick(Minecraft game) {
        // Validate.
        assert game != null : "HCsCR: Parameter 'game' is null.";
        assert game.isSameThread() : "HCsCR: Handling game tick NOT from the main thread. (thread: " + Thread.currentThread() + ", game: " + game + ')';

        // Get and push the profiler.
        ProfilerFiller profiler = HStonecutter.profilerOfGame(game); // Implicit NPE for 'game'
        profiler.push("hcscr:tick");

        // Keybinds.
        handleConfigBind(game, profiler); // Implicit NPE for 'game'
        handleToggleBind(game, profiler); // Implicit NPE for 'game'

        // Entities/anchors.
        handleHiddenEntities(game, profiler); // Implicit NPE for 'game'
        handleClippingBlocks(game, profiler); // Implicit NPE for 'game'

        // Pop the profiler.
        profiler.pop();
    }

    /**
     * Handles the client rendering frame. Removes redundant elements from {@link #SCHEDULED_ENTITIES}.
     *
     * @param profiler Client profiler instance
     */
    public static void handleFrameTick(ProfilerFiller profiler) {
        // Validate.
        assert profiler != null : "HCsCR: Parameter 'profiler' is null.";
        assert Minecraft.getInstance().isSameThread() : "HCsCR: Handling frame tick NOT from the main thread. (thread: " + Thread.currentThread() + ", profiler: " + profiler + ')';

        // Push the profiler.
        profiler.push("hcscr:handle_frame"); // Implicit NPE for 'profiler'

        // Skip if there's no entities to remove.
        if (SCHEDULED_ENTITIES.isEmpty()) {
            // Pop, stop.
            profiler.pop();
            return;
        }

        // Remove all entities that have expired or no longer in the world.
        int resync = HConfig.crystalsResync();
        long now = System.nanoTime();
        ObjectIterator<Object2LongMap.Entry<Entity>> iterator = SCHEDULED_ENTITIES.object2LongEntrySet().iterator();
        while (iterator.hasNext()) {
            // Extract.
            Object2LongMap.Entry<Entity> entry = iterator.next();
            Entity entity = entry.getKey();
            long expiry = entry.getLongValue();

            // Entity has been deleted from the world.
            if (HStonecutter.isEntityRemoved(entity)) {
                // Remove from iterator.
                iterator.remove();

                // Log. (**DEBUG**)
                if (!LOGGER.isDebugEnabled(HCSCR_MARKER)) continue;
                LOGGER.debug(HCSCR_MARKER, "HCsCR: Forgot hit entity. (now: {}, entity: {}, expiry: {})", now, entity, expiry);
                continue;
            }

            // Skip if entry is still in the world and hasn't reached the timeout.
            if ((expiry - now) >= 0L) continue;

            // Remove from iterator.
            iterator.remove();

            // Hide or remove the entity.
            if (resync == 0) {
                HStonecutter.removeEntity(entity);
            } else {
                HIDDEN_ENTITIES.put(entity, resync);
            }

            // Log. (**DEBUG**)
            if (!LOGGER.isDebugEnabled(HCSCR_MARKER)) continue;
            LOGGER.debug(HCSCR_MARKER, "HCsCR: Removed/hidden hit entity. (now: {}, entity: {}, expiry: {})", now, entity, expiry);
        }

        // Pop the profiler.
        profiler.pop();
    }

    /**
     * Handles the entity hit. Removes the entity client-side or promotes to {@link #SCHEDULED_ENTITIES}, if required.
     *
     * @param entity  Target hit entity
     * @param source  Hit damage source
     * @param amount  Amount of dealt damage
     * @return Whether the entity has been removed
     * @see HStonecutter#hurtEntity(Entity, DamageSource, float)
     * @see HConfig#enable()
     * @see HConfig#shouldProcess(Entity)
     * @see HConfig#crystals()
     * @see HConfig#crystalsResync()
     * @see HConfig#crystalsDelay()
     */
    public static boolean handleEntityHit(Entity entity, DamageSource source, float amount) {
        // Validate.
        assert entity != null : "HCsCR: Parameter 'entity' is null. (source: " + source + ", amount: " + amount + ')';
        assert source != null : "HCsCR: Parameter 'source' is null. (entity: " + entity + ", amount: " + amount + ')';
        assert Float.isFinite(amount) : "HCsCR: Parameter 'amount' is not finite. (entity: " + entity + ", source: " + source + ", amount: " + amount + ')';

        // Do NOT process hit if any of the following conditions is met:
        // - The current level (world) is not client-side. (e.g., integrated server world)
        // - The amount of dealt damage is zero or less.
        // - The damaged entity is already scheduled for removal.
        // - The mod is disabled via config or keybind.
        // - This entity type shouldn't be processed at all (e.g. any living entity) or by the current config (e.g. slime).
        // - The damaging entity is not a player.
        if (!HStonecutter.levelOfEntity(entity).isClientSide() || (amount <= 0.0F) || // Implicit NPE for 'entity'
                HStonecutter.isEntityRemoved(entity) || !HConfig.enable() ||
                !HConfig.shouldProcess(entity)) return false;

        // Validate.
        //noinspection ObjectEquality // <- Should be the same reference.
        assert (source.getEntity() instanceof LocalPlayer) && (source.getEntity() == source.getDirectEntity()) && (source.getEntity() == Minecraft.getInstance().player) : "HCsCR: Source entity is not LocalPlayer. (entity: " + entity + ", source: " + source + ", amount: " + amount + ", sourceEntity: " + source.getEntity() + ", sourceDirectEntity: " + source.getDirectEntity() + ')';
        assert Minecraft.getInstance().isSameThread() : "HCsCR: Handling entity attack NOT from the main thread. (thread: " + Thread.currentThread() + ", entity: " + entity + ", source: " + source + ", amount: " + amount + ')';

        // Don't process player hits that deal zero damage, e.g. with the weakness effect.
        LocalPlayer player = (LocalPlayer) source.getEntity(); // Implicit NPE for 'source'
        AttributeMap map = player.getAttributes();
        for (MobEffectInstance instance : player.getActiveEffects()) {
            //? if >=1.20.6 {
            instance.getEffect().value().addAttributeModifiers(map, instance.getAmplifier());
            //?} else if >=1.20.2 {
            /*instance.getEffect().addAttributeModifiers(map, instance.getAmplifier());
            *///?} else {
            /*instance.getEffect().addAttributeModifiers(player, map, instance.getAmplifier());
            *///?}
        }
        double attributeAmount = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        for (MobEffectInstance instance : player.getActiveEffects()) {
            //? if >=1.20.6 {
            instance.getEffect().value().removeAttributeModifiers(map);
            //?} else if >=1.20.2 {
            /*instance.getEffect().removeAttributeModifiers(map);
             *///?} else
            /*instance.getEffect().removeAttributeModifiers(player, map, instance.getAmplifier());*/
        }
        if (attributeAmount <= 0.0D) return false;

        // Fast-remove one crystal entity, if the mode is DIRECT.
        CrystalMode mode = HConfig.crystals();
        if (mode == CrystalMode.DIRECT) {
            // Remove/hide the entity, if there's no delay.
            int delay = HConfig.crystalsDelay();
            if (delay == 0) {
                // Remove the entity instantly, if there's no resync.
                int resync = HConfig.crystalsResync();
                if (resync == 0) {
                    HStonecutter.removeEntity(entity);
                    return true;
                }

                // Hide the entity.
                HIDDEN_ENTITIES.put(entity, resync);
                return true;
            }

            // Schedule the removal of the entity, if the delay exists.
            SCHEDULED_ENTITIES.putIfAbsent(entity, System.nanoTime() + delay);
            return true;
        }

        // Get the enveloped entities.
        AABB entityBox = entity.getBoundingBox();
        List<Entity> entities = HStonecutter.levelOfEntity(entity).getEntities(entity, entity.getBoundingBox(), other -> {
            // Do NOT process hit if any of the following conditions is met:
            // - The damaged entity is already scheduled for removal.
            // - This entity type shouldn't be processed at all (e.g. any living entity) or by the current config (e.g. slime).
            // - The other entity is not fully contained inside the enveloping entity.
            if (HStonecutter.isEntityRemoved(other) || !HConfig.shouldProcess(other)) return false;
            AABB otherBox = other.getBoundingBox();
            return (otherBox.minX >= entityBox.minX) && (otherBox.maxX <= entityBox.maxX) &&
                    (otherBox.minY >= entityBox.minY) && (otherBox.maxY <= entityBox.maxY) &&
                    (otherBox.minZ >= entityBox.minZ) && (otherBox.maxZ <= entityBox.maxZ);
        });

        // Remove/hide the entities, if there's no delay.
        int delay = HConfig.crystalsDelay();
        if (delay == 0) {
            // Remove the entities instantly, if there's no resync.
            int resync = HConfig.crystalsResync();
            if (resync == 0) {
                HStonecutter.removeEntity(entity);
                for (Entity other : entities) {
                    HStonecutter.removeEntity(other);
                }
                return true;
            }

            // Hide the entity.
            HIDDEN_ENTITIES.put(entity, resync);
            for (Entity other : entities) {
                HIDDEN_ENTITIES.put(other, resync);
            }
            return true;
        }

        // Schedule the removal of the entities, if the delay exists.
        long removeAt = (System.nanoTime() + delay);
        SCHEDULED_ENTITIES.putIfAbsent(entity, removeAt);
        for (Entity other : entities) {
            SCHEDULED_ENTITIES.putIfAbsent(other, removeAt);
        }
        return true;
    }

    /**
     * Handles the config keybind.
     *
     * @param game     Current game instance
     * @param profiler Game profiler
     * @see #handleGameTick(Minecraft)
     * @see #handleToggleBind(Minecraft, ProfilerFiller)
     */
    private static void handleConfigBind(Minecraft game, ProfilerFiller profiler) {
        // Validate.
        assert game != null : "HCsCR: Parameter 'game' is null. (profiler: " + profiler + ')';
        assert profiler != null : "HCsCR: Parameter 'profiler' is null. (game: " + game + ')';
        assert game.isSameThread() : "HCsCR: Handling the config bind NOT from the main thread. (thread: " + Thread.currentThread() + ", game: " + game + ", profiler: " + profiler + ')';

        // Push the profiler.
        profiler.push("hcscr:config_bind"); // Implicit NPE for 'profiler'

        // Consume the bind.
        if (!CONFIG_BIND.consumeClick()) {
            // Pop, stop.
            profiler.pop();
            return;
        }

        // Log. (**TRACE**)
        LOGGER.trace(HCSCR_MARKER, "HCsCR: Config keybind was consumed, opening the config screen. (game: {}, keybind: {})", game, CONFIG_BIND);

        // Check the open screen.
        Screen prev = game.screen; // Implicit NPE for 'game'
        if (prev != null) {
            // Log, pop, stop. (**DEBUG**)
            LOGGER.debug(HCSCR_MARKER, "HCsCR: Can't open the config screen via the keybind, screen is open. (game: {}, prev: {}, keybind: {})", game, prev, CONFIG_BIND);
            profiler.pop();
            return;
        }

        // Open the screen.
        HScreen screen = new HScreen(null);
        game.setScreen(screen);

        // Log. (**DEBUG**)
        LOGGER.debug(HCSCR_MARKER, "HCsCR: Opened the config screen via the keybind. (game: {}, screen: {}, keybind: {})", game, screen, CONFIG_BIND);

        // Pop the profiler.
        profiler.pop();
    }

    /**
     * Handles the toggle keybind.
     *
     * @param game     Current game instance
     * @param profiler Game profiler
     * @see #handleGameTick(Minecraft)
     * @see #handleConfigBind(Minecraft, ProfilerFiller)
     */
    private static void handleToggleBind(Minecraft game, ProfilerFiller profiler) {
        // Validate.
        assert game != null : "HCsCR: Parameter 'game' is null. (profiler: " + profiler + ')';
        assert profiler != null : "HCsCR: Parameter 'profiler' is null. (game: " + game + ')';
        assert game.isSameThread() : "HCsCR: Handling the toggle bind NOT from the main thread. (thread: " + Thread.currentThread() + ", game: " + game + ", profiler: " + profiler + ')';

        // Push the profiler.
        profiler.push("hcscr:toggle_bind"); // Implicit NPE for 'profiler'

        // Consume the bind.
        if (!TOGGLE_BIND.consumeClick()) {
            // Pop, stop.
            profiler.pop();
            return;
        }

        // Log. (**TRACE**)
        LOGGER.trace(HCSCR_MARKER, "HCsCR: Toggle keybind was consumed, toggling the mode. (game: {}, keybind: {})", game, TOGGLE_BIND);

        // Toggle the mod.
        boolean newState = HConfig.toggle();

        // Show the bar, play the sound.
        game.gui.setOverlayMessage(HStonecutter.translate("hcscr." + newState) // Implicit NPE for 'game'
                .withStyle(newState ? ChatFormatting.GREEN : ChatFormatting.RED)
                .withStyle(ChatFormatting.BOLD), /*rainbow=*/false);
        game.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING, newState ? 2.0F : 0.0F));

        // Log. (**DEBUG**)
        LOGGER.debug(HCSCR_MARKER, "HCsCR: Mod has been toggled via the keybind. (game: {}, newState: {}, keybind: {})", game, newState, TOGGLE_BIND);

        // Pop the profiler.
        profiler.pop();
    }

    /**
     * Handles the hidden entities. Removes redundant entities from {@link #HIDDEN_ENTITIES} or processes them in a
     * way similar to {@link #handleEntityHit(Entity, DamageSource, float)}.
     *
     * @param game     Current game instance
     * @param profiler Game profiler
     * @see #handleGameTick(Minecraft)
     * @see #HIDDEN_ENTITIES
     * @see #handleEntityHit(Entity, DamageSource, float)
     */
    private static void handleHiddenEntities(Minecraft game, ProfilerFiller profiler) {
        // Validate.
        assert game != null : "HCsCR: Parameter 'game' is null. (profiler: " + profiler + ')';
        assert profiler != null : "HCsCR: Parameter 'profiler' is null. (game: " + game + ')';
        assert game.isSameThread() : "HCsCR: Handling hidden entities NOT from the main thread. (thread: " + Thread.currentThread() + ", game: " + game + ", profiler: " + profiler + ')';

        // Push the profiler.
        profiler.push("hcscr:hidden_entities"); // Implicit NPE for 'profiler'

        // Skip if no hidden entities.
        if (HIDDEN_ENTITIES.isEmpty()) {
            // Pop, stop.
            profiler.pop();
            return;
        }

        // Nuke all entities, if level is empty.
        if (game.level == null) { // Implicit NPE for 'game'
            // Log. (**TRACE**)
            LOGGER.trace(HCSCR_MARKER, "HCsCR: Level has been unloaded, nuking hidden entities...");

            // Clear.
            HIDDEN_ENTITIES.clear();

            // Log, pop, stop. (**DEBUG**)
            LOGGER.debug(HCSCR_MARKER, "HCsCR: Level has been unloaded, nuked hidden entities.");
            profiler.pop();
            return;
        }

        // Iterate.
        ObjectIterator<Object2IntMap.Entry<Entity>> iterator = HIDDEN_ENTITIES.object2IntEntrySet().iterator();
        while (iterator.hasNext()) {
            // Extract.
            Object2IntMap.Entry<Entity> entry = iterator.next();
            Entity entity = entry.getKey();
            int left = entry.getIntValue();

            // Log. (**TRACE**)
            if (LOGGER.isTraceEnabled(HCSCR_MARKER)) {
                LOGGER.trace(HCSCR_MARKER, "HCsCR: Ticking hidden entity... (entity: {}, left: {})", entity, left);
            }

            // Entity has been removed.
            if (HStonecutter.isEntityRemoved(entity)) {
                // Remove.
                iterator.remove();

                // Log, continue. (**DEBUG**)
                if (!LOGGER.isDebugEnabled(HCSCR_MARKER)) continue;
                LOGGER.debug(HCSCR_MARKER, "HCsCR: Removed hidden entity. (entity: {}, left: {})", entity, left);
                continue;
            }

            // Entity should be resynced.
            if (left <= 0) {
                // Remove.
                iterator.remove();

                // Log, continue. (**DEBUG**)
                if (!LOGGER.isDebugEnabled(HCSCR_MARKER)) continue;
                LOGGER.debug(HCSCR_MARKER, "HCsCR: Resynced hidden entity. (entity: {}, left: {})", entity, left);
                continue;
            }

            // Countdown.
            entry.setValue(left - 1);
        }

        // Pop the profiler.
        profiler.pop();
    }

    /**
     * Handles the clipping blocks. Removes redundant entries from {@link #CLIPPING_BLOCKS}.
     *
     * @param game     Current game instance
     * @param profiler Game profiler
     * @see #handleGameTick(Minecraft)
     * @see #CLIPPING_BLOCKS
     */
    private static void handleClippingBlocks(Minecraft game, ProfilerFiller profiler) {
        // Validate.
        assert game != null : "HCsCR: Parameter 'game' is null. (profiler: " + profiler + ')';
        assert profiler != null : "HCsCR: Parameter 'profiler' is null. (game: " + game + ')';
        assert game.isSameThread() : "HCsCR: Handling clipping blocks NOT from the main thread. (thread: " + Thread.currentThread() + ", game: " + game + ", profiler: " + profiler + ')';

        // Push the profiler.
        profiler.push("hcscr:clipping_blocks"); // Implicit NPE for 'profiler'

        // Skip if no clipping blocks.
        if (CLIPPING_BLOCKS.isEmpty()) {
            // Pop, stop.
            profiler.pop();
            return;
        }

        // Nuke all blocks, if level is empty.
        ClientLevel level = game.level; // Implicit NPE for 'game'
        if (level == null) {
            // Log. (**TRACE**)
            LOGGER.trace(HCSCR_MARKER, "HCsCR: Level has been unloaded, nuking clipping blocks...");

            // Clear.
            CLIPPING_BLOCKS.clear();

            // Log, pop, stop. (**DEBUG**)
            LOGGER.debug(HCSCR_MARKER, "HCsCR: Level has been unloaded, nuked clipping blocks.");
            profiler.pop();
            return;
        }

        // Iterate.
        Iterator<Map.Entry<BlockPos, BlockState>> iterator = CLIPPING_BLOCKS.entrySet().iterator();
        while (iterator.hasNext()) {
            // Extract.
            Map.Entry<BlockPos, BlockState> entry = iterator.next();
            BlockPos pos = entry.getKey();
            BlockState expectedState = entry.getValue();

            // Log. (**TRACE**)
            LOGGER.trace(HCSCR_MARKER, "HCsCR: Ticking clipping block... (pos: {}, expectedState: {})", pos, expectedState);

            // Block is still there.
            BlockState actualState = level.getBlockState(pos);
            if (actualState.equals(expectedState)) continue;

            // Remove.
            iterator.remove();

            // Log. (**DEBUG**)
            LOGGER.debug(HCSCR_MARKER, "HCsCR: Removed clipping block. (pos: {}, expectedState: {}, actualState: {})", pos, expectedState, actualState);
        }

        // Pop the profiler.
        profiler.pop();
    }
}
