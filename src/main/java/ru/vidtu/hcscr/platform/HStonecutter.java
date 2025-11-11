/*
 * HCsCR is a third-party mod for Minecraft Java Edition
 * that allows removing the end crystals faster.
 *
 * Copyright (c) 2023 Offenderify
 * Copyright (c) 2023-2025 VidTu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.vidtu.hcscr.platform;

import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import ru.vidtu.hcscr.HCsCR;
import ru.vidtu.hcscr.config.HScreen;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;

/**
 * A helper class that contains methods that depend on Stonecutter, a Java source code preprocessor.
 *
 * @author VidTu
 * @apiNote Internal use only
 */
@ApiStatus.Internal
@NullMarked
public final class HStonecutter {
    /**
     * Game config directory.
     */
    //? if fabric {
    public static final Path CONFIG_DIRECTORY = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir();
    //?} else if neoforge {
    /*public static final Path CONFIG_DIRECTORY = net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get();
    *///?} else
    /*public static final Path CONFIG_DIRECTORY = net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get();*/

    //? if >=1.21.10 {
    /**
     * Key category for {@link #keyBind(String)}.
     *
     * @see #keyBind(String)
     */
        //? if neoforge {
            /*//? if >=1.21.11 {
    /^package-private^/ static final KeyMapping.Category KEY_CATEGORY = new KeyMapping.Category(net.minecraft.resources.Identifier.fromNamespaceAndPath("hcscr", "root"));
            //?} else {
    /^/^¹package-private¹^/ static final KeyMapping.Category KEY_CATEGORY = new KeyMapping.Category(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("hcscr", "root"));
            ^///?}
        *///?} else {
            //? if >=1.21.11 {
    private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(net.minecraft.resources.Identifier.fromNamespaceAndPath("hcscr", "root"));
            //?} else {
    /*private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("hcscr", "root"));
            *///?}
        //?}
    //?}

    /**
     * A channel identifier for servers to know that this mod is installed.
     */
    //? if >=1.21.11 {
    /*package-private*/ static final net.minecraft.resources.Identifier CHANNEL_IDENTIFIER = net.minecraft.resources.Identifier.fromNamespaceAndPath("hcscr", "imhere");
    //?} elif >=1.21.1 || (forge && (!hacky_neoforge) && >=1.18.2 && (!1.20.2)) {
    /*/^package-private^/ static final net.minecraft.resources.ResourceLocation CHANNEL_IDENTIFIER = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("hcscr", "imhere");
    *///?} else
    /*/^package-private^/ static final net.minecraft.resources.ResourceLocation CHANNEL_IDENTIFIER = new net.minecraft.resources.ResourceLocation("hcscr", "imhere");*/

    /**
     * A duration for tooltips in version-dependant units. Currently {@code 250} milliseconds.
     */
    //? if >=1.20.6 {
    private static final java.time.Duration TOOLTIP_DURATION = java.time.Duration.ofMillis(250L);
    //?} else if >=1.19.4 {
    /*private static final int TOOLTIP_DURATION = 250; // Millis.
    *///?} else
    /*private static final long TOOLTIP_DURATION = 250_000_000L;*/ // Nanos.

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private HStonecutter() {
        throw new AssertionError("HCsCR: No instances.");
    }

    /**
     * Creates a new linear (array-baked) map that supports {@code setValue(int)} operations in iterators.
     * The created map will have initial capacity of {@code 0}.
     *
     * @param <T> Map value type
     * @return A newly created linear map
     * @implNote In some cases (due to a faulty implementation), a suboptimal hash-baked map will be used to allow {@code setValue(int)}
     */
    @Contract(value = "-> new", pure = true)
    public static <T> Object2IntMap<T> linearRemovableInt2ObjectMap() {
        // Ideally, an array-baked map should be always used here. Due to a bug in fastutil, setValue(int) is not
        // supported until 8.5.12: https://github.com/vigna/fastutil/blob/fcac58f7d3df8e7d903fad533f4caada7f4937cf/CHANGES#L4
        //? if >=1.21.4 {
        return new it.unimi.dsi.fastutil.objects.Object2IntArrayMap<>(0);
        //?} else
        /*return new it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap<>(0);*/
    }

