package ru.vidtu.hcscr.platform;

import net.minecraft.network.chat.MutableComponent;

public class HStonecutter {
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
}
