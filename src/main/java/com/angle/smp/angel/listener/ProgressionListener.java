package com.angle.smp.angel.listener;

import com.angle.smp.angel.AngelPlugin;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class ProgressionListener implements Listener {
    private final AngelPlugin plugin;

    public ProgressionListener(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMobKill(EntityDeathEvent e) {
        LivingEntity dead = e.getEntity();
        Player killer = dead.getKiller();
        if (killer != null) {
            plugin.grantXp(killer, plugin.cfg().xpPerMobKill());
        }
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent e) {
        Player killer = e.getEntity().getKiller();
        if (killer != null) {
            plugin.grantXp(killer, plugin.cfg().xpPerPlayerKill());
        }
    }
}
