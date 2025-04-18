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

//? if <1.17.1 {
/*package ru.vidtu.hcscr;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import ru.vidtu.hcscr.mixins.EntityCollisionContextMixin;

/^*
 * Mixin extender for {@link EntityCollisionContextMixin} that allows getting the entity of
 * {@link EntityCollisionContext}. (<1.17.1)
 *
 * @author VidTu
 * @apiNote Internal use only
 ^/
@ApiStatus.Internal
@NullMarked
public interface HEntityCollisionContext {
    /^*
     * Gets the entity.
     *
     * @return Entity involving in the context, {@code null} if none
     ^/
    @Contract(pure = true)
    @Nullable
    Entity hcscr_entity();
}
*///?}
