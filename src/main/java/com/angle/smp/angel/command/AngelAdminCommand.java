package com.angle.smp.angel.command;

import com.angle.smp.angel.AngelPlugin;
import com.angle.smp.angel.data.PlayerData;
import com.angle.smp.angel.gui.GuiFactory;
import com.angle.smp.angel.model.Element;
import com.angle.smp.angel.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

public class AngelAdminCommand implements CommandExecutor {
    private final AngelPlugin plugin;
    private final GuiFactory guiFactory;

    public AngelAdminCommand(AngelPlugin plugin) {
        this.plugin = plugin;
        this.guiFactory = new GuiFactory(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("angel.admin")) {
            sender.sendMessage("No permission");
            return true;
        }
        if (args.length == 0 && sender instanceof Player p) {
            p.openInventory(guiFactory.adminMain());
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage("/angeladmin <set/reset/givexp/reload/debug>");
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "set" -> {
                if (args.length < 3) {
                    sender.sendMessage("/angeladmin set <player> <element>");
                    return true;
                }
                Player t = Bukkit.getPlayer(args[1]);
                if (t == null) {
                    sender.sendMessage("Player offline.");
                    return true;
                }
                try {
                    Element e = Element.valueOf(args[2].toUpperCase(Locale.ROOT));
                    PlayerData data = plugin.data(t);
                    data.setElement(e);
                    plugin.abilityService().unlockForLevel(data);
                    sender.sendMessage(Msg.good("Set " + t.getName() + " to " + e.name()));
                } catch (IllegalArgumentException ex) {
                    sender.sendMessage(Msg.bad("Invalid element."));
                }
            }
            case "reset" -> {
                if (args.length < 2) {
                    sender.sendMessage("/angeladmin reset <player>");
                    return true;
                }
                Player t = Bukkit.getPlayer(args[1]);
                if (t != null) {
                    plugin.store().reset(t.getUniqueId());
                    sender.sendMessage(Msg.good("Reset data for " + t.getName()));
                }
            }
            case "givexp" -> {
                if (args.length < 3) {
                    sender.sendMessage("/angeladmin givexp <player> <amount>");
                    return true;
                }
                Player t = Bukkit.getPlayer(args[1]);
                if (t == null) {
                    sender.sendMessage("Player offline");
                    return true;
                }
                double amt = Double.parseDouble(args[2]);
                plugin.grantXp(t, amt);
                sender.sendMessage(Msg.good("Added XP."));
            }
            case "reload" -> {
                plugin.reloadAngel();
                sender.sendMessage(Msg.good("Angel config reloaded."));
            }
            case "debug" -> {
                plugin.toggleDebug();
                sender.sendMessage(Msg.good("Debug = " + plugin.debug()));
            }
            default -> sender.sendMessage(Msg.bad("Unknown subcommand."));
        }
        return true;
    }
}