    /**
     * Creates a new key bind. The key bind won't have a default key.
     *
     * @param id Keybind ID
     * @return A newly created unbound key bind
     * @see HCsCR#CONFIG_BIND
     * @see HCsCR#TOGGLE_BIND
     */
    @Contract(value = "_ -> new", pure = true)
    public static KeyMapping keyBind(String id) {
        // Validate.
        assert id != null : "HCsCR: Parameter 'id' is null.";
        assert !id.isEmpty() : "HCsCR: Creating a key binding with an empty ID.";

        // Delegate.
        //? if >=1.21.10 {
        return new KeyMapping(id, InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
        //?} else
        /*return new KeyMapping(id, InputConstants.UNKNOWN.getValue(), "key.category.hcscr.root");*/
    }

    /**
     * Creates a new translatable component.
     *
     * @param key Translation key
     * @return A new translatable component
     */
    @Contract(value = "_ -> new", pure = true)
    public static MutableComponent translate(String key) {
        // Validate.
        assert key != null : "HCsCR: Parameter 'key' is null.";
        assert !key.isEmpty() : "HCsCR: Creating a translatable component with an empty key.";

        // Delegate.
        //? if >=1.19.2 {
        return Component.translatable(key);
        //?} else
        /*return new net.minecraft.network.chat.TranslatableComponent(key);*/
    }

    /**
     * Creates a new translatable component.
     *
     * @param key  Translation key
     * @param args Translation args
     * @return A new translatable component
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static MutableComponent translate(String key, Object... args) {
        // Validate.
        assert key != null : "HCsCR: Parameter 'key' is null. (args: " + Arrays.toString(args) + ')';
        assert args != null : "HCsCR: Parameter 'args' is null. (key: " + key + ')';
        assert !key.isEmpty() : "HCsCR: Creating a translatable component with an empty key. (args: " + Arrays.toString(args) + ')';
        assert args.length != 0 : "HCsCR: Creating a translatable components with empty args array. (key: " + key + ')';

        // Delegate.
        //? if >=1.19.2 {
        return Component.translatable(key, args);
        //?} else
        /*return new net.minecraft.network.chat.TranslatableComponent(key, args);*/
    }

    /**
     * Gets the profiler of the game.
     *
     * @param game Current game instance
     * @return Game profiler
     */
    @Contract(pure = true)
    public static ProfilerFiller profilerOfGame(Minecraft game) {
        // Validate.
        assert game != null : "HCsCR: Parameter 'game' is null.";
        assert game.isSameThread() : "HCsCR: Getting the game profiler NOT from the main thread. (thread: " + Thread.currentThread() + ", game: " + game + ')';

        // Delegate.
        //? if >=1.21.3 {
        return net.minecraft.util.profiling.Profiler.get();
        //?} else
        /*return game.getProfiler();*/ // Implicit NPE for 'game'
    }

    /**
     * Gets the level of the entity.
     *
     * @param entity Target entity
     * @return The level (world) in which the entity is currently located or was last located
     */
    @Contract(pure = true)
    public static Level levelOfEntity(Entity entity) {
        // Validate.
        assert entity != null : "HCsCR: Parameter 'entity' is null.";
        // No thread checks here because this can be called from the integrated server.

        //? if >=1.20.1 {
        return entity.level(); // Implicit NPE for 'entity'
        //?} else
        /*return entity.level;*/ // Implicit NPE for 'entity'
    }

    /**
     * Checks whether the entity has been removed from the world or marked for removal from the world.
     *
     * @param entity Target entity to check
     * @return Whether the entity has been removed
     * @see #removeEntity(Entity)
     */
    @SuppressWarnings({"deprecation", "RedundantSuppression"}) // <- Forge 1.16.5.
    @Contract(pure = true)
    public static boolean isEntityRemoved(Entity entity) {
        // Validate.
        assert entity != null : "HCsCR: Parameter 'entity' is null.";
        assert Minecraft.getInstance().isSameThread() : "HCsCR: Checking entity removal NOT from the main thread. (thread: " + Thread.currentThread() + ", entity: " + entity + ')';

        // Delegate.
        //? if >=1.17.1 {
        return entity.isRemoved(); // Implicit NPE for 'entity'
        //?} else
        /*return entity.removed;*/ // Implicit NPE for 'entity'
    }

