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

//? if fabric {
package ru.vidtu.hcscr.platform;

import com.google.errorprone.annotations.DoNotCall;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import ru.vidtu.hcscr.config.HConfig;
import ru.vidtu.hcscr.config.HScreen;

//? if >=1.20.4 {
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.google.common.net.HttpHeaders;
import com.google.errorprone.annotations.CompileTimeConstant;
import com.terraformersmc.modmenu.api.UpdateChannel;
import com.terraformersmc.modmenu.api.UpdateChecker;
import com.terraformersmc.modmenu.api.UpdateInfo;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import ru.vidtu.hcscr.HCsCR;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Properties;
//?}

/**
 * HCsCR entrypoint for the ModMenu API.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see HFabric
 * @see HConfig
 */
@ApiStatus.Internal
@NullMarked
public final class HModMenu implements ModMenuApi {
    /**
     * Creates a new entrypoint.
     *
     * @apiNote Do not call, called by ModMenu
     */
    @Contract(pure = true)
    public HModMenu() {
        // Empty.
    }

    /**
     * Gets the ModMenu config screen factory.
     *
     * @return Config screen factory for ModMenu
     * @apiNote Do not call, called by ModMenu
     */
    @DoNotCall("Called by ModMenu")
    @Contract(pure = true)
    @Override
    public ConfigScreenFactory<HScreen> getModConfigScreenFactory() {
        return HScreen::new; // No need to use singletons, called once.
    }

    //? if >=1.20.4 {
    /**
     * Gets the ModMenu update checker.
     *
     * @return A custom update checker for HCsCR
     * @apiNote Do not call, called by ModMenu
     */
    @DoNotCall("Called by ModMenu")
    @Contract(value = "-> new", pure = true)
    @Override
    public UpdateChecker getUpdateChecker() {
        return new Updater(); // No need to use singletons, called once.
    }
    //?}

    @Contract(pure = true)
    @Override
    public String toString() {
        return "HCsCR/HModMenu{}";
    }

