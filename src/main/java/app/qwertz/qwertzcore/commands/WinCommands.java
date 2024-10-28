/*
        Copyright (C) 2024 QWERTZ_EXE

        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License
        as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
        without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
        See the GNU Affero General Public License for more details.

        You should have received a copy of the GNU Affero General Public License along with this program.
        If not, see <http://www.gnu.org/licenses/>.
*/

package app.qwertz.qwertzcore.commands;

import app.qwertz.qwertzcore.QWERTZcore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WinCommands implements CommandExecutor {

    private final QWERTZcore plugin;

    public WinCommands(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (label.toLowerCase()) {
            case "addwin":
                return handleAddWin(sender, args);
            case "removewin":
                return handleRemoveWin(sender, args);
            case "wins":
                return handleWins(sender, args);
            default:
                return false;
        }
    }

    private boolean handleAddWin(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(plugin.getConfigManager().getColor("colorError") + "Usage: /addwin <player>");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(plugin.getConfigManager().getColor("colorError") + "Player not found.");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        plugin.getDatabaseManager().addWin(target.getUniqueId());
        int wins = plugin.getDatabaseManager().getWins(target.getUniqueId());

        plugin.getMessageManager().broadcastMessage(QWERTZcore.CORE_ICON + plugin.getConfigManager().getColor("colorAlive") + " Added a win for " + plugin.getConfigManager().getColor("colorPrimary") + target.getName() + plugin.getConfigManager().getColor("colorAlive") + ". They now have " + plugin.getConfigManager().getColor("colorPrimary") + wins + plugin.getConfigManager().getColor("colorAlive") + " wins.");
        target.sendMessage(plugin.getConfigManager().getColor("colorAlive") + "You have been awarded a win! You now have " +  plugin.getConfigManager().getColor("colorPrimary") + wins + plugin.getConfigManager().getColor("colorAlive") + " wins.");
        plugin.getSoundManager().broadcastConfigSound();
        return true;
    }

    private boolean handleRemoveWin(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(plugin.getConfigManager().getColor("colorError") + "Usage: /removewin <player>");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(plugin.getConfigManager().getColor("colorError") + "Player not found.");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        int currentWins = plugin.getDatabaseManager().getWins(target.getUniqueId());
        if (currentWins <= 0) {
            sender.sendMessage(plugin.getConfigManager().getColor("colorError") + target.getName() + " has no wins to remove.");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        plugin.getDatabaseManager().removeWin(target.getUniqueId());
        int newWins = plugin.getDatabaseManager().getWins(target.getUniqueId());

        plugin.getMessageManager().broadcastMessage(QWERTZcore.CORE_ICON + plugin.getConfigManager().getColor("colorDead") + " Removed a win from " + plugin.getConfigManager().getColor("colorPrimary") + target.getName() + plugin.getConfigManager().getColor("colorDead") + ". They now have " + plugin.getConfigManager().getColor("colorPrimary") + newWins + plugin.getConfigManager().getColor("colorDead") + " wins.");
        target.sendMessage(plugin.getConfigManager().getColor("colorDead") + "A win has been removed from your record. You now have " + plugin.getConfigManager().getColor("colorPrimary") + newWins + plugin.getConfigManager().getColor("colorDead") + " wins.");
        plugin.getSoundManager().broadcastConfigSound();
        return true;
    }

    private boolean handleWins(CommandSender sender, String[] args) {
        Player target;
        if (args.length == 0 && sender instanceof Player) {
            target = (Player) sender;
        } else if (args.length == 1) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(plugin.getConfigManager().getColor("colorError") + "Player not found.");
                plugin.getSoundManager().playSoundToSender(sender);
                return true;
            }
        } else {
            sender.sendMessage(plugin.getConfigManager().getColor("colorError") + "Usage: /wins [player]");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        int wins = plugin.getDatabaseManager().getWins(target.getUniqueId());
        sender.sendMessage(QWERTZcore.CORE_ICON + plugin.getConfigManager().getColor("colorPrimary") + " " + target.getName() + plugin.getConfigManager().getColor("colorAlive") + " has " + wins + " wins.");
        plugin.getSoundManager().playSoundToSender(sender);
        return true;
    }
}