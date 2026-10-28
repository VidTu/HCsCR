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

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import ru.vidtu.hcscr.compile.Variables;
import ru.vidtu.hcscr.config.Config;
import ru.vidtu.hcscr.config.CrystalMode;

import java.util.List;

/**
 * Main entity logic for the mod.
 *
 * @author VidTu
 * @apiNote Internal use only
 */
@ApiStatus.Internal
@NullMarked
public final class Entities {
    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private Entities() {
        if (Variables.DEBUG_ASSERTS) {
            throw new AssertionError("HCsCR: No instances.");
        }
    }

    /**
     * Handles the entity hit by a player. Removes the entity client-side or promotes to {@link ScheduledEntities}, if required.
     *
     * @param player Player attacking the entity
     * @param entity Entity being attacked by a player
     * @param source Attack source (inaccurate if invoked on the client)
     * @param amount Total amount of damage done to the entity (inaccurate if invoked on the client)
     * @return Whether the entity has been removed
     * @see Config#enable()
     * @see Config#shouldProcess(Player, Entity)
     * @see Config#crystals()
     * @see Config#crystalsResync()
     * @see Config#crystalsDelay()
     */
    @SuppressWarnings({"deprecation", "RedundantSuppression"}) // <- Forge 1.16.5 for Entity.removed.
    public static boolean hit(final Player player, final Entity entity, final DamageSource source, final float amount) {
        // Validate.
        if (Variables.DEBUG_ASSERTS) {
            assert (player != null) : "HCsCR: Parameter 'player' is null. (entity: " + entity + ", source: " + source + ", amount: " + amount + ')';
            assert (entity != null) : "HCsCR: Parameter 'entity' is null. (player: " + player + ", source: " + source + ", amount: " + amount + ')';
            assert (source != null) : "HCsCR: Parameter 'source' is null. (player: " + player + ", entity: " + entity + ", amount: " + amount + ')';
            // Note: This is not a runtime check and it could be affected by malfunctioning
            // servers setting invalid attribute values (NaN or Infinities), but vanilla
            // should never behave like that so we're leaving this check in debug env.
            // To avoid issues from NaN values we use reverse zero comparison in damage
            // checks. That is: "!(amount > 0.0)" instead of "amount <= 0.0".
            assert (Float.isFinite(amount)) : "HCsCR: Parameter 'amount' is not finite. (player: " + player + ", entity: " + entity + ", source: " + source + ", amount: " + amount + ')';
        }

        // Do NOT process hit if any of the following conditions is met:
        // - The amount of dealt damage is zero or less. (or NaN)
        // - The damaged entity is already scheduled for removal.
        // - The current level (world) is not client-side. (e.g., integrated server world)
        // - The mod is disabled via config or keybind.
        // - This entity type shouldn't be processed at all (e.g., any living entity) or by the current config (e.g., slime).
        //~ if >=1.17.1 'removed' -> 'isRemoved()' {
            //~ if >=1.20.1 '.level' -> '.level()' {
        if (!(amount > 0.0f) || entity.isRemoved() || !entity.level().isClientSide() || // Implicit NPE for 'entity'
                !Config.enable() || !Config.shouldProcess(player, entity)) return false;

        // Validate.
        if (Variables.DEBUG_ASSERTS) {
            //noinspection ObjectEquality // <- Should be the same reference.
            final Entity sourceEntity = source.getEntity();
            assert (player == sourceEntity) : "HCsCR: Invalid source. (player: " + player + ", entity: " + entity + ", source: " + source + ", amount: " + amount + ", sourceEntity: " + sourceEntity + ')';
            final Entity sourceDirectEntity = source.getEntity();
            assert (player == sourceDirectEntity) : "HCsCR: Indirect attack. (player: " + player + ", entity: " + entity + ", source: " + source + ", amount: " + amount + ", sourceDirectEntity: " + sourceDirectEntity + ')';
            final Minecraft client = Minecraft.getInstance();
            assert (client.isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", player: " + player + ", entity: " + entity + ", source: " + source + ", amount: " + amount + ')';
            final LocalPlayer mcPlayer = client.player;
            assert (player == mcPlayer) : "HCsCR: Invalid player. (player: " + player + ", entity: " + entity + ", source: " + source + ", amount: " + amount + ", mcPlayer: " + mcPlayer + ')';
        }

        // Don't process player hits that deal zero damage with effects. (e.g., with the weakness effect.)
        final double effectsAmount = calculateDamageWithEffects(player);
        if (!(effectsAmount > 0.0d)) return false;

        // Fast-remove one crystal entity, if the mode is DIRECT.
        final CrystalMode mode = Config.crystals();
        if (mode == CrystalMode.DIRECT) {
            // Remove/hide the entity, if there's no delay.
            final int delay = Config.crystalsDelay();
            if (delay == 0) {
                // Remove the entity instantly, if there's no resync.
                final int resync = Config.crystalsResync();
                if (resync == 0) {
                    //$ remove_entity 'entity'
                    entity.discard();
                    return true;
                }

                // Hide the entity.
                HiddenEntities.hideFor(entity, resync);
                return true;
            }

            // Schedule the removal of the entity, if the delay exists.
            ScheduledEntities.scheduleAt(entity, System.nanoTime() + delay);
            return true;
        }

        // Get the enveloped entities.
        final AABB entityBox = entity.getBoundingBox();
        final List<Entity> entities = entity.level().getEntities(entity, entity.getBoundingBox(), (final Entity other) -> {
            //~}
            // Validate.
            if (Variables.DEBUG_ASSERTS) {
                assert (other != null) : "HCsCR: Parameter 'other' is null. (entity: " + entity + ')';
            }

            // Do NOT process hit if any of the following conditions is met:
            // - The damaged entity is already scheduled for removal.
            // - This entity type shouldn't be processed at all (e.g., any living entity) or by the current config (e.g., slime).
            // - The other entity is not fully contained inside the enveloping entity.
            if (other.isRemoved() || !Config.shouldProcess(player, other)) return false;
        //~}
            final AABB otherBox = other.getBoundingBox();
            return ((otherBox.minX >= entityBox.minX) && (otherBox.maxX <= entityBox.maxX) &&
                    (otherBox.minY >= entityBox.minY) && (otherBox.maxY <= entityBox.maxY) &&
                    (otherBox.minZ >= entityBox.minZ) && (otherBox.maxZ <= entityBox.maxZ));
        });

        // Remove/hide the entities, if there's no delay.
        final int delay = Config.crystalsDelay();
        if (delay == 0) {
            // Remove the entities instantly, if there's no resync.
            final int resync = Config.crystalsResync();
            if (resync == 0) {
                //$ remove_entity 'entity'
                entity.discard();
                for (final Entity other : entities) {
                    //$ remove_entity 'other'
                    other.discard();
                }
                return true;
            }

            // Hide the entity.
            HiddenEntities.hideFor(entity, resync);
            for (final Entity other : entities) {
                HiddenEntities.hideFor(other, resync);
            }
            return true;
        }

        // Schedule the removal of the entities, if the delay exists.
        final long removeAt = (System.nanoTime() + delay);
        ScheduledEntities.scheduleAt(entity, removeAt);
        for (final Entity other : entities) {
            ScheduledEntities.scheduleAt(other, removeAt);
        }
        return true;
    }

    /**
     * Calculates damage dealt by a player with all the attribute modifiers from potion effects applied.
     * <p>
     * Redundant in newer versions (after most attributes are applied on the server), left
     * for backwards compatibility with older versions and protocol hacks (e.g., ViaVersion).
     *
     * @param player Attacking player to calculate the damage of
     * @return Amount of damage from the player with attribute modifiers from potion effects applied
     */
    @Contract(pure = true)
    private static double calculateDamageWithEffects(final Player player) {
        // Validate.
        if (Variables.DEBUG_ASSERTS) {
            assert (player != null) : "HCsCR: Parameter 'player' is null.";
            final Minecraft client = Minecraft.getInstance();
            assert (client.isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", player: " + player + ')';
            final LocalPlayer mcPlayer = client.player;
            assert (player == mcPlayer) : "HCsCR: Invalid player. (player: " + player + ", mcPlayer: " + mcPlayer + ')';
        }

        // Obtain the attribute map.
        final AttributeMap map = player.getAttributes(); // Implicit NPE for 'player'

        // Apply effect attribute modifiers.
        for (final MobEffectInstance instance : player.getActiveEffects()) {
            //? if >=1.20.6 {
            instance.getEffect().value().addAttributeModifiers(map, instance.getAmplifier()); // Implicit NPE for 'effect', 'map'
            //?} elif >=1.20.2 {
            /*instance.getEffect().addAttributeModifiers(map, instance.getAmplifier()); // Implicit NPE for 'effect', 'map'
            *///?} else {
            /*instance.getEffect().addAttributeModifiers(player, map, instance.getAmplifier()); // Implicit NPE for 'player', 'effect', 'map'
            *///?}
        }

        // Calculate the damage.
        final double damage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);

        // Validate finite.
        if (Variables.DEBUG_ASSERTS) {
            // Note: This is not a runtime check and it could be affected by malfunctioning
            // servers setting invalid attribute values (NaN or Infinities), but vanilla
            // should never behave like that so we're leaving this check in debug env.
            // To avoid issues from NaN values we use reverse zero comparison in damage
            // checks. That is: "!(amount > 0.0)" instead of "amount <= 0.0".
            assert (Double.isFinite(damage)) : "HCsCR: Damage is non-finite. (player: " + player + ", damage: " + damage + ')';
        }

        // Unapply effect attribute modifiers.
        for (final MobEffectInstance instance : player.getActiveEffects()) {
            //? if >=1.20.6 {
            instance.getEffect().value().removeAttributeModifiers(map); // Implicit NPE for 'effect', 'map'
            //?} elif >=1.20.2 {
            /*instance.getEffect().removeAttributeModifiers(map); // Implicit NPE for 'effect', 'map'
            *///?} else {
            /*instance.getEffect().removeAttributeModifiers(player, map, instance.getAmplifier()); // Implicit NPE for 'player', 'effect', 'map'
            *///?}
        }

        // Return the damage.
        return damage;
    }
}
