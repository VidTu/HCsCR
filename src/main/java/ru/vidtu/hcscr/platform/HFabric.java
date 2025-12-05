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

//? if fabric {
package ru.vidtu.hcscr.platform;

import com.google.errorprone.annotations.DoNotCall;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import ru.vidtu.hcscr.HCsCR;
import ru.vidtu.hcscr.config.HConfig;

/**
 * Main HCsCR class for Fabric.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see HCsCR
 * @see HModMenu
 */
@ApiStatus.Internal
@NullMarked
public final class HFabric implements ClientModInitializer {
    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = LogManager.getLogger("HCsCR/HFabric");

    /**
     * Creates a new mod.
     *
     * @apiNote Do not call, called by Fabric
     */
    @Contract(pure = true)
    public HFabric() {
        // Empty.
    }

    /**
     * Initializes the client.
     *
     * @apiNote Do not call, called by Fabric
     */
    @DoNotCall("Called by Fabric")
    @Override
    public void onInitializeClient() {
        // Log.
        long start = System.nanoTime();
        LOGGER.info(HCsCR.HCSCR_MARKER, "HCsCR: Loading... (platform: fabric)");

        // Load the config.
        HConfig.load();

        // Register the network.
        //? if >=1.20.6 {
        var type = new net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<>(HStonecutter.CHANNEL_IDENTIFIER);
        var instance = new net.minecraft.network.protocol.common.custom.CustomPacketPayload() {
            @Contract(pure = true)
            @Override
            public Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type() {
                return type;
            }

            @Contract(pure = true)
            @Override
            public String toString() {
                return "HCsCR/HFabric$CustomPacketPayload{}";
            }
        };
        var codec = net.minecraft.network.codec.StreamCodec.<net.minecraft.network.FriendlyByteBuf, net.minecraft.network.protocol.common.custom.CustomPacketPayload>of((output, value) -> {}, input -> {
            input.skipBytes(input.readableBytes()); // Implicit NPE for 'input'
            return instance;
        });
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.configurationS2C().register(type, codec);
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(type, codec);
        net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking.registerGlobalReceiver(type, (payload, context) -> context.responseSender().disconnect(HStonecutter.translate("hcscr.false")));
        ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> context.responseSender().disconnect(HStonecutter.translate("hcscr.false")));
        //?} elif >=1.20.2 {
        /*net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking.registerGlobalReceiver(HStonecutter.CHANNEL_IDENTIFIER, (client, handler, buf, responseSender) -> handler.onDisconnect(HStonecutter.translate("hcscr.false")));
        ClientPlayNetworking.registerGlobalReceiver(HStonecutter.CHANNEL_IDENTIFIER, (client, handler, buf, responseSender) -> handler.getConnection().disconnect(HStonecutter.translate("hcscr.false")));
        *///?} else
        /*ClientPlayNetworking.registerGlobalReceiver(HStonecutter.CHANNEL_IDENTIFIER, (client, handler, buf, responseSender) -> handler.getConnection().disconnect(HStonecutter.translate("hcscr.false")));*/

        // Register the binds.
        KeyBindingHelper.registerKeyBinding(HCsCR.CONFIG_BIND);
        KeyBindingHelper.registerKeyBinding(HCsCR.TOGGLE_BIND);

        // Register the client tick end handler.
        ClientTickEvents.END_CLIENT_TICK.register(HCsCR::handleClientTickEnd);

        // Client game loop handling is done via the MinecraftMixin. (Fabric only for now)
        // Config screen handling (ModMenu entrypoint) is in the HModMenu class.

        // Done.
        LOGGER.info(HCsCR.HCSCR_MARKER, "HCsCR: Ready to remove 'em crystals. ({} ms)", (System.nanoTime() - start) / 1_000_000L);
    }

    @Contract(pure = true)
    @Override
    public String toString() {
        return "HCsCR/HFabric{}";
    }
}
//?}
