/*
 * Copyright (c) 2023 Offenderify
 * Copyright (c) 2023-2024 VidTu
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
 */

package ru.vidtu.hcscr;

import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
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
import org.lwjgl.glfw.GLFW;
import ru.vidtu.hcscr.config.Batching;
import ru.vidtu.hcscr.config.HConfig;

import java.util.List;
import java.util.function.Predicate;

/**
 * Main HCsCR class.
 *
 * @author Offenderify
 * @author VidTu
 */
public final class HCsCRFabric implements ClientModInitializer {
    /**
     * Component containing the mod name, "HCsCR".
     */
    @NotNull
    public static final Component NAME = Component.literal("HCsCR");

    /**
     * Logger for this class.
     */
    @NotNull
    private static final Logger LOGGER = LogManager.getLogger("HCsCR/HCsCRFabric");

    /**
     * Type for toggle bind toasts.
     */
    @NotNull
    private static final SystemToast.SystemToastIds TOGGLE_TOAST = SystemToast.SystemToastIds.valueOf("HCSCR$TOGGLE_TOAST");

    /**
     * Config keybind. Not bound by default.
     */
    @NotNull
    private static final KeyMapping CONFIG_BIND = new KeyMapping("hcscr.key.config", GLFW.GLFW_KEY_UNKNOWN, "hcscr.key.category");

    /**
     * Toggle keybind. Not bound by default.
     */
    @NotNull
    private static final KeyMapping TOGGLE_BIND = new KeyMapping("hcscr.key.toggle", GLFW.GLFW_KEY_UNKNOWN, "hcscr.key.category");

    /**
     * Entities scheduled for removal.
     */
    // This map is not expected to grow more than a few elements, so it's an array-baked map, not a hash-baked one.
    @NotNull
    private static final Object2LongMap<Entity> SCHEDULE_REMOVAL = new Object2LongArrayMap<>(0);

