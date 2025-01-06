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

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Mod server state custom payload packet.
 *
 * @author VidTu
 */
final class ServerStatePacketHandler {
    /**
     * Packet channel.
     */
    @NotNull
    private static final ResourceLocation PACKET_CHANNEL = new ResourceLocation("hcscr", "v1");

    /**
     * Type for server state toasts.
     */
    @NotNull
    private static final SystemToast.SystemToastIds SERVER_TOAST = SystemToast.SystemToastIds.valueOf("HCSCR$SERVER_TOAST");

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     */
    @Contract(value = "-> fail", pure = true)
    private ServerStatePacketHandler() {
        throw new AssertionError("No instances.");
    }

    /**
     * Initializes the packet.
     */
    static void init() {
        // Register the packet handler.
        ClientPlayNetworking.registerGlobalReceiver(PACKET_CHANNEL, (client, handler, buf, responseSender) -> handlePacket(buf.readBoolean()));

        // Register the resetters.
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> resetState(client));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> resetState(client));
    }

    /**
     * Handles the packet.
     *
     * @param enabled Whether the mod should be enabled, {@code true} by default
     */
    private static void handlePacket(boolean enabled) {
        // Change the state, skip if the state didn't change.
        boolean changed = HCsCR.serverEnabled(enabled);
        if (!changed) return;

        // Display the toast.
        Minecraft client = Minecraft.getInstance();
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
