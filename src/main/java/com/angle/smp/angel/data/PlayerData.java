package com.angle.smp.angel.data;

import com.angle.smp.angel.model.Element;

import java.util.*;

public class PlayerData {
    private final UUID uuid;
    private Element element;
    private int level;
    private double xp;
    private final Set<String> unlockedAbilities;
    private final Map<String, Long> cooldowns;
    private final Map<String, Boolean> toggles;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.level = 1;
        this.xp = 0.0;
        this.unlockedAbilities = new HashSet<>();
        this.cooldowns = new HashMap<>();
        this.toggles = new HashMap<>();
    }

    public UUID getUuid() { return uuid; }
    public Element getElement() { return element; }
    public void setElement(Element element) { this.element = element; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = Math.max(1, level); }
    public double getXp() { return xp; }
    public void setXp(double xp) { this.xp = Math.max(0, xp); }
    public Set<String> getUnlockedAbilities() { return unlockedAbilities; }
    public Map<String, Long> getCooldowns() { return cooldowns; }
    public Map<String, Boolean> getToggles() { return toggles; }

    public boolean isUnlocked(String ability) {
        return unlockedAbilities.contains(ability.toLowerCase(Locale.ROOT));
    }

    public void unlock(String ability) {
        unlockedAbilities.add(ability.toLowerCase(Locale.ROOT));
    }

    public boolean isToggled(String ability) {
        return toggles.getOrDefault(ability.toLowerCase(Locale.ROOT), true);
    }

    public void toggle(String ability) {
        String key = ability.toLowerCase(Locale.ROOT);
        toggles.put(key, !toggles.getOrDefault(key, true));
    }

    public long getCooldownLeftMillis(String key) {
        long end = cooldowns.getOrDefault(key, 0L);
        return Math.max(0L, end - System.currentTimeMillis());
    }

    public boolean onCooldown(String key) {
        return getCooldownLeftMillis(key) > 0;
    }

    public void setCooldown(String key, long millis) {
        cooldowns.put(key, System.currentTimeMillis() + millis);
    }
}
