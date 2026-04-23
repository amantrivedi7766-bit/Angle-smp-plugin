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
            case "set" -> handleSet(sender, args);
            case "reset" -> handleReset(sender, args);
            case "givexp" -> handleGiveXp(sender, args);
            case "reload" -> {
                if (!sender.hasPermission("angel.reload")) {
                    sender.sendMessage(Msg.bad("Missing angel.reload permission"));
                    return true;
                }
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

    private void handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("angel.set")) {
            sender.sendMessage(Msg.bad("Missing angel.set permission"));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage("/angeladmin set <player> <element>");
            return;
        }
        Player t = Bukkit.getPlayer(args[1]);
        if (t == null) {
            sender.sendMessage("Player offline.");
            return;
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

    private void handleReset(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("/angeladmin reset <player>");
            return;
        }
        Player t = Bukkit.getPlayer(args[1]);
        if (t != null) {
            plugin.store().reset(t.getUniqueId());
            sender.sendMessage(Msg.good("Reset data for " + t.getName()));
        }
    }

    private void handleGiveXp(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("/angeladmin givexp <player> <amount>");
            return;
        }
        Player t = Bukkit.getPlayer(args[1]);
        if (t == null) {
            sender.sendMessage("Player offline");
            return;
        }
        double amt;
        try {
            amt = Double.parseDouble(args[2]);
        } catch (NumberFormatException ex) {
            sender.sendMessage(Msg.bad("Amount must be numeric."));
            return;
        }
        if (amt <= 0) {
            sender.sendMessage(Msg.bad("Amount must be > 0"));
            return;
        }
        plugin.grantXp(t, amt);
        sender.sendMessage(Msg.good("Added XP."));
    }
}
