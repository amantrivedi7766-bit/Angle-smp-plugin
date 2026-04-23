package com.angle.smp.angel.listener;

import com.angle.smp.angel.AngelPlugin;
import com.angle.smp.angel.data.PlayerData;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProgressionListener implements Listener {
    private final AngelPlugin plugin;
    private final Map<UUID, BossBar> bossBars = new HashMap<>();

    public ProgressionListener(AngelPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getScheduler().runTaskTimer(plugin, this::refreshHud, 20L, 20L);
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

    private void refreshHud() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerData data = plugin.data(p);
            if (plugin.cfg().enableScoreboard()) updateScoreboard(p, data);
            if (plugin.cfg().enableBossbar()) updateBossBar(p, data);
        }
    }

    private void updateScoreboard(Player p, PlayerData data) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("angel", "dummy", "§6§lGuardian Angel");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.getScore("§fElement: §e" + (data.getElement() == null ? "None" : data.getElement().name())).setScore(4);
        obj.getScore("§fLevel: §a" + data.getLevel()).setScore(3);
        obj.getScore("§fXP: §b" + (int) data.getXp() + "/" + (int) plugin.xpForNext(data.getLevel())).setScore(2);
        obj.getScore("§7play.angle-smp").setScore(1);
        p.setScoreboard(board);
    }

    private void updateBossBar(Player p, PlayerData data) {
        double next = plugin.xpForNext(data.getLevel());
        float progress = (float) Math.max(0.0, Math.min(1.0, data.getXp() / next));
        BossBar bar = bossBars.computeIfAbsent(p.getUniqueId(), u -> BossBar.bossBar(
                Component.text("Angel XP", NamedTextColor.GOLD), 0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS
        ));
        bar.name(Component.text("Level " + data.getLevel() + " • " + (int) data.getXp() + "/" + (int) next + " XP", NamedTextColor.GOLD));
        bar.progress(progress);
        p.showBossBar(bar);
    }
}
