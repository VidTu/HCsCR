/*
 * Copyright (c) 2023 Offenderify
 * Copyright (c) 2023-2024 VidTu
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

import net.minecraft.client.gui.components.toasts.SystemToast;
import org.jetbrains.annotations.Contract;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

/**
 * Mixin that adds custom system toast IDs.
 *
 * @author VidTu
 * @see <a href="https://github.com/SpongePowered/Mixin/issues/387#issuecomment-888408556">Reference</a>
 */
@Mixin(SystemToast.SystemToastIds.class)
public final class SystemToastIdsMixin {
    @Shadow
    @Final
    @Mutable
    private static SystemToast.SystemToastIds[] $VALUES;

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     */
    @Contract(value = "-> fail", pure = true)
    private SystemToastIdsMixin() {
        throw new AssertionError("No instances.");
    }

    // Adds two fields to the SystemToastIds enum. (YES, adds fields to the ENUM)
    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void hcscr$clinit$return(CallbackInfo ci) {
        SystemToast.SystemToastIds[] values = Arrays.copyOf($VALUES, $VALUES.length + 2);
        SystemToast.SystemToastIds toggleToast = hcscr$init("HCSCR$TOGGLE_TOAST", values.length - 2);
        SystemToast.SystemToastIds serverToast = hcscr$init("HCSCR$SERVER_TOAST", values.length - 1);
        values[values.length - 2] = toggleToast;
        values[values.length - 1] = serverToast;
        $VALUES = values;
    }

    @Invoker("<init>")
    public static SystemToast.SystemToastIds hcscr$init(String name, int ordinal) {
        throw new AssertionError();
    }
}
