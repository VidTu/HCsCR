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
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import ru.vidtu.hcscr.HCsCR;
import ru.vidtu.hcscr.config.HConfig;

//? if >=1.20.6 {
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//?} elif >=1.20.2 {
/*import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
*///?} else {
/*import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
*///?}

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
        final long start = System.nanoTime();
        LOGGER.info(HCsCR.HCSCR_MARKER, "HCsCR: Loading... (platform: fabric)");

        // Load the config.
        HConfig.load();

        // Register the networking.
        //? if >=1.20.6 {
        final CustomPacketPayload.Type<CustomPacketPayload> type = new CustomPacketPayload.Type<>(HStonecutter.CHANNEL_IDENTIFIER);
        final CustomPacketPayload instance = new CustomPacketPayload() {
            @Contract(pure = true)
            @Override
            public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
                return type;
            }

            @Contract(pure = true)
            @Override
            public String toString() {
                return "HCsCR/HFabric$CustomPacketPayload{}";
            }
        };
        final StreamCodec<FriendlyByteBuf, CustomPacketPayload> codec = StreamCodec.of((final FriendlyByteBuf output, final CustomPacketPayload value) -> {
            // Throw unconditionally.
            throw new IllegalStateException("HCsCR: Client-side mod should not send/encode this packet. (output: " + output + ", value: " + value + ')');
        }, (final FriendlyByteBuf input) -> {
            // Validate.
            assert input != null : "HCsCR: Parameter 'input' is null.";

            // Skip bytes. (whatever they are)
            input.skipBytes(input.readableBytes()); // Implicit NPE for 'input'
            return instance;
        });
        PayloadTypeRegistry.configurationS2C().register(type, codec);
        PayloadTypeRegistry.playS2C().register(type, codec);
        ClientConfigurationNetworking.registerGlobalReceiver(type, (final CustomPacketPayload payload, final ClientConfigurationNetworking.Context context) -> {
            // Validate.
            assert context != null : "HCsCR: Parameter 'context' is null. (payload:" + payload + ')';
            final PacketSender sender = context.responseSender(); // Implicit NPE for 'context'
            assert sender != null : "HCsCR: Response sender is null. (payload: " + payload + ", context: " + context + ')';

            // Close the connection.
            sender.disconnect(HStonecutter.translate("hcscr.false")); // Implicit NPE for 'sender'
        });
        ClientPlayNetworking.registerGlobalReceiver(type, (final CustomPacketPayload payload, final ClientPlayNetworking.Context context) -> {
            // Validate.
            assert context != null : "HCsCR: Parameter 'context' is null. (payload:" + payload + ')';
            final PacketSender sender = context.responseSender(); // Implicit NPE for 'context'
            assert sender != null : "HCsCR: Response sender is null. (payload: " + payload + ", context: " + context + ')';

            // Close the connection.
            sender.disconnect(HStonecutter.translate("hcscr.false")); // Implicit NPE for 'sender'
        });
        //?} elif >=1.20.2 {
        /*ClientConfigurationNetworking.registerGlobalReceiver(HStonecutter.CHANNEL_IDENTIFIER, (final Minecraft client, final ClientConfigurationPacketListenerImpl handler, final FriendlyByteBuf buf, final PacketSender responseSender) -> {
            // Validate.
            assert client != null : "HCsCR: Parameter 'client' is null. (handler: " + handler + ", buf: " + buf + ", responseSender: " + responseSender + ')';
            assert handler != null : "HCsCR: Parameter 'handler' is null. (client: " + client + ", buf: " + buf + ", responseSender: " + responseSender + ')';
            assert buf != null : "HCsCR: Parameter 'buf' is null. (client: " + client + ", handler: " + handler + ", responseSender: " + responseSender + ')';
            assert responseSender != null : "HCsCR: Parameter 'responseSender' is null. (client: " + client + ", handler: " + handler + ", buf: " + buf + ')';

            // Close the connection.
            handler.onDisconnect(HStonecutter.translate("hcscr.false")); // Implicit NPE for 'handler'
        });
        ClientPlayNetworking.registerGlobalReceiver(HStonecutter.CHANNEL_IDENTIFIER, (final Minecraft client, final ClientPacketListener handler, final FriendlyByteBuf buf, final PacketSender responseSender) -> {
            // Validate.
            assert client != null : "HCsCR: Parameter 'client' is null. (handler: " + handler + ", buf: " + buf + ", responseSender: " + responseSender + ')';
            assert handler != null : "HCsCR: Parameter 'handler' is null. (client: " + client + ", buf: " + buf + ", responseSender: " + responseSender + ')';
            assert buf != null : "HCsCR: Parameter 'buf' is null. (client: " + client + ", handler: " + handler + ", responseSender: " + responseSender + ')';
            assert responseSender != null : "HCsCR: Parameter 'responseSender' is null. (client: " + client + ", handler: " + handler + ", buf: " + buf + ')';

            // Close the connection.
            handler.onDisconnect(HStonecutter.translate("hcscr.false")); // Implicit NPE for 'handler'
        });
        *///?} else {
        /*ClientPlayNetworking.registerGlobalReceiver(HStonecutter.CHANNEL_IDENTIFIER, (final Minecraft client, final ClientPacketListener handler, final FriendlyByteBuf buf, final PacketSender responseSender) -> {
            // Validate.
            assert client != null : "HCsCR: Parameter 'client' is null. (handler: " + handler + ", buf: " + buf + ", responseSender: " + responseSender + ')';
            assert handler != null : "HCsCR: Parameter 'handler' is null. (client: " + client + ", buf: " + buf + ", responseSender: " + responseSender + ')';
            assert buf != null : "HCsCR: Parameter 'buf' is null. (client: " + client + ", handler: " + handler + ", responseSender: " + responseSender + ')';
            assert responseSender != null : "HCsCR: Parameter 'responseSender' is null. (client: " + client + ", handler: " + handler + ", buf: " + buf + ')';

            // Close the connection.
            handler.onDisconnect(HStonecutter.translate("hcscr.false")); // Implicit NPE for 'handler'
        });
        *///?}

        // Register the binds.
        KeyBindingHelper.registerKeyBinding(HCsCR.CONFIG_BIND);
        KeyBindingHelper.registerKeyBinding(HCsCR.TOGGLE_BIND);

        // Register the client tick end handler.
        ClientTickEvents.END_CLIENT_TICK.register(HCsCR::handleClientTickEnd);

        // Client game loop handling is done via the MinecraftMixin. (Fabric only for now)
        // Config screen handling (ModMenu entrypoint) is in the HModMenu class.

        // Done.
        LOGGER.info(HCsCR.HCSCR_MARKER, "HCsCR: Ready to remove 'em crystals. ({} ms)", (System.nanoTime() - start) / HCsCR.NANOS_IN_MS);
    }

    @Contract(pure = true)
    @Override
    public String toString() {
        return "HCsCR/HFabric{}";
    }
}
//?}
