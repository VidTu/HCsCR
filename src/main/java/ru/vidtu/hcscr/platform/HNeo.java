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
 *
 * SPDX-License-Identifier: Apache-2.0
 */

//? if neoforge {
/*package ru.vidtu.hcscr.platform;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Unit;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.IExtensionPoint;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import ru.vidtu.hcscr.HCsCR;
import ru.vidtu.hcscr.config.HConfig;
import ru.vidtu.hcscr.config.HScreen;

/^*
 * Main HCsCR class for NeoForge.
 *
 * @author VidTu
 * @apiNote Internal use only
 ^/
@ApiStatus.Internal
//? if >=1.20.6 {
@Mod(value = "hcscr", dist = Dist.CLIENT)
//?} else
/^@Mod("hcscr")^/
@NullMarked
public final class HNeo {
    /^*
     * Logger for this class.
     ^/
    private static final Logger LOGGER = LogManager.getLogger("HCsCR/HNeo");

    /^*
     * Creates a new mod.
     *
     * @param dist      Current physical side
     * @param container Mod container
     * @param bus       Mod-specific event bus
     ^/
    public HNeo(Dist dist, ModContainer container, IEventBus bus) {
        // Log.
        long start = System.nanoTime();
        LOGGER.info("HCsCR: Loading... (platform: neoforge)");

        // Load the config.
        HConfig.load();

        // Not sure how long the Forge does have the "clientSideOnly" field in the TOML,
        // so I'll do an additional exception check here.
        if (dist != Dist.CLIENT) {
            throw new UnsupportedOperationException("HCsCR: You've tried to load the HCsCR mod on a server. This won't work.");
        }

        // Register the networking.
        //? if >=1.20.6 {
        bus.addListener(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent.class, event -> event.registrar("hcscr").commonToClient(new net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<>(HStonecutter.CHANNEL_IDENTIFIER), net.minecraft.network.codec.StreamCodec.unit(null), (payload, context) -> context.disconnect(HStonecutter.translate("hcscr.false"))));
        //?} else if >=1.20.4 {
        /^bus.addListener(net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent.class, event -> event.registrar("hcscr").common(HStonecutter.CHANNEL_IDENTIFIER, buf -> null, (arg, context) -> context.replyHandler().disconnect(HStonecutter.translate("hcscr.false"))));
        ^///?} else
        /^net.neoforged.neoforge.network.NetworkRegistry.newSimpleChannel(HStonecutter.CHANNEL_IDENTIFIER, () -> "hcscr:imhere", s -> true, s -> true).registerMessage(0, Unit.class, (unit, buf) -> {}, buf -> null, (unit, context) -> context.getNetworkManager().disconnect(HStonecutter.translate("hcscr.false")));^/

        // Register the binds.
        bus.addListener(RegisterKeyMappingsEvent.class, event -> {
            event.register(HCsCR.CONFIG_BIND);
            event.register(HCsCR.TOGGLE_BIND);
        });
        //? if >=1.20.6 {
        NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.client.event.ClientTickEvent.Post.class, event -> HCsCR.handleTick(Minecraft.getInstance()));
        //?} else {
        /^NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.TickEvent.ClientTickEvent.class, event -> {
            if (event.phase != net.neoforged.neoforge.event.TickEvent.Phase.END) return;
            HCsCR.handleTick(Minecraft.getInstance());
        });
        ^///?}

        // Register the scheduled remover.
        //? if >=1.20.6 {
        NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.client.event.RenderFrameEvent.Post.class, event -> HCsCR.handleFrame(HStonecutter.profilerOf(Minecraft.getInstance())));
        //?} else {
        /^NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.TickEvent.RenderTickEvent.class, event -> {
            if (event.phase != net.neoforged.neoforge.event.TickEvent.Phase.END) return;
            HCsCR.handleFrame(HStonecutter.profilerOf(Minecraft.getInstance()));
        });
        ^///?}

        // Register the config screen.
        //? if >=1.20.6 {
        container.registerExtensionPoint(net.neoforged.neoforge.client.gui.IConfigScreenFactory.class, (modOrGame, parent) -> new HScreen(parent));
        //?} else {
        /^container.registerExtensionPoint(net.neoforged.neoforge.client.ConfigScreenHandler.ConfigScreenFactory.class, () -> new net.neoforged.neoforge.client.ConfigScreenHandler.ConfigScreenFactory((game, screen) -> new HScreen(screen)));
        container.registerExtensionPoint(net.neoforged.fml.IExtensionPoint.DisplayTest.class, () -> new net.neoforged.fml.IExtensionPoint.DisplayTest(() -> IExtensionPoint.DisplayTest.IGNORESERVERONLY, (version, fromServer) -> true));
        ^///?}

        // Done.
        LOGGER.info("HCsCR: Ready to remove 'em crystals. ({} ms)", (System.nanoTime() - start) / 1_000_000L);
    }
}
*///?}
