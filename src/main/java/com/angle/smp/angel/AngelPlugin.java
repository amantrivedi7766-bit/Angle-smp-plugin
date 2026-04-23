package com.angle.smp.angel;

import com.angle.smp.angel.ability.AbilityService;
import com.angle.smp.angel.ability.ElementKitRegistry;
import com.angle.smp.angel.command.AngelAdminCommand;
import com.angle.smp.angel.command.AngelCommand;
import com.angle.smp.angel.command.AngelTabCompleter;
import com.angle.smp.angel.config.AngelConfig;
import com.angle.smp.angel.data.PlayerData;
import com.angle.smp.angel.data.PlayerDataStore;
import com.angle.smp.angel.listener.AngelListener;
import com.angle.smp.angel.listener.ProgressionListener;
import com.angle.smp.angel.model.Element;
import com.angle.smp.angel.service.HudService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class AngelPlugin extends JavaPlugin {
    private PlayerDataStore store;
    private AngelConfig cfg;
    private ElementKitRegistry registry;
    private AbilityService abilityService;
    private HudService hud;
    private boolean debug;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.cfg = new AngelConfig(this);
        this.store = new PlayerDataStore(this);
        this.registry = new ElementKitRegistry();
        this.abilityService = new AbilityService(this, registry);
        this.hud = new HudService(this);
        this.debug = cfg.debug();

        getServer().getPluginManager().registerEvents(new AngelListener(this), this);
        getServer().getPluginManager().registerEvents(new ProgressionListener(this), this);

        AngelCommand angelCommand = new AngelCommand(this);
        AngelAdminCommand adminCommand = new AngelAdminCommand(this);
        AngelTabCompleter completer = new AngelTabCompleter(this);

        getCommand("angel").setExecutor(angelCommand);
        getCommand("angel").setTabCompleter(completer);
        getCommand("angeladmin").setExecutor(adminCommand);
        getCommand("angeladmin").setTabCompleter(completer);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                PlayerData data = data(p);
                abilityService.unlockForLevel(data);
                abilityService.runPassives(p, data);
            }
        }, cfg.passiveTickInterval(), cfg.passiveTickInterval());

        Bukkit.getScheduler().runTaskTimer(this, hud::tick, cfg.hudTickInterval(), cfg.hudTickInterval());
        Bukkit.getScheduler().runTaskTimer(this, store::saveAll, 20L * cfg.autosaveSeconds(), 20L * cfg.autosaveSeconds());
    }

    @Override
    public void onDisable() {
        store.saveAll();
    }

    public PlayerData data(Player player) {
        return store.get(player.getUniqueId());
    }

    public void grantXp(Player player, double amount) {
        PlayerData data = data(player);
        data.setXp(data.getXp() + amount);
        while (data.getXp() >= xpForNext(data.getLevel())) {
            data.setXp(data.getXp() - xpForNext(data.getLevel()));
            data.setLevel(data.getLevel() + 1);
            abilityService.unlockForLevel(data);
            player.sendTitle("§6Level Up!", "§fNow level §e" + data.getLevel(), 5, 35, 10);
            player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        }
    }

    public double xpForNext(int level) {
        return level * cfg.xpLevelScale();
    }

    public void assignRandomElement(Player player) {
        Element random = Arrays.stream(Element.values()).toList().get(ThreadLocalRandom.current().nextInt(Element.values().length));
        PlayerData data = data(player);
        data.setElement(random);
        abilityService.unlockForLevel(data);
    }

    public void reloadAngel() {
        cfg.reload();
        this.debug = cfg.debug();
    }

    public double levelBonus(Player p) {
        return data(p).getLevel() * 0.15;
    }

    public PlayerDataStore store() { return store; }
    public AngelConfig cfg() { return cfg; }
    public ElementKitRegistry registry() { return registry; }
    public AbilityService abilityService() { return abilityService; }
    public HudService hud() { return hud; }
    public boolean debug() { return debug; }
    public void toggleDebug() { this.debug = !this.debug; }
}
