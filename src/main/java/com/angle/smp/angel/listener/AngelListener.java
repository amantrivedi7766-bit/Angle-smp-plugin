package com.angle.smp.angel.listener;

import com.angle.smp.angel.AngelPlugin;
import com.angle.smp.angel.data.PlayerData;
import com.angle.smp.angel.gui.GuiFactory;
import com.angle.smp.angel.model.Element;
import com.angle.smp.angel.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.Locale;

public class AngelListener implements Listener {
    private final AngelPlugin plugin;
    private final GuiFactory gui;

    public AngelListener(AngelPlugin plugin) {
        this.plugin = plugin;
        this.gui = new GuiFactory(plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        PlayerData data = plugin.data(p);
        if (data.getElement() == null) {
            if (plugin.cfg().randomOnFirstJoin()) {
                plugin.assignRandomElement(p);
                p.sendMessage(Msg.good("A Guardian Angel chose you: " + plugin.data(p).getElement().name()));
            } else {
                Bukkit.getScheduler().runTaskLater(plugin, () -> p.openInventory(gui.elementChoose()), 20L);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.store().unload(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (p.getGameMode() == GameMode.SPECTATOR) return;
        if (e.getAction().isRightClick()) {
            if (p.isSneaking()) {
                plugin.abilityService().runSecondary(p, plugin.data(p));
            } else {
                plugin.abilityService().runPrimary(p, plugin.data(p));
            }
        }
    }

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent e) {
        if (plugin.abilityService().handleWindDoubleJumpFlight(e.getPlayer(), plugin.data(e.getPlayer()))) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onFall(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL && plugin.data(p).getElement() == Element.WIND) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        String title = PlainTextComponentSerializer.plainText().serialize(e.getView().title());
        if (title.contains("Choose Your Element")) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null) return;
            String name = e.getCurrentItem().getType().name();
            for (Element element : Element.values()) {
                if (element.icon().name().equals(name)) {
                    PlayerData data = plugin.data(p);
                    data.setElement(element);
                    plugin.abilityService().unlockForLevel(data);
                    p.sendMessage(Msg.good("Element selected: " + element.name()));
                    p.closeInventory();
                    break;
                }
            }
        }
        if (title.contains("Guardian Angel")) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null) return;
            switch (e.getCurrentItem().getType()) {
                case ENCHANTED_BOOK -> p.openInventory(gui.abilityList(p));
                case NETHER_STAR -> {
                    if (p.hasPermission("angel.choose")) p.openInventory(gui.elementChoose());
                    else p.sendMessage(Msg.bad("No permission."));
                }
            }
        }
        if (title.contains("Angel Abilities")) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) return;
            String ability = e.getCurrentItem().getItemMeta().getDisplayName().toLowerCase(Locale.ROOT).replace(" ", "_");
            plugin.data(p).toggle(ability);
            p.sendMessage(Msg.info("Toggled " + ability + " = " + plugin.data(p).isToggled(ability)));
            p.openInventory(gui.abilityList(p));
        }
    }
}
