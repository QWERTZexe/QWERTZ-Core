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
import app.qwertz.qwertzcore.commands.HideCommand;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;

public class PlayerEventListener implements Listener {
    private final QWERTZcore plugin;
    private final HideCommand hideCommand;
    private final UpdateChecker updateChecker;



    public PlayerEventListener(QWERTZcore plugin, HideCommand hideCommand, UpdateChecker updateChecker) {
        this.plugin = plugin;
        this.hideCommand = hideCommand;
        this.updateChecker = updateChecker;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getVanishManager().hideVanishedPlayers(event.getPlayer());
        if ((Boolean) plugin.getConfigManager().get("checkForUpdates")) {
            updateChecker.notifyPlayer(player);
        }
        hideCommand.handlePlayerJoin(event.getPlayer());
        plugin.getEventManager().addNewPlayer(event.getPlayer());
        if (plugin.getConfigManager().getTpOnJoin()) {
            event.getPlayer().teleport(plugin.getConfigManager().getSpawnLocation());
        }
        plugin.getScoreboardManager().setScoreboard(event.getPlayer());
        plugin.getTablistManager().updateTablist(event.getPlayer());
        if (plugin.getConfigManager().get("suppressVanilla").equals(true)) {
            int fakeCount = plugin.getVanishManager().getNonVanishedPlayerCount()-1;
            int newCount = fakeCount + 1;
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%name%", player.getName());
            localMap.put("%count%", String.valueOf(fakeCount));
            localMap.put("%newCount%", String.valueOf(newCount));
            plugin.getMessageManager().broadcastMessage("chatting.join-msg", localMap);
            event.setJoinMessage(null);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getEventManager().removePlayer(event.getPlayer());
        plugin.getVanishManager().removeVanishedPlayer(event.getPlayer());
        plugin.getScoreboardManager().removeScoreboard(event.getPlayer());

        if (plugin.getConfigManager().get("suppressVanilla").equals(true)) {
            int fakeCount = plugin.getVanishManager().getNonVanishedPlayerCount();
            int newCount = fakeCount - 1;
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%name%", event.getPlayer().getName());
            localMap.put("%count%", String.valueOf(fakeCount));
            localMap.put("%newCount%", String.valueOf(newCount));
            plugin.getMessageManager().broadcastMessage("chatting.leave-msg", localMap);
            event.setQuitMessage(null);
        }
    }
}