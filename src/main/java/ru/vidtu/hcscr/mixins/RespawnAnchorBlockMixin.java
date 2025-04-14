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

package ru.vidtu.hcscr.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Contract;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.vidtu.hcscr.config.HConfig;

/**
 * Mixin that allows respawn anchors to be removed.
 *
 * @author VidTu
 */
@Mixin(RespawnAnchorBlock.class)
public final class RespawnAnchorBlockMixin {
    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     */
    @Contract(value = "-> fail", pure = true)
    private RespawnAnchorBlockMixin() {
        throw new AssertionError("No instances.");
    }

    //? if >=1.20.6 {
    // Removes the anchor if this feature is enabled.
    @Inject(method = "useWithoutItem", at = @At("HEAD"))
    public void hcscr$useWithoutItem$head(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult result,
                               CallbackInfoReturnable<InteractionResult> cir) {
        // Do NOT process anchors if any of the following conditions is met:
        // - The mod is disabled via config or keybind.
        // - The mod is disabled by the current server.
        // - The "remove anchors" feature is disabled.
        // - The current level (world) is not client-side. (e.g. integrated server world)
        // - The anchor doesn't have any charges.
        // - The anchor doesn't explode in the current dimension.
        if (!HConfig.enabled || !HConfig.removeAnchors || !level.isClientSide()
                || state.getValue(RespawnAnchorBlock.CHARGE) == 0 || RespawnAnchorBlock.canSetSpawn(level)) return;

        // Remove the anchor.
        level.removeBlock(pos, false);
    }
    //?} else {
    /*// Removes the anchor if this feature is enabled.
    @Inject(method = "use", at = @At("HEAD"))
    public void hcscr$use$head(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                               BlockHitResult result, CallbackInfoReturnable<InteractionResult> cir) {
        // Do NOT process anchors if any of the following conditions is met:
        // - The mod is disabled via config or keybind.
        // - The mod is disabled by the current server.
        // - The "remove anchors" feature is disabled.
        // - The current level (world) is not client-side. (e.g. integrated server world)
        // - The anchor doesn't have any charges.
        // - The anchor doesn't explode in the current dimension.
        // - The anchor is currently being charged.
        if (!HConfig.enabled || !HConfig.removeAnchors || !level.isClientSide()
                || state.getValue(RespawnAnchorBlock.CHARGE) == 0 || RespawnAnchorBlock.canSetSpawn(level)) return;
        ItemStack stack = player.getItemInHand(hand);
        if ((hand == InteractionHand.MAIN_HAND && !isRespawnFuel(stack) &&
                isRespawnFuel(player.getItemInHand(InteractionHand.OFF_HAND))) ||
                (isRespawnFuel(stack) && canBeCharged(state))) return;

        // Remove the anchor.
        level.removeBlock(pos, false);
    }

    @Shadow
    private static boolean isRespawnFuel(ItemStack stack) {
        return false;
    }

    @Shadow
    private static boolean canBeCharged(BlockState state) {
        return false;
    }
    *///?}
}
