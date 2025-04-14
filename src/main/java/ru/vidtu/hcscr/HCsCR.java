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

import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import org.lwjgl.glfw.GLFW;
import ru.vidtu.hcscr.config.Batching;
import ru.vidtu.hcscr.config.ConfigScreen;
import ru.vidtu.hcscr.config.HConfig;
import ru.vidtu.hcscr.platform.HStonecutter;

import java.util.List;
import java.util.function.Predicate;

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
     * Logger for this class.
     */
    private static final Logger LOGGER = LogManager.getLogger("HCsCR");

    /**
     * Entities scheduled for removal mapped to their time of creation in units of {@link System#nanoTime()}.
     */
    // This map is not expected to grow more than a few elements, so it's an array-baked map, not a hash-baked one.
    private static final Object2LongMap<Entity> SCHEDULE_REMOVAL = new Object2LongArrayMap<>(0);

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
        profiler.push("hcscr:keybinds");

        // Handle "Open the config screen" keybind.
        if (CONFIG_BIND.consumeClick()) {
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
            ConfigScreen screen = new ConfigScreen(null);
            game.setScreen(screen); // Implicit NPE for 'game'

            // Log, pop, stop. (**DEBUG**)
            LOGGER.debug("HCsCR: Opened the config screen via the keybind. (game: {}, screen: {}, keybind: {})", game, screen, CONFIG_BIND);
            profiler.pop();
            return;
        }

        // Handle "Toggle the mod" keybind.
        if (!TOGGLE_BIND.consumeClick()) {
            // Pop, stop.
            profiler.pop();
            return;
        }

        // Log. (**TRACE**)
        LOGGER.trace("HCsCR: Toggle keybind was consumed, toggling the mode. (game: {}, keybind: {})", game, TOGGLE_BIND);

        // Toggle the mod.
        boolean newState = (HConfig.enabled = !HConfig.enabled);

        // Save the config.
        HConfig.saveOrLog();

        // Show the bar, play the sound.
        game.gui.setOverlayMessage(HStonecutter.translate("hcscr." + newState) // Implicit NPE for 'game'
                .withStyle(newState ? ChatFormatting.GREEN : ChatFormatting.RED), /*rainbow=*/false);
        game.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING, newState ? 2.0F : 0.0F));

        // Log. (**DEBUG**)
        LOGGER.debug("HCsCR: Mod has been toggle via the keybind. (game: {}, newState: {}, keybind: {})", game, newState, TOGGLE_BIND);

        // Pop.
        profiler.pop();
    }

    /**
     * Handles the client rendering frame. Removes redundant elements from {@link #SCHEDULE_REMOVAL}.
     *
     * @param profiler Client profiler instance
     */
    public static void handleFrame(ProfilerFiller profiler) {
        // Validate.
        assert profiler != null : "Parameter 'profiler' is null.";

        // Push the profiler.
        profiler.push("hcscr:scheduled_removal"); // Implicit NPE for 'profiler'

        // Skip if there's no entities to remove.
        if (SCHEDULE_REMOVAL.isEmpty()) {
            // Pop, stop.
            profiler.pop();
            return;
        }

        // Remove all entities that have expired or no longer in the world.
        long now = System.nanoTime();
        long timeout = (HConfig.delay * 1_000_000L);
        ObjectIterator<Object2LongMap.Entry<Entity>> iterator = SCHEDULE_REMOVAL.object2LongEntrySet().iterator();
        while (iterator.hasNext()) {
            // Extract.
            Object2LongMap.Entry<Entity> entry = iterator.next();
            Entity entity = entry.getKey();
            long start = entry.getLongValue();

            // Skip if entry is still in the world and hasn't reached the timeout.
            if (!HStonecutter.isEntityRemoved(entity) && ((now - start) < timeout)) continue;

            // Log. (**TRACE**)
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("HCsCR: Removing entity scheduled for removal... (now: {}, timeout: {}, entity: {}, start: {}, map: {})", now, timeout, entity, start, SCHEDULE_REMOVAL);
            }

            // Remove the entity from the world and from the map.
            HStonecutter.removeEntity(entity);
            iterator.remove();

            // Log. (**DEBUG**)
            if (!LOGGER.isDebugEnabled()) continue;
            LOGGER.trace("HCsCR: Removed entity scheduled for removal. (now: {}, timeout: {}, entity: {}, start: {}, map: {})", now, timeout, entity, start, SCHEDULE_REMOVAL);
        }

        // Pop the profile.
        profiler.pop();
    }

    /**
     * Handles the world switch. Clears the {@link #SCHEDULE_REMOVAL} map.
     *
     * @param game Current game instance
     */
    public static void handleWorldSwitch(Minecraft game) {
        // Get and push the profiler.
        ProfilerFiller profiler = HStonecutter.profilerOf(game); // Implicit NPE for 'game'
        profiler.push("hcscr:clear_data");

        // Log. (**TRACE**)
        LOGGER.trace("HCsCR: Clearing data... (game: {}, map: {})", game, SCHEDULE_REMOVAL);

        // Clear the map.
        SCHEDULE_REMOVAL.clear();

        // Log. (**DEBUG**)
        LOGGER.debug("HCsCR: Cleared data. (game: {}, map: {})", game, SCHEDULE_REMOVAL);

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
        // - This entity type shouldn't be processed at all (e.g. any living entity) or by the current config (e.g. slime).
        // - The current level (world) is not client-side. (e.g. integrated server world)
        // - The damaging entity is not a player.
        // - The damaged entity is invulnerable.
        // - The damaged entity has already been processed. (by checked set)
        if (!HConfig.enabled || HStonecutter.isEntityRemoved(entity) || amount <= 0.0F ||
                !shouldProcessEntityType(entity) || !HStonecutter.levelOf(entity).isClientSide() ||
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

        // Fast-remove one entity, if batching is disabled.
        Batching batching = HConfig.batching;
        if (batching == Batching.DISABLED) {
            // Just remove the entity, if there's no delay.
            int delay = HConfig.delay;
            if (delay <= 0) {
                HStonecutter.removeEntity(entity);
                return true;
            }

            // Schedule the removal, if the delay exists.
            SCHEDULE_REMOVAL.putIfAbsent(entity, System.nanoTime() + delay * 1_000_000L);
            return true;
        }

        // Create the filter based on batching mode.
        AABB box = entity.getBoundingBox();
        Predicate<? super Entity> filter;
        switch (batching) {
            case INTERSECTING:
                filter = (other -> (entity == other || !HStonecutter.isEntityRemoved(other) && shouldProcessEntityType(other)));
                break;
            case CONTAINING:
                filter = (other -> (entity == other || !HStonecutter.isEntityRemoved(other) && shouldProcessEntityType(other) && contains(box, other.getBoundingBox())));
                break;
            case CONTAINING_CONTAINED:
                filter = (other -> {
                    if (entity == other) return true;
                    if (HStonecutter.isEntityRemoved(other) || !shouldProcessEntityType(other)) return false;
                    AABB otherBox = other.getBoundingBox();
                    return contains(box, otherBox) || contains(otherBox, box);
                });
                break;
            default: // Shouldn't happen really.
                filter = (other -> (entity == other));
                break;
        }

        // Get filtered entities, skip if none.
        List<Entity> entities = HStonecutter.levelOf(entity).getEntities((Entity) null, entity.getBoundingBox(), filter);
        if (entities.isEmpty()) return false;

        // Just remove the entities, if there's no delay.
        int delay = HConfig.delay;
        if (delay <= 0) {
            HStonecutter.removeEntity(entity);
            for (Entity other : entities) {
                HStonecutter.removeEntity(other);
            }
            return true;
        }

        // Schedule the removal, if the delay exists.
        long removeAt = System.nanoTime() + delay * 1_000_000L;
        SCHEDULE_REMOVAL.putIfAbsent(entity, removeAt);
        for (Entity other : entities) {
            SCHEDULE_REMOVAL.putIfAbsent(other, removeAt);
        }
        return false;
    }

    /**
     * Gets whether the entity type should be removed by the current config.
     *
     * @param entity Target damaged entity
     * @return Whether the entity should be removed on hit
     * @apiNote This checks only for entity types and disregards factors like {@link HConfig#enabled}
     */
    @Contract(pure = true)
    private static boolean shouldProcessEntityType(@NotNull Entity entity) {
        if (entity instanceof EndCrystal) return HConfig.removeCrystals;
        if (entity instanceof Slime) return HConfig.removeSlimes && entity.isInvisible();
        //? if >=1.19.4
        if (entity instanceof net.minecraft.world.entity.Interaction) return HConfig.removeInteractions;
        return false;
    }

    /**
     * Checks whether the first (@{code containing}) bounding box fully contains the second ({@code contained}) box.
     *
     * @param containing Containing box
     * @param contained  Contained box
     * @return Whether the first bounding box fully contains the second box
     */
    @Contract(pure = true)
    private static boolean contains(@NotNull AABB containing, @NotNull AABB contained) {
        return contained.minX >= containing.minX && contained.minY >= containing.minY &&
                contained.minZ >= containing.minZ && contained.maxX <= containing.maxX &&
                contained.maxY <= containing.maxY && contained.maxZ <= containing.maxZ;
    }
}
