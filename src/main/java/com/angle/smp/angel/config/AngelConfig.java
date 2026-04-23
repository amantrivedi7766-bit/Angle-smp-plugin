package com.angle.smp.angel.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class AngelConfig {
    private final JavaPlugin plugin;

    public AngelConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.reloadConfig();
    }

    private FileConfiguration c() {
        return plugin.getConfig();
    }

    public boolean randomOnFirstJoin() {
        return c().getBoolean("first-join.random-element", false);
    }

    public int xpPerMobKill() { return c().getInt("xp.mob-kill", 10); }
    public int xpPerPlayerKill() { return c().getInt("xp.player-kill", 30); }
    public double xpLevelScale() { return c().getDouble("xp.level-scale", 65.0); }
    public boolean enableScoreboard() { return c().getBoolean("features.scoreboard", true); }
    public boolean enableBossbar() { return c().getBoolean("features.bossbar", true); }
    public boolean debug() { return c().getBoolean("debug", false); }

    public double value(String path, double def) {
        return c().getDouble(path, def);
    }

    public int valueInt(String path, int def) {
        return c().getInt(path, def);
    }

    public String text(String path, String def) {
        return c().getString(path, def);
    }
}
