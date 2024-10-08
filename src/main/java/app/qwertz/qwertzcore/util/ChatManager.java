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
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatManager implements Listener {
    private final QWERTZcore plugin;

    public ChatManager(QWERTZcore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!plugin.getConfigManager().getChat() && !event.getPlayer().hasPermission("qwertzcore.chat.bypass")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(QWERTZcore.CORE_ICON + plugin.getConfigManager().getColor("colorError") + " Chat is disabled!");
            return;
        }
        if (plugin.getConfigManager().getChatFormatting()) {
            event.setCancelled(true);

            Player player = event.getPlayer();
            String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getRankManager().getPrefix(player));
            String suffix = ChatColor.translateAlternateColorCodes('&', plugin.getRankManager().getSuffix(player));
            String message = event.getMessage();
            String formattedMessage;

            formattedMessage = String.format("%s%s%s: %s",
                    prefix,
                    player.getName(),
                    suffix,
                    message
            );


            plugin.getServer().broadcastMessage(formattedMessage);
        }
    }
}