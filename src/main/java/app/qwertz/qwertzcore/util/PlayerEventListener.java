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
        
        // Handle rejoin system first
        if (plugin.getRejoinManager().isRejoining(player)) {
            plugin.getRejoinManager().handlePlayerRejoin(player);
            // Skip normal join processing for rejoining players
            plugin.getVanishManager().hideVanishedPlayers(player);
            plugin.getScoreboardManager().setScoreboard(player);
            plugin.getTablistManager().updateTablist(player);
            return;
        }
        
        // Normal join processing for non-rejoining players
        plugin.getVanishManager().hideVanishedPlayers(player);
        if ((Boolean) plugin.getConfigManager().get("checkForUpdates")) {
            updateChecker.notifyPlayer(player);
        }
        hideCommand.handlePlayerJoin(player);
        plugin.getEventManager().addNewPlayer(player);
        
        // Clear inventory on join if enabled and player doesn't have bypass permission
        if (plugin.getConfigManager().getClearOnJoin() && !player.hasPermission("qwertzcore.staff.bypassclear")) {
            player.getInventory().clear();
            player.getInventory().setArmorContents(new org.bukkit.inventory.ItemStack[4]);
            player.getInventory().setItemInOffHand(null);
            // Clear cursor slot and crafting grid
            if (player.getOpenInventory() != null) {
                player.getOpenInventory().setCursor(null);
                player.getOpenInventory().getTopInventory().clear();
            }
        }
        
        if (plugin.getConfigManager().getTpOnJoin()) {
            player.teleport(plugin.getConfigManager().getSpawnLocation());
        }
        plugin.getScoreboardManager().setScoreboard(player);
        plugin.getTablistManager().updateTablist(player);
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
        Player player = event.getPlayer();
        
        // Record player leave for rejoin system
        plugin.getRejoinManager().recordPlayerLeave(player);
        
        plugin.getEventManager().removePlayer(player);
        plugin.getVanishManager().removeVanishedPlayer(player);
        plugin.getScoreboardManager().removeScoreboard(player);

        if (plugin.getConfigManager().get("suppressVanilla").equals(true)) {
            int fakeCount = plugin.getVanishManager().getNonVanishedPlayerCount();
            int newCount = fakeCount - 1;
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%name%", player.getName());
            localMap.put("%count%", String.valueOf(fakeCount));
            localMap.put("%newCount%", String.valueOf(newCount));
            plugin.getMessageManager().broadcastMessage("chatting.leave-msg", localMap);
            event.setQuitMessage(null);
        }
    }
}