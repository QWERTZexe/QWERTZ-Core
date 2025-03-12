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

import java.util.*;

public class EventManager {
    private final QWERTZcore plugin;
    private final Set<UUID> deadPlayers;
    private final Set<UUID> alivePlayers;
    private final Map<UUID, Long> deathTimes;

    public EventManager(QWERTZcore plugin) {
        this.plugin = plugin;
        this.deadPlayers = new HashSet<>();
        this.alivePlayers = new HashSet<>();
        this.deathTimes = new HashMap<>();
    }

    public boolean revivePlayer(Player target, Player executor) {
        UUID targetUUID = target.getUniqueId();

        if (deadPlayers.remove(targetUUID)) {
            alivePlayers.add(targetUUID);
            if (plugin.getConfigManager().getTpOnRevive()) {
                target.teleport(executor.getLocation());
            }
            deathTimes.remove(targetUUID);
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%name%", target.getName());
            broadcastMessage("event.revive", localMap);
            return true;
        }
        else {
            return false;
        }
    }


    public boolean unrevivePlayer(Player target) {
        UUID targetUUID = target.getUniqueId();

        if (alivePlayers.remove(targetUUID)) {
            deadPlayers.add(targetUUID);
            if (plugin.getConfigManager().getTpOnUnrevive()) {
                target.teleport(plugin.getConfigManager().getSpawnLocation());
            }
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%name%", target.getName());
            broadcastMessage("event.unrevive", localMap);
            return true;
        }
        else {
            return false;
        }
    }
    public int getAliveCount() {
        return alivePlayers.size();
    }

    public int getDeadCount() {
        return deadPlayers.size();
    }
    public void handlePlayerDeath(Player player, boolean noTp) {
        UUID playerUUID = player.getUniqueId();
        if (alivePlayers.remove(playerUUID)) {
            deathTimes.put(playerUUID, System.currentTimeMillis());
            deadPlayers.add(playerUUID);
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%name%", player.getName());
            broadcastMessage("chatting.death", localMap);
        } else if (!deadPlayers.contains(playerUUID)) {
            // If the player wasn't in either list (e.g., new player who died immediately)
            deadPlayers.add(playerUUID);
        }
        if (plugin.getConfigManager().getTpOnDeath() && !noTp) {
            player.teleport(plugin.getConfigManager().getSpawnLocation());
        }
    }

    public void reviveAll(Player sender) {
        for (UUID uuid : deadPlayers) {
            if (plugin.getConfigManager().getTpOnRevive()) {
                Player player = Bukkit.getPlayer(uuid);
                player.teleport(sender.getLocation());
            }
            alivePlayers.add(uuid);
        }
        deadPlayers.clear();
    }

    public void unReviveAll() {
        for (UUID uuid : alivePlayers) {
            if (plugin.getConfigManager().getTpOnUnrevive()) {
                Player player = Bukkit.getPlayer(uuid);
                player.teleport(plugin.getConfigManager().getSpawnLocation());
            }
            deadPlayers.add(uuid);
        }
        alivePlayers.clear();
    }

    public boolean isPlayerDead(Player player) {
        return deadPlayers.contains(player.getUniqueId());
    }

    public boolean isPlayerAlive(Player player) {
        return alivePlayers.contains(player.getUniqueId());
    }

    public Set<UUID> getDeadPlayers() {
        return new HashSet<>(deadPlayers);
    }

    public Set<UUID> getAlivePlayers() {
        return new HashSet<>(alivePlayers);
    }

    public Integer getAlivePlayerCountWithoutVanish() {
        Integer counter = 0;
        for (UUID uuid : alivePlayers) {
            if (!plugin.getVanishManager().getVanishedPlayers().contains(uuid)) {
                counter++;
            }
        }
        return counter;
    }
    public Integer getDeadPlayerCountWithoutVanish() {
        Integer counter = 0;
        for (UUID uuid : deadPlayers) {
            if (!plugin.getVanishManager().getVanishedPlayers().contains(uuid)) {
                counter++;
            }
        }
        return counter;
    }

    public void addNewPlayer(Player player) {
        deadPlayers.add(player.getUniqueId());
    }

    public void removePlayer(Player player) {
        UUID playerUUID = player.getUniqueId();
        deadPlayers.remove(playerUUID);
        alivePlayers.remove(playerUUID);
    }

    public List<Player> getRecentlyDeadPlayers(int seconds) {
        long currentTime = System.currentTimeMillis();
        long timeThreshold = currentTime - (seconds * 1000L);
        List<Player> recentlyDead = new ArrayList<>();

        for (Map.Entry<UUID, Long> entry : deathTimes.entrySet()) {
            if (entry.getValue() >= timeThreshold) {
                Player player = plugin.getServer().getPlayer(entry.getKey());
                if (player != null) {
                    recentlyDead.add(player);
                }
            }
        }

        return recentlyDead;
    }

    private void broadcastMessage(String message, HashMap<String, String> localMap) {
        plugin.getMessageManager().broadcastMessage(message, localMap);
        plugin.getSoundManager().broadcastConfigSound();
    }
}