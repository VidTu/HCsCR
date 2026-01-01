/*
 * HCsCR is a third-party mod for Minecraft Java Edition
 * that allows removing the end crystals faster.
 *
 * Copyright (c) 2023 Offenderify
 * Copyright (c) 2023-2026 VidTu
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

//? if forge {
/*package ru.vidtu.hcscr.platform;

import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import ru.vidtu.hcscr.HCsCR;
import ru.vidtu.hcscr.config.HConfig;
import ru.vidtu.hcscr.config.HScreen;

//? if hacky_neoforge {
/^import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import java.util.function.Supplier;
^///?} elif >=1.21.8 {
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.eventbus.api.bus.EventBus;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
//?} elif >=1.20.2 {
/^import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
^///?} elif >=1.21.8 {
/^import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.eventbus.api.bus.EventBus;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
^///?} elif >=1.20.2 {
/^import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
^///?} elif >=1.19.2 {
/^import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import java.util.function.Supplier;
^///?} elif >=1.18.2 {
/^import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import java.util.function.Supplier;
^///?} elif >=1.17.1 {
/^import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmlclient.ConfigGuiHandler;
import net.minecraftforge.fmlclient.registry.ClientRegistry;
import net.minecraftforge.fmllegacy.network.FMLNetworkConstants;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import java.util.function.Supplier;
^///?} else {
/^import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import org.apache.commons.lang3.tuple.Pair;
import java.util.function.Supplier;
^///?}

/^*
 * Main HCsCR class for Forge.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see HCsCR
 ^/
@ApiStatus.Internal
@Mod("hcscr")
@NullMarked
public final class HForge {
    /^*
     * Logger for this class.
     ^/
    private static final Logger LOGGER = LogManager.getLogger("HCsCR/HForge");

    //? if hacky_neoforge {
    /^/^¹*
     * Creates and loads a new mod.
     *
     * @param container Current mod container
     * @param bus       Loading event bus
     * @apiNote Do not call, called by Forge
     ¹^/
    public HForge(final ModContainer container, final IEventBus bus) {
        // Validate.
        if (HCompile.DEBUG_ASSERTS) {
            assert (container != null) : "HCsCR: Parameter 'container' is null. (bus: " + bus + ", mod: " + this + ')';
            assert (bus != null) : "HCsCR: Parameter 'bus' is null. (container: " + container + ", mod: " + this + ')';
        }
    ^///?} elif >=1.19.2 && (!1.20.2) {
    /^*
     * Creates and loads a new mod.
     *
     * @param ctx Loading context
     * @apiNote Do not call, called by Forge
     ^/
    public HForge(final FMLJavaModLoadingContext ctx) {
        // Validate.
        if (HCompile.DEBUG_ASSERTS) {
            assert (ctx != null) : "HCsCR: Parameter 'ctx' is null. (mod: " + this + ')';
        }
    //?} else {
    /^/^¹*
     * Creates a new mod.
     *
     * @apiNote Do not call, called by Forge
     ¹^/
    public HForge() {
        // Get the context.
        FMLJavaModLoadingContext ctx = FMLJavaModLoadingContext.get();
    ^///?}
        // Log.
        final long start = System.nanoTime();
        final String modVersion = HForge.class.getPackage().getImplementationVersion();
        LOGGER.info(HCsCR.HCSCR_MARKER, "HCsCR: Loading... (platform: forge, modVersion: {})", modVersion);
        LOGGER.info(HCsCR.HCSCR_MARKER, "HCsCR: This build of the mod has " +
                "asserts " + (HCompile.DEBUG_ASSERTS ? "ON" : "OFF") +
                ", logs " + (HCompile.DEBUG_LOGS ? "ON" : "OFF") +
                ", profiler " + (HCompile.DEBUG_PROFILER ? "ON" : "OFF") +
                '.');

        // Not sure how long the Forge does have the "clientSideOnly" field in the TOML,
        // so I'll do an additional exception check here.
        if (FMLEnvironment.dist != Dist.CLIENT) {
            throw new UnsupportedOperationException("HCsCR: You've tried to load the HCsCR mod on a server. This won't work.");
        }

        // Load the config.
        HConfig.load();

        // Register the networking.
        //? if >=1.20.2 {
        ChannelBuilder.named(HStonecutter.CHANNEL_IDENTIFIER).networkProtocolVersion(1).acceptedVersions((final Channel.VersionTest.Status status, final int version) -> true).eventNetworkChannel().addListener((final CustomPayloadEvent event) -> {
            // Validate.
            if (HCompile.DEBUG_ASSERTS) {
                assert (event != null) : "HCsCR: Parameter 'event' is null.";
            }

            // Skip if there's no payload. (e.g., a channel registration)
            if (event.getPayload() == null) return; // Implicit NPE for 'event'

            // Check if coming from the server.
            final CustomPayloadEvent.Context source = event.getSource();
            if (HCompile.DEBUG_ASSERTS) {
                assert (source != null) : "HCsCR: Source is null. (event: " + event + ')';
            }
            if (!source.isClientSide()) return; // Implicit NPE for 'source'
            source.setPacketHandled(true);

            // Close the connection.
            final Connection connection = source.getConnection();
            if (HCompile.DEBUG_ASSERTS) {
                assert (connection != null) : "HCsCR: Connection is null. (event: " + event + ", source: " + source + ')';
            }
            connection.disconnect(HStonecutter.translate("hcscr.false")); // Implicit NPE for 'connection'
        });
        //?} else {
        /^NetworkRegistry.newEventChannel(HStonecutter.CHANNEL_IDENTIFIER, () -> "hcscr", (final String version) -> true, (final String version) -> true).addListener((final NetworkEvent event) -> {
            // Validate.
            if (HCompile.DEBUG_ASSERTS) {
                assert (event != null) : "HCsCR: Parameter 'event' is null.";
            }

            // Skip if there's no payload. (e.g., a channel registration)
            if (event.getPayload() == null) return; // Implicit NPE for 'event'

            // Check if coming from the server.
            final Supplier<NetworkEvent.Context> sourceGetter = event.getSource();
            if (HCompile.DEBUG_ASSERTS) {
                assert (sourceGetter != null) : "HCsCR: Source getter is null. (event: " + event + ')';
            }
            final NetworkEvent.Context source = sourceGetter.get(); // Implicit NPE for 'sourceGetter'
            if (HCompile.DEBUG_ASSERTS) {
                assert (source != null) : "HCsCR: Source is null. (event: " + event + ", sourceGetter: " + sourceGetter + ')';
            }
            if (source.getDirection().getReceptionSide() != LogicalSide.CLIENT) return; // Implicit NPE for 'source'
            source.setPacketHandled(true);

            // Close the connection.
            final Connection connection = source.getNetworkManager();
            if (HCompile.DEBUG_ASSERTS) {
                assert (connection != null) : "HCsCR: Connection is null. (event: " + event + ", sourceGetter: " + sourceGetter + ", source: " + source + ')';
            }
            connection.disconnect(HStonecutter.translate("hcscr.false")); // Implicit NPE for 'connection'
        });
        ^///?}

        // Register the binds.
        //? if >=1.21.10 {
        /^final EventBus<RegisterKeyMappingsEvent> bus = RegisterKeyMappingsEvent.BUS;
        ^///?} elif >=1.21.8 {
        final EventBus<RegisterKeyMappingsEvent> bus = RegisterKeyMappingsEvent.getBus(ctx.getModBusGroup()); // Implicit NPE for 'ctx'
        //?} elif !hacky_neoforge {
        /^final IEventBus bus = ctx.getModEventBus(); // Implicit NPE for 'ctx'
        ^///?}
        //? if >=1.19.2 {
        bus.addListener((final RegisterKeyMappingsEvent event) -> { // Implicit NPE for 'bus'
            // Validate.
            if (HCompile.DEBUG_ASSERTS) {
                assert (event != null) : "HCsCR: Parameter 'event' is null.";
            }

            // Register.
            event.register(HCsCR.CONFIG_BIND); // Implicit NPE for 'event'
            event.register(HCsCR.TOGGLE_BIND);
        });
        //?} else {
        /^bus.addListener((final FMLClientSetupEvent event) -> {
            // Validate.
            if (HCompile.DEBUG_ASSERTS) {
                assert (event != null) : "HCsCR: Parameter 'event' is null.";
            }

            // Register.
            ClientRegistry.registerKeyBinding(HCsCR.CONFIG_BIND);
            ClientRegistry.registerKeyBinding(HCsCR.TOGGLE_BIND);
        });
        ^///?}

        // Register the client tick end handler.
        //? if >=1.21.8 {
        TickEvent.ClientTickEvent.Post.BUS.addListener((final TickEvent.ClientTickEvent.Post event) -> {
            // Validate.
            if (HCompile.DEBUG_ASSERTS) {
                assert (event != null) : "HCsCR: Parameter 'event' is null.";
            }

            // Handle.
            HCsCR.handleClientTickEnd(Minecraft.getInstance());
        });
        //?} elif >=1.20.4 {
        /^MinecraftForge.EVENT_BUS.addListener((final TickEvent.ClientTickEvent.Post event) -> {
            // Validate.
            if (HCompile.DEBUG_ASSERTS) {
                assert (event != null) : "HCsCR: Parameter 'event' is null.";
            }

            // Handle.
            HCsCR.handleClientTickEnd(Minecraft.getInstance());
        });
        ^///?} else {
        /^MinecraftForge.EVENT_BUS.addListener((final TickEvent.ClientTickEvent event) -> {
            // Validate.
            if (HCompile.DEBUG_ASSERTS) {
                assert (event != null) : "HCsCR: Parameter 'event' is null.";
            }

            // Handle.
            if (event.phase != TickEvent.Phase.END) return; // Implicit NPE for 'event'
            HCsCR.handleClientTickEnd(Minecraft.getInstance());
        });
        ^///?}

        // Register the client game loop handler.
        //? if >=1.21.8 {
        TickEvent.RenderTickEvent.Post.BUS.addListener((final TickEvent.RenderTickEvent.Post event) -> {
            // Validate.
            if (HCompile.DEBUG_ASSERTS) {
                assert (event != null) : "HCsCR: Parameter 'event' is null.";
            }

            // Handle.
            HCsCR.handleClientMainLoop(Minecraft.getInstance());
        });
        //?} elif >=1.20.4 {
        /^MinecraftForge.EVENT_BUS.addListener((final TickEvent.RenderTickEvent.Post event) -> {
            // Validate.
            if (HCompile.DEBUG_ASSERTS) {
                assert (event != null) : "HCsCR: Parameter 'event' is null.";
            }

            // Handle.
            HCsCR.handleClientMainLoop(Minecraft.getInstance());
        });
        ^///?} else {
        /^MinecraftForge.EVENT_BUS.addListener((final TickEvent.RenderTickEvent event) -> {
            // Validate.
            if (HCompile.DEBUG_ASSERTS) {
                assert (event != null) : "HCsCR: Parameter 'event' is null.";
            }

            // Handle.
            if (event.phase != TickEvent.Phase.END) return; // Implicit NPE for 'event'
            HCsCR.handleClientMainLoop(Minecraft.getInstance());
        });
        ^///?}

        // Register the config screen.
        //? if hacky_neoforge {
        /^container.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((final Minecraft mcClient, final Screen modsScreen) -> {
            // Validate.
            if (HCompile.DEBUG_ASSERTS) {
                assert (mcClient != null) : "HCsCR: Parameter 'mcClient' is null. (modsScreen: " + modsScreen + ')';
            }

            // Create.
            return new HScreen(modsScreen);
        }));
        container.registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> IExtensionPoint.DisplayTest.IGNORESERVERONLY, (final String remoteVersion, final Boolean isFromServer) -> true));
        ^///?} elif 1.20.2 {
        /^ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((final Minecraft mcClient, final Screen modsScreen) -> {
            // Validate.
            if (HCompile.DEBUG_ASSERTS) {
                assert (mcClient != null) : "HCsCR: Parameter 'mcClient' is null. (modsScreen: " + modsScreen + ')';
            }

            // Create.
            return new HScreen(modsScreen);
        }));
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> IExtensionPoint.DisplayTest.IGNORESERVERONLY, (final String remoteVersion, final Boolean isFromServer) -> true));
        ^///?} elif >=1.19.2 {
        ctx.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory(HScreen::new));
        ctx.registerDisplayTest(IExtensionPoint.DisplayTest.IGNORE_SERVER_VERSION);
        //?} elif >=1.18.2 {
        /^ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () -> new ConfigGuiHandler.ConfigGuiFactory(HScreen::new));
        ModLoadingContext.get().registerDisplayTest(IExtensionPoint.DisplayTest.IGNORE_SERVER_VERSION);
        ^///?} elif >=1.17.1 {
        /^ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () -> new ConfigGuiHandler.ConfigGuiFactory((final Minecraft mcClient, final Screen modsScreen) -> {
            // Validate.
            if (HCompile.DEBUG_ASSERTS) {
                assert (mcClient != null) : "HCsCR: Parameter 'mcClient' is null. (modsScreen: " + modsScreen + ')';
            }

            // Create.
            return new HScreen(modsScreen);
        }));
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> FMLNetworkConstants.IGNORESERVERONLY, (final String remoteVersion, final Boolean isFromServer) -> true));
        ^///?} else {
        /^ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (final Minecraft mcClient, final Screen modsScreen) -> {
            // Validate.
            if (HCompile.DEBUG_ASSERTS) {
                assert (mcClient != null) : "HCsCR: Parameter 'mcClient' is null. (modsScreen: " + modsScreen + ')';
            }

            // Create.
            return new HScreen(modsScreen);
        });
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (final String remoteVersion, final Boolean isFromServer) -> true));
        ^///?}

        // Done.
        LOGGER.info(HCsCR.HCSCR_MARKER, "HCsCR: Ready to remove 'em crystals. ({} ms)", (System.nanoTime() - start) / HCsCR.NANOS_IN_MS);
    }

    @Contract(pure = true)
    @Override
    public String toString() {
        return "HCsCR/HForge{}";
    }
}
*///?}
