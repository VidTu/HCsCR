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

package ru.offenderify.hcscr;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main mod class.
 *
 * @author Offenderify
 */
public class HCsCR implements ClientModInitializer {
    public static final Logger LOG = LoggerFactory.getLogger("HCSCR");
    public static final ResourceLocation LOCATION = new ResourceLocation("hcscr", "haram");

    // "It's not homo if it's a femboy." - VidTu, 2022.
    // "It's not a hack if it can be disabled by the server." - VidTu, 2023.
    public static boolean disabledByCurrentServer;

    @Override
    public void onInitializeClient() {
        LOG.info("I'm ready to remove any end crystals you have!");
        ClientPlayNetworking.registerGlobalReceiver(LOCATION, (client, handler, buf, responseSender) -> {
            disabledByCurrentServer = buf.readBoolean();
            client.execute(() -> client.getToasts().addToast(SystemToast.multiline(client, SystemToast.SystemToastIds.TUTORIAL_HINT,
                    Component.literal("HaramClientsideCrystalRemover"),
                    Component.translatable(disabledByCurrentServer ? "hcscr.server.disabled" : "hcscr.server.enabled"))));
        });
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> disabledByCurrentServer = false);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> disabledByCurrentServer = false);
    }
}
