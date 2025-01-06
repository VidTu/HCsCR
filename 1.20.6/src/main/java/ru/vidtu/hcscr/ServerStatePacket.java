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
 */

package ru.vidtu.hcscr;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Mod server state custom payload packet.
 *
 * @author VidTu
 * @see #ENABLED
 * @see #DISABLED
 */
final class ServerStatePacket implements CustomPacketPayload {
    /**
     * Shared enabled state.
     */
    @NotNull
    private static final ServerStatePacket ENABLED = new ServerStatePacket(true);

    /**
     * Shared disabled state.
     */
    @NotNull
    private static final ServerStatePacket DISABLED = new ServerStatePacket(false);

    /**
     * Packet channel.
     */
    @NotNull
    private static final ResourceLocation PACKET_CHANNEL = new ResourceLocation("hcscr", "v1");

    /**
     * Packet type.
     */
    @NotNull
    private static final Type<ServerStatePacket> PACKET_TYPE = new Type<>(PACKET_CHANNEL);

    /**
     * Stream codec for this packet type.
     */
    @NotNull
    private static final StreamCodec<ByteBuf, ServerStatePacket> STREAM_CODEC = ByteBufCodecs.BOOL.map(enabled -> enabled ? ENABLED : DISABLED, packet -> packet.enabled);

    /**
     * Type for server state toasts.
     */
    @NotNull
    private static final SystemToast.SystemToastId SERVER_TOAST = new SystemToast.SystemToastId();

    /**
     * Whether the mod should be enabled, {@code true} by default.
     */
    private final boolean enabled;

    /**
     * Creates a new state.
     *
     * @param enabled Whether the mod should be enabled, {@code true} by default
     * @see #ENABLED
     * @see #DISABLED
     */
    @Contract(pure = true)
    private ServerStatePacket(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Initializes the packet.
     */
    static void init() {
        // Register the packet types.
        PayloadTypeRegistry.configurationS2C().register(PACKET_TYPE, STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(PACKET_TYPE, STREAM_CODEC);

        // Register the packet handlers.
        ClientConfigurationNetworking.registerGlobalReceiver(PACKET_TYPE, (payload, context) -> handlePacket(payload, context.client()));
        ClientPlayNetworking.registerGlobalReceiver(PACKET_TYPE, (payload, context) -> handlePacket(payload, context.client()));

        // Register the resetters.
        ClientConfigurationConnectionEvents.START.register((handler, client) -> resetState(client));
        ClientConfigurationConnectionEvents.DISCONNECT.register((handler, client) -> resetState(client));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> resetState(client));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> resetState(client));
    }

    @ApiStatus.Internal
    @Contract(pure = true)
    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ServerStatePacket that)) return false;
        return this.enabled == that.enabled;
    }

    @Contract(pure = true)
    @Override
    public int hashCode() {
        return this.enabled ? 208319 : 104287;
    }

    @Contract(pure = true)
    @Override
    @NotNull
    public String toString() {
        return "HCsCR/ServerStatePacket{" +
                "enabled=" + this.enabled +
                '}';
    }

    /**
     * Handles the packet.
     *
     * @param packet Packet to handle
     * @param client Minecraft client instance
     */
    private static void handlePacket(@NotNull ServerStatePacket packet, @NotNull Minecraft client) {
        // Change the state, skip if the state didn't change.
        boolean enabled = packet.enabled;
        boolean changed = HCsCR.serverEnabled(enabled);
        if (!changed) return;

        // Display the toast.
        client.execute(() -> SystemToast.addOrUpdate(client.getToasts(), SERVER_TOAST, HCsCRFabric.NAME,
                Component.translatable("hcscr.server." + enabled)
                        .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED)));
    }

    /**
     * Resets the state.
     *
     * @param client Minecraft client instance
     */
    private static void resetState(@NotNull Minecraft client) {
        // Reset the state, skip if the state didn't change.
        boolean changed = HCsCR.serverEnabledReset();
        if (!changed) return;

        // Display the toast.
        client.execute(() -> SystemToast.addOrUpdate(client.getToasts(), SERVER_TOAST, HCsCRFabric.NAME,
                Component.translatable("hcscr.server.reset").withStyle(ChatFormatting.GREEN)));
    }
}
