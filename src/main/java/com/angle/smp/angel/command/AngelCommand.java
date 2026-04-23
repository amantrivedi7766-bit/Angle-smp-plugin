package com.angle.smp.angel.command;

import com.angle.smp.angel.AngelPlugin;
import com.angle.smp.angel.ability.AbilityDefinition;
import com.angle.smp.angel.data.PlayerData;
import com.angle.smp.angel.gui.GuiFactory;
import com.angle.smp.angel.model.Element;
import com.angle.smp.angel.util.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

public class AngelCommand implements CommandExecutor {
    private final AngelPlugin plugin;
    private final GuiFactory guiFactory;

    public AngelCommand(AngelPlugin plugin) {
        this.plugin = plugin;
        this.guiFactory = new GuiFactory(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only");
            return true;
        }
        if (!player.hasPermission("angel.use")) {
            player.sendMessage(Msg.bad("No permission."));
            return true;
        }

        PlayerData data = plugin.data(player);
        if (args.length == 0) {
            player.openInventory(guiFactory.playerMain(player));
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "info" -> player.sendMessage(Msg.info("Element=" + (data.getElement() == null ? "None" : data.getElement().name()) +
                    " | Level=" + data.getLevel() + " | XP=" + (int) data.getXp() + "/" + (int) plugin.xpForNext(data.getLevel())));
            case "abilities" -> player.openInventory(guiFactory.abilityList(player));
            case "choose" -> {
                if (!player.hasPermission("angel.choose")) {
                    player.sendMessage(Msg.bad("No permission to choose."));
                } else {
                    player.openInventory(guiFactory.elementChoose());
                }
            }
            case "toggle" -> {
                if (args.length < 2) {
                    player.sendMessage(Msg.bad("Usage: /angel toggle <ability>"));
                    return true;
                }
                data.toggle(args[1]);
                player.sendMessage(Msg.good(args[1] + " is now " + (data.isToggled(args[1]) ? "ON" : "OFF")));
            }
            default -> player.sendMessage(Msg.bad("Subcommands: info, abilities, choose, toggle"));
        }
        return true;
    }
}
