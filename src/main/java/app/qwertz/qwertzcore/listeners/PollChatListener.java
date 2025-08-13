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
import app.qwertz.qwertzcore.commands.PollCommand;
import app.qwertz.qwertzcore.gui.PollGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PollChatListener implements Listener {

    private final QWERTZcore plugin;
    private final PollCommand pollCommand;

    public PollChatListener(QWERTZcore plugin, PollCommand pollCommand) {
        this.plugin = plugin;
        this.pollCommand = pollCommand;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // Check if this player has a pending poll input
        if (PollGUI.hasPendingInput(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            String input = event.getMessage();
            
            // Handle the chat input
            PollGUI.handleChatInput(event.getPlayer(), input, plugin, pollCommand);
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clean up any pending inputs when player leaves
        PollGUI.removePendingInput(event.getPlayer().getUniqueId());
        pollCommand.clearPlayerState(event.getPlayer().getUniqueId());
    }
}
