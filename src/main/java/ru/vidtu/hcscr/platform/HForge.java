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

//? if forge {
/*package ru.vidtu.hcscr.platform;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import ru.vidtu.hcscr.HCsCR;
import ru.vidtu.hcscr.config.HConfig;
import ru.vidtu.hcscr.config.HScreen;

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

    //? if hackyNeoForge {
    /^/^¹*
     * Creates and loads a new mod.
     *
     * @param container Current mod container
     * @param bus       Loading event bus
     * @apiNote Do not call, called by Forge
     ¹^/
    public HForge(net.minecraftforge.fml.ModContainer container, net.minecraftforge.eventbus.api.IEventBus bus) {
        // Validate.
        assert container != null : "HCsCR: Parameter 'container' is null. (bus: " + bus + ", mod: " + this + ')';
        assert bus != null : "HCsCR: Parameter 'bus' is null. (container: " + container + ", mod: " + this + ')';
    ^///?} else if >=1.19.2 && (!1.20.2) {
    /^*
     * Creates and loads a new mod.
     *
     * @param ctx Loading context
     * @apiNote Do not call, called by Forge
     ^/
    public HForge(FMLJavaModLoadingContext ctx) {
        // Validate.
        assert ctx != null : "HCsCR: Parameter 'ctx' is null. (mod: " + this + ')';
    //?} else {
    /^/^¹*
     * Creates a new mod.
     *
     * @apiNote Do not call, called by Forge
     ¹^/
    public HForge() {
    ^///?}
        // Log.
        long start = System.nanoTime();
        LOGGER.info(HCsCR.HCSCR_MARKER, "HCsCR: Loading... (platform: forge)");

        // Not sure how long the Forge does have the "clientSideOnly" field in the TOML,
        // so I'll do an additional exception check here.
        if (FMLEnvironment.dist != Dist.CLIENT) {
            throw new UnsupportedOperationException("HCsCR: You've tried to load the HCsCR mod on a server. This won't work.");
        }

        // Load the config.
        HConfig.load();

        // Register the networking.
        //? if >=1.20.2 {
        net.minecraftforge.network.ChannelBuilder.named(HStonecutter.CHANNEL_IDENTIFIER).networkProtocolVersion(1).acceptedVersions((status, version) -> true).eventNetworkChannel().addListener(event -> {
            if (event.getPayload() == null) return;
            var src = event.getSource();
            if (!src.isClientSide()) return;
            src.getConnection().disconnect(HStonecutter.translate("hcscr.false"));
            src.setPacketHandled(true);
        });
        //?} else {
        /^//? if >=1.18.2 {
        net.minecraftforge.network.NetworkRegistry.newEventChannel(HStonecutter.CHANNEL_IDENTIFIER, () -> "hcscr", version -> true, version -> true).addListener(event -> {
        //?} else if >=1.17.1 {
        /^¹net.minecraftforge.fmllegacy.network.NetworkRegistry.newEventChannel(HStonecutter.CHANNEL_IDENTIFIER, () -> "hcscr", version -> true, version -> true).addListener(event -> {
        ¹^///?} else
        /^¹net.minecraftforge.fml.network.NetworkRegistry.newEventChannel(HStonecutter.CHANNEL_IDENTIFIER, () -> "hcscr", version -> true, version -> true).addListener(event -> {¹^/
            if (event.getPayload() == null) return;
            //? if >=1.17.1 {
            var src = event.getSource().get();
            //?} else
            /^¹net.minecraftforge.fml.network.NetworkEvent.Context src = event.getSource().get();¹^/
            if (src.getDirection().getReceptionSide() != net.minecraftforge.fml.LogicalSide.CLIENT) return;
            src.getNetworkManager().disconnect(HStonecutter.translate("hcscr.false"));
            src.setPacketHandled(true);
        });
        ^///?}

        // Register the binds.
        //? if >=1.21.8 {
        var bus = net.minecraftforge.client.event.RegisterKeyMappingsEvent.getBus(ctx.getModBusGroup());
        //?} else if 1.20.2 {
        /^var bus = FMLJavaModLoadingContext.get().getModEventBus();
        ^///?} else >=1.19.2 && (!hackyNeoForge)
        /^var bus = ctx.getModEventBus();^/ // Implicit NPE for 'ctx'
        //? if >=1.19.2 {
        bus.addListener((net.minecraftforge.client.event.RegisterKeyMappingsEvent event) -> {
            event.register(HCsCR.CONFIG_BIND);
            event.register(HCsCR.TOGGLE_BIND);
        });
        //?} else {
        /^FMLJavaModLoadingContext.get().getModEventBus().addListener((net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent event) -> {
            //? if >=1.18.2 {
            net.minecraftforge.client.ClientRegistry.registerKeyBinding(HCsCR.CONFIG_BIND);
            net.minecraftforge.client.ClientRegistry.registerKeyBinding(HCsCR.TOGGLE_BIND);
            //?} else if >=1.17.1 {
            /^¹net.minecraftforge.fmlclient.registry.ClientRegistry.registerKeyBinding(HCsCR.CONFIG_BIND);
            net.minecraftforge.fmlclient.registry.ClientRegistry.registerKeyBinding(HCsCR.TOGGLE_BIND);
            ¹^///?} else {
            /^¹net.minecraftforge.fml.client.registry.ClientRegistry.registerKeyBinding(HCsCR.CONFIG_BIND);
            net.minecraftforge.fml.client.registry.ClientRegistry.registerKeyBinding(HCsCR.TOGGLE_BIND);
            ¹^///?}
        });
        ^///?}
        Minecraft client = Minecraft.getInstance();
        //? if >=1.21.8 {
        TickEvent.ClientTickEvent.Post.BUS.addListener(event -> HCsCR.handleGameTick(client));
        //?} else if >=1.20.4 {
        /^net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent.Post event) -> HCsCR.handleGameTick(client));
        ^///?} else {
        /^net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if (event.phase != TickEvent.Phase.END) return;
            HCsCR.handleGameTick(client);
        });
        ^///?}

        // Register the scheduled remover.
        //? if >=1.21.8 {
        TickEvent.RenderTickEvent.Post.BUS.addListener(event -> HCsCR.handleFrameTick(HStonecutter.profilerOfGame(client)));
        //?} else if >=1.20.4 {
        /^net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((TickEvent.RenderTickEvent.Post event) -> HCsCR.handleFrameTick(HStonecutter.profilerOfGame(client)));
        ^///?} else {
        /^net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((TickEvent.RenderTickEvent event) -> {
            if (event.phase != TickEvent.Phase.END) return;
            HCsCR.handleFrameTick(HStonecutter.profilerOfGame(client));
        });
        ^///?}

        // Register the config screen.
        //? if hackyNeoForge {
        /^container.registerExtensionPoint(net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory.class, () -> new net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) -> new HScreen(screen)));
        container.registerExtensionPoint(net.minecraftforge.fml.IExtensionPoint.DisplayTest.class, () -> new net.minecraftforge.fml.IExtensionPoint.DisplayTest(() -> net.minecraftforge.fml.IExtensionPoint.DisplayTest.IGNORESERVERONLY, (version, fromServer) -> true));
        ^///?} else if 1.20.2 {
        /^net.minecraftforge.fml.ModLoadingContext.get().registerExtensionPoint(net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory.class, () -> new net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) -> new HScreen(screen)));
        net.minecraftforge.fml.ModLoadingContext.get().registerExtensionPoint(net.minecraftforge.fml.IExtensionPoint.DisplayTest.class, () -> new net.minecraftforge.fml.IExtensionPoint.DisplayTest(() -> net.minecraftforge.fml.IExtensionPoint.DisplayTest.IGNORESERVERONLY, (version, fromServer) -> true));
        ^///?} else if >=1.19.2 {
        ctx.registerExtensionPoint(net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory.class, () -> new net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory(HScreen::new));
        ctx.registerDisplayTest(net.minecraftforge.fml.IExtensionPoint.DisplayTest.IGNORE_SERVER_VERSION);
        //?} else if >=1.18.2 {
        /^net.minecraftforge.fml.ModLoadingContext.get().registerExtensionPoint(net.minecraftforge.client.ConfigGuiHandler.ConfigGuiFactory.class, () -> new net.minecraftforge.client.ConfigGuiHandler.ConfigGuiFactory(HScreen::new));
        net.minecraftforge.fml.ModLoadingContext.get().registerDisplayTest(net.minecraftforge.fml.IExtensionPoint.DisplayTest.IGNORE_SERVER_VERSION);
        ^///?} else if >=1.17.1 {
        /^net.minecraftforge.fml.ModLoadingContext.get().registerExtensionPoint(net.minecraftforge.fmlclient.ConfigGuiHandler.ConfigGuiFactory.class, () -> new net.minecraftforge.fmlclient.ConfigGuiHandler.ConfigGuiFactory((minecraft, screen) -> new HScreen(screen)));
        net.minecraftforge.fml.ModLoadingContext.get().registerExtensionPoint(net.minecraftforge.fml.IExtensionPoint.DisplayTest.class, () -> new net.minecraftforge.fml.IExtensionPoint.DisplayTest(() -> net.minecraftforge.fmllegacy.network.FMLNetworkConstants.IGNORESERVERONLY, (version, fromServer) -> true));
        ^///?} else {
        /^net.minecraftforge.fml.ModLoadingContext.get().registerExtensionPoint(net.minecraftforge.fml.ExtensionPoint.CONFIGGUIFACTORY, () -> (minecraft, screen) -> new HScreen(screen));
        net.minecraftforge.fml.ModLoadingContext.get().registerExtensionPoint(net.minecraftforge.fml.ExtensionPoint.DISPLAYTEST, () -> org.apache.commons.lang3.tuple.Pair.of(() -> net.minecraftforge.fml.network.FMLNetworkConstants.IGNORESERVERONLY, (version, fromServer) -> true));
        ^///?}

        // Done.
        LOGGER.info(HCsCR.HCSCR_MARKER, "HCsCR: Ready to remove 'em crystals. ({} ms)", (System.nanoTime() - start) / 1_000_000L);
    }

    @Contract(pure = true)
    @Override
    public String toString() {
        return "HCsCR/HForge{}";
    }
}
*///?}
