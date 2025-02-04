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

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jetbrains.annotations.Contract;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.vidtu.hcscr.HCsCRFabric;

/**
 * Mixin that removes scheduled entities on world load and unload.
 *
 * @author VidTu
 */
@Mixin(Minecraft.class)
public final class MinecraftMixin {
    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     */
    @Contract(value = "-> fail", pure = true)
    private MinecraftMixin() {
        throw new AssertionError("No instances.");
    }

    // Removes the entities on level update. This gets called in setLevel() and disconnect().
    @Inject(method = "updateLevelInEngines", at = @At("HEAD"))
    public void hcscr$updateLevelInEngines$head(ClientLevel level, CallbackInfo ci) {
        HCsCRFabric.clearScheduledRemovals();
    }
}