    /**
     * Gets the entity involved in the collision context.
     *
     * @param ctx Target context
     * @return Entity involving in the context, {@code null} if none
     */
    @Contract(pure = true)
    @Nullable
    public static Entity collisionContextEntity(EntityCollisionContext ctx) {
        // Validate.
        assert ctx != null : "HCsCR: Parameter 'ctx' is null.";
        // No thread checks here because this can be called from the integrated server.

        // Delegate.
        //? if >=1.18.2 {
        return ctx.getEntity(); // Implicit NPE for 'ctx'
        //?} else if >=1.17.1 {
        /*return ctx.getEntity().orElse(null); // Implicit NPE for 'ctx'
        *///?} else {
        /*//noinspection CastToIncompatibleInterface // <- Mixin Accessor.
        return ((ru.vidtu.hcscr.HEntityCollisionContext) ctx).hcscr_entity(); // Implicit NPE for 'ctx'
        *///?}
    }

    /**
     * Marks the entity as to be removed from the world.
     *
     * @param entity Target entity to remove
     * @see #isEntityRemoved(Entity)
     */
    public static void removeEntity(Entity entity) {
        // Validate.
        assert entity != null : "HCsCR: Parameter 'entity' is null.";
        assert Minecraft.getInstance().isSameThread() : "HCsCR: Removing an entity NOT from the main thread. (thread: " + Thread.currentThread() + ", entity: " + entity + ')';

        // Delegate.
        //? if >=1.17.1 {
        entity.discard(); // Implicit NPE for 'entity'
        //?} else
        /*entity.remove();*/ // Implicit NPE for 'entity'
    }

    /**
     * Hurts (damages) the entity the specified amount.
     *
     * @param entity Target entity to hurt
     * @param source Hurt source
     * @param amount Amount of damage
     * @return The result of the hurting
     */
    @SuppressWarnings({"deprecation", "RedundantSuppression"}) // <- Used in vanilla code. (1.21.3+)
    @CheckReturnValue
    public static boolean hurtEntity(Entity entity, DamageSource source, float amount) {
        // Validate.
        assert entity != null : "HCsCR: Parameter 'entity' is null. (source: " + source + ", amount: " + ')';
        assert source != null : "HCsCR: Parameter 'source' is null. (entity: " + entity + ", amount: " + ')';
        assert Float.isFinite(amount) : "HCsCR: Parameter 'amount' is not finite. (entity: " + entity + ", source: " + source + ", amount: " + ')';
        // No thread checks here because this can be called from the integrated server.

        // Delegate.
        //? if >=1.21.3 {
        return entity.hurtOrSimulate(source, amount); // Implicit NPE for 'entity', 'source'
        //?} else
        /*return entity.hurt(source, amount);*/ // Implicit NPE for 'entity', 'source'
    }

    /**
     * Adds the effect attributes onto the player.
     *
     * @param effect Effect to add the attributes from
     * @param player Target player
     * @param map    Player's attribute map
     */
    public static void addEffectAttributes(MobEffectInstance effect, LocalPlayer player, AttributeMap map) {
        // Validate.
        assert effect != null : "HCsCR: Parameter 'effect' is null. (player: " + player + ", map: " + map + ')';
        assert player != null : "HCsCR: Parameter 'player' is null. (effect: " + effect + ", map: " + map + ')';
        assert map != null : "HCsCR: Parameter 'map' is null. (effect: " + effect + ", player: " + player + ')';
        //noinspection ObjectEquality // <- Should be the same reference.
        assert player.getAttributes() == map : "HCsCR: Provided map is not from the provided player while adding attributes. (effect: " + effect + ", player: " + player + ", map: " + map + ", playerMap: " + player.getAttributes() + ')';
        assert player.getActiveEffects().contains(effect) : "HCsCR: Provided effect is not active for the player while adding attributes. (effect: " + effect + ", player: " + player + ", map: " + map + ", playerEffects: " + player.getActiveEffects() + ')';
        assert Minecraft.getInstance().isSameThread() : "HCsCR: Adding effect attributes NOT from the main thread. (thread: " + Thread.currentThread() + ", effect: " + effect + ", player: " + player + ", map: " + map + ')';

        // Delegate.
        //? if >=1.20.6 {
        effect.getEffect().value().addAttributeModifiers(map, effect.getAmplifier()); // Implicit NPE for 'effect', 'map'
        //?} else if >=1.20.2 {
        /*effect.getEffect().addAttributeModifiers(map, effect.getAmplifier()); // Implicit NPE for 'effect', 'map'
         *///?} else {
        /*effect.getEffect().addAttributeModifiers(player, map, effect.getAmplifier()); // Implicit NPE for 'player', 'effect', 'map'
         *///?}
    }

