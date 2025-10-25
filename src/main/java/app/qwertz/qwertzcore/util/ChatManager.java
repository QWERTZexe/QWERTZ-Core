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

package app.qwertz.qwertzcore.util;

import app.qwertz.qwertzcore.QWERTZcore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.List;

public class ChatManager implements Listener {
    private final QWERTZcore plugin;
        
    public ChatManager(QWERTZcore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {

        if (event.isCancelled()) return;
            
        if (!plugin.getConfigManager().getChat() && !event.getPlayer().hasPermission("qwertzcore.chat.bypass")) {
            event.setCancelled(true);
            plugin.getMessageManager().sendMessage(event.getPlayer(), "chatting.disabled");
            plugin.getSoundManager().playSound(event.getPlayer());
            return;
        }
        if (plugin.getConfigManager().getChatFormatting()) {
            event.setCancelled(true);

            Player player = event.getPlayer();
            String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getRankManager().getPrefix(player));
            String suffix = ChatColor.translateAlternateColorCodes('&', plugin.getRankManager().getSuffix(player));
            String message = event.getMessage();
            
            // Apply color codes if colored chat is enabled and player has permission
            if (plugin.getConfigManager().getColoredChat() && player.hasPermission("qwertzcore.chat.color")) {
                message = plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', message));
            }
            
            // Apply pinging if enabled and player has permission
            if (plugin.getConfigManager().getPinging() && player.hasPermission("qwertzcore.chat.ping")) {
                message = applyPinging(message);
            }
            
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%name%", player.getName());
            localMap.put("%prefix%", prefix);
            localMap.put("%suffix%", suffix);
            String withoutMsg = plugin.translateHexColorCodes(plugin.getMessageManager().prepareMessage(plugin.getMessageManager().getMessage("chatting.chat"), localMap));
            Bukkit.broadcastMessage(withoutMsg.replace("%message%", translateEmojis(message, player)));
        }
    }

    public String translateEmojis(String message, Player player) {
        if (player.hasPermission("qwertzcore.player.emojis") && (Boolean) plugin.getConfigManager().get("emojis")) {
            List<String> emojis = plugin.getMessageManager().getStringList("emojis.emojis");
            for (String emoji : emojis) {
                String[] parts = emoji.split("\\|");  // Escape pipe character
                message = message.replace(parts[0], parts.length > 1 ? parts[1] : parts[0]);
            }
        }
        return message;
    }

    public String applyPinging(String message) {
        // Get all online players
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            String playerName = onlinePlayer.getName();
            // Check if the message contains the player's name (case-insensitive)
            if (message.toLowerCase().contains(playerName.toLowerCase())) {
                // Replace the player name with the ping format
                plugin.getSoundManager().playSound(onlinePlayer);
                String pingFormat = plugin.getMessageManager().prepareMessage(plugin.getMessageManager().getMessage("general.ping-format"), new HashMap<>());
                pingFormat = pingFormat.replace("%name%", playerName);
                // Use case-insensitive replacement
                message = message.replaceAll("(?i)" + java.util.regex.Pattern.quote(playerName), pingFormat);
            }
        }
        return message;
    }
}
