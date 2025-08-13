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

package app.qwertz.qwertzcore.listeners;

import app.qwertz.qwertzcore.QWERTZcore;
import app.qwertz.qwertzcore.commands.ChatReviveCommand;
import app.qwertz.qwertzcore.gui.ChatRevivalGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ChatRevivalChatListener implements Listener {

    private final QWERTZcore plugin;
    private final ChatReviveCommand chatReviveCommand;

    public ChatRevivalChatListener(QWERTZcore plugin, ChatReviveCommand chatReviveCommand) {
        this.plugin = plugin;
        this.chatReviveCommand = chatReviveCommand;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (ChatRevivalGUI.hasPendingInput(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            String input = event.getMessage();
            ChatRevivalGUI.handleChatInput(event.getPlayer(), input, plugin, chatReviveCommand);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        ChatRevivalGUI.removePendingInput(event.getPlayer().getUniqueId());
        chatReviveCommand.clearPlayerState(event.getPlayer().getUniqueId());
    }
}
