package com.angle.smp.angel.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class Msg {
    private Msg() {}

    public static Component info(String msg) {
        return Component.text("[Angel] ", NamedTextColor.GOLD).append(Component.text(msg, NamedTextColor.WHITE));
    }

    public static Component good(String msg) {
        return Component.text(msg, NamedTextColor.GREEN);
    }

    public static Component bad(String msg) {
        return Component.text(msg, NamedTextColor.RED);
    }
}
