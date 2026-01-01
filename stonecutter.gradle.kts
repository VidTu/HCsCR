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

// This is the root Stonecutter entrypoint. It configures some
// version-independent aspects of the Stonecutter preprocessor.
// See "build.gradle.kts" for the per-version Gradle buildscript.
// See "compile" for the compile-time constants and Blossom configuration.
// See "settings.gradle.kts" for the Gradle configuration.

// Plugins.
plugins {
    id("dev.kikugie.stonecutter")
}

// Active Stonecutter version. See:
// https://stonecutter.kikugie.dev/wiki/glossary#active-version
// https://stonecutter.kikugie.dev/wiki/glossary#vcs-version
stonecutter active "1.21.11-fabric" /* [SC] DO NOT EDIT */

// Process the JSON files via Stonecutter.
// This is needed for the Mixin configuration.
stonecutter handlers {
    inherit("java", "json")
}
