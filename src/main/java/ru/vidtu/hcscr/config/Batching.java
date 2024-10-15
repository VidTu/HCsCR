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

package ru.vidtu.hcscr.config;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Batching mode.
 *
 * @author VidTu
 */
public enum Batching {
    /**
     * Batching is disabled. Entities are removed one-by-one.
     */
    DISABLED("hcscr.config.batching.disabled"),

    /**
     * Batching (least aggressive) will remove entities that are fully inside the hit entity.
     *
     * @apiNote This option exists to combat various protection techniques against crystal optimizers, but it may remove some entities
     */
    CONTAINING("hcscr.config.batching.containing"),

    /**
     * Batching (more aggressive) will remove entities that are fully inside or fully outside the hit entity.
     *
     * @apiNote This option exists to may combat various protection techniques against crystal optimizers, but it may remove entities
     */
    CONTAINING_CONTAINED("hcscr.config.batching.containingContained"),

    /**
     * Batching (most aggressive) will remove entities that intersect with the hit entity.
     *
     * @apiNote This option exists to may combat various protection techniques against crystal optimizers, but it may remove all adjacent entities
     */
    INTERSECTING("hcscr.config.batching.intersecting");

    /**
     * Batching translation key.
     */
    @NotNull
    private final String key;

    /**
     * Creates a new mode.
     *
     * @param key Batching translation key
     */
    @Contract(pure = true)
    Batching(@NotNull String key) {
        this.key = key;
    }

    /**
     * Gets the translation key.
     *
     * @return Batching translation key
     */
    @Contract(pure = true)
    @Override
    @NotNull
    public String toString() {
        return this.key;
    }
}
