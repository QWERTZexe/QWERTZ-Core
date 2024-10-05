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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class DatabaseManager {

    private final QWERTZcore plugin;
    private final File databaseFile;
    private Map<String, Object> database;
    private Map<String, String> specialBlocks;
    private final Gson gson;

    public DatabaseManager(QWERTZcore plugin) {
        this.plugin = plugin;
        this.databaseFile = new File(plugin.getDataFolder(), "database.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.specialBlocks = new HashMap<>();
        loadDatabase();
    }
    public void initializeSpecialBlocks() {
        loadSpecialBlocks();
    }
    private void loadDatabase() {
        if (!databaseFile.exists()) {
            database = new HashMap<>();
            database.put("players", new HashMap<String, PlayerData>());
            database.put("specialBlocks", new HashMap<String, String>());
            saveDatabase();
        } else {
            try (FileReader reader = new FileReader(databaseFile)) {
                Type type = new TypeToken<Map<String, Object>>(){}.getType();
                database = gson.fromJson(reader, type);
                if (database == null) {
                    database = new HashMap<>();
                    database.put("players", new HashMap<String, PlayerData>());
                    database.put("specialBlocks", new HashMap<String, String>());
                }

                // Convert player data to PlayerData objects
                Map<String, PlayerData> players = new HashMap<>();
                Map<String, Object> playersData = (Map<String, Object>) database.get("players");
                if (playersData != null) {
                    for (Map.Entry<String, Object> entry : playersData.entrySet()) {
                        players.put(entry.getKey(), gson.fromJson(gson.toJson(entry.getValue()), PlayerData.class));
                    }
                }
                database.put("players", players);

                // Ensure specialBlocks exists
                if (!database.containsKey("specialBlocks")) {
                    database.put("specialBlocks", new HashMap<String, String>());
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Could not load database: " + e.getMessage());
                database = new HashMap<>();
                database.put("players", new HashMap<String, PlayerData>());
                database.put("specialBlocks", new HashMap<String, String>());
            }
        }
    }

    private void saveDatabase() {
        try (FileWriter writer = new FileWriter(databaseFile)) {
            gson.toJson(database, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save database: " + e.getMessage());
        }
    }

    public int getWins(UUID playerUUID) {
        PlayerData playerData = getPlayerData(playerUUID);
        return playerData.getWins();
    }

    public void addWin(UUID playerUUID) {
        PlayerData playerData = getPlayerData(playerUUID);
        playerData.addWin();
        saveDatabase();
    }
    public void removeWin(UUID playerUUID) {
        PlayerData playerData = getPlayerData(playerUUID);
        playerData.removeWin();
        saveDatabase();
    }

    public int getReviveTokens(UUID playerUUID) {
        PlayerData playerData = getPlayerData(playerUUID);
        return playerData.getReviveTokens();
    }

    public void addReviveToken(UUID playerUUID) {
        PlayerData playerData = getPlayerData(playerUUID);
        playerData.addReviveToken();
        saveDatabase();
    }

    public void removeReviveToken(UUID playerUUID) {
        PlayerData playerData = getPlayerData(playerUUID);
        playerData.removeReviveToken();
        saveDatabase();
    }

    public void setReviveRequestCooldown(UUID playerUUID, long cooldownEndTime) {
        PlayerData playerData = getPlayerData(playerUUID);
        playerData.setReviveRequestCooldown(cooldownEndTime);
        saveDatabase();
    }

    public long getReviveRequestCooldown(UUID playerUUID) {
        PlayerData playerData = getPlayerData(playerUUID);
        return playerData.getReviveRequestCooldown();
    }

    // New methods for message toggle functionality
    public boolean isMessageToggleEnabled(UUID playerUUID) {
        PlayerData playerData = getPlayerData(playerUUID);
        return playerData.isMessageToggleEnabled();
    }

    public void setMessageToggleEnabled(UUID playerUUID, boolean enabled) {
        PlayerData playerData = getPlayerData(playerUUID);
        playerData.setMessageToggleEnabled(enabled);
        saveDatabase();
    }
    private PlayerData getPlayerData(UUID playerUUID) {
        String uuidString = playerUUID.toString();
        Map<String, PlayerData> players = (Map<String, PlayerData>) database.get("players");
        return players.computeIfAbsent(uuidString, k -> new PlayerData());
    }

    private static class PlayerData {
        private int wins;
        private int reviveTokens;
        private long reviveRequestCooldown;
        private boolean messageToggleEnabled;

        public PlayerData() {
            this.wins = 0;
            this.reviveTokens = 0;
            this.reviveRequestCooldown = 0;
            this.messageToggleEnabled = true;
        }

        public int getWins() {
            return wins;
        }

        public void addWin() {
            this.wins++;
        }

        public void removeWin() {
            this.wins--;
        }

        public int getReviveTokens() {
            return reviveTokens;
        }

        public void addReviveToken() {
            this.reviveTokens++;
        }

        public void removeReviveToken() {
            this.reviveTokens = Math.max(0, this.reviveTokens - 1);
        }

        public long getReviveRequestCooldown() {
            return reviveRequestCooldown;
        }

        public void setReviveRequestCooldown(long cooldownEndTime) {
            this.reviveRequestCooldown = cooldownEndTime;
        }
        public boolean isMessageToggleEnabled() {
            return messageToggleEnabled;
        }

        public void setMessageToggleEnabled(boolean messageToggleEnabled) {
            this.messageToggleEnabled = messageToggleEnabled;
        }
    }
    private void loadSpecialBlocks() {
        Map<String, String> loadedBlocks = plugin.getDatabaseManager().getSpecialBlocks();
        if (loadedBlocks != null && !loadedBlocks.isEmpty()) {
            specialBlocks.clear();
            specialBlocks.putAll(loadedBlocks);
        } else {
        }
    }
    public void saveSpecialBlocks(Map<String, String> blocks) {
        database.put("specialBlocks", blocks);
        saveDatabase();
    }

    public Map<String, String> getSpecialBlocks() {
        return new HashMap<>((Map<String, String>) database.get("specialBlocks"));
    }
}