    /**
     * Removes the effect attributes onto the player.
     *
     * @param effect Effect to remove the attributes from
     * @param player Target player
     * @param map    Player's attribute map
     */
    public static void removeEffectAttributes(MobEffectInstance effect, LocalPlayer player, AttributeMap map) {
        // Validate.
        assert effect != null : "HCsCR: Parameter 'effect' is null. (player: " + player + ", map: " + map + ')';
        assert player != null : "HCsCR: Parameter 'player' is null. (effect: " + effect + ", map: " + map + ')';
        assert map != null : "HCsCR: Parameter 'map' is null. (effect: " + effect + ", player: " + player + ')';
        //noinspection ObjectEquality // <- Should be the same reference.
        assert player.getAttributes() == map : "HCsCR: Provided map is not from the provided player while removing attributes. (effect: " + effect + ", player: " + player + ", map: " + map + ", playerMap: " + player.getAttributes() + ')';
        assert player.getActiveEffects().contains(effect) : "HCsCR: Provided effect is not active for the player while removing attributes. (effect: " + effect + ", player: " + player + ", map: " + map + ", playerEffects: " + player.getActiveEffects() + ')';
        assert Minecraft.getInstance().isSameThread() : "HCsCR: Removing effect attributes NOT from the main thread. (thread: " + Thread.currentThread() + ", effect: " + effect + ", player: " + player + ", map: " + map + ')';

        // Delegate.
        //? if >=1.20.6 {
        effect.getEffect().value().removeAttributeModifiers(map); // Implicit NPE for 'effect', 'map'
        //?} else if >=1.20.2 {
        /*effect.getEffect().removeAttributeModifiers(map); // Implicit NPE for 'effect', 'map'
         *///?} else {
        /*effect.getEffect().removeAttributeModifiers(player, map, effect.getAmplifier()); // Implicit NPE for 'player', 'effect', 'map'
         *///?}
    }

    /**
     * Checks if the bed will explode when attempting to sleep.
     *
     * @param level Level in which bed is located in
     * @return {@code true} if the bed will explode, {@code false} if the player will sleep
     */
    @Contract(pure = true)
    public static boolean willBedExplode(Level level) {
        // Validate.
        assert level != null : "HCsCR: Parameter 'level' is null.";
        assert Minecraft.getInstance().isSameThread() : "HCsCR: Checking bed explosion status NOT from the main thread. (thread: " + Thread.currentThread() + ", level: " + level + ')';

        // Delegate.
        //? if >=1.21.11 {
        // Environmental attributes from 25w42a for BED_WORKS are NOT synced to the client,
        // so we just guess and check by comparing if the dimension doesn't have an OVERWORLD skybox.
        // TODO(VidTu): Check this in late 1.21.11 development cycle, as of 25w45a it is a enum, might change.
        return level.dimensionType().skybox() != net.minecraft.world.level.dimension.DimensionType.Skybox.OVERWORLD; // Implicit NPE for 'level'
        //?} else
        /*return !net.minecraft.world.level.block.BedBlock.canSetSpawn(level);*/ // Implicit NPE for 'level'
    }

