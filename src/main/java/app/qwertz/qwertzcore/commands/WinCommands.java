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
            sender.sendMessage(ChatColor.RED + "Usage: /addwin <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        plugin.getDatabaseManager().addWin(target.getUniqueId());
        int wins = plugin.getDatabaseManager().getWins(target.getUniqueId());

        Bukkit.broadcastMessage(QWERTZcore.CORE_ICON + ChatColor.GREEN + " Added a win for " + ChatColor.YELLOW + target.getName() + ChatColor.GREEN + ". They now have " + ChatColor.YELLOW + wins +  ChatColor.GREEN + " wins.");
        target.sendMessage(ChatColor.GREEN + "You have been awarded a win! You now have " +  ChatColor.YELLOW + wins + ChatColor.GREEN + " wins.");

        return true;
    }

    private boolean handleRemoveWin(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /removewin <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        int currentWins = plugin.getDatabaseManager().getWins(target.getUniqueId());
        if (currentWins <= 0) {
            sender.sendMessage(ChatColor.RED + target.getName() + " has no wins to remove.");
            return true;
        }

        plugin.getDatabaseManager().removeWin(target.getUniqueId());
        int newWins = plugin.getDatabaseManager().getWins(target.getUniqueId());

        Bukkit.broadcastMessage(QWERTZcore.CORE_ICON + ChatColor.RED + " Removed a win from " + ChatColor.YELLOW + target.getName() + ChatColor.RED + ". They now have " + ChatColor.YELLOW + newWins + ChatColor.RED + " wins.");
        target.sendMessage(ChatColor.GREEN + "A win has been removed from your record. You now have " + ChatColor.YELLOW + newWins + ChatColor.RED + " wins.");

        return true;
    }

    private boolean handleWins(CommandSender sender, String[] args) {
        Player target;
        if (args.length == 0 && sender instanceof Player) {
            target = (Player) sender;
        } else if (args.length == 1) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Usage: /wins [player]");
            return true;
        }

        int wins = plugin.getDatabaseManager().getWins(target.getUniqueId());
        sender.sendMessage(QWERTZcore.CORE_ICON + ChatColor.YELLOW + " " + target.getName() + ChatColor.GREEN + " has " + wins + " wins.");

        return true;
    }
}