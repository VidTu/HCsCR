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
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.vidtu.hcscr.mixin.block;

import com.google.errorprone.annotations.DoNotCall;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.vidtu.hcscr.HCsCR;
import ru.vidtu.hcscr.compile.Variables;
import ru.vidtu.hcscr.config.BlockMode;
import ru.vidtu.hcscr.config.Config;
import ru.vidtu.hcscr.handler.BlockClips;

/**
 * Mixin that calls {@link BlockClips#removeClip(BlockPos)} on block update packets.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see BlockClips#removeClip(BlockPos)
 * @see BlockMode#COLLISION
 * @see Config#blocks()
 */
// @ApiStatus.Internal // Can't annotate this without logging in the console.
@Mixin(ClientPacketListener.class)
@NullMarked
public final class ClientPacketListenerMixin {
    /**
     * Current level, {@code null} if none.
     * <p>
     * Provided by the implementation. ({@link Shadow})
     */
    @Shadow
    @Nullable
    private /*non-final*/ ClientLevel level;

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
        if (Variables.DEBUG_ASSERTS) {
            throw new AssertionError("HCsCR: No instances.");
        }
    }

    /**
     * Handles the single block update packet.
     * <p>
     * Removes the packet block position via {@link BlockClips#removeClip(BlockPos)} when
     * receiving a block update packet from the server. Does nothing if there wasn't any clips.
     * <p>
     * Gets called on the game thread.
     * <p>
     * Additionally removes the clip for the related bed part,
     * if there a (client) bed at the position on the client.
     *
     * @param packet Packet that updates the block state
     * @param ci     Callback data, ignored
     * @apiNote Do not call, called by Mixin
     * @see BlockClips#removeClip(BlockPos)
     */
    @DoNotCall("Called by Mixin")
    @Inject(method = "handleBlockUpdate", at = @At("RETURN")) // RETURN here is needed for thread scheduling.
    private void hcscr_handleBlockUpdate_return(final ClientboundBlockUpdatePacket packet, final CallbackInfo ci) {
        // Validate.
        if (Variables.DEBUG_ASSERTS) {
            assert (packet != null) : "HCsCR: Parameter 'packet' is null. (handler: " + this + ')';
            assert (Minecraft.getInstance().isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", packet: " + packet + ", handler: " + this + ')';
        }

        // TODO(VidTu): Logging.

        // Do nothing, if the level is null. Servers might send this packet,
        // it's important that either both or neither we/vanilla fuck up.
        final Level level = this.level;
        if (level == null) return;

        // Remove the block's clip.
        final BlockPos pos = packet.getPos(); // Implicit NPE for 'packet'
        if (Variables.DEBUG_ASSERTS) {
            assert (pos != null) : "HCsCR: Position is null. (thread: " + Thread.currentThread() + ", packet: " + packet + ", handler: " + this + ')';
        }
        BlockClips.removeClip(pos);

        // Find the bed, do nothing if the block is not a bed.
        final BlockState state = level.getBlockState(pos);
        if (!state.is(BlockTags.BEDS)) return;

        // Find the bed's connected part, do nothing if the other part is not a bed.
        final BlockPos connectedPos = pos.relative(BedBlock.getConnectedDirection(state));
        final BlockState connectedState = level.getBlockState(connectedPos);
        if (!connectedState.is(BlockTags.BEDS)) return;

        // Remove the bed's connected part clip.
        BlockClips.removeClip(connectedPos);
    }

    // TODO(VidTu): Remove on chunk update and multiblock update. (1.16.x era block updates too?)
}
