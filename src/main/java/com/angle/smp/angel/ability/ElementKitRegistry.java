package com.angle.smp.angel.ability;

import com.angle.smp.angel.model.AbilityType;
import com.angle.smp.angel.model.Element;

import java.util.*;

public class ElementKitRegistry {
    private final Map<Element, List<AbilityDefinition>> map = new EnumMap<>(Element.class);

    public ElementKitRegistry() {
        map.put(Element.FIRE, List.of(
                new AbilityDefinition("fire_blast", "Fire Blast", AbilityType.PRIMARY, 1, 6, 5, "Shoots a fiery projectile that burns enemies."),
                new AbilityDefinition("fire_dash", "Fire Dash", AbilityType.SECONDARY, 4, 4, 10, "Dashes forward leaving a burning trail."),
                new AbilityDefinition("flame_aura", "Flame Aura", AbilityType.PASSIVE, 2, 2, 0, "Nearby enemies are periodically scorched.")
        ));
        map.put(Element.ICE, List.of(
                new AbilityDefinition("ice_spike", "Ice Spike", AbilityType.PRIMARY, 1, 5, 6, "Summons frost spikes from the ground."),
                new AbilityDefinition("freeze", "Freeze", AbilityType.SECONDARY, 4, 3, 10, "Freezes target, slowing movement."),
                new AbilityDefinition("water_walk", "Water Walk", AbilityType.PASSIVE, 2, 0, 0, "Temporarily freezes water under your feet.")
        ));
        map.put(Element.LIGHTNING, List.of(
                new AbilityDefinition("lightning_strike", "Lightning Strike", AbilityType.PRIMARY, 1, 7, 8, "Calls lightning on looked-at target."),
                new AbilityDefinition("speed_boost", "Speed Boost", AbilityType.SECONDARY, 3, 0, 14, "Gain a burst of speed."),
                new AbilityDefinition("fast_attack", "Fast Attack", AbilityType.PASSIVE, 2, 0, 0, "Permanent haste-like combat tempo.")
        ));
        map.put(Element.WIND, List.of(
                new AbilityDefinition("wind_push", "Wind Push", AbilityType.PRIMARY, 1, 4, 5, "Pushes enemies away in a cone."),
                new AbilityDefinition("double_jump", "Double Jump", AbilityType.SECONDARY, 2, 0, 3, "Air jump with wind burst."),
                new AbilityDefinition("fall_immunity", "Fall Immunity", AbilityType.PASSIVE, 1, 0, 0, "Negates all fall damage.")
        ));
        map.put(Element.EARTH, List.of(
                new AbilityDefinition("earth_wall", "Earth Wall", AbilityType.PRIMARY, 1, 0, 12, "Raises a temporary wall."),
                new AbilityDefinition("stomp", "Stomp", AbilityType.SECONDARY, 3, 6, 10, "Slams ground for AoE damage."),
                new AbilityDefinition("earth_skin", "Earth Skin", AbilityType.PASSIVE, 2, 0, 0, "Natural resistance to incoming damage.")
        ));
        map.put(Element.LIGHT, List.of(
                new AbilityDefinition("heal", "Heal", AbilityType.PRIMARY, 1, 0, 9, "Heals you and nearby allies."),
                new AbilityDefinition("flash", "Blindness Flash", AbilityType.SECONDARY, 4, 2, 10, "Blinds enemies in front of you."),
                new AbilityDefinition("regen", "Radiant Regeneration", AbilityType.PASSIVE, 2, 0, 0, "Grants steady regeneration.")
        ));
    }

    public List<AbilityDefinition> get(Element element) {
        return map.getOrDefault(element, List.of());
    }

    public Optional<AbilityDefinition> byId(Element element, String id) {
        return get(element).stream().filter(a -> a.id().equalsIgnoreCase(id)).findFirst();
    }
}
