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

package ru.vidtu.hcscr.mixins;

import com.google.errorprone.annotations.DoNotCall;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.vidtu.hcscr.HCsCR;

/**
 * Mixin that nukes blocks from {@link HCsCR#CLIPPING_ANCHORS} on block update.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see HCsCR#CLIPPING_ANCHORS
 */
// @ApiStatus.Internal // Can't annotate this without logging in the console.
@Mixin(ClientPacketListener.class)
@NullMarked
public final class ClientPacketListenerMixin {
    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    // @ApiStatus.ScheduledForRemoval // Can't annotate this without logging in the console.
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private ClientPacketListenerMixin() {
        throw new AssertionError("HCsCR: No instances.");
    }

    /**
     * Nukes the packet block position from {@link HCsCR#CLIPPING_ANCHORS}.
     *
     * @param packet Block update packet
     * @param ci     Callback data, ignored
     * @apiNote Do not call, called by Mixin
     * @see HCsCR#CLIPPING_ANCHORS
     */
    @DoNotCall("Called by Mixin")
    @Inject(method = "handleBlockUpdate", at = @At("RETURN"))
    private void hcscr_handleBlockUpdate_return(ClientboundBlockUpdatePacket packet, CallbackInfo ci) {
        // Validate.
        assert packet != null : "HCsCR: Parameter 'packet' is null. (handler: " + this + ')';

        // Nuke.
        HCsCR.CLIPPING_ANCHORS.remove(packet.getPos()); // Implicit NPE for 'packet'
    }
}
