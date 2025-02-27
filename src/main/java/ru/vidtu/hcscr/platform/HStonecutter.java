package ru.vidtu.hcscr.platform;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;

public class HStonecutter {
    public static void stonecutter_removeEntity(Entity entity) {
        //? if >=1.17.1 {
        /*entity.discard();
         *///?} else {
        entity.remove();
        //?}
    }

    public static boolean stonecutter_isEntityRemoved(Entity entity) {
        //? if >=1.17.1 {
        /*return entity.isRemoved();
         *///?} else {
        return entity.removed;
        //?}
    }

    public static MutableComponent stonecutter_newLiteralComponent(String text) {
        //? if >=1.19.2 {
        /*return net.minecraft.network.chat.Component.literal(text);
         *///?} else {
        return new net.minecraft.network.chat.TextComponent(text);
        //?}
    }

    public static MutableComponent stonecutter_newTranslatableComponent(String key, Object... args) {
        //? if >=1.19.2 {
        /*return net.minecraft.network.chat.Component.translatable(key, args);
         *///?} else {
        return new net.minecraft.network.chat.TranslatableComponent(key, args);
        //?}
    }

    public static Component stonecutter_createValueComponent(Component name, Component value) {
        //? if >=1.17.1 {
        /*return CommonComponents.optionNameValue(name, value);
         *///?} else {
        return HStonecutter.stonecutter_newTranslatableComponent("options.generic_value", name, value);
        //?}
    }
}
