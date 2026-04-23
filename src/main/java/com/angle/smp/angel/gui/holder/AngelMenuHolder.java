package com.angle.smp.angel.gui.holder;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class AngelMenuHolder implements InventoryHolder {
    private final MenuType type;

    public AngelMenuHolder(MenuType type) {
        this.type = type;
    }

    public MenuType getType() {
        return type;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