    /**
     * Checks if the anchor will explode when attempting to set the spawn point.
     *
     * @param level Level in which anchor is located in
     * @return {@code true} if the anchor will explode, {@code false} if the player will set the spawn point
     */
    @Contract(pure = true)
    public static boolean willAnchorExplode(Level level) {
        // Validate.
        assert level != null : "HCsCR: Parameter 'level' is null.";
        assert Minecraft.getInstance().isSameThread() : "HCsCR: Checking anchor explosion status NOT from the main thread. (thread: " + Thread.currentThread() + ", level: " + level + ')';

        // Delegate.
        //? if >=1.21.11 {
        // Environmental attributes from 25w42a for RESPAWN_ANCHOR_WORKS are NOT synced to the client,
        // so we just guess and check by comparing if the dimension "doesn't" have skybox like NETHER.
        // TODO(VidTu): Check this in late 1.21.11 development cycle, as of 25w45a it is a enum, might change.
        return level.dimensionType().skybox() != net.minecraft.world.level.dimension.DimensionType.Skybox.NONE; // Implicit NPE for 'level'
        //?} else
        /*return !net.minecraft.world.level.block.RespawnAnchorBlock.canSetSpawn(level);*/ // Implicit NPE for 'level'
    }

    /**
     * Checks if the Shift key is down.
     *
     * @param game Current game instance
     * @return {@code true} if the Shift key is down, {@code false} if not
     */
    @Contract(pure = true)
    public static boolean isShiftKeyDown(Minecraft game) {
        // Validate.
        assert game != null : "HCsCR: Parameter 'game' is null.";
        assert game.isSameThread() : "HCsCR: Checking the shift key state NOT from the main thread. (thread: " + Thread.currentThread() + ", game: " + game + ')';

        // Delegate.
        //? if >=1.21.10 {
        return game.hasShiftDown(); // Implicit NPE for 'game'
        //?} else
        /*return net.minecraft.client.gui.screens.Screen.hasShiftDown();*/
    }

