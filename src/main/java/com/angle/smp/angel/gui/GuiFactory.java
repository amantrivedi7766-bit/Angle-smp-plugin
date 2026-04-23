package com.angle.smp.angel.gui;

import com.angle.smp.angel.AngelPlugin;
import com.angle.smp.angel.ability.AbilityDefinition;
import com.angle.smp.angel.data.PlayerData;
import com.angle.smp.angel.gui.holder.AngelMenuHolder;
import com.angle.smp.angel.gui.holder.MenuType;
import com.angle.smp.angel.model.Element;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class GuiFactory {
    private final AngelPlugin plugin;
    private final NamespacedKey abilityKey;

    public GuiFactory(AngelPlugin plugin) {
        this.plugin = plugin;
        this.abilityKey = new NamespacedKey(plugin, "ability_id");
    }

    public Inventory playerMain(Player player) {
        PlayerData data = plugin.data(player);
        Inventory inv = Bukkit.createInventory(new AngelMenuHolder(MenuType.PLAYER_MAIN), 27, Component.text("Guardian Angel", NamedTextColor.GOLD));
        ItemStack profile = new ItemStack(data.getElement() == null ? Material.BARRIER : data.getElement().icon());
        ItemMeta pm = profile.getItemMeta();
        pm.displayName(Component.text("Current Element", NamedTextColor.YELLOW));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Element: " + (data.getElement() == null ? "None" : data.getElement().name()), NamedTextColor.WHITE));
        lore.add(Component.text("Level: " + data.getLevel(), NamedTextColor.WHITE));
        lore.add(Component.text("XP: " + (int) data.getXp() + "/" + (int) plugin.xpForNext(data.getLevel()), NamedTextColor.WHITE));
        pm.lore(lore);
        profile.setItemMeta(pm);
        inv.setItem(13, profile);

        ItemStack abilities = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta am = abilities.getItemMeta();
        am.displayName(Component.text("Abilities", NamedTextColor.AQUA));
        am.lore(List.of(Component.text("Click to inspect your toolkit", NamedTextColor.GRAY)));
        abilities.setItemMeta(am);
        inv.setItem(11, abilities);

        ItemStack choose = new ItemStack(Material.NETHER_STAR);
        ItemMeta cm = choose.getItemMeta();
        cm.displayName(Component.text("Choose Element", NamedTextColor.LIGHT_PURPLE));
        cm.lore(List.of(Component.text("Requires angel.choose permission", NamedTextColor.GRAY)));
        choose.setItemMeta(cm);
        inv.setItem(15, choose);

        return inv;
    }

    public Inventory abilityList(Player player) {
        PlayerData data = plugin.data(player);
        Inventory inv = Bukkit.createInventory(new AngelMenuHolder(MenuType.ABILITIES), 27, Component.text("Angel Abilities", NamedTextColor.AQUA));
        if (data.getElement() == null) return inv;
        int slot = 10;
        for (AbilityDefinition def : plugin.registry().get(data.getElement())) {
            ItemStack it = new ItemStack(Material.BOOK);
            ItemMeta meta = it.getItemMeta();
            meta.displayName(Component.text(def.name(), NamedTextColor.YELLOW));
            meta.getPersistentDataContainer().set(abilityKey, PersistentDataType.STRING, def.id());
            meta.lore(List.of(
                    Component.text(def.description(), NamedTextColor.GRAY),
                    Component.text("Damage: " + def.baseDamage(), NamedTextColor.WHITE),
                    Component.text("Cooldown: " + def.cooldownSeconds() + "s", NamedTextColor.WHITE),
                    Component.text("Unlock: L" + def.unlockLevel(), NamedTextColor.WHITE),
                    Component.text("Status: " + (data.isUnlocked(def.id()) ? "Unlocked" : "Locked"), data.isUnlocked(def.id()) ? NamedTextColor.GREEN : NamedTextColor.RED),
                    Component.text("Toggle: " + (data.isToggled(def.id()) ? "ON" : "OFF"), data.isToggled(def.id()) ? NamedTextColor.GREEN : NamedTextColor.RED),
                    Component.text("Click to toggle", NamedTextColor.DARK_GRAY)
            ));
            it.setItemMeta(meta);
            inv.setItem(slot++, it);
        }
        return inv;
    }

    public Inventory elementChoose() {
        Inventory inv = Bukkit.createInventory(new AngelMenuHolder(MenuType.ELEMENT_CHOOSE), 27, Component.text("Choose Your Element", NamedTextColor.LIGHT_PURPLE));
        int slot = 10;
        for (Element e : Element.values()) {
            ItemStack it = new ItemStack(e.icon());
            ItemMeta meta = it.getItemMeta();
            meta.displayName(Component.text(e.name(), NamedTextColor.GOLD));
            meta.lore(List.of(Component.text(e.flavor(), NamedTextColor.GRAY)));
            it.setItemMeta(meta);
            inv.setItem(slot++, it);
        }
        return inv;
    }

    public Inventory adminMain() {
        Inventory inv = Bukkit.createInventory(new AngelMenuHolder(MenuType.ADMIN_MAIN), 27, Component.text("Angel Admin", NamedTextColor.RED));
        ItemStack i1 = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta m1 = i1.getItemMeta();
        m1.displayName(Component.text("Set / Reset Players", NamedTextColor.YELLOW));
        i1.setItemMeta(m1);
        inv.setItem(11, i1);

        ItemStack i2 = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta m2 = i2.getItemMeta();
        m2.displayName(Component.text("Give XP", NamedTextColor.GREEN));
        i2.setItemMeta(m2);
        inv.setItem(13, i2);

        ItemStack i3 = new ItemStack(Material.COMPARATOR);
        ItemMeta m3 = i3.getItemMeta();
        m3.displayName(Component.text("Reload / Debug", NamedTextColor.AQUA));
        i3.setItemMeta(m3);
        inv.setItem(15, i3);
        return inv;
    }

    public NamespacedKey abilityKey() {
        return abilityKey;
    }
}
