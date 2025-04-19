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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ScoreboardManager {
    private final QWERTZcore plugin;
    private final Map<UUID, Scoreboard> playerScoreboards;
    public String eventcountdown;
    private FileConfiguration fileScoreboardConfig;
    private FileConfiguration scoreboardConfig;
    private FileConfiguration internalScoreboardConfig;
    private File scoreboardFile;
    private int scoreboardTaskID = -1; // Store the task ID.  -1 indicates no task is running.

    public ScoreboardManager(QWERTZcore plugin) {
        this.plugin = plugin;
        this.playerScoreboards = new HashMap<>();
        this.eventcountdown = "...";
        loadScoreboardConfig();  // Load scoreboard config
        this.scoreboardConfig = getConfigToUse();
        startScoreboardUpdater();
        initialScoreboards();
    }
    // Helper method to determine which config to use
    private FileConfiguration getConfigToUse() {
        String activeTheme = plugin.getMessageManager().messagesConfig.getString("active-theme");
        if (Objects.equals(activeTheme, "file")) {
            return fileScoreboardConfig;
        } else if (Objects.equals(activeTheme, "internal")) {
            return internalScoreboardConfig;
        } else {
            // Attempt to load from repo
            FileConfiguration repoConfig = plugin.getMessageManager().loadFromRepo(activeTheme, "scoreboard");
            if (repoConfig != null) {
                return repoConfig;
            } else {
                plugin.getLogger().warning("Failed to load theme from repo, using internal.");
                return internalScoreboardConfig; // Fallback to default
            }
        }
    }
    private void loadScoreboardConfig() {
        scoreboardFile = new File(plugin.getDataFolder(), "scoreboard.yml");
        if (!scoreboardFile.exists()) {
            plugin.saveResource("scoreboard.yml", false);
        }
        fileScoreboardConfig = YamlConfiguration.loadConfiguration(scoreboardFile);
        // Load default messages from JAR
        InputStream defaultStream = plugin.getResource("scoreboard.yml");
        if (defaultStream != null) {
            internalScoreboardConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
        }
    }

    public void setScoreboard(Player player) {
        if (plugin.getConfigManager().getScoreBoard()) {
            Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
            String title = scoreboardConfig.getString("title", "&e" + plugin.getConfigManager().getServerName()); // Default title
            title = title.replace("%event%", plugin.getConfigManager().getEventName())
                    .replace("%alive%", String.valueOf(plugin.getEventManager().getAlivePlayerCountWithoutVanish()))
                    .replace("%dead%", String.valueOf(plugin.getEventManager().getDeadPlayerCountWithoutVanish()))
                    .replace("%countdown%", this.eventcountdown)
                    .replace("%player%", player.getName())
                    .replace("%server%", plugin.getConfigManager().getServerName())
                    .replace("%rank%", plugin.getRankManager().getRank(player))
                    .replace("%wins%", String.valueOf(plugin.getDatabaseManager().getWins(player.getUniqueId())))
                    .replace("%tokens%", String.valueOf(plugin.getDatabaseManager().getReviveTokens(player.getUniqueId())));
            Objective obj = board.registerNewObjective("qwertzcore", "dummy", ChatColor.translateAlternateColorCodes('&', title));
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            updateScoreboard(player, board, obj);
            player.setScoreboard(board);
            playerScoreboards.put(player.getUniqueId(), board);
        }
    }

    private void updateScoreboard(Player player, Scoreboard board, Objective obj) {
        board.getEntries().forEach(board::resetScores); // Clear existing scores

        Map<String, Object> lines = scoreboardConfig.getConfigurationSection("lines").getValues(false);

        for (Map.Entry<String, Object> entry : lines.entrySet()) {
            try {
                int lineNumber = Integer.parseInt(entry.getKey());
                String lineValue = (String) entry.getValue();
                if (lineValue != null && !lineValue.isEmpty()) {
                    // Replace placeholders

                    String formattedLine = lineValue.replace("%event%", plugin.getConfigManager().getEventName())
                            .replace("%alive%", String.valueOf(plugin.getEventManager().getAlivePlayerCountWithoutVanish()))
                            .replace("%dead%", String.valueOf(plugin.getEventManager().getDeadPlayerCountWithoutVanish()))
                            .replace("%countdown%", this.eventcountdown)
                            .replace("%player%", player.getName())
                            .replace("%server%", plugin.getConfigManager().getServerName())
                            .replace("%rank%", plugin.getRankManager().getRank(player))
                            .replace("%wins%", String.valueOf(plugin.getDatabaseManager().getWins(player.getUniqueId())))
                            .replace("%tokens%", String.valueOf(plugin.getDatabaseManager().getReviveTokens(player.getUniqueId())));
                    formattedLine = plugin.getMessageManager().prepareMessage(formattedLine, new HashMap<>());
                    obj.getScore(ChatColor.translateAlternateColorCodes('&', formattedLine)).setScore(lineNumber);
                }
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Invalid line number in scoreboard.yml: " + entry.getKey());
            } catch (Exception e) {
                plugin.getLogger().warning("Error processing scoreboard line: " + entry.getKey() + " - " + e.getMessage());
                e.printStackTrace(); // Log the full error
            }
        }

        //Old scoreboard lines
        //obj.getScore(ChatColor.STRIKETHROUGH + "----------------" + ChatColor.RESET).setScore(12);
        // obj.getScore(plugin.getConfigManager().getColor("colorPrimary") + "EVENT").setScore(11);
        // obj.getScore(ChatColor.GRAY + "| " + ChatColor.WHITE + configManager.formatScoreboardText("Name: ") + plugin.getConfigManager().getColor("colorSecondary") + plugin.getConfigManager().getEventName()).setScore(10);
        // obj.getScore(ChatColor.GRAY + "| " + ChatColor.WHITE + configManager.formatScoreboardText("Alive: ") + plugin.getConfigManager().getColor("colorAlive") + eventManager.getAlivePlayerCountWithoutVanish()).setScore(9);
        // obj.getScore(ChatColor.GRAY + "| " + ChatColor.WHITE + configManager.formatScoreboardText("Dead: ") + plugin.getConfigManager().getColor("colorDead") + eventManager.getDeadPlayerCountWithoutVanish()).setScore(8);
        // obj.getScore(ChatColor.GRAY + "| " + ChatColor.WHITE + configManager.formatScoreboardText("Starting in: ") + plugin.getConfigManager().getColor("colorAqua") + this.eventcountdown).setScore(7);
        // obj.getScore(" ").setScore(6); // Empty line
        // obj.getScore(plugin.getConfigManager().getColor("colorPrimary") + player.getName()).setScore(5);
        // obj.getScore(ChatColor.GRAY + "| " + ChatColor.WHITE + configManager.formatScoreboardText("Rank: ") + plugin.getConfigManager().getColor("colorSecondary") + plugin.getRankManager().getRank(player)).setScore(4);
        // obj.getScore(ChatColor.GRAY + "| " + ChatColor.WHITE + configManager.formatScoreboardText("Wins: ") + ChatColor.LIGHT_PURPLE + plugin.getDatabaseManager().getWins(player.getUniqueId())).setScore(3);
        // obj.getScore(ChatColor.GRAY + "| " + ChatColor.WHITE + configManager.formatScoreboardText("Revive Tokens: ") + plugin.getConfigManager().getColor("colorTertiary") + plugin.getDatabaseManager().getReviveTokens(player.getUniqueId())).setScore(2);
        // obj.getScore(ChatColor.STRIKETHROUGH + "----------------").setScore(1);
        // obj.getScore(QWERTZcore.CORE_ICON + ChatColor.GOLD + " QWERTZ Core").setScore(0);
    }

    private void startScoreboardUpdater() {
        stopScoreboardUpdater();
        scoreboardTaskID = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Scoreboard board = playerScoreboards.get(player.getUniqueId());
                if (board != null) {
                    updateScoreboard(player, board, board.getObjective("qwertzcore"));
                }
            }
        }, 20L, 100L).getTaskId(); // Update every second
    }

    public void stopScoreboardUpdater() {
        if (scoreboardTaskID != -1) {
            Bukkit.getScheduler().cancelTask(scoreboardTaskID);
            scoreboardTaskID = -1;
        }
    }

    public int getScoreboardTaskID() {
        return scoreboardTaskID;
    }

    public void updateCountdown(String timeLeft) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Scoreboard board = player.getScoreboard();
            Objective obj = board.getObjective("qwertzcore");
            this.eventcountdown = timeLeft;
            updateScoreboard(player, board, obj);
        }
    }
    public void removeScoreboard(Player player) {
        playerScoreboards.remove(player.getUniqueId());
    }
    public void removeScoreboardFromAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerScoreboards.remove(player.getUniqueId());
            player.setScoreboard(Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard()); // Set them to a blank scoreboard
        }
    }
    private void initialScoreboards() {
        for (Player player: Bukkit.getOnlinePlayers()) {
            setScoreboard(player);
        }
    }
}