    //? if >=1.20.4 {
    /**
     * A ModMenu update checker for HCsCR.
     *
     * @author VidTu
     * @apiNote Internal use only
     */
    @ApiStatus.Internal
    @NullMarked
    /*package-private*/ static final class Updater implements UpdateChecker {
        /**
         * URL for fetching the update info.
         */
        @CompileTimeConstant
        private static final String UPDATER_URL = "https://raw.githubusercontent.com/VidTu/HCsCR/main/updater_hcscr_fabric.properties";

        /**
         * Fallback updater link.
         */
        @CompileTimeConstant
        private static final String FALLBACK_LINK = "https://github.com/VidTu/HCsCR/releases/latest";

        /**
         * User agent for update requests.
         *
         * @see HttpHeaders#USER_AGENT
         */
        @CompileTimeConstant
        private static final String USER_AGENT = "VidTu/HCsCR/" + HCompile.VERSION + " (updater; https://github.com/VidTu/HCsCR; pig@vidtu.ru)";

        /**
         * Maximum length for the updater response to prevent abuse.
         * <p>
         * Equals to {@code 32767} units.
         * <p>
         * Depending on the implementation, this might be counted
         * in either or both UTF-8 codepoints or UTF-8 bytes.
         */
        @CompileTimeConstant
        private static final int MAX_BODY_LENGTH = 32767;

        /**
         * Maximum length for the updater single component to prevent abuse.
         * <p>
         * Equals to {@code 255} units.
         * <p>
         * Depending on the implementation, this might be counted
         * in either or both UTF-8 codepoints or UTF-8 bytes.
         */
        @CompileTimeConstant
        private static final int MAX_COMPONENT_LENGTH = 255;

        /**
         * Timeout for update checking.
         * <p>
         * Equals to {@code 30} seconds.
         */
        private static final Duration TIMEOUT = Duration.ofSeconds(30L);

        /**
         * Logger for this class.
         */
        private static final Logger LOGGER = LogManager.getLogger("HCsCR/HModMenu$Updater");

        /**
         * Escaper for the body, escapes ASCII controls in messages for printing.
         */
        private static final Escaper SANITIZER;
        static {
            final Escapers.Builder builder = Escapers.builder();
            for (char c = 0; c < 32; c++) {
                builder.addEscape(c, "\\u%04X".formatted((int) c));
            }
            SANITIZER = builder
                    .addEscape('\0', "\\0")
                    .addEscape('\b', "\\b")
                    .addEscape('\t', "\\t")
                    .addEscape('\n', "\\n")
                    .addEscape('\f', "\\f")
                    .addEscape('\r', "\\r")
                    // .addEscape('\s', "\\s") // Don't escape spaces.
                    .addEscape('\"', "\\\"")
                    .addEscape('\'', "\\'")
                    .addEscape('\\', "\\\\")
                    .addEscape((char) 127, "\\u007F")
                    .build();
        }

        /**
         * Creates a new updater.
         */
        @Contract(pure = true)
        Updater() {
            // Empty.
        }

        /**
         * Checks for updates.
         *
         * @return Found update, {@code null} if update is not needed
         * @apiNote Do not call, called by ModMenu
         */
        @Blocking
        @DoNotCall("Called by ModMenu")
        @CheckReturnValue
        @Nullable
        @Override
        public UpdateInfo checkForUpdates() {
            // Wrap.
            try {
                // Log. (**TRACE**)
                if (HCompile.DEBUG_LOGS) {
                    LOGGER.trace(HCsCR.HCSCR_MARKER, "HCsCR: Checking for updates via ModMenu...");
                }

                // Allow forcefully disabling the updater.
                if (Boolean.getBoolean("ru.vidtu.hcscr.nomodmenuupdater")) {
                    // Log. (**DEBUG**)
                    if (HCompile.DEBUG_LOGS) {
                        LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: ModMenu updater is disabled via the 'ru.vidtu.hcscr.nomodmenuupdater' property.");
                    }

                    // Stop.
                    return null;
                }

                // Create an HTTP client.
                final HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(TIMEOUT)
                        .version(HttpClient.Version.HTTP_2)
                        .executor(Runnable::run)
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .build();

                // Wrap to close later. (if possible)
                try {
                    // Extract the version.
                    //? if >=1.21.8 {
                    final String gameVersion = SharedConstants.getCurrentVersion().id();
                    //?} else {
                    /*final String gameVersion = SharedConstants.getCurrentVersion().getId();
                    *///?}
                    final UpdateChannel channel = UpdateChannel.getUserPreference();

                    // Log. (**TRACE**)
                    if (HCompile.DEBUG_LOGS) {
                        LOGGER.trace(HCsCR.HCSCR_MARKER, "HCsCR: Will check updates for '{}' game version and '{}' channel... (ua: " + USER_AGENT + ')', gameVersion, channel);
                    }

                    // Send the request.
                    final HttpResponse<String> response = client.send(HttpRequest.newBuilder()
                            .uri(new URI(UPDATER_URL))
                            .timeout(TIMEOUT)
                            .header(HttpHeaders.USER_AGENT, USER_AGENT)
                            .GET()
                            //? if >=26.1.2 {
                            .build(), HttpResponse.BodyHandlers.limiting(HttpResponse.BodyHandlers.ofString(), MAX_BODY_LENGTH));
                            //? } else {
                            /*.build(), HttpResponse.BodyHandlers.ofString());
                            *///?}

                    // Extract.
                    final String body = response.body();
                    final int code = response.statusCode();

                    // Validate the length.
                    final int bodyLength = body.length();
                    if (bodyLength > MAX_BODY_LENGTH) {
                        throw new IllegalStateException("HCsCR: HTTP response body is too long for update checking. (code: " + code + ", response: " + response + ", bodyLength: " + bodyLength + ')');
                    }

                    // Log. (**DEBUG**)
                    if (HCompile.DEBUG_LOGS && LOGGER.isDebugEnabled(HCsCR.HCSCR_MARKER)) {
                        final String sanitizedBody = SANITIZER.escape(body);
                        LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Got a response from the update API. (code: {}, response: {}, sanitizedBody: '{}')", code, response, sanitizedBody);
                    }

                    // Check the code.
                    if ((code < 200) || (code >= 300)) {
                        final String sanitizedBody = SANITIZER.escape(body);
                        throw new IllegalStateException("HCsCR: Non-success HTTP code returned for update checking. (code: " + code + ", response: " + response + ", sanitizedBody: '" + sanitizedBody + "')");
                    }

                    // Parse the properties.
                    final Properties properties = new Properties();
                    try (final StringReader reader = new StringReader(body)) { // Technically needn't be try-with-resources.
                        properties.load(reader);
                    }

                    // Get the version, return nothing if not found.
                    final String versionKey = (gameVersion + '@' + channel + "@version");
                    final String rawVersion = properties.getProperty(versionKey);
                    if ((rawVersion == null) || rawVersion.isBlank()) {
                        // Log. (**DEBUG**)
                        if (HCompile.DEBUG_LOGS && LOGGER.isDebugEnabled(HCsCR.HCSCR_MARKER)) {
                            @SuppressWarnings("StreamToLoop") // <- Debug-only.
                            final String sanitizedKeys = '[' + properties.keySet().stream()
                                    .map((final Object key) -> '"' + SANITIZER.escape(key.toString()) + '"')
                                    .reduce((final String first, final String second) -> first + ", " + second)
                                    .orElse("") + ']';
                            LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: No update property found for key '{}'. (sanitizedKeys: {})", versionKey, sanitizedKeys);
                        }

                        // Stop.
                        return null;
                    }

                    // Validate the version length.
                    final int rawVersionLength = rawVersion.length();
                    if (rawVersionLength > MAX_COMPONENT_LENGTH) {
                        final String sanitizedRawVersion = SANITIZER.escape(rawVersion);
                        throw new IllegalStateException("HCsCR: Version is too long for update checking. (sanitizedRawVersion: '" + sanitizedRawVersion + "', rawVersionLength: " + rawVersionLength + ')');
                    }

                    // Parse the versions. skip if already up-to-date.
                    final Version currentVersionMeta = FabricLoader.getInstance().getModContainer("hcscr")
                            .orElseThrow()
                            .getMetadata()
                            .getVersion();
                    final Version currentVersionConstant = Version.parse(HCompile.VERSION);
                    final Version remoteVersion = Version.parse(rawVersion);
                    if ((currentVersionMeta.compareTo(remoteVersion) >= 0) &&
                            (currentVersionConstant.compareTo(remoteVersion) >= 0)) {
                        // Log. (**DEBUG**)
                        if (HCompile.DEBUG_LOGS && LOGGER.isDebugEnabled(HCsCR.HCSCR_MARKER)) {
                            final String sanitizedRemoteVersion = SANITIZER.escape(remoteVersion.toString());
                            LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Both current version from meta '{}' and from compilation '{}' are at least as up-to-date as the remote version '{}'.", currentVersionMeta, currentVersionConstant, sanitizedRemoteVersion);
                        }

                        // Stop.
                        return null;
                    }

                    // Extract the link.
                    final String linkKey = (gameVersion + '@' + channel + "@link");
                    final String rawLink = properties.getProperty(linkKey, FALLBACK_LINK);

                    // Validate the link.
                    final int rawLinkLength = rawLink.length();
                    if (rawLinkLength > MAX_COMPONENT_LENGTH) {
                        final String sanitizedRawLink = SANITIZER.escape(rawLink);
                        throw new IllegalStateException("HCsCR: Link is too long for update checking. (sanitizedRawLink: '" + sanitizedRawLink + "', rawLinkLength: " + rawLinkLength + ')');
                    }
                    final URI link;
                    try {
                        link = new URI(rawLink);
                    } catch (final Throwable t) {
                        final String sanitizedRawLink = SANITIZER.escape(rawLink);
                        throw new IllegalStateException("HCsCR: Invalid link URI for update checking. (sanitizedRawLink: '" + sanitizedRawLink + "')", t);
                    }
                    final String asciiLink = link.toASCIIString();
                    if (!"https".equals(link.getScheme()) || !"github.com".equals(link.getHost()) ||
                            (link.getPort() != -1) || (link.getRawQuery() != null) ||
                            (link.getRawFragment() != null) || (link.getRawUserInfo() != null)) {
                        throw new IllegalStateException("HCsCR: Invalid link data for update checking. (link: '" + asciiLink + "')");
                    }

                    // Log.
                    LOGGER.warn(HCsCR.HCSCR_MARKER, "HCsCR: Found an update from '{}' or '{}' to '{}' (for Minecraft '{}' and channel '{}'). Download at: {}", currentVersionMeta, currentVersionConstant, remoteVersion, rawVersion, channel, asciiLink);

                    // Create an update.
                    return new Update(channel, asciiLink, remoteVersion.getFriendlyString());
                } finally {
                    // Close the client if it's closable. (Java 21+)
                    //? if >=1.20.6 {
                    client.close();
                    //?} else {
                    /*if (client instanceof final AutoCloseable closeable) closeable.close();
                    *///?}
                }
            } catch (final Throwable t) {
                // Log.
                if (HCompile.DEBUG_LOGS) {
                    LOGGER.warn(HCsCR.HCSCR_MARKER, "HCsCR: Unable to check for updates.", t);
                } else {
                    LOGGER.warn(HCsCR.HCSCR_MARKER, "HCsCR: Unable to check for updates.");
                }

                // Return null. (no update)
                return null;
            }
        }

        @Contract(pure = true)
        @Override
        public String toString() {
            return "HCsCR/HModMenu$Updater{}";
        }
    }

