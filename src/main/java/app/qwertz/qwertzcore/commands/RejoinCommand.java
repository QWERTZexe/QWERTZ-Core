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
import app.qwertz.qwertzcore.util.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class RejoinCommand implements CommandExecutor {
    private final QWERTZcore plugin;
    private final ConfigManager configManager;

    public RejoinCommand(QWERTZcore plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "general.only-player-execute");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("qwertzcore.admin.rejoin")) {
            plugin.getMessageManager().sendMessage(player, "no-permission");
            return true;
        }

        if (args.length == 0) {
            // Show current status
            boolean allowRejoining = configManager.getAllowRejoining();
            int rejoinTime = configManager.getRejoinTime();
            
            plugin.getMessageManager().sendMessage(player, "rejoin.status-title");
            if (allowRejoining) {
                plugin.getMessageManager().sendMessage(player, "rejoin.status-enabled");
            } else {
                plugin.getMessageManager().sendMessage(player, "rejoin.status-disabled");
            }
            
            HashMap<String, String> timeMap = new HashMap<>();
            timeMap.put("%time%", String.valueOf(rejoinTime));
            plugin.getMessageManager().sendMessage(player, "rejoin.status-time", timeMap);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "enable":
                configManager.set("allowRejoining", true);
                plugin.getMessageManager().sendMessage(player, "rejoin.enabled");
                break;
            case "disable":
                configManager.set("allowRejoining", false);
                plugin.getMessageManager().sendMessage(player, "rejoin.disabled");
                break;
            case "time":
                if (args.length < 2) {
                    HashMap<String, String> usageMap = new HashMap<>();
                    usageMap.put("%usage%", "/rejoin time <seconds>");
                    plugin.getMessageManager().sendMessage(player, "general.invalid-usage", usageMap);
                    return true;
                }
                try {
                    int newTime = Integer.parseInt(args[1]);
                    if (newTime < 0) {
                        plugin.getMessageManager().sendMessage(player, "rejoin.invalid-number");
                        return true;
                    }
                    configManager.set("rejoinTime", newTime);
                    HashMap<String, String> timeMap = new HashMap<>();
                    timeMap.put("%time%", String.valueOf(newTime));
                    plugin.getMessageManager().sendMessage(player, "rejoin.time-set", timeMap);
                } catch (NumberFormatException e) {
                    plugin.getMessageManager().sendMessage(player, "rejoin.invalid-number");
                    return true;
                }
                break;
            default:
                plugin.getMessageManager().sendInvalidUsage(player, "/rejoin [enable|disable|time <seconds>]");
                break;
        }

        return true;
    }
}
