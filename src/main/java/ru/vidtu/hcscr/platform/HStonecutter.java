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

//? if >=1.21.11 {
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.resources.Identifier;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.level.dimension.DimensionType;
import java.time.Duration;
//?} elif >=1.21.8 {
/*import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import java.time.Duration;
*///?} elif >=1.21.4 {
/*import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import java.time.Duration;
*///?} elif >=1.21.3 {
/*import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import java.time.Duration;
*///?} elif >=1.20.6 {
/*import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import java.time.Duration;
*///?} elif >=1.19.4 {
/*import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
*///?} elif >=1.19.2 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import org.apache.commons.lang3.mutable.MutableObject;
*///?} elif >=1.17.1 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import org.apache.commons.lang3.mutable.MutableObject;
*///?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import org.apache.commons.lang3.mutable.MutableObject;
import ru.vidtu.hcscr.HEntityCollisionContext;
*///?}

//? if fabric {
import net.fabricmc.loader.api.FabricLoader;
//?} elif neoforge {
/*import net.neoforged.fml.loading.FMLPaths;
*///?} else {
/*import net.minecraftforge.fml.loading.FMLPaths;
*///?}

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
    public static final Path CONFIG_DIRECTORY = FabricLoader.getInstance().getConfigDir();
    //?} else {
    /*public static final Path CONFIG_DIRECTORY = FMLPaths.CONFIGDIR.get();
    *///?}

    //? if >=1.21.10 {
    /**
     * Key category for {@link #keyBind(String)}.
     *
     * @see #keyBind(String)
     */
        //? if neoforge {
            /*//? if >=1.21.11 {
    /^/^¹package-private¹^/ static final KeyMapping.Category KEY_CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath("hcscr", "root"));
            ^///?} else {
    /^package-private^/ static final KeyMapping.Category KEY_CATEGORY = new KeyMapping.Category(ResourceLocation.fromNamespaceAndPath("hcscr", "root"));
            //?}
        *///?} else {
            //? if >=1.21.11 {
    private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("hcscr", "root"));
            //?} else {
    /*private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(ResourceLocation.fromNamespaceAndPath("hcscr", "root"));
            *///?}
        //?}
    //?}

    /**
     * A channel identifier for servers to know that this mod is installed.
     */
    //? if >=1.21.11 {
    /*package-private*/ static final Identifier CHANNEL_IDENTIFIER = Identifier.fromNamespaceAndPath("hcscr", "imhere");
    //?} elif >=1.21.1 || (forge && (!hacky_neoforge) && >=1.18.2 && (!1.20.2)) {
    /*/^package-private^/ static final ResourceLocation CHANNEL_IDENTIFIER = ResourceLocation.fromNamespaceAndPath("hcscr", "imhere");
    *///?} else {
    /*/^package-private^/ static final ResourceLocation CHANNEL_IDENTIFIER = new ResourceLocation("hcscr", "imhere");
    *///?}

    /**
     * A duration for tooltips in version-dependant units. Currently {@code 250} milliseconds.
     */
    //? if >=1.20.6 {
    private static final Duration TOOLTIP_DURATION = Duration.ofMillis(250L);
    //?} elif >=1.19.4 {
    /*private static final int TOOLTIP_DURATION = 250; // Millis.
    *///?} else {
    /*private static final long TOOLTIP_DURATION = 250_000_000L; // Nanos.
    *///?}

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
        return new Object2IntArrayMap<>(0);
        //?} else {
        /*return new Object2IntOpenHashMap<>(0);
        *///?}
    }

    /**
     * Creates a new key bind. The key bind won't have a default key.
     *
     * @param id Key bind ID
     * @return A newly created unbound key bind
     * @see HCsCR#CONFIG_BIND
     * @see HCsCR#TOGGLE_BIND
     */
    @Contract(value = "_ -> new", pure = true)
    public static KeyMapping keyBind(final String id) {
        // Validate.
        if (HCompile.DEBUG_ASSERTS) {
            assert (id != null) : "HCsCR: Parameter 'id' is null.";
            assert (!id.isEmpty()) : "HCsCR: Creating a key binding with an empty ID.";
        }

        // Delegate.
        //? if >=1.21.10 {
        return new KeyMapping(id, InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
        //?} else {
        /*return new KeyMapping(id, InputConstants.UNKNOWN.getValue(), "key.category.hcscr.root");
        *///?}
    }

    /**
     * Creates a new translatable component.
     *
     * @param key Translation key
     * @return A new translatable component
     */
    @Contract(value = "_ -> new", pure = true)
    public static MutableComponent translate(final String key) {
        // Validate.
        if (HCompile.DEBUG_ASSERTS) {
            assert (key != null) : "HCsCR: Parameter 'key' is null.";
            assert (!key.isEmpty()) : "HCsCR: Creating a translatable component with an empty key.";
        }

        // Delegate.
        //? if >=1.19.2 {
        return Component.translatable(key);
        //?} else {
        /*return new TranslatableComponent(key);
        *///?}
    }

    /**
     * Creates a new translatable component.
     *
     * @param key  Translation key
     * @param args Translation args
     * @return A new translatable component
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static MutableComponent translate(final String key, final Object... args) {
        // Validate.
        if (HCompile.DEBUG_ASSERTS) {
            assert (key != null) : "HCsCR: Parameter 'key' is null. (args: " + Arrays.toString(args) + ')';
            assert (args != null) : "HCsCR: Parameter 'args' is null. (key: " + key + ')';
            assert (!key.isEmpty()) : "HCsCR: Creating a translatable component with an empty key. (args: " + Arrays.toString(args) + ')';
            assert (args.length != 0) : "HCsCR: Creating a translatable components with empty args array. (key: " + key + ')';
        }

        // Delegate.
        //? if >=1.19.2 {
        return Component.translatable(key, args);
        //?} else {
        /*return new TranslatableComponent(key, args);
        *///?}
    }

    /**
     * Gets the profiler of the game client.
     *
     * @param client Client game instance
     * @return Client profiler
     */
    @Contract(pure = true)
    public static ProfilerFiller profilerOfClient(final Minecraft client) {
        // Validate.
        if (HCompile.DEBUG_ASSERTS) {
            assert (client != null) : "HCsCR: Parameter 'client' is null.";
            assert (client.isSameThread()) : "HCsCR: Getting the client profiler NOT from the main thread. (thread: " + Thread.currentThread() + ", client: " + client + ')';
        }

        // TODO(VidTu): Integrate with HCompile?
        // Delegate.
        //? if >=1.21.3 {
        return Profiler.get();
        //?} else {
        /*return client.getProfiler(); // Implicit NPE for 'client'
        *///?}
    }

    /**
     * Gets the level of the entity.
     *
     * @param entity Target entity to get the level of
     * @return The level (world) in which the entity is currently located or was last located
     */
    @Contract(pure = true)
    public static Level levelOfEntity(final Entity entity) {
        // Validate.
        if (HCompile.DEBUG_ASSERTS) {
            assert (entity != null) : "HCsCR: Parameter 'entity' is null.";
            // No thread checks here because this can be called from the integrated server.
        }

        //? if >=1.20.1 {
        return entity.level(); // Implicit NPE for 'entity'
        //?} else {
        /*return entity.level; // Implicit NPE for 'entity'
        *///?}
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
    public static boolean isEntityRemoved(final Entity entity) {
        // Validate.
        if (HCompile.DEBUG_ASSERTS) {
            assert (entity != null) : "HCsCR: Parameter 'entity' is null.";
            assert (Minecraft.getInstance().isSameThread()) : "HCsCR: Checking entity removal NOT from the main thread. (thread: " + Thread.currentThread() + ", entity: " + entity + ')';
        }

        // Delegate.
        //? if >=1.17.1 {
        return entity.isRemoved(); // Implicit NPE for 'entity'
        //?} else {
        /*return entity.removed; // Implicit NPE for 'entity'
        *///?}
    }

    /**
     * Gets the entity involved in the collision context.
     *
     * @param ctx Target collision context to get the entity from
     * @return Entity involving in the context, {@code null} if none
     */
    @Contract(pure = true)
    @Nullable
    public static Entity collisionContextEntity(final EntityCollisionContext ctx) {
        // Validate.
        if (HCompile.DEBUG_ASSERTS) {
            assert (ctx != null) : "HCsCR: Parameter 'ctx' is null.";
            // No thread checks here because this can be called from the integrated server.
        }

        // Delegate.
        //? if >=1.18.2 {
        return ctx.getEntity(); // Implicit NPE for 'ctx'
        //?} elif >=1.17.1 {
        /*return ctx.getEntity().orElse(null); // Implicit NPE for 'ctx'
        *///?} else {
        /*//noinspection CastToIncompatibleInterface // <- Mixin Accessor.
        return ((HEntityCollisionContext) ctx).hcscr_entity(); // Implicit NPE for 'ctx'
        *///?}
    }

    /**
     * Marks the entity as to be removed from the world.
     *
     * @param entity Target entity to remove
     * @see #isEntityRemoved(Entity)
     */
    public static void removeEntity(final Entity entity) {
        // Validate.
        if (HCompile.DEBUG_ASSERTS) {
            assert (entity != null) : "HCsCR: Parameter 'entity' is null.";
            assert (Minecraft.getInstance().isSameThread()) : "HCsCR: Removing an entity NOT from the main thread. (thread: " + Thread.currentThread() + ", entity: " + entity + ')';
        }

        // Delegate.
        //? if >=1.17.1 {
        entity.discard(); // Implicit NPE for 'entity'
        //?} else {
        /*entity.remove(); // Implicit NPE for 'entity'
        *///?}
    }

    /**
     * Hurts (damages) the entity the specified amount.
     *
     * @param entity Target entity to hurt
     * @param source Attack source (inaccurate if invoked on the client)
     * @param amount Total amount of damage done to the entity (inaccurate if invoked on the client)
     * @return The result of the hurting
     */
    @SuppressWarnings({"deprecation", "RedundantSuppression"}) // <- Used in vanilla code. (1.21.3+)
    @CheckReturnValue
    public static boolean hurtEntity(final Entity entity, final DamageSource source, final float amount) {
        // Validate.
        if (HCompile.DEBUG_ASSERTS) {
            assert (entity != null) : "HCsCR: Parameter 'entity' is null. (source: " + source + ", amount: " + ')';
            assert (source != null) : "HCsCR: Parameter 'source' is null. (entity: " + entity + ", amount: " + ')';
            assert (Float.isFinite(amount)) : "HCsCR: Parameter 'amount' is not finite. (entity: " + entity + ", source: " + source + ", amount: " + ')';
            // No thread checks here because this can be called from the integrated server.
        }

        // Delegate.
        //? if >=1.21.3 {
        return entity.hurtOrSimulate(source, amount); // Implicit NPE for 'entity', 'source'
        //?} else {
        /*return entity.hurt(source, amount); // Implicit NPE for 'entity', 'source'
        *///?}
    }

    /**
     * Adds the effect attributes onto the player.
     *
     * @param effect Effect to add the attributes from
     * @param player Target player
     * @param map    Player's attribute map
     */
    public static void addEffectAttributes(final MobEffectInstance effect, final LocalPlayer player,
                                           final AttributeMap map) {
        // Validate.
        if (HCompile.DEBUG_ASSERTS) {
            assert (effect != null) : "HCsCR: Parameter 'effect' is null. (player: " + player + ", map: " + map + ')';
            assert (player != null) : "HCsCR: Parameter 'player' is null. (effect: " + effect + ", map: " + map + ')';
            assert (map != null) : "HCsCR: Parameter 'map' is null. (effect: " + effect + ", player: " + player + ')';
            //noinspection ObjectEquality // <- Should be the same reference.
            assert (player.getAttributes() == map) : "HCsCR: Provided map is not from the provided player while adding attributes. (effect: " + effect + ", player: " + player + ", map: " + map + ", playerMap: " + player.getAttributes() + ')';
            assert (player.getActiveEffects().contains(effect)) : "HCsCR: Provided effect is not active for the player while adding attributes. (effect: " + effect + ", player: " + player + ", map: " + map + ", playerEffects: " + player.getActiveEffects() + ')';
            assert (Minecraft.getInstance().isSameThread()) : "HCsCR: Adding effect attributes NOT from the main thread. (thread: " + Thread.currentThread() + ", effect: " + effect + ", player: " + player + ", map: " + map + ')';
        }

        // Delegate.
        //? if >=1.20.6 {
        effect.getEffect().value().addAttributeModifiers(map, effect.getAmplifier()); // Implicit NPE for 'effect', 'map'
        //?} elif >=1.20.2 {
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
    public static void removeEffectAttributes(final MobEffectInstance effect, final LocalPlayer player,
                                              final AttributeMap map) {
        // Validate.
        if (HCompile.DEBUG_ASSERTS) {
            assert (effect != null) : "HCsCR: Parameter 'effect' is null. (player: " + player + ", map: " + map + ')';
            assert (player != null) : "HCsCR: Parameter 'player' is null. (effect: " + effect + ", map: " + map + ')';
            assert (map != null) : "HCsCR: Parameter 'map' is null. (effect: " + effect + ", player: " + player + ')';
            //noinspection ObjectEquality // <- Should be the same reference.
            assert (player.getAttributes() == map) : "HCsCR: Provided map is not from the provided player while removing attributes. (effect: " + effect + ", player: " + player + ", map: " + map + ", playerMap: " + player.getAttributes() + ')';
            assert (player.getActiveEffects().contains(effect)) : "HCsCR: Provided effect is not active for the player while removing attributes. (effect: " + effect + ", player: " + player + ", map: " + map + ", playerEffects: " + player.getActiveEffects() + ')';
            assert (Minecraft.getInstance().isSameThread()) : "HCsCR: Removing effect attributes NOT from the main thread. (thread: " + Thread.currentThread() + ", effect: " + effect + ", player: " + player + ", map: " + map + ')';
        }

        // Delegate.
        //? if >=1.20.6 {
        effect.getEffect().value().removeAttributeModifiers(map); // Implicit NPE for 'effect', 'map'
        //?} elif >=1.20.2 {
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
    public static boolean willBedExplode(final Level level) {
        // Validate.
        if (HCompile.DEBUG_ASSERTS) {
            assert (level != null) : "HCsCR: Parameter 'level' is null.";
            assert (Minecraft.getInstance().isSameThread()) : "HCsCR: Checking bed explosion status NOT from the main thread. (thread: " + Thread.currentThread() + ", level: " + level + ')';
        }

        // Delegate.
        //? if >=1.21.11 {
        // Environmental attributes from 25w42a for BED_WORKS are NOT synced to the client,
        // so we just guess and check by comparing if the dimension doesn't have an OVERWORLD skybox.
        return level.dimensionType().skybox() != DimensionType.Skybox.OVERWORLD; // Implicit NPE for 'level'
        //?} else {
        /*return !BedBlock.canSetSpawn(level); // Implicit NPE for 'level'
        *///?}
    }

    /**
     * Checks if the anchor will explode when attempting to set the spawn point.
     *
     * @param level Level in which anchor is located in
     * @return {@code true} if the anchor will explode, {@code false} if the player will set the spawn point
     */
    @Contract(pure = true)
    public static boolean willAnchorExplode(final Level level) {
        // Validate.
        if (HCompile.DEBUG_ASSERTS) {
            assert (level != null) : "HCsCR: Parameter 'level' is null.";
            assert (Minecraft.getInstance().isSameThread()) : "HCsCR: Checking anchor explosion status NOT from the main thread. (thread: " + Thread.currentThread() + ", level: " + level + ')';
        }

        // Delegate.
        //? if >=1.21.11 {
        // Environmental attributes from 25w42a for RESPAWN_ANCHOR_WORKS are NOT synced to the client,
        // so we just guess and check by comparing if the dimension "doesn't" have skybox like NETHER.
        return level.dimensionType().skybox() != DimensionType.Skybox.NONE; // Implicit NPE for 'level'
        //?} else {
        /*return !RespawnAnchorBlock.canSetSpawn(level); // Implicit NPE for 'level'
        *///?}
    }

    /**
     * Checks if the Shift key is down.
     *
     * @param client Client game instance
     * @return {@code true} if the Shift key is down, {@code false} if not
     */
    @Contract(pure = true)
    public static boolean isShiftKeyDown(final Minecraft client) {
        // Validate.
        if (HCompile.DEBUG_ASSERTS) {
            assert (client != null) : "HCsCR: Parameter 'client' is null.";
            assert (client.isSameThread()) : "HCsCR: Checking the shift key state NOT from the main thread. (thread: " + Thread.currentThread() + ", client: " + client + ')';
        }

        // Delegate.
        //? if >=1.21.10 {
        return client.hasShiftDown(); // Implicit NPE for 'client'
        //?} else {
        /*return Screen.hasShiftDown();
        *///?}
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
    public static Button createButton(final Font font, final int x, final int y, final int width, final int height,
                                      final Component message, final Component tooltip,
                                      final BiConsumer<Button, Consumer<Component>> handler,
                                      final Consumer<List<FormattedCharSequence>> tooltipRenderer) {
        // Validate.
        if (HCompile.DEBUG_ASSERTS) {
            assert (font != null) : "HCsCR: Parameter 'font' is null. (x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", message: " + message + ", tooltip: " + tooltip + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
            assert ((x >= -320) && (x <= Math.max(Minecraft.getInstance().getWindow().getGuiScaledWidth(), 320))) : "HCsCR: Parameter 'x' is not in the [" + -320 + ".." + Math.max(Minecraft.getInstance().getWindow().getGuiScaledWidth(), 320) + "] range. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", message: " + message + ", tooltip: " + tooltip + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
            assert ((y >= -240) && (y <= Math.max(Minecraft.getInstance().getWindow().getGuiScaledHeight(), 240))) : "HCsCR: Parameter 'y' is not in the [" + -240 + ".." + Math.max(Minecraft.getInstance().getWindow().getGuiScaledHeight(), 240) + "] range. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", message: " + message + ", tooltip: " + tooltip + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
            assert (width == 200) : "HCsCR: Parameter 'width' is not 200. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", message: " + message + ", tooltip: " + tooltip + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
            assert (height == 20) : "HCsCR: Parameter 'height' is 20. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", message: " + message + ", tooltip: " + tooltip + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
            assert (message != null) : "HCsCR: Parameter 'message' is null. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", tooltip: " + tooltip + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
            assert (tooltip != null) : "HCsCR: Parameter 'tooltip' is null. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", message: " + message + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
            assert (handler != null) : "HCsCR: Parameter 'handler' is null. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", message: " + message + ", tooltip: " + tooltip + ", tooltipRenderer: " + tooltipRenderer + ')';
            assert (tooltipRenderer != null) : "HCsCR: Parameter 'tooltipRenderer' is null. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", message: " + message + ", tooltip: " + tooltip + ", handler: " + handler + ')';
            assert (Minecraft.getInstance().isSameThread()) : "HCsCR: Creating a button NOT from the main thread. (thread: " + Thread.currentThread() + ", font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", message: " + message + ", tooltip: " + tooltip + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
        }

        // Create.
        //? if >=1.19.4 {
        final Button button = Button.builder(message, (final Button innerButton) -> handler.accept(innerButton, (final Component newTip) -> { // Implicit NPE for 'handler'
            // Validate.
            if (HCompile.DEBUG_ASSERTS) {
                assert (newTip != null) : "HCsCR: Parameter 'newTip' is null. (innerButton: " + innerButton + ')';
                assert (Minecraft.getInstance().isSameThread()) : "HCsCR: Setting a button tip NOT from the main thread. (thread: " + Thread.currentThread() + ", newTip: " + newTip + ", innerButton: " + innerButton + ')';
            }

            // Set.
            innerButton.setTooltip(Tooltip.create(newTip));
            innerButton.setTooltipDelay(TOOLTIP_DURATION);
        })).tooltip(Tooltip.create(tooltip)).bounds(x, y, width, height).build();
        button.setTooltipDelay(TOOLTIP_DURATION);
        return button;
        //?} else {
        /*final MutableObject<List<FormattedCharSequence>> tipHolder = new MutableObject<>(font.split(tooltip, 170)); // Implicit NPE for 'font', 'tooltip'
        return new Button(x, y, width, height, message, (final Button innerButton) -> handler.accept(innerButton, (final Component newTip) -> { // Implicit NPE for 'handler'
            // Validate.
            if (HCompile.DEBUG_ASSERTS) {
                assert (newTip != null) : "HCsCR: Parameter 'newTip' is null. (innerButton: " + innerButton + ')';
                assert (Minecraft.getInstance().isSameThread()) : "HCsCR: Setting a button tip NOT from the main thread. (thread: " + Thread.currentThread() + ", newTip: " + newTip + ", innerButton: " + innerButton + ')';
            }

            // Set.
            tipHolder.setValue(font.split(newTip, 170));
        })) {
            /^*
             * Last time when the mouse was NOT over this element in units of {@link System#nanoTime()}.
             ^/
            private /^non-final^/ long lastAway = System.nanoTime();

            @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter") // <- Parameter names are not provided by Mojmap.
            @Override
            public void renderButton(final PoseStack graphics, final int mouseX, final int mouseY, final float delta) {
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
    public static Checkbox createCheckbox(final Font font, final int x, final int y, final Component message,
                                          final Component tooltip, final boolean check, final BooleanConsumer handler,
                                          final Consumer<List<FormattedCharSequence>> tooltipRenderer) {
        // Validate.
        if (HCompile.DEBUG_ASSERTS) {
            assert (font != null) : "HCsCR: Parameter 'font' is null. (x: " + x + ", y: " + y + ", message: " + message + ", tooltip: " + tooltip + ", check: " + check + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
            assert ((x >= -320) && (x <= Math.max(Minecraft.getInstance().getWindow().getGuiScaledWidth(), 320))) : "HCsCR: Parameter 'x' is not in the [" + -320 + ".." + Math.max(Minecraft.getInstance().getWindow().getGuiScaledWidth(), 320) + "] range. (font: " + font + ", x: " + x + ", y: " + y + ", message: " + message + ", tooltip: " + tooltip + ", check: " + check + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
            assert ((y >= -240) && (y <= Math.max(Minecraft.getInstance().getWindow().getGuiScaledHeight(), 240))) : "HCsCR: Parameter 'y' is not in the [" + -240 + ".." + Math.max(Minecraft.getInstance().getWindow().getGuiScaledHeight(), 240) + "] range. (font: " + font + ", x: " + x + ", y: " + y + ", message: " + message + ", tooltip: " + tooltip + ", check: " + check + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
            assert (message != null) : "HCsCR: Parameter 'message' is null. (font: " + font + ", x: " + x + ", y: " + y + ", tooltip: " + tooltip + ", check: " + check + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
            assert (tooltip != null) : "HCsCR: Parameter 'tooltip' is null. (font: " + font + ", x: " + x + ", y: " + y + ", message: " + message + ", check: " + check + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
            assert (handler != null) : "HCsCR: Parameter 'handler' is null. (font: " + font + ", x: " + x + ", y: " + y + ", message: " + message + ", tooltip: " + tooltip + ", check: " + check + ", tooltipRenderer: " + tooltipRenderer + ')';
            assert (tooltipRenderer != null) : "HCsCR: Parameter 'tooltipRenderer' is null. (font: " + font + ", x: " + x + ", y: " + y + ", message: " + message + ", tooltip: " + tooltip + ", check: " + check + ", handler: " + handler + ')';
            assert (Minecraft.getInstance().isSameThread()) : "HCsCR: Creating a checkbox NOT from the main thread. (thread: " + Thread.currentThread() + ", font: " + font + ", x: " + x + ", y: " + y + ", message: " + message + ", tooltip: " + tooltip + ", check: " + check + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
        }

        // Create.
        //? if >=1.20.4 {
        final Checkbox box = Checkbox.builder(message, font) // Implicit NPE for 'message', 'font'
                .pos(x - ((font.width(message) + 24) / 2), y)
                .selected(check)
                .onValueChange((final Checkbox checkbox, final boolean value) -> handler.accept(value)) // Implicit NPE for 'handler'
                .build();
        //?} else {
        /*final int width = font.width(message) + 24; // Implicit NPE for 'font', 'message'
        final Checkbox box = new Checkbox(x - (width / 2), y, width, 20, message, check) {
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
            private /^¹non-final¹^/ long lastAway = System.nanoTime();

            @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter") // <- Parameter names are not provided by Mojmap.
            @Override
            public void renderButton(final PoseStack graphics, final int mouseX, final int mouseY, final float delta) {
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
        box.setTooltip(Tooltip.create(tooltip));
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
    @Contract(value = "_, _, _, _, _, _, _, _, _, _, _, _ -> new", pure = true)
    public static AbstractSliderButton createSlider(final Font font, final int x, final int y, final int width,
                                                    final int height, final IntFunction<Component> provider,
                                                    final Component tooltip, final int value, final int min,
                                                    final int max, final IntConsumer handler,
                                                    final Consumer<List<FormattedCharSequence>> tooltipRenderer) {
        // Validate.
        if (HCompile.DEBUG_ASSERTS) {
            assert (font != null) : "HCsCR: Parameter 'font' is null. (x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
            assert ((x >= -320) && (x <= Math.max(Minecraft.getInstance().getWindow().getGuiScaledWidth(), 320))) : "HCsCR: Parameter 'x' is not in the [" + -320 + ".." + Math.max(Minecraft.getInstance().getWindow().getGuiScaledWidth(), 320) + "] range. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
            assert ((y >= -240) && (y <= Math.max(Minecraft.getInstance().getWindow().getGuiScaledHeight(), 240))) : "HCsCR: Parameter 'y' is not in the [" + -240 + ".." + Math.max(Minecraft.getInstance().getWindow().getGuiScaledHeight(), 240) + "] range. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
            assert (width == 200) : "HCsCR: Parameter 'width' is not 200. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
            assert (height == 20) : "HCsCR: Parameter 'height' is 20. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
            assert (provider != null) : "HCsCR: Parameter 'provider' is null. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
            assert (tooltip != null) : "HCsCR: Parameter 'tooltip' is null. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
            assert (max > min) : "HCsCR: Parameter 'min' is not bigger than 'max'. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
            assert (value >= min) : "HCsCR: Parameter 'value' is smaller than 'min'. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
            assert (value <= max) : "HCsCR: Parameter 'value' is bigger than 'max'. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
            assert (handler != null) : "HCsCR: Parameter 'handler' is null. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", tooltipRenderer: " + tooltipRenderer + ')';
            assert (tooltipRenderer != null) : "HCsCR: Parameter 'tooltipRenderer' is null. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ')';
            assert (Minecraft.getInstance().isSameThread()) : "HCsCR: Creating a slider NOT from the main thread. (thread: " + Thread.currentThread() + ", font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
        }

        // Create the slider.
        final int clamped = Mth.clamp(value, min, max);
        final double normalized = (double) (clamped - min) / (max - min);
        final Component message = provider.apply(clamped);
        if (HCompile.DEBUG_ASSERTS) {
            assert (message != null) : "HCsCR: Slider's message was returned null initially. (font: " + font + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ", provider: " + provider + ", tooltip: " + tooltip + ", value: " + value + ", min: " + min + ", max: " + max + ", handler: " + handler + ", tooltipRenderer: " + tooltipRenderer + ')';
        }
        final AbstractSliderButton slider = new AbstractSliderButton(x, y, width, height, message, normalized) { // Implicit NPE for 'provider'
            /**
             * A denormalized value, i.e. back in its original range.
             */
            private /*non-final*/ int denormalized = clamped;

            @Override
            protected void updateMessage() {
                final Component message = provider.apply(this.denormalized);
                if (HCompile.DEBUG_ASSERTS) {
                    assert (message != null) : "HCsCR: Slider's message was returned null after updating. (provider: " + provider + ", slider: " + this + ')';
                }
                this.setMessage(message);
            }

            @Override
            protected void applyValue() {
                final int denormalized = this.denormalized = (int) Math.round(Mth.lerp(this.value, min, max));
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
            private /^non-final^/ long lastAway = System.nanoTime();

            @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter") // <- Parameter names are not provided by Mojmap.
            @Override
            public void renderButton(final PoseStack graphics, final int mouseX, final int mouseY, final float delta) {
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
        slider.setTooltip(Tooltip.create(tooltip));
        slider.setTooltipDelay(TOOLTIP_DURATION);
        //?}
        return slider;
    }
}
