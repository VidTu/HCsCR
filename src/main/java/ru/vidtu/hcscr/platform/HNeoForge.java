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
import net.minecraft.client.gui.screens.Screen;

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

//? if >=1.20.6 {
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
//?} else if >=1.20.4 {
/^import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.IExtensionPoint;
import net.neoforged.neoforge.client.ConfigScreenHandler;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IReplyHandler;
^///?} else {
/^import net.minecraft.network.Connection;
import net.neoforged.fml.IExtensionPoint;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.client.ConfigScreenHandler;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.NetworkRegistry;
^///?}

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
//?} else {
/^@Mod("hcscr")
^///?}
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
    public HNeoForge(final Dist dist, final ModContainer container, final IEventBus bus) {
        // Validate.
        assert dist != null : "HCsCR: Parameter 'dist' is null. (container: " + container + ", bus: " + bus + ", mod: " + this + ')';
        assert container != null : "HCsCR: Parameter 'container' is null. (dist: " + dist + ", bus: " + bus + ", mod: " + this + ')';
        assert bus != null : "HCsCR: Parameter 'bus' is null. (dist: " + dist + ", container: " + container + ", mod: " + this + ')';

        // Log.
        final long start = System.nanoTime();
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
        bus.addListener(RegisterPayloadHandlersEvent.class, (final RegisterPayloadHandlersEvent event) -> { // Implicit NPE for 'bus'
            // Validate.
            assert event != null : "HCsCR: Parameter 'event' is null.";

            // Prepare.
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
                    return "HCsCR/HNeoForge$CustomPacketPayload{}";
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

            // Register.
            event.registrar("hcscr").optional().commonToClient(type, codec, (final CustomPacketPayload payload, final IPayloadContext context) -> { // Implicit NPE for 'event'
                // Validate.
                assert payload != null : "HCsCR: Parameter 'payload' is null. (context: " + context + ')';
                assert context != null : "HCsCR: Parameter 'context' is null. (payload: " + payload + ')';

                // Close the connection.
                context.disconnect(HStonecutter.translate("hcscr.false")); // Implicit NPE for 'context'
            });
        });
        //?} elif >=1.20.4 {
        /^bus.addListener(RegisterPayloadHandlerEvent.class, (final RegisterPayloadHandlerEvent event) -> { // Implicit NPE for 'bus'
            // Validate.
            assert event != null : "HCsCR: Parameter 'event' is null.";

            // Prepare.
            CustomPacketPayload instance = new CustomPacketPayload() {
                @Contract(value = "_ -> fail", pure = true)
                @Override
                public void write(FriendlyByteBuf output) {
                    // Throw unconditionally.
                    throw new IllegalStateException("HCsCR: Client-side mod should not send/encode this packet. (output: " + output + ", packet: " + this + ')');
                }

                @Contract(pure = true)
                @Override
                public ResourceLocation id() {
                    return HStonecutter.CHANNEL_IDENTIFIER;
                }

                @Contract(pure = true)
                @Override
                public String toString() {
                    return "HCsCR/HNeoForge$CustomPacketPayload{}";
                }
            };

            // Register.
            event.registrar("hcscr").optional().common(HStonecutter.CHANNEL_IDENTIFIER, (final FriendlyByteBuf input) -> { // Implicit NPE for 'event'
                // Validate.
                assert input != null : "HCsCR: Parameter 'input' is null.";

                // Skip bytes. (whatever they are)
                input.skipBytes(input.readableBytes()); // Implicit NPE for 'input'
                return instance;
            }, (final CustomPacketPayload payload, final IPayloadContext context) -> {
                // Validate.
                assert payload != null : "HCsCR: Parameter 'payload' is null. (context: " + context + ')';
                assert context != null : "HCsCR: Parameter 'context' is null. (payload: " + payload + ')';
                final IReplyHandler handler = context.replyHandler(); // Implicit NPE for 'context'
                assert handler != null : "HCsCR: Reply handler is null. (payload: " + payload + ", context: " + context + ')';

                // Close the connection.
                handler.disconnect(HStonecutter.translate("hcscr.false")); // Implicit NPE for 'handler'
            });
        });
        ^///?} else {
        /^NetworkRegistry.newEventChannel(HStonecutter.CHANNEL_IDENTIFIER, () -> "hcscr", (final String version) -> true, (final String version) -> true).addListener((final NetworkEvent event) -> {
            // Validate.
            assert event != null : "HCsCR: Parameter 'event' is null.";

            // Skip if there's no payload. (e.g., a channel registration)
            if (event.getPayload() == null) return; // Implicit NPE for 'event'

            // Check if coming from the server.
            final NetworkEvent.Context source = event.getSource();
            assert source != null : "HCsCR: Source is null. (event: " + event + ')';
            if (source.getDirection().getReceptionSide() != LogicalSide.CLIENT) return; // Implicit NPE for 'source'
            source.setPacketHandled(true);

            // Close the connection.
            final Connection connection = source.getNetworkManager();
            assert connection != null : "HCsCR: Connection is null. (event: " + event + ", source: " + source + ')';
            connection.disconnect(HStonecutter.translate("hcscr.false")); // Implicit NPE for 'connection'
        });
        ^///?}

        // Register the binds.
        bus.addListener(RegisterKeyMappingsEvent.class, (final RegisterKeyMappingsEvent event) -> { // Implicit NPE for 'bus'
            // Validate.
            assert event != null : "HCsCR: Parameter 'event' is null.";

            // Register.
            //? if >=1.21.10 {
            event.registerCategory(HStonecutter.KEY_CATEGORY);
            //?}
            event.register(HCsCR.CONFIG_BIND);
            event.register(HCsCR.TOGGLE_BIND);
        });

        // Register the client tick end handler.
        //? if >=1.20.6 {
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, (final ClientTickEvent.Post event) -> {
            // Validate.
            assert event != null : "HCsCR: Parameter 'event' is null.";

            // Handle.
            HCsCR.handleClientTickEnd(Minecraft.getInstance());
        });
        //?} else {
        /^NeoForge.EVENT_BUS.addListener(TickEvent.ClientTickEvent.class, (final TickEvent.ClientTickEvent event) -> {
            // Validate.
            assert event != null : "HCsCR: Parameter 'event' is null.";

            // Handle.
            if (event.phase != TickEvent.Phase.END) return; // Implicit NPE for 'event'
            HCsCR.handleClientTickEnd(Minecraft.getInstance());
        });
        ^///?}

        // Register the client game loop handler.
        //? if >=1.20.6 {
        NeoForge.EVENT_BUS.addListener(RenderFrameEvent.Post.class, (final RenderFrameEvent.Post event) -> {
            // Validate.
            assert event != null : "HCsCR: Parameter 'event' is null.";

            // Handle.
            HCsCR.handleClientMainLoop(Minecraft.getInstance());
        });
        //?} else {
        /^NeoForge.EVENT_BUS.addListener(TickEvent.RenderTickEvent.class, (final TickEvent.RenderTickEvent event) -> {
            // Validate.
            assert event != null : "HCsCR: Parameter 'event' is null.";

            // Handle.
            if (event.phase != TickEvent.Phase.END) return; // Implicit NPE for 'event'
            HCsCR.handleClientMainLoop(Minecraft.getInstance());
        });
        ^///?}

        // Register the config screen.
        //? if >=1.21.1 {
        container.registerExtensionPoint(IConfigScreenFactory.class, (final ModContainer innerContainer, final Screen modListScreen) -> { // Implicit NPE for 'container'
            // Validate.
            assert innerContainer != null : "HCsCR: Parameter 'innerContainer' is null. (innerContainer: " + innerContainer + ')';

            // Return.
            return new HScreen(modListScreen);
        });
        //?} else if >=1.20.6 {
        /^container.registerExtensionPoint(IConfigScreenFactory.class, (final Minecraft mcClient, final Screen modListScreen) -> { // Implicit NPE for 'container'
            // Validate.
            assert mcClient != null : "HCsCR: Parameter 'mcClient' is null. (mcClient: " + mcClient + ')';

            // Return.
            return new HScreen(modListScreen);
        });
        ^///?} else {
        /^container.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((final Minecraft mcClient, final Screen modsScreen) -> { // Implicit NPE for 'container'
            // Validate.
            assert mcClient != null : "HCsCR: Parameter 'mcClient' is null. (modsScreen: " + modsScreen + ')';

            // Return.
            return new HScreen(modsScreen);
        }));
        container.registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> IExtensionPoint.DisplayTest.IGNORESERVERONLY, (final String remoteVersion, final Boolean isFromServer) -> true));
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
