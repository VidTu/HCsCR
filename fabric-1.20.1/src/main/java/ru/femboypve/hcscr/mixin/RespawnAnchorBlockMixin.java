package ru.femboypve.hcscr.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.femboypve.hcscr.HCsCR;

/**
 * Mixin that allows respawn anchors to be removed.
 *
 * @author VidTu
 */
@Mixin(RespawnAnchorBlock.class)
public abstract class RespawnAnchorBlockMixin {
    @Shadow @Final public static IntegerProperty CHARGE;

    private RespawnAnchorBlockMixin() {
        throw new AssertionError("The life is hard, but initializing @Mixin is harder.");
    }

    @Inject(method = "use", at = @At("HEAD"))
    public void use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result, CallbackInfoReturnable<InteractionResult> cir) {
        if (!HCsCR.enabled || HCsCR.serverDisabled || !HCsCR.removeAnchors) return;
        ItemStack stack = player.getItemInHand(hand);
        if (hand == InteractionHand.MAIN_HAND && !isRespawnFuel(stack) && isRespawnFuel(player
                .getItemInHand(InteractionHand.OFF_HAND)) || isRespawnFuel(stack) && canBeCharged(state)
                || state.getValue(CHARGE) == 0 || canSetSpawn(level)) return;
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

    @Shadow
    public static boolean canSetSpawn(Level level) {
        return false;
    }
}