    /**
     * Found HCsCR for the ModMenu to display.
     *
     * @author VidTu
     * @apiNote Internal use only
     */
    @ApiStatus.Internal
    @NullMarked
    static final class Update implements UpdateInfo {
        /**
         * The channel this update was found in.
         */
        private final UpdateChannel channel;

        /**
         * Update download link.
         */
        private final String link;

        /**
         * The update label to display in the ModMenu.
         */
        private final Component label;

        /**
         * Creates a new update.
         *
         * @param channel The channel this update was found in
         * @param link    Update download link
         * @param version New updated version
         */
        @Contract(pure = true)
        Update(final UpdateChannel channel, final String link, final String version) {
            // Validate.
            if (HCompile.DEBUG_ASSERTS) {
                assert channel != null : "HCsCR: Parameter 'channel' is null. (link: " + link + ", version: " + version + ", update: " + this + ')';
                assert link != null : "HCsCR: Parameter 'link' is null. (channel: " + channel + ", version: " + version + ", update: " + this + ')';
                assert version != null : "HCsCR: Parameter 'version' is null. (channel: " + channel + ", link: " + link + ", update: " + this + ')';
            }

            // Assign.
            this.channel = channel;
            this.link = link.intern(); // Implicit NPE for 'link'

            // Create the component.
            this.label = Component.translatable("modmenu.install_version", version.intern()); // Implicit NPE for 'version'
        }

        /**
         * Always returns {@code true}, because the mod
         * doesn't create an update without an update.
         *
         * @return {@code true}
         */
        @Contract(value = "-> true", pure = true)
        @Override
        public boolean isUpdateAvailable() {
            return true;
        }

        /**
         * Gets the label.
         *
         * @return The update label to display in the ModMenu
         */
        @Contract(pure = true)
        @Override
        public Component getUpdateMessage() {
            return this.label;
        }

        /**
         * Gets the link.
         *
         * @return Update download link
         */
        @Contract(pure = true)
        @Override
        public String getDownloadLink() {
            return this.link;
        }

        /**
         * Gets the channel.
         *
         * @return The channel this update was found in
         */
        @Contract(pure = true)
        @Override
        public UpdateChannel getUpdateChannel() {
            return this.channel;
        }

        @Contract(pure = true)
        @Override
        public String toString() {
            return "HCsCR/HModMenu$Update{" +
                    "channel=" + this.channel +
                    ", link='" + this.link + '\'' +
                    ", label=" + this.label +
                    '}';
        }
    }
    //?}
}
//?}
