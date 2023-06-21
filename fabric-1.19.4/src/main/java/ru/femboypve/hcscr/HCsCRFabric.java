/*
 * Copyright (c) 2023 Offenderify
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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Main mod class.
 *
 * @author Offenderify
 */
public class HCsCRFabric implements ClientModInitializer {
    /**
     * Main mod logger named <code>HCSCR</code>.
     */
    public static final Logger LOG = LoggerFactory.getLogger("HCsCR");

    /**
     * Mod's resource location <code>hcscr:haram</code> to inform the server.
     */
    public static final ResourceLocation LOCATION = new ResourceLocation("hcscr", "haram");

    /**
     * Keybinding for mod toggling.
     */
    public static final KeyMapping TOGGLE_BIND = new KeyMapping("hcscr.key.toggle", GLFW.GLFW_KEY_UNKNOWN, "hcscr.key.category");

    /**
     * Shared GSON instance.
     */
    public static final Gson GSON = new Gson();

    /**
     * Is the mod enabled via configuration? Default value: <code>true</code>
     */
    public static boolean enabled = true;

    /**
     * Is this mod disabled by current server via sending packets on {@link #LOCATION}? Default value: <code>false</code>
     */
    public static boolean disabledByCurrentServer;

    @Override
    public void onInitializeClient() {
        loadConfig();
        KeyBindingHelper.registerKeyBinding(TOGGLE_BIND);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (TOGGLE_BIND.consumeClick()) {
                enabled = !enabled;
                if (!enabled) {
                    client.gui.setOverlayMessage(Component.translatable("hcscr.toggle.disabled")
                            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD), false);
                } else if (disabledByCurrentServer) {
                    client.gui.setOverlayMessage(Component.translatable("hcscr.toggle.enabledBut")
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
                } else {
                    client.gui.setOverlayMessage(Component.translatable("hcscr.toggle.enabled")
                            .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), false);
                }
                saveConfig();
            }
        });

        // "It's not homo if it's a femboy." - VidTu, 2022.
        // "It's not a hack if it can be disabled by the server." - VidTu, 2023.
        ClientPlayNetworking.registerGlobalReceiver(LOCATION, (client, handler, buf, responseSender) -> {
            disabledByCurrentServer = buf.readBoolean();
            client.execute(() -> client.getToasts().addToast(SystemToast.multiline(client, SystemToast.SystemToastIds.TUTORIAL_HINT,
                    Component.literal("HaramClientsideCrystalRemover"),
                    Component.translatable(disabledByCurrentServer ? "hcscr.server.disabled" : "hcscr.server.enabled"))));
        });
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> disabledByCurrentServer = false);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> disabledByCurrentServer = false);

        LOG.info("I'm ready to remove any end crystals you have!");
    }

    /**
     * Checks if the crystal can be removed client-side.
     *
     * @param crystal Target crystal
     * @param source  Damage source of crystal (first arg from {@link EndCrystal#hurt(DamageSource, float)})
     * @param amount  Amount of damage (second arg from {@link EndCrystal#hurt(DamageSource, float)}))
     * @return <code>true</code> if explosion all conditions are met and the mod is {@link #enabled} and not {@link #disabledByCurrentServer}
     * @apiNote This method may be (and designed to be) modified by any third-party mods via bytecode manipulation (e.g. Mixins).
     */
    public static boolean explodesClientSide(EndCrystal crystal, DamageSource source, float amount) {
        if (!HCsCRFabric.enabled || HCsCRFabric.disabledByCurrentServer || !crystal.level.isClientSide() || crystal.isRemoved() ||
                crystal.isInvulnerableTo(source) || source.getEntity() instanceof EnderDragon || amount <= 0) return false;

        // Hrukjang Studios Moment.
        if (source.getEntity() instanceof Player) {
            Player player = (Player) source.getEntity();
            if (player.getAttributeValue(Attributes.ATTACK_DAMAGE) <= 0) return false;
            AttributeMap map = player.getAttributes();
            for (MobEffectInstance instance : player.getActiveEffects()) {
                instance.getEffect().addAttributeModifiers(player, map, instance.getAmplifier());
            }
            amount = Math.min(amount, (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE));
            for (MobEffectInstance instance : player.getActiveEffects()) {
                instance.getEffect().removeAttributeModifiers(player, map, instance.getAmplifier());
            }
            return amount > 0;
        }

        return true;
    }

    /**
     * Loads mod config.
     */
    public static void loadConfig() {
        try {
            Path file = FabricLoader.getInstance().getConfigDir().resolve("hcscr.json");
            if (!Files.isRegularFile(file)) return;
            JsonObject json = GSON.fromJson(new String(Files.readAllBytes(file), StandardCharsets.UTF_8), JsonObject.class);
            enabled = json.get("enabled").getAsBoolean();
        } catch (Exception e) {
            LOG.warn("Unable to load HCsCR config.", e);
        }
    }

    /**
     * Saves mod config.
     */
    public static void saveConfig() {
        try {
            Path file = FabricLoader.getInstance().getConfigDir().resolve("hcscr.json");
            Files.createDirectories(file.getParent());
            JsonObject json = new JsonObject();
            json.addProperty("enabled", enabled);
            Files.write(file, GSON.toJson(json).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            LOG.warn("Unable to load HCsCR config.", e);
        }
    }
}
