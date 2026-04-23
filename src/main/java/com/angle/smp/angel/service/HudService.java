package com.angle.smp.angel.service;

import com.angle.smp.angel.AngelPlugin;
import com.angle.smp.angel.data.PlayerData;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HudService {
    private final AngelPlugin plugin;
    private final Map<UUID, BossBar> bossBars = new HashMap<>();
    private final Map<UUID, String> scoreboardCache = new HashMap<>();

    public HudService(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    public void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = plugin.data(player);
            if (plugin.cfg().enableScoreboard()) {
                updateScoreboard(player, data);
            }
            if (plugin.cfg().enableBossbar()) {
                updateBossBar(player, data);
            }
        }
    }

    public void clear(Player player) {
        BossBar bar = bossBars.remove(player.getUniqueId());
        if (bar != null) {
            player.hideBossBar(bar);
        }
        scoreboardCache.remove(player.getUniqueId());
    }

    private void updateScoreboard(Player p, PlayerData data) {
        String signature = (data.getElement() == null ? "none" : data.getElement().name()) + "|" + data.getLevel() + "|" + (int) data.getXp();
        if (signature.equals(scoreboardCache.get(p.getUniqueId()))) {
            return;
        }
        scoreboardCache.put(p.getUniqueId(), signature);

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("angel", "dummy", "§6§lGuardian Angel");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.getScore("§fElement: §e" + (data.getElement() == null ? "None" : data.getElement().name())).setScore(4);
        obj.getScore("§fLevel: §a" + data.getLevel()).setScore(3);
        obj.getScore("§fXP: §b" + (int) data.getXp() + "/" + (int) plugin.xpForNext(data.getLevel())).setScore(2);
        obj.getScore("§7smooth elemental SMP").setScore(1);
        p.setScoreboard(board);
    }

    private void updateBossBar(Player p, PlayerData data) {
        double next = plugin.xpForNext(data.getLevel());
        float progress = (float) Math.max(0.0, Math.min(1.0, data.getXp() / next));
        BossBar bar = bossBars.computeIfAbsent(p.getUniqueId(), u -> BossBar.bossBar(
                Component.text("Angel XP", NamedTextColor.GOLD), 0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS
        ));
        bar.name(Component.text("Lv." + data.getLevel() + " • " + (int) data.getXp() + "/" + (int) next + " XP", NamedTextColor.GOLD));
        bar.progress(progress);
        p.showBossBar(bar);
    }
}
