/*
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

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import org.lwjgl.glfw.GLFW;
import ru.vidtu.hcscr.config.CrystalMode;
import ru.vidtu.hcscr.config.HConfig;
import ru.vidtu.hcscr.config.HScreen;
import ru.vidtu.hcscr.mixins.BlockStateBaseMixin;
import ru.vidtu.hcscr.mixins.EntityMixin;
import ru.vidtu.hcscr.platform.HStonecutter;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
     * Open config screen keybind. Not bound by default.
     */
    public static final KeyMapping CONFIG_BIND = new KeyMapping("hcscr.key.config", GLFW.GLFW_KEY_UNKNOWN, "hcscr.key");

    /**
     * Toggle the mod keybind. Not bound by default.
     */
    public static final KeyMapping TOGGLE_BIND = new KeyMapping("hcscr.key.toggle", GLFW.GLFW_KEY_UNKNOWN, "hcscr.key");

    /**
     * Hit entities mapped to their time of removal/hiding time in units of {@link System#nanoTime()}. As soon as
     * current time will reach the removal time, {@link #handleFrame(ProfilerFiller)} will either remove them
     * via {@link HStonecutter#removeEntity(Entity)} or mark them as hidden entities into {@link #HIDDEN_ENTITIES}.
     *
     * @see #handleFrame(ProfilerFiller)
     * @see HStonecutter#removeEntity(Entity)
     * @see #HIDDEN_ENTITIES
     */
    // This map is not expected to grow more than a few elements, so it's an array-baked map, not a hash-baked one.
    // Moreover, it's being iterated linearly anyway in handleFrame(...).
    public static final Object2LongMap<Entity> SCHEDULED_ENTITIES = new Object2LongArrayMap<>(0);

    /**
     * Hidden entities mapped to their remaining resync ticks. These entities won't appear in the world as their
     * hitbox will be removed via {@link EntityMixin}. They are counted down in {@link #handleTick(Minecraft)}.
     *
     * @see EntityMixin
     * @see #handleTick(Minecraft)
     */
    // This map is not expected to grow more than a few elements, so it's an array-baked map, not a hash-baked one.
    // Moreover, it's being iterated linearly anyway in handleTick(...).
    public static final Object2IntMap<Entity> HIDDEN_ENTITIES = new Object2IntArrayMap<>(0);

    /**
     * Clipping anchors mapped. These anchors won't collide in the world as their hitbox will be removed via
     * {@link BlockStateBaseMixin}. They are checkin in {@link #handleTick(Minecraft)}.
     *
     * @see #handleTick(Minecraft)
     */
    // This map is not expected to grow more than a few elements, so it's an array-baked map, not a hash-baked one.
    // Moreover, it's being iterated linearly anyway in handleTick(...).
    public static final Set<BlockPos> CLIPPING_ANCHORS = new ObjectArraySet<>(0);

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
    public HCsCR() {
        throw new AssertionError("No instances.");
    }

    /**
     * Handles the client tick end. Handles {@link #CONFIG_BIND} and {@link #TOGGLE_BIND}.
     *
     * @param game Current game instance
     */
    public static void handleTick(Minecraft game) {
        // Validate.
        assert game != null : "Parameter 'game' is null.";

        // Get and push the profiler.
        ProfilerFiller profiler = HStonecutter.profilerOf(game); // Implicit NPE for 'game'
        profiler.push("hcscr:tick");

        // Keybinds.
        handleConfigBind(game, profiler);
        handleToggleBind(game, profiler);

        // Entities/anchors.
        handleHiddenEntities(game, profiler);
        handleClippingAnchors(game, profiler);

        // Pop the profiler.
        profiler.pop();
    }

    /**
     * Handles the client rendering frame. Removes redundant elements from {@link #SCHEDULED_ENTITIES}.
     *
     * @param profiler Client profiler instance
     */
    public static void handleFrame(ProfilerFiller profiler) {
        // Validate.
        assert profiler != null : "Parameter 'profiler' is null.";

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
                if (!LOGGER.isDebugEnabled()) continue;
                LOGGER.debug("HCsCR: Forgot hit entity. (now: {}, entity: {}, expiry: {})", now, entity, expiry);
                continue;
            }

            // Skip if entry is still in the world and hasn't reached the timeout.
            if ((expiry - now) >= 0) continue;

            // Remove from iterator.
            iterator.remove();

            // Hide or remove the entity.
            if (resync == 0) {
                HStonecutter.removeEntity(entity);
            } else {
                HIDDEN_ENTITIES.put(entity, resync);
            }

            // Log. (**DEBUG**)
            if (!LOGGER.isDebugEnabled()) continue;
            LOGGER.debug("HCsCR: Removed/hidden hit entity. (now: {}, entity: {}, expiry: {})", now, entity, expiry);
        }

        // Pop the profiler.
        profiler.pop();
    }

    /**
     * Handles the entity hit. Removes the entity client-side, if required.
     *
     * @param entity  Target hit entity
     * @param source  Hit damage source
     * @param amount  Amount of dealt damage
     * @return Whether the entity has been removed
     */
    public static boolean handleEntityHit(@NotNull Entity entity, @NotNull DamageSource source, float amount) {
        // Do NOT process hit if any of the following conditions is met:
        // - The mod is disabled via config or keybind.
        // - The damaged entity is already scheduled for removal.
        // - The amount of dealt damage is zero or below.
        // - The current level (world) is not client-side. (e.g. integrated server world)
        // - This entity type shouldn't be processed at all (e.g. any living entity) or by the current config (e.g. slime).
        // - The damaging entity is not a player.
        if (!HConfig.enable() || HStonecutter.isEntityRemoved(entity) || amount <= 0.0F ||
                !HStonecutter.levelOf(entity).isClientSide() || !HConfig.shouldProcess(entity) ||
                !(source.getEntity() instanceof Player)) return false;

        // Don't process player hits that deal zero damage, e.g. with the weakness effect.
        Player player = (Player) source.getEntity();
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
            long delay = HConfig.crystalsDelay();
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
        List<Entity> entities = HStonecutter.levelOf(entity).getEntities(entity, entity.getBoundingBox(), other -> {
            // Do NOT process hit if any of the following conditions is met:
            // - The damaged entity is already scheduled for removal.
            // - This entity type shouldn't be processed at all (e.g. any living entity) or by the current config (e.g. slime).
            // - The other entity is not fully contained inside the enveloping entity.
            if (HStonecutter.isEntityRemoved(other) || !HConfig.shouldProcess(other)) return false;
            AABB otherBox = other.getBoundingBox();
            return otherBox.minX >= entityBox.minX && otherBox.maxX <= entityBox.maxX &&
                    otherBox.minY >= entityBox.minY && otherBox.maxY <= entityBox.maxY &&
                    otherBox.minZ >= entityBox.minZ && otherBox.maxZ <= entityBox.maxZ;
        });

        // Remove/hide the entities, if there's no delay.
        long delay = HConfig.crystalsDelay();
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
     */
    private static void handleConfigBind(Minecraft game, ProfilerFiller profiler) {
        // Push the profiler.
        profiler.push("hcscr:config_bind");

        // Consume the bind.
        if (!CONFIG_BIND.consumeClick()) {
            // Pop, stop.
            profiler.pop();
            return;
        }

        // Log. (**TRACE**)
        LOGGER.trace("HCsCR: Config keybind was consumed, opening the config screen. (game: {}, keybind: {})", game, CONFIG_BIND);

        // Check the open screen.
        Screen prev = game.screen;
        if (prev != null) {
            // Log, pop, stop. (**DEBUG**)
            LOGGER.debug("HCsCR: Can't open the config screen via the keybind, screen is open. (game: {}, prev: {}, keybind: {})", game, prev, CONFIG_BIND);
            profiler.pop();
            return;
        }

        // Open the screen.
        HScreen screen = new HScreen(null);
        game.setScreen(screen); // Implicit NPE for 'game'

        // Log. (**DEBUG**)
        LOGGER.debug("HCsCR: Opened the config screen via the keybind. (game: {}, screen: {}, keybind: {})", game, screen, CONFIG_BIND);

        // Pop the profiler.
        profiler.pop();
    }

    /**
     * Handles the toggle keybind.
     *
     * @param game     Current game instance
     * @param profiler Game profiler
     */
    private static void handleToggleBind(Minecraft game, ProfilerFiller profiler) {
        // Push the profiler.
        profiler.push("hcscr:toggle_bind");

        // Consume the bind.
        if (!TOGGLE_BIND.consumeClick()) {
            // Pop, stop.
            profiler.pop();
            return;
        }

        // Log. (**TRACE**)
        LOGGER.trace("HCsCR: Toggle keybind was consumed, toggling the mode. (game: {}, keybind: {})", game, TOGGLE_BIND);

        // Toggle the mod.
        boolean newState = HConfig.toggle();

        // Show the bar, play the sound.
        game.gui.setOverlayMessage(HStonecutter.translate("hcscr." + newState) // Implicit NPE for 'game'
                .withStyle(newState ? ChatFormatting.GREEN : ChatFormatting.RED), /*rainbow=*/false);
        game.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING, newState ? 2.0F : 0.0F));

        // Log. (**DEBUG**)
        LOGGER.debug("HCsCR: Mod has been toggled via the keybind. (game: {}, newState: {}, keybind: {})", game, newState, TOGGLE_BIND);

        // Pop the profiler.
        profiler.pop();
    }

    /**
     * Handles the hidden entities.
     *
     * @param game     Current game instance
     * @param profiler Game profiler
     */
    private static void handleHiddenEntities(Minecraft game, ProfilerFiller profiler) {
        // Push the profiler.
        profiler.push("hcscr:hidden_entities");

        // Skip if no hidden entities.
        if (HIDDEN_ENTITIES.isEmpty()) {
            // Pop, stop.
            profiler.pop();
            return;
        }

        // Nuke all entities, if level is empty.
        if (game.level == null) {
            // Log. (**TRACE**)
            LOGGER.trace("HCscR: Level has been unloaded, nuking hidden entities...");

            // Clear.
            HIDDEN_ENTITIES.clear();

            // Log, pop, stop. (**DEBUG**)
            LOGGER.debug("HCscR: Level has been unloaded, nuked hidden entities.");
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
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("HCsCR: Ticking hidden entity... (entity: {}, left: {})", entity, left);
            }

            // Entity has been removed.
            if (HStonecutter.isEntityRemoved(entity)) {
                // Remove.
                iterator.remove();

                // Log, continue. (**DEBUG**)
                if (!LOGGER.isDebugEnabled()) continue;
                LOGGER.debug("HCscR: Removed hidden entity. (entity: {}, left: {})", entity, left);
                continue;
            }

            // Entity should be resynced.
            if (left <= 0) {
                // Remove.
                iterator.remove();

                // Log, continue. (**DEBUG**)
                if (!LOGGER.isDebugEnabled()) continue;
                LOGGER.debug("HCscR: Resynced hidden entity. (entity: {}, left: {})", entity, left);
                continue;
            }

            // Countdown.
            entry.setValue(left - 1);
        }

        // Pop the profiler.
        profiler.pop();
    }

    /**
     * Handles the clipping anchors.
     *
     * @param game     Current game instance
     * @param profiler Game profiler
     */
    private static void handleClippingAnchors(Minecraft game, ProfilerFiller profiler) {
        // Push the profiler.
        profiler.push("hcscr:clipping_anchors");

        // Skip if no clipping anchors.
        if (CLIPPING_ANCHORS.isEmpty()) {
            // Pop, stop.
            profiler.pop();
            return;
        }

        // Nuke all anchors, if level is empty.
        ClientLevel level = game.level;
        if (level == null) {
            // Log. (**TRACE**)
            LOGGER.trace("HCscR: Level has been unloaded, nuking clipping anchors...");

            // Clear.
            CLIPPING_ANCHORS.clear();

            // Log, pop, stop. (**DEBUG**)
            LOGGER.debug("HCscR: Level has been unloaded, nuked clipping anchors.");
            profiler.pop();
            return;
        }

        // Iterate.
        Iterator<BlockPos> iterator = CLIPPING_ANCHORS.iterator();
        while (iterator.hasNext()) {
            // Extract.
            BlockPos pos = iterator.next();

            // Log. (**TRACE**)
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("HCsCR: Ticking clipping anchor... (pos: {})", pos);
            }

            // Anchor is still there.
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.RESPAWN_ANCHOR)) continue;

            // Remove.
            iterator.remove();

            // Log, continue. (**DEBUG**)
            if (!LOGGER.isDebugEnabled()) continue;
            LOGGER.debug("HCscR: Removed clipping anchor. (pos: {}, state: {})", pos, state);
            continue;
        }

        // Pop the profiler.
        profiler.pop();
    }
}
