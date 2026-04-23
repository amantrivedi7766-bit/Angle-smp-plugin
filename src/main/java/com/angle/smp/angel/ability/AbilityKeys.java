package com.angle.smp.angel.ability;

import com.angle.smp.angel.model.Element;

import java.util.*;

public final class AbilityKeys {
    private AbilityKeys() {}

    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("fireblast", "fire_blast"),
            Map.entry("firedash", "fire_dash"),
            Map.entry("flameaura", "flame_aura"),
            Map.entry("icespike", "ice_spike"),
            Map.entry("waterwalk", "water_walk"),
            Map.entry("lightningstrike", "lightning_strike"),
            Map.entry("speedboost", "speed_boost"),
            Map.entry("windpush", "wind_push"),
            Map.entry("doublejump", "double_jump"),
            Map.entry("earthwall", "earth_wall"),
            Map.entry("earthskin", "earth_skin"),
            Map.entry("blindnessflash", "flash")
    );

    public static String normalize(String raw) {
        String compact = raw.toLowerCase(Locale.ROOT).replace(" ", "").replace("_", "").replace("-", "");
        return ALIASES.getOrDefault(compact, raw.toLowerCase(Locale.ROOT).replace("-", "_"));
    }

    public static List<String> forElement(Element element, ElementKitRegistry registry) {
        if (element == null) return List.of();
        return registry.get(element).stream().map(AbilityDefinition::id).toList();
    }
}
