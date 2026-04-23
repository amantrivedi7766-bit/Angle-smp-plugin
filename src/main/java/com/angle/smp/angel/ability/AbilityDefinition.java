package com.angle.smp.angel.ability;

import com.angle.smp.angel.model.AbilityType;

public record AbilityDefinition(String id, String name, AbilityType type, int unlockLevel, double baseDamage, int cooldownSeconds, String description) {
}