    /**
     * Creates a new GUI button instance.
     *
     * @param font            Font renderer used by the GUI
     * @param x               Button X position
     * @param y               Button Y position
     * @param width           Button width in scaled pixels
     * @param height          Button height in scaled pixels
     * @param message         Button label
     * @param tooltip         Button tooltip
     * @param handler         Button click handler (button itself and tooltip setter)
     * @param tooltipRenderer Last pass tooltip renderer, typically {@link HScreen}
     * @return A new button instance
     */
    @Contract(value = "_, _, _, _, _, _, _, _, _ -> new", pure = true)
    public static Button createButton(Font font, int x, int y, int width, int height, Component message,
                                      Component tooltip, BiConsumer<Button, Consumer<Component>> handler,
                                      Consumer<List<FormattedCharSequence>> tooltipRenderer) {
        // Validate.
        assert font != null : "HCsCR: Parameter 'font' is null. (x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", message: " + message + ", tooltip: " + tooltip + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
        assert (x >= -320) && (x <= Math.max(Minecraft.getInstance().getWindow().getGuiScaledWidth(), 320)) : "HCsCR: Parameter 'x' is not in the [" + -320 + ".." + Math.max(Minecraft.getInstance().getWindow().getGuiScaledWidth(), 320) + "] range. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", message: " + message + ", tooltip: " + tooltip + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
        assert (y >= -240) && (y <= Math.max(Minecraft.getInstance().getWindow().getGuiScaledHeight(), 240)) : "HCsCR: Parameter 'y' is not in the [" + -240 + ".." + Math.max(Minecraft.getInstance().getWindow().getGuiScaledHeight(), 240) + "] range. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", message: " + message + ", tooltip: " + tooltip + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
        assert width == 200 : "HCsCR: Parameter 'width' is not 200. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", message: " + message + ", tooltip: " + tooltip + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
        assert height == 20 : "HCsCR: Parameter 'height' is 20. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", message: " + message + ", tooltip: " + tooltip + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
        assert message != null : "HCsCR: Parameter 'message' is null. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", tooltip: " + tooltip + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
        assert tooltip != null : "HCsCR: Parameter 'tooltip' is null. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", message: " + message + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
        assert handler != null : "HCsCR: Parameter 'handler' is null. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", message: " + message + ", tooltip: " + tooltip + ", tooltipRenderer: " + tooltipRenderer + ')';
        assert tooltipRenderer != null : "HCsCR: Parameter 'tooltipRenderer' is null. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", message: " + message + ", tooltip: " + tooltip + ", handler: " + handler + ')';
        assert Minecraft.getInstance().isSameThread() : "HCsCR: Creating a button NOT from the main thread. (thread: " + Thread.currentThread() + ", font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", message: " + message + ", tooltip: " + tooltip + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';

        // Create.
        //? if >=1.19.4 {
        Button button = Button.builder(message, btn -> handler.accept(btn, tip -> { // Implicit NPE for 'handler'
            btn.setTooltip(net.minecraft.client.gui.components.Tooltip.create(tip));
            btn.setTooltipDelay(TOOLTIP_DURATION);
        })).tooltip(net.minecraft.client.gui.components.Tooltip.create(tooltip)).bounds(x, y, width, height).build();
        button.setTooltipDelay(TOOLTIP_DURATION);
        return button;
        //?} else {
        /*org.apache.commons.lang3.mutable.Mutable<List<FormattedCharSequence>> tipHolder = new org.apache.commons.lang3.mutable.MutableObject<>(font.split(tooltip, 170)); // Implicit NPE for 'font', 'tooltip'
        return new Button(x, y, width, height, message, btn -> handler.accept(btn, tip -> tipHolder.setValue(font.split(tip, 170)))) { // Implicit NPE for 'handler'
            /^*
             * Last time when the mouse was NOT over this element in units of {@link System#nanoTime()}.
             ^/
            private long lastAway = System.nanoTime();

            @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter") // <- Parameter names are not provided by Mojmap.
            @Override
            public void renderButton(com.mojang.blaze3d.vertex.PoseStack graphics, int mouseX, int mouseY, float delta) {
                // Render the element itself.
                super.renderButton(graphics, mouseX, mouseY, delta);

                // Button is not hovered, update the state.
                if (!this.isHovered) {
                    this.lastAway = System.nanoTime();
                    return;
                }

                // Button is not hovered for enough time.
                if ((System.nanoTime() - this.lastAway) < TOOLTIP_DURATION) return;

                // Render the tooltip.
                tooltipRenderer.accept(tipHolder.getValue()); // Implicit NPE for 'tooltipRenderer'
            }
        };
        *///?}
    }

