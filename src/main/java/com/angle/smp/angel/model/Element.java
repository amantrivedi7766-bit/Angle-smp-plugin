package com.angle.smp.angel.model;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum Element {
    FIRE(ChatColor.RED, Material.BLAZE_POWDER, "Aggressive burst + burning pressure"),
    ICE(ChatColor.AQUA, Material.PACKED_ICE, "Control space + freezing tools"),
    LIGHTNING(ChatColor.YELLOW, Material.LIGHTNING_ROD, "Fast burst + mobility"),
    WIND(ChatColor.GREEN, Material.FEATHER, "Knockback + movement freedom"),
    EARTH(ChatColor.DARK_GREEN, Material.MOSS_BLOCK, "Tank + area denial"),
    LIGHT(ChatColor.GOLD, Material.GLOWSTONE_DUST, "Support + sustain");

    private final ChatColor color;
    private final Material icon;
    private final String flavor;

    Element(ChatColor color, Material icon, String flavor) {
        this.color = color;
        this.icon = icon;
        this.flavor = flavor;
    }

    public ChatColor color() {
        return color;
    }

    public Material icon() {
        return icon;
    }

    public String flavor() {
        return flavor;
    }

    public String display() {
        return color + name();
    }
}
