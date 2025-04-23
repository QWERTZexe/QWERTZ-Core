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

import java.util.HashMap;

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
            plugin.getMessageManager().sendInvalidUsage(sender, " /addwin <player>");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            plugin.getMessageManager().sendMessage(sender, "general.player-not-found");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        plugin.getDatabaseManager().addWin(target.getUniqueId());
        int wins = plugin.getDatabaseManager().getWins(target.getUniqueId());

        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%wins%", String.valueOf(wins));
        localMap.put("%name%", target.getName());

        HashMap<String, String> localMap2 = new HashMap<>();
        localMap2.put("%wins%", String.valueOf(wins));
        plugin.getMessageManager().broadcastMessage("wins.win-broadcast", localMap);
        plugin.getMessageManager().sendMessage(target, "wins.give", localMap2);        plugin.getSoundManager().broadcastConfigSound();
        return true;
    }

    private boolean handleRemoveWin(CommandSender sender, String[] args) {
        if (args.length != 1) {
            plugin.getMessageManager().sendInvalidUsage(sender, " /removewin <player>");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            plugin.getMessageManager().sendMessage(sender, "general.player-not-found");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        int currentWins = plugin.getDatabaseManager().getWins(target.getUniqueId());
        if (currentWins <= 0) {
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%name%", target.getName());
            plugin.getMessageManager().sendMessage(sender, "wins.no-wins", localMap);
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        plugin.getDatabaseManager().removeWin(target.getUniqueId());
        int newWins = plugin.getDatabaseManager().getWins(target.getUniqueId());

        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%wins%", String.valueOf(newWins));
        localMap.put("%name%", target.getName());

        HashMap<String, String> localMap2 = new HashMap<>();
        localMap2.put("%wins%", String.valueOf(newWins));
        plugin.getMessageManager().broadcastMessage("wins.win-removed-broadcast", localMap);
        plugin.getMessageManager().sendMessage(target, "wins.remove", localMap2);
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
                plugin.getMessageManager().sendMessage(sender, "general.player-not-found");
                plugin.getSoundManager().playSoundToSender(sender);
                return true;
            }
        } else {
            plugin.getMessageManager().sendInvalidUsage(sender, " /wins [player]");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        int wins = plugin.getDatabaseManager().getWins(target.getUniqueId());
        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%wins%", String.valueOf(wins));
        localMap.put("%name%", target.getName());
        plugin.getMessageManager().sendMessage(sender, "wins.show-wins", localMap);
        plugin.getSoundManager().playSoundToSender(sender);
        return true;
    }
}