    /**
     * Creates a new GUI checkbox instance.
     *
     * @param font            Font renderer used by the GUI
     * @param x               Checkbox X position
     * @param y               Checkbox Y position
     * @param message         Checkbox label
     * @param tooltip         Checkbox tooltip
     * @param check           Whether the checkbox is checked
     * @param handler         Checkbox click handler
     * @param tooltipRenderer Last pass tooltip renderer, typically {@link HScreen}
     * @return A new checkbox instance
     */
    @SuppressWarnings("BooleanParameter") // <- Boolean method used as a state, not as control flow. (checkbox "checked" state)
    @Contract(value = "_, _, _, _, _, _, _, _ -> new", pure = true)
    public static Checkbox createCheckbox(Font font, int x, int y, Component message, Component tooltip, boolean check,
                                          BooleanConsumer handler, Consumer<List<FormattedCharSequence>> tooltipRenderer) {
        // Validate.
        assert font != null : "HCsCR: Parameter 'font' is null. (x: " + x + ", y: " + y + ", message: " + message + ", tooltip: " + tooltip + ", check: " + check + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
        assert (x >= -320) && (x <= Math.max(Minecraft.getInstance().getWindow().getGuiScaledWidth(), 320)) : "HCsCR: Parameter 'x' is not in the [" + -320 + ".." + Math.max(Minecraft.getInstance().getWindow().getGuiScaledWidth(), 320) + "] range. (font: " + font + ", x: " + x + ", y: " + y + ", message: " + message + ", tooltip: " + tooltip + ", check: " + check + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
        assert (y >= -240) && (y <= Math.max(Minecraft.getInstance().getWindow().getGuiScaledHeight(), 240)) : "HCsCR: Parameter 'y' is not in the [" + -240 + ".." + Math.max(Minecraft.getInstance().getWindow().getGuiScaledHeight(), 240) + "] range. (font: " + font + ", x: " + x + ", y: " + y + ", message: " + message + ", tooltip: " + tooltip + ", check: " + check + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
        assert message != null : "HCsCR: Parameter 'message' is null. (font: " + font + ", x: " + x + ", y: " + y + ", tooltip: " + tooltip + ", check: " + check + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
        assert tooltip != null : "HCsCR: Parameter 'tooltip' is null. (font: " + font + ", x: " + x + ", y: " + y + ", message: " + message + ", check: " + check + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
        assert handler != null : "HCsCR: Parameter 'handler' is null. (font: " + font + ", x: " + x + ", y: " + y + ", message: " + message + ", tooltip: " + tooltip + ", check: " + check + ", tooltipRenderer: " + tooltipRenderer + ')';
        assert tooltipRenderer != null : "HCsCR: Parameter 'tooltipRenderer' is null. (font: " + font + ", x: " + x + ", y: " + y + ", message: " + message + ", tooltip: " + tooltip + ", check: " + check + ", handler: " + handler + ')';
        assert Minecraft.getInstance().isSameThread() : "HCsCR: Creating a checkbox NOT from the main thread. (thread: " + Thread.currentThread() + ", font: " + font + ", x: " + x + ", y: " + y + ", message: " + message + ", tooltip: " + tooltip + ", check: " + check + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';

        // Create.
        //? if >=1.20.4 {
        Checkbox box = Checkbox.builder(message, font) // Implicit NPE for 'message', 'font'
                .pos(x - ((font.width(message) + 24) / 2), y)
                .selected(check)
                .onValueChange((checkbox, value) -> handler.accept(value)) // Implicit NPE for 'handler'
                .build();
        //?} else {
        /*int width = font.width(message) + 24; // Implicit NPE for 'font', 'message'
        Checkbox box = new Checkbox(x - (width / 2), y, width, 20, message, check) {
            @Override
            public void onPress() {
                // Toggle the checkbox.
                super.onPress();

                // Invoke the handler.
                handler.accept(this.selected()); // Implicit NPE for 'handler'
            }

            //? if <1.19.4 {
            /^/^¹*
             * A tooltip split to {@code 170} scaled pixels wide, a value used in modern versions
             ¹^/
            private final List<FormattedCharSequence> tip = font.split(tooltip, 170); // Implicit NPE for 'tooltip'

            /^¹*
             * Last time when the mouse was NOT over this element in units of {@link System#nanoTime()}.
             ¹^/
            private long lastAway = System.nanoTime();

            @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter") // <- Parameter names are not provided by Mojmap.
            @Override
            public void renderButton(com.mojang.blaze3d.vertex.PoseStack graphics, int mouseX, int mouseY, float delta) {
                // Render the element itself.
                super.renderButton(graphics, mouseX, mouseY, delta);

                // Button is not hovered, update the state.
                if (!this.isHovered) {
                    this.lastAway = System.nanoTime();
                    return;
                }

                // Button is not hovered for enough time.
                if ((System.nanoTime() - this.lastAway) < TOOLTIP_DURATION) return;

                // Render the tooltip.
                tooltipRenderer.accept(this.tip); // Implicit NPE for 'tooltipRenderer'
            }
            ^///?}
        };
        *///?}
        //? if >=1.19.4 {
        box.setTooltip(net.minecraft.client.gui.components.Tooltip.create(tooltip));
        box.setTooltipDelay(TOOLTIP_DURATION);
        //?}
        return box;
    }

