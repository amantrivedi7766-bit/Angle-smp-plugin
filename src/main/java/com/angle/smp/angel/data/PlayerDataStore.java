package com.angle.smp.angel.data;

import com.angle.smp.angel.model.Element;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerDataStore {
    private final JavaPlugin plugin;
    private final File folder;
    private final Map<UUID, PlayerData> cache = new HashMap<>();

    public PlayerDataStore(JavaPlugin plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "players");
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public PlayerData get(UUID uuid) {
        return cache.computeIfAbsent(uuid, this::load);
    }

    public void unload(UUID uuid) {
        PlayerData data = cache.remove(uuid);
        if (data != null) {
            save(data);
        }
    }

    public void saveAll() {
        cache.values().forEach(this::save);
    }

    public void reset(UUID uuid) {
        cache.remove(uuid);
        File file = file(uuid);
        if (file.exists()) {
            file.delete();
        }
    }

    private PlayerData load(UUID uuid) {
        PlayerData data = new PlayerData(uuid);
        File file = file(uuid);
        if (!file.exists()) {
            return data;
        }
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        String elementName = yml.getString("element");
        if (elementName != null) {
            try {
                data.setElement(Element.valueOf(elementName));
            } catch (IllegalArgumentException ignored) {}
        }
        data.setLevel(yml.getInt("level", 1));
        data.setXp(yml.getDouble("xp", 0.0));
        data.getUnlockedAbilities().addAll(yml.getStringList("unlocked"));

        ConfigurationSection cds = yml.getConfigurationSection("cooldowns");
        if (cds != null) {
            for (String k : cds.getKeys(false)) {
                data.getCooldowns().put(k, cds.getLong(k));
            }
        }
        ConfigurationSection tgl = yml.getConfigurationSection("toggles");
        if (tgl != null) {
            for (String k : tgl.getKeys(false)) {
                data.getToggles().put(k, tgl.getBoolean(k));
            }
        }
        return data;
    }

    public void save(PlayerData data) {
        File file = file(data.getUuid());
        YamlConfiguration yml = new YamlConfiguration();
        if (data.getElement() != null) {
            yml.set("element", data.getElement().name());
        }
        yml.set("level", data.getLevel());
        yml.set("xp", data.getXp());
        yml.set("unlocked", new ArrayList<>(data.getUnlockedAbilities()));
        data.getCooldowns().forEach((k, v) -> yml.set("cooldowns." + k, v));
        data.getToggles().forEach((k, v) -> yml.set("toggles." + k, v));
        try {
            yml.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save data for " + data.getUuid() + ": " + e.getMessage());
        }
    }

    private File file(UUID uuid) {
        return new File(folder, uuid + ".yml");
    }
}
