package com.angle.smp.angel.command;

import com.angle.smp.angel.AngelPlugin;
import com.angle.smp.angel.ability.AbilityKeys;
import com.angle.smp.angel.model.Element;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AngelTabCompleter implements TabCompleter {
    private final AngelPlugin plugin;

    public AngelTabCompleter(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(org.bukkit.command.CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("angel")) {
            if (args.length == 1) return filter(List.of("info", "abilities", "choose", "toggle"), args[0]);
            if (args.length == 2 && args[0].equalsIgnoreCase("toggle") && sender instanceof Player p) {
                return filter(AbilityKeys.forElement(plugin.data(p).getElement(), plugin.registry()), args[1]);
            }
        }

        if (command.getName().equalsIgnoreCase("angeladmin")) {
            if (args.length == 1) return filter(List.of("set", "reset", "givexp", "reload", "debug"), args[0]);
            if (args.length == 2 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("givexp"))) {
                return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[1]);
            }
            if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
                return filter(List.of(Element.FIRE.name(), Element.ICE.name(), Element.LIGHTNING.name(), Element.WIND.name(), Element.EARTH.name(), Element.LIGHT.name()), args[2]);
            }
        }

        return List.of();
    }

    private List<String> filter(List<String> source, String token) {
        String t = token.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String s : source) {
            if (s.toLowerCase(Locale.ROOT).startsWith(t)) out.add(s);
        }
        return out;
    }
}