    /**
     * Creates a new GUI slider instance.
     *
     * @param font            Font renderer used by the GUI
     * @param x               Slider X position
     * @param y               Slider Y position
     * @param width           Slider width in scaled pixels
     * @param height          Slider height in scaled pixels
     * @param provider        Slider label provider by value
     * @param tooltip         Slider tooltip
     * @param value           Slider value
     * @param min             Slider minimum allowed value
     * @param max             Slider maximum value
     * @param handler         Slider move handler
     * @param tooltipRenderer Last pass tooltip renderer, typically {@link HScreen}
     * @return A new slider instance
     */
    public static AbstractSliderButton createSlider(Font font, int x, int y, int width, int height,
                                                    IntFunction<Component> provider, Component tooltip,
                                                    int value, int min, int max, IntConsumer handler,
                                                    Consumer<List<FormattedCharSequence>> tooltipRenderer) {
        // Validate.
        assert font != null : "HCsCR: Parameter 'font' is null. (x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
        assert (x >= -320) && (x <= Math.max(Minecraft.getInstance().getWindow().getGuiScaledWidth(), 320)) : "HCsCR: Parameter 'x' is not in the [" + -320 + ".." + Math.max(Minecraft.getInstance().getWindow().getGuiScaledWidth(), 320) + "] range. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
        assert (y >= -240) && (y <= Math.max(Minecraft.getInstance().getWindow().getGuiScaledHeight(), 240)) : "HCsCR: Parameter 'y' is not in the [" + -240 + ".." + Math.max(Minecraft.getInstance().getWindow().getGuiScaledHeight(), 240) + "] range. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
        assert width == 200 : "HCsCR: Parameter 'width' is not 200. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
        assert height == 20 : "HCsCR: Parameter 'height' is 20. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
        assert provider != null : "HCsCR: Parameter 'provider' is null. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
        assert tooltip != null : "HCsCR: Parameter 'tooltip' is null. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
        assert max > min : "HCsCR: Parameter 'min' is not bigger than 'max'. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
        assert value >= min : "HCsCR: Parameter 'value' is smaller than 'min'. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
        assert value <= max : "HCsCR: Parameter 'value' is bigger than 'max'. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
        assert handler != null : "HCsCR: Parameter 'handler' is null. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", tooltipRenderer: " + tooltipRenderer + ')';
        assert tooltipRenderer != null : "HCsCR: Parameter 'tooltipRenderer' is null. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ')';
        assert Minecraft.getInstance().isSameThread() : "HCsCR: Creating a slider NOT from the main thread. (thread: " + Thread.currentThread() + ", font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';

        // Create the slider.
        int clamped = Mth.clamp(value, min, max);
        double normalized = (double) (clamped - min) / (max - min);
        AbstractSliderButton slider = new AbstractSliderButton(x, y, width, height, provider.apply(clamped), normalized) { // Implicit NPE for 'provider'
            /**
             * A denormalized value, i.e. back in its original range.
             */
            private int denormalized = clamped;

            @Override
            protected void updateMessage() {
                this.setMessage(provider.apply(this.denormalized));
            }

            @Override
            protected void applyValue() {
                int denormalized = this.denormalized = (int) Math.round(Mth.lerp(this.value, min, max));
                handler.accept(denormalized); // Implicit NPE for 'handler'
            }

            //? if <1.19.4 {
            /*/^*
             * A tooltip split to {@code 170} scaled pixels wide, a value used in modern versions
             ^/
            private final List<FormattedCharSequence> tip = font.split(tooltip, 170); // Implicit NPE for 'font', 'tooltip'

            /^*
             * Last time when the mouse was NOT over this element in units of {@link System#nanoTime()}.
             ^/
            private long lastAway = System.nanoTime();

            @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter") // <- Parameter names are not provided by Mojmap.
            @Override
            public void renderButton(com.mojang.blaze3d.vertex.PoseStack graphics, int mouseX, int mouseY, float delta) {
                // Render the element itself.
                super.renderButton(graphics, mouseX, mouseY, delta);

                // Button is not hovered, update the state.
                if (!this.isHovered) {
                    this.lastAway = System.nanoTime();
                    return;
                }

                // Button is not hovered for enough time.
                if ((System.nanoTime() - this.lastAway) < TOOLTIP_DURATION) return;

                // Render the tooltip.
                tooltipRenderer.accept(this.tip); // Implicit NPE for 'tooltipRenderer'
            }
            *///?}
        };
        //? if >=1.19.4 {
        slider.setTooltip(net.minecraft.client.gui.components.Tooltip.create(tooltip));
        slider.setTooltipDelay(TOOLTIP_DURATION);
        //?}
        return slider;
    }
}