    @ApiStatus.Internal
    @Override
    public void onInitializeClient() {
        // Log.
        LOGGER.info("HCsCR: Initializing HCsCR...");

        // Load the config.
        HConfig.loadOrLog();

        // Register the network.
        ServerStatePacket.init();

        // Register the config bind.
        KeyBindingHelper.registerKeyBinding(CONFIG_BIND);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Don't do anything if didn't click yet or if there's an open screen.
            if (!CONFIG_BIND.consumeClick() || client.screen != null) return;

            // Open the config screen.
            client.setScreen(new ConfigScreen(null));
        });

        // Register the toggle bind.
        KeyBindingHelper.registerKeyBinding(TOGGLE_BIND);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Don't do anything if didn't click yet.
            if (!TOGGLE_BIND.consumeClick()) return;

            // Toggle.
            boolean newState = HCsCR.toggle();

            // Show the toast.
            if (!newState) {
                SystemToast.addOrUpdate(client.getToasts(), TOGGLE_TOAST, NAME,
                        Component.translatable("hcscr.toggle.false").withStyle(ChatFormatting.RED));
            } else if (!HCsCR.serverEnabled()) {
                SystemToast.addOrUpdate(client.getToasts(), TOGGLE_TOAST, NAME,
                        Component.translatable("hcscr.toggle.server").withStyle(ChatFormatting.GOLD));
            } else {
                SystemToast.addOrUpdate(client.getToasts(), TOGGLE_TOAST, NAME,
                        Component.translatable("hcscr.toggle.true").withStyle(ChatFormatting.GREEN));
            }
        });

        // Register the crystal remover.
        WorldRenderEvents.END.register(context -> {
            // Skip if there's no entities to remove.
            if (SCHEDULE_REMOVAL.isEmpty()) return;

            // Remove all entities that have expired or should be removed.
            ProfilerFiller profiler = context.profiler();
            profiler.push("hcscr_scheduled_removal");
            long now = System.nanoTime();
            SCHEDULE_REMOVAL.object2LongEntrySet().removeIf(entry -> {
                Entity entity = entry.getKey();
                if (!entity.isRemoved() && entry.getLongValue() >= now) return false;
                entity.discard();
                return true;
            });
            profiler.pop();
        });

        // Register the crystal remover cleaners.
        ClientConfigurationConnectionEvents.INIT.register((handler, client) -> clearScheduledRemovals());
        ClientConfigurationConnectionEvents.DISCONNECT.register((handler, client) -> clearScheduledRemovals());
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> clearScheduledRemovals());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> clearScheduledRemovals());

        // Done.
        LOGGER.info("HCsCR: HCsCR is ready to remove 'em crystals.");
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
        // - The mod is disabled by the current server.
        // - The damaged entity is already scheduled for removal.
        // - The amount of dealt damage is zero or below.
        // - This entity type shouldn't be processed at all (e.g. any living entity) or by the current config (e.g. slime).
        // - The current level (world) is not client-side. (e.g. integrated server world)
        // - The damaging entity is not a player.
        // - The damaged entity is invulnerable.
        // - The damaged entity has already been processed. (by checked set)
        if (!HConfig.enabled || !HCsCR.serverEnabled() || entity.isRemoved() || amount <= 0.0F ||
                !shouldProcessEntityType(entity) || !entity.level().isClientSide() ||
                !(source.getEntity() instanceof Player player) || entity.isInvulnerableTo(source)) return false;

        // Don't process player hits that deal zero damage, e.g. with the weakness effect.
        AttributeMap map = player.getAttributes();
        for (MobEffectInstance instance : player.getActiveEffects()) {
            instance.getEffect().addAttributeModifiers(map, instance.getAmplifier());
        }
        double attributeAmount = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        for (MobEffectInstance instance : player.getActiveEffects()) {
            instance.getEffect().removeAttributeModifiers(map);
        }
        if (attributeAmount <= 0.0D) return false;

        // Fast-remove one entity, if batching is disabled.
        Batching batching = HConfig.batching;
        if (batching == Batching.DISABLED) {
            // Just remove the entity, if there's no delay.
            int delay = HConfig.delay;
            if (delay <= 0) {
                entity.discard();
                return true;
            }

            // Schedule the removal, if the delay exists.
            SCHEDULE_REMOVAL.putIfAbsent(entity, System.nanoTime() + delay * 1_000_000L);
            return true;
        }

        // Create the filter based on batching mode.
        AABB box = entity.getBoundingBox();
        Predicate<? super Entity> filter = switch (batching) {
            case INTERSECTING -> (other -> (entity == other || !other.isRemoved() && shouldProcessEntityType(other) && !other.isInvulnerableTo(source)));
            case CONTAINING -> (other -> (entity == other || !other.isRemoved() && shouldProcessEntityType(other) && !other.isInvulnerableTo(source) && contains(box, other.getBoundingBox())));
            case CONTAINING_CONTAINED -> (other -> {
                if (entity == other) return true;
                if (other.isRemoved() || !shouldProcessEntityType(other) || other.isInvulnerableTo(source)) return false;
                AABB otherBox = other.getBoundingBox();
                return contains(box, otherBox) || contains(otherBox, box);
            });
            default -> (ignored -> (entity == ignored)); // Shouldn't happen really.
        };

        // Get filtered entities, skip if none.
        List<Entity> entities = entity.level().getEntities((Entity) null, entity.getBoundingBox(), filter);
        if (!entities.isEmpty()) return false;

        // Just remove the entities, if there's no delay.
        int delay = HConfig.delay;
        if (delay <= 0) {
            entity.discard();
            for (Entity other : entities) {
                other.discard();
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
     * Clears the entities scheduled for removal.
     */
    public static void clearScheduledRemovals() {
        SCHEDULE_REMOVAL.clear();
    }

    /**
     * Gets whether the entity type should be removed by the current config.
     *
     * @param entity Target damaged entity
     * @return Whether the entity should be removed on hit
     * @apiNote This checks only for entity types and disregards factors like {@link HConfig#enabled} or {@link HCsCR#serverEnabled()}
     */
    @Contract(pure = true)
    private static boolean shouldProcessEntityType(@NotNull Entity entity) {
        if (entity instanceof EndCrystal) return HConfig.removeAnchors;
        if (entity instanceof Slime slime) return HConfig.removeSlimes && slime.isInvisible();
        if (entity instanceof Interaction) return HConfig.removeInteractions;
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
