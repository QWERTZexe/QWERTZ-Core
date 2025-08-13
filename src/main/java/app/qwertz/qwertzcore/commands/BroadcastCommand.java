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

public class BroadcastCommand implements CommandExecutor {

    private final QWERTZcore plugin;

    public BroadcastCommand(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            plugin.getMessageManager().sendInvalidUsage(sender, "/broadcast <message>");
            return true;
        }

        // Join all arguments to form the message
        String message = String.join(" ", args);
        
        // Apply color codes to the message
        message = plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', message));
        
        // Create placeholder map for the broadcast message
        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%broadcast%", message);
        
        // Get the formatted broadcast message from the theme
        String formattedMessage = plugin.getMessageManager().prepareMessage(
            plugin.getMessageManager().getMessage("broadcast.message"), 
            localMap
        );
        
        // Apply color codes to the formatted message
        formattedMessage = plugin.translateHexColorCodes(formattedMessage);
        
        // Broadcast the message in "bigger form" (with empty lines) regardless of biggermessages config
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(formattedMessage);
        Bukkit.broadcastMessage("");
        
        // Send the message to action bar for all online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle("", message, 10, 70, 20);
        }
        
        return true;
    }
}
