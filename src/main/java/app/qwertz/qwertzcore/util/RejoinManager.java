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
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RejoinManager {
    private final QWERTZcore plugin;
    private final Map<UUID, Long> playerLeaveTimes; // Player UUID -> Leave timestamp
    private final Map<UUID, Location> playerLeaveLocations; // Player UUID -> Leave location
    private final Map<UUID, Boolean> playerWasAlive; // Player UUID -> Was alive when left

    public RejoinManager(QWERTZcore plugin) {
        this.plugin = plugin;
        this.playerLeaveTimes = new HashMap<>();
        this.playerLeaveLocations = new HashMap<>();
        this.playerWasAlive = new HashMap<>();
        
        // Schedule cleanup task every 5 minutes
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::cleanupExpiredData, 6000L, 6000L); // 5 minutes
    }

    /**
     * Records when a player leaves the server
     */
    public void recordPlayerLeave(Player player) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        playerLeaveTimes.put(playerId, currentTime);
        playerLeaveLocations.put(playerId, player.getLocation());
        playerWasAlive.put(playerId, plugin.getEventManager().isPlayerAlive(player));
        
        plugin.getLogger().info("Recorded player leave: " + player.getName() + " at " + currentTime);
    }

    /**
     * Checks if a player is rejoining within the allowed time window
     */
    public boolean isRejoining(Player player) {
        if (!plugin.getConfigManager().getAllowRejoining()) {
            return false;
        }

        UUID playerId = player.getUniqueId();
        if (!playerLeaveTimes.containsKey(playerId)) {
            return false;
        }

        long leaveTime = playerLeaveTimes.get(playerId);
        long currentTime = System.currentTimeMillis();
        long rejoinTimeMs = plugin.getConfigManager().getRejoinTime() * 1000L; // Convert to milliseconds

        boolean isRejoining = (currentTime - leaveTime) <= rejoinTimeMs;
        
        if (isRejoining) {
            plugin.getLogger().info("Player " + player.getName() + " is rejoining within " + 
                plugin.getConfigManager().getRejoinTime() + " seconds");
        }

        return isRejoining;
    }

    /**
     * Gets the location where the player was when they left
     */
    public Location getLeaveLocation(UUID playerId) {
        return playerLeaveLocations.get(playerId);
    }

    /**
     * Checks if the player was alive when they left
     */
    public boolean wasAliveWhenLeft(UUID playerId) {
        return playerWasAlive.getOrDefault(playerId, false);
    }

    /**
     * Cleans up tracking data for a player (called after successful rejoin or timeout)
     */
    public void cleanupPlayerData(UUID playerId) {
        playerLeaveTimes.remove(playerId);
        playerLeaveLocations.remove(playerId);
        playerWasAlive.remove(playerId);
    }

    /**
     * Handles player rejoin logic
     */
    public void handlePlayerRejoin(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (!isRejoining(player)) {
            // Not rejoining, clean up any old data and handle as normal join
            cleanupPlayerData(playerId);
            return;
        }

        plugin.getLogger().info("Handling rejoin for player: " + player.getName());

        // Get the player's previous state
        Location leaveLocation = getLeaveLocation(playerId);
        boolean wasAlive = wasAliveWhenLeft(playerId);

        // Teleport player back to where they were
        if (leaveLocation != null && leaveLocation.getWorld() != null) {
            player.teleport(leaveLocation);
            plugin.getLogger().info("Teleported rejoining player " + player.getName() + " back to previous location");
        }

        // Restore their alive/dead state
        if (wasAlive) {
                plugin.getEventManager().revivePlayer(player, player);
                plugin.getLogger().info("Restored alive state for rejoining player: " + player.getName());
            }

        // Clean up tracking data
        cleanupPlayerData(playerId);
        
        // Send rejoin message to the player
        plugin.getMessageManager().sendMessage(player, "rejoin.welcome-back");
        
        // Broadcast rejoin message to everyone
        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%name%", player.getName());
        plugin.getMessageManager().broadcastMessage("chatting.rejoin-msg", localMap);
        
        plugin.getLogger().info("Successfully handled rejoin for player: " + player.getName());
    }

    /**
     * Cleans up expired tracking data (players who left too long ago)
     */
    public void cleanupExpiredData() {
        long currentTime = System.currentTimeMillis();
        long rejoinTimeMs = plugin.getConfigManager().getRejoinTime() * 1000L;
        
        playerLeaveTimes.entrySet().removeIf(entry -> {
            boolean expired = (currentTime - entry.getValue()) > rejoinTimeMs;
            if (expired) {
                UUID playerId = entry.getKey();
                playerLeaveLocations.remove(playerId);
                playerWasAlive.remove(playerId);
                plugin.getLogger().info("Cleaned up expired rejoin data for player: " + playerId);
            }
            return expired;
        });
    }

    /**
     * Gets the number of players currently being tracked for rejoin
     */
    public int getTrackedPlayerCount() {
        return playerLeaveTimes.size();
    }
}
