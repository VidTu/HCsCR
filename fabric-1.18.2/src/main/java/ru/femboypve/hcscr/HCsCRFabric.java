/*
 * Copyright (c) 2023 Offenderify, VidTu
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

package ru.femboypve.hcscr;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;

/**
 * Main HCsCR class.
 *
 * @author Offenderify
 * @author VidTu
 */
public final class HCsCRFabric implements ClientModInitializer {
    private static final ResourceLocation LOCATION = new ResourceLocation("hcscr", "haram");
    private static final KeyMapping TOGGLE_BIND = new KeyMapping("hcscr.key.toggle", GLFW.GLFW_KEY_UNKNOWN, "hcscr.key.category");
    private static final Map<Entity, Long> SCHEDULE_REMOVAL = new WeakHashMap<>();

    @Override
    public void onInitializeClient() {
        HCsCR.loadConfig(FabricLoader.getInstance().getConfigDir());
        KeyBindingHelper.registerKeyBinding(TOGGLE_BIND);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!TOGGLE_BIND.consumeClick()) return;
            HCsCR.enabled = !HCsCR.enabled;
            if (!HCsCR.enabled) {
                SystemToast.addOrUpdate(client.getToasts(), SystemToast.SystemToastIds.NARRATOR_TOGGLE,
                        new TextComponent("HaramClientsideCrystalRemover"),
                        new TranslatableComponent("hcscr.toggle.disabled").withStyle(ChatFormatting.RED));
            } else if (HCsCR.serverDisabled) {
                SystemToast.addOrUpdate(client.getToasts(), SystemToast.SystemToastIds.NARRATOR_TOGGLE,
                        new TextComponent("HaramClientsideCrystalRemover"),
                        new TranslatableComponent("hcscr.toggle.enabledBut").withStyle(ChatFormatting.GOLD));
            } else {
                SystemToast.addOrUpdate(client.getToasts(), SystemToast.SystemToastIds.NARRATOR_TOGGLE,
                        new TextComponent("HaramClientsideCrystalRemover"),
                        new TranslatableComponent("hcscr.toggle.enabled").withStyle(ChatFormatting.GREEN));
            }
            HCsCR.saveConfig(FabricLoader.getInstance().getConfigDir());
        });
        ClientPlayNetworking.registerGlobalReceiver(LOCATION, (client, handler, buf, responseSender) -> {
            HCsCR.serverDisabled = buf.readBoolean();
            client.execute(() -> SystemToast.addOrUpdate(client.getToasts(), SystemToast.SystemToastIds.TUTORIAL_HINT,
                    new TextComponent("HaramClientsideCrystalRemover"),
                    new TranslatableComponent(HCsCR.serverDisabled ? "hcscr.server.disabled" : "hcscr.server.enabled")));
        });
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> HCsCR.serverDisabled = false);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> HCsCR.serverDisabled = false);
        Consumer<Minecraft> tick = client -> {
            if (client.level == null || HCsCR.delay == 0) {
                if (SCHEDULE_REMOVAL.isEmpty()) return;
                SCHEDULE_REMOVAL.clear();
                return;
            }
            SCHEDULE_REMOVAL.entrySet().removeIf(en -> {
                Entity entity = en.getKey();
                if (entity.isRemoved()) return true;
                long time = en.getValue();
                if (System.nanoTime() > time) {
                    entity.discard();
                    return true;
                }
                return false;
            });
        };
        if (HCsCR.absolutePrecision) {
            WorldRenderEvents.END.register(context -> tick.accept(Minecraft.getInstance()));
            HCsCR.LOG.info("HCsCR is using absolute precision! (using: rendering ticks)");
        } else {
            ClientTickEvents.END_CLIENT_TICK.register(tick::accept);
            HCsCR.LOG.info("HCsCR is NOT using absolute precision! (using: game ticks)");
        }
        HCsCR.LOG.info("HCsCR is ready to remove any end crystals you have!");
    }

    /**
     * Removes the entity client-side, if required.
     *
     * @param entity Target entity
     * @param source Damage source
     * @param amount Amount of damage
     * @return Whether the entity has been removed
     */
    public static boolean removeClientSide(Entity entity, DamageSource source, float amount) {
        if (!HCsCR.enabled || HCsCR.serverDisabled || amount <= 0F || entity.isRemoved()
                || !entity.level.isClientSide() || !(source.getEntity() instanceof Player player)
                || entity.isInvulnerableTo(source)) return false;
        if (entity instanceof EndCrystal) {
            if (!HCsCR.removeCrystals) return false;
        } else if (entity instanceof Slime slime) {
            if (!HCsCR.removeSlimes || !slime.isInvisible()) return false;
        } else {
            return false;
        }
        if (player.getAttributeValue(Attributes.ATTACK_DAMAGE) <= 0) return false;
        AttributeMap map = player.getAttributes();
        for (MobEffectInstance instance : player.getActiveEffects()) {
            instance.getEffect().addAttributeModifiers(player, map, instance.getAmplifier());
        }
        amount = Math.min(amount, (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE));
        for (MobEffectInstance instance : player.getActiveEffects()) {
            instance.getEffect().removeAttributeModifiers(player, map, instance.getAmplifier());
        }
        if (amount < 0) return false;
        if (HCsCR.delay > 0) {
            SCHEDULE_REMOVAL.put(entity, System.nanoTime() + HCsCR.delay * 1_000_000L);
            return true;
        }
        entity.discard();
        return true;
    }
}
