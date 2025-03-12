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
    private final EventManager eventManager;
    private final ConfigManager configManager;
    private final ScoreboardManager scoreboardManager;
    private final TablistManager tablistManager;
    private final HideCommand hideCommand;
    private final UpdateChecker updateChecker;
    private final VanishManager vanishManager;
    private final MessageManager messageManager;



    public PlayerEventListener(EventManager eventManager, VanishManager vanishManager, ConfigManager configManager, ScoreboardManager scoreboardManager, TablistManager tablistManager, HideCommand hideCommand, UpdateChecker updateChecker, MessageManager messageManager) {
        this.eventManager = eventManager;
        this.configManager = configManager;
        this.scoreboardManager = scoreboardManager;
        this.tablistManager = tablistManager;
        this.hideCommand = hideCommand;
        this.updateChecker = updateChecker;
        this.vanishManager = vanishManager;
        this.messageManager = messageManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        vanishManager.hideVanishedPlayers(event.getPlayer());
        if ((Boolean) configManager.get("checkForUpdates")) {
            updateChecker.notifyPlayer(player);
        }
        hideCommand.handlePlayerJoin(event.getPlayer());
        eventManager.addNewPlayer(event.getPlayer());
        if (configManager.getTpOnJoin()) {
            event.getPlayer().teleport(configManager.getSpawnLocation());
        }
        scoreboardManager.setScoreboard(event.getPlayer());
        tablistManager.updateTablist(event.getPlayer());
        if (configManager.get("suppressVanilla").equals(true)) {
            int fakeCount = vanishManager.getNonVanishedPlayerCount()-1;
            int newCount = fakeCount + 1;
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%name%", player.getName());
            localMap.put("%count%", String.valueOf(fakeCount));
            localMap.put("%newCount%", String.valueOf(newCount));
            messageManager.broadcastMessage("chatting.join-msg", localMap);
            event.setJoinMessage(null);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        eventManager.removePlayer(event.getPlayer());
        vanishManager.removeVanishedPlayer(event.getPlayer());
        scoreboardManager.removeScoreboard(event.getPlayer());

        if (configManager.get("suppressVanilla").equals(true)) {
            int fakeCount = vanishManager.getNonVanishedPlayerCount();
            int newCount = fakeCount - 1;
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%name%", event.getPlayer().getName());
            localMap.put("%count%", String.valueOf(fakeCount));
            localMap.put("%newCount%", String.valueOf(newCount));
            messageManager.broadcastMessage("chatting.join-msg", localMap);
            event.setQuitMessage(null);
        }
    }
}