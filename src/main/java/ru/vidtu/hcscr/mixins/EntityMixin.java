package ru.vidtu.hcscr.mixins;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.vidtu.hcscr.HCsCR;

@Mixin(Entity.class)
public class EntityMixin {
    @Shadow
    @Final
    private static AABB INITIAL_AABB;

    @Inject(method = "getBoundingBox", at = @At("HEAD"), cancellable = true)
    private void hcscr_clinit_return(CallbackInfoReturnable<AABB> cir) {
        if (HCsCR.allowPicking((Entity) (Object) this)) return;
        cir.setReturnValue(INITIAL_AABB);
    }
}
