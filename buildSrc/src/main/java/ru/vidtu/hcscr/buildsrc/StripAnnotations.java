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

package ru.vidtu.hcscr.buildsrc;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/// A collection of data about annotations to be stripped.
///
/// Static logic of this class *is* thread-safe.
///
/// @author VidTu
/// @apiNote Internal use only
@ApiStatus.Internal
@NullMarked
public final class StripAnnotations {
    /// An immutable set of annotations VM names to strip.
    ///
    /// @see #STRIPPED_PACKAGES
    /// @see #shouldStripDescriptor(String)
    /// @see #shouldStripInternal(String)
    @Unmodifiable
    private static final Set<String> STRIPPED_ANNOTATIONS = Set.of(
            "java/lang/Deprecated"
    );

    /// An array of annotation VM prefixes (packages) to strip.
    ///
    /// Individual VM annotations are cached via [#STRIPPED_CACHE].
    ///
    /// @see #STRIPPED_ANNOTATIONS
    /// @see #STRIPPED_CACHE
    /// @see #shouldStripDescriptor(String)
    /// @see #shouldStripInternal(String)
    private static final String @Unmodifiable [] STRIPPED_PACKAGES = {
            "com/google/errorprone/annotations/",
            "org/intellij/lang/annotations/",
            "org/jetbrains/annotations/",
            "org/jspecify/annotations/"
    };

    /// A mutable cache for individual annotations for [#STRIPPED_PACKAGES].
    ///
    /// This cache *is* thread-safe and it is the only part of the `static`(!)
    /// thread-safe-dependant API in the *Strip* implementation.
    ///
    /// @see #STRIPPED_PACKAGES
    /// @see #shouldStripDescriptor(String)
    /// @see #shouldStripInternal(String)
    private static final Map<String, Boolean> STRIPPED_CACHE = new ConcurrentHashMap<>(128);

    /// An instance of this class cannot be created.
    ///
    /// @throws AssertionError Always
    /// @deprecated Always throws
    @Deprecated(forRemoval = true)
    @Contract(value = "-> fail", pure = true)
    private StripAnnotations() {
        throw new AssertionError("HCsCR: No instances.");
    }

    /// Checks if the internal annotation VM descriptor should be stripped.
    ///
    /// Annotations will be stripped if their name is found in [#STRIPPED_ANNOTATIONS],
    /// or their name starts with a prefix (package) found in [#STRIPPED_PACKAGES].
    ///
    /// Unlike [#shouldStripInternal(String)], this method **IS** intended
    /// for whole descriptors, containing `L` at the beginning and `;` at the end.
    ///
    /// @param name Annotation internal VM descriptor to check
    /// @return `true` if the annotation should be stripped, `false` otherwise
    /// @see #STRIPPED_ANNOTATIONS
    /// @see #STRIPPED_PACKAGES
    /// @see #shouldStripInternal(String)
    @Contract(pure = true)
    static boolean shouldStripDescriptor(final String name) {
        // Validate.
        assert (name != null) : "HCsCR: Parameter 'name' is null.";
        assert (!name.isBlank()) : "HCsCR: Blank name. (name: " + name + ')';
        assert ((name.charAt(0) == 'L') && (name.charAt(name.length() - 1) == ';')) : "HCsCR: Internal name used in descriptor name stripping. (name: " + name + ')';

        // Delegate.
        final String internal = name.substring(1, (name.length() - 1)); // Implicit NPE for 'name'
        return shouldStripInternal(internal);
    }

    /// Checks if the internal annotation VM name should be stripped.
    ///
    /// Annotations will be stripped if their name is found in [#STRIPPED_ANNOTATIONS],
    /// or their name starts with a prefix (package) found in [#STRIPPED_PACKAGES].
    ///
    /// Unlike [#shouldStripDescriptor(String)], this method is **NOT** intended for whole
    /// descriptors, so it should **NOT** contain `L` at the beginning and `;` at the end.
    ///
    /// @param name Annotation internal VM name to check
    /// @return `true` if the annotation should be stripped, `false` otherwise
    /// @see #STRIPPED_ANNOTATIONS
    /// @see #STRIPPED_PACKAGES
    /// @see #shouldStripDescriptor(String)
    @Contract(pure = true)
    static boolean shouldStripInternal(final String name) {
        // Validate.
        assert (name != null) : "HCsCR: Parameter 'name' is null.";
        assert (!name.isBlank()) : "HCsCR: Blank name. (name: " + name + ')';
        assert ((name.charAt(0) != 'L') || (name.charAt(name.length() - 1) != ';')) : "HCsCR: Descriptor name used in internal name stripping. (name: " + name + ')';

        // Fast path for exact match: Strip if name directly matches one of desired annotations.
        if (STRIPPED_ANNOTATIONS.contains(name)) return true; // Implicit NPE for 'name'

        // Use the cache if available.
        return STRIPPED_CACHE.computeIfAbsent(name, (final String _) -> {
            // Strip if name starts with package name of desired annotations.
            for (final String pkg : STRIPPED_PACKAGES) {
                if (!name.startsWith(pkg)) continue;
                return true;
            }
            return false;
        });
    }
}
