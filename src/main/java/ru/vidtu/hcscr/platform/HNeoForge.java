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

//? if neoforge {
/*package ru.vidtu.hcscr.platform;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import ru.vidtu.hcscr.HCsCR;
import ru.vidtu.hcscr.config.HConfig;
import ru.vidtu.hcscr.config.HScreen;

/^*
 * Main HCsCR class for NeoForge.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see HCsCR
 ^/
@ApiStatus.Internal
//? if >=1.20.6 {
@Mod(value = "hcscr", dist = Dist.CLIENT)
//?} else
/^@Mod("hcscr")^/
@NullMarked
public final class HNeoForge {
    /^*
     * Logger for this class.
     ^/
    private static final Logger LOGGER = LogManager.getLogger("HCsCR/HNeoForge");

    /^*
     * Creates and loads a new mod.
     *
     * @param dist      Current physical side
     * @param container Mod container
     * @param bus       Mod-specific event bus
     * @apiNote Do not call, called by NeoForge
     ^/
    public HNeoForge(Dist dist, ModContainer container, IEventBus bus) {
        // Validate.
        assert dist != null : "HCsCR: Parameter 'dist' is null. (container: " + container + ", bus: " + bus + ", mod: " + this + ')';
        assert container != null : "HCsCR: Parameter 'container' is null. (dist: " + dist + ", bus: " + bus + ", mod: " + this + ')';
        assert bus != null : "HCsCR: Parameter 'bus' is null. (dist: " + dist + ", container: " + container + ", mod: " + this + ')';

        // Log.
        long start = System.nanoTime();
        LOGGER.info(HCsCR.HCSCR_MARKER, "HCsCR: Loading... (platform: neoforge)");

        // Not sure how long the Forge does have the "clientSideOnly" field in the TOML,
        // so I'll do an additional exception check here.
        if (dist != Dist.CLIENT) { // Implicit null-UOE for 'dist'
            throw new UnsupportedOperationException("HCsCR: You've tried to load the HCsCR mod on a server. This won't work.");
        }

        // Load the config.
        HConfig.load();

        // Register the networking.
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
                return "HCsCR/HNeoForge$CustomPacketPayload{}";
            }
        };
        bus.addListener(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent.class, event -> event.registrar("hcscr").optional().commonToClient(type, net.minecraft.network.codec.StreamCodec.of((buf, payload) -> {}, buf -> { // Implicit NPE for 'bus'
            buf.skipBytes(buf.readableBytes());
            return instance;
        }), (payload, context) -> context.disconnect(HStonecutter.translate("hcscr.false"))));
        //?} else if >=1.20.4 {
        /^var instance = new net.minecraft.network.protocol.common.custom.CustomPacketPayload() {
            @Contract(pure = true)
            @Override
            public void write(net.minecraft.network.FriendlyByteBuf buf) {

            }

            @Contract(pure = true)
            @Override
            public net.minecraft.resources.ResourceLocation id() {
                return HStonecutter.CHANNEL_IDENTIFIER;
            }

            @Contract(pure = true)
            @Override
            public String toString() {
                return "HCsCR/HNeoForge$CustomPacketPayload{}";
            }
        };
        bus.addListener(net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent.class, event -> event.registrar("hcscr").optional().common(HStonecutter.CHANNEL_IDENTIFIER, buf -> { // Implicit NPE for 'bus'
            buf.skipBytes(buf.readableBytes());
            return instance;
        }, (payload, context) -> context.replyHandler().disconnect(HStonecutter.translate("hcscr.false"))));
        ^///?} else {
        /^net.neoforged.neoforge.network.NetworkRegistry.newEventChannel(HStonecutter.CHANNEL_IDENTIFIER, () -> "hcscr", version -> true, version -> true).addListener(event -> {
            if (event.getPayload() == null) return;
            var src = event.getSource();
            if (src.getDirection().getReceptionSide() != net.neoforged.fml.LogicalSide.CLIENT) return;
            src.getNetworkManager().disconnect(HStonecutter.translate("hcscr.false"));
            src.setPacketHandled(true);
        });
        ^///?}

        // Register the binds.
        bus.addListener(RegisterKeyMappingsEvent.class, event -> { // Implicit NPE for 'bus'
            event.register(HCsCR.CONFIG_BIND);
            event.register(HCsCR.TOGGLE_BIND);
        });
        Minecraft client = Minecraft.getInstance();
        //? if >=1.20.6 {
        NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.client.event.ClientTickEvent.Post.class, event -> HCsCR.handleGameTick(client));
        //?} else {
        /^NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.TickEvent.ClientTickEvent.class, event -> {
            if (event.phase != net.neoforged.neoforge.event.TickEvent.Phase.END) return;
            HCsCR.handleGameTick(client);
        });
        ^///?}

        // Register the scheduled remover.
        //? if >=1.20.6 {
        NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.client.event.RenderFrameEvent.Post.class, event -> HCsCR.handleFrameTick(client));
        //?} else {
        /^NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.TickEvent.RenderTickEvent.class, event -> {
            if (event.phase != net.neoforged.neoforge.event.TickEvent.Phase.END) return;
            HCsCR.handleFrameTick(client);
        });
        ^///?}

        // Register the config screen.
        //? if >=1.20.6 {
        container.registerExtensionPoint(net.neoforged.neoforge.client.gui.IConfigScreenFactory.class, (modOrGame, parent) -> new HScreen(parent)); // Implicit NPE for 'container'
        //?} else {
        /^container.registerExtensionPoint(net.neoforged.neoforge.client.ConfigScreenHandler.ConfigScreenFactory.class, () -> new net.neoforged.neoforge.client.ConfigScreenHandler.ConfigScreenFactory((game, screen) -> new HScreen(screen))); // Implicit NPE for 'container'
        container.registerExtensionPoint(net.neoforged.fml.IExtensionPoint.DisplayTest.class, () -> new net.neoforged.fml.IExtensionPoint.DisplayTest(() -> net.neoforged.fml.IExtensionPoint.DisplayTest.IGNORESERVERONLY, (version, fromServer) -> true));
        ^///?}

        // Done.
        LOGGER.info(HCsCR.HCSCR_MARKER, "HCsCR: Ready to remove 'em crystals. ({} ms)", (System.nanoTime() - start) / 1_000_000L);
    }

    @Contract(pure = true)
    @Override
    public String toString() {
        return "HCsCR/HNeoForge{}";
    }
}
*///?}
