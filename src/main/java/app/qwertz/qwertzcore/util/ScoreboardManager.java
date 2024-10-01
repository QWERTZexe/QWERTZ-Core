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
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {
    private final QWERTZcore plugin;
    private final EventManager eventManager;
    private final ConfigManager configManager;
    private final Map<UUID, Scoreboard> playerScoreboards;
    private String eventcountdown;

    public ScoreboardManager(QWERTZcore plugin, EventManager eventManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.eventManager = eventManager;
        this.configManager = configManager;
        this.playerScoreboards = new HashMap<>();
        this.eventcountdown = "...";
        startScoreboardUpdater();
    }

    public void setScoreboard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("qwertzcore", "dummy", ChatColor.GOLD + plugin.getConfigManager().getServerName());
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        updateScoreboard(player, board, obj);
        player.setScoreboard(board);
        playerScoreboards.put(player.getUniqueId(), board);
    }

    private void updateScoreboard(Player player, Scoreboard board, Objective obj) {
        board.getEntries().forEach(board::resetScores);
        if (this.eventcountdown.equals("0s")) {
            this.eventcountdown = "...";
        }
        obj.getScore(ChatColor.STRIKETHROUGH + "----------------" + ChatColor.RESET).setScore(12);
        obj.getScore(ChatColor.YELLOW + "EVENT").setScore(11);
        obj.getScore(ChatColor.GRAY + "| " + ChatColor.WHITE + configManager.formatScoreboardText("Name: ") + ChatColor.GOLD + plugin.getConfigManager().getEventName()).setScore(10);
        obj.getScore(ChatColor.GRAY + "| " + ChatColor.WHITE + configManager.formatScoreboardText("Alive: ") + ChatColor.GREEN + eventManager.getAliveCount()).setScore(9);
        obj.getScore(ChatColor.GRAY + "| " + ChatColor.WHITE + configManager.formatScoreboardText("Dead: ") + ChatColor.RED + eventManager.getDeadCount()).setScore(8);
        obj.getScore(ChatColor.GRAY + "| " + ChatColor.WHITE + configManager.formatScoreboardText("Starting in: ") + ChatColor.AQUA + this.eventcountdown).setScore(7);
        obj.getScore(" ").setScore(6); // Empty line
        obj.getScore(ChatColor.YELLOW + player.getName()).setScore(5);
        obj.getScore(ChatColor.GRAY + "| " + ChatColor.WHITE + configManager.formatScoreboardText("Rank: ") + ChatColor.GOLD + plugin.getRankManager().getRank(player)).setScore(4);
        obj.getScore(ChatColor.GRAY + "| " + ChatColor.WHITE + configManager.formatScoreboardText("Wins: ") + ChatColor.LIGHT_PURPLE + plugin.getDatabaseManager().getWins(player.getUniqueId())).setScore(3);
        obj.getScore(ChatColor.GRAY + "| " + ChatColor.WHITE + configManager.formatScoreboardText("Revive Tokens: ") + ChatColor.AQUA + plugin.getDatabaseManager().getReviveTokens(player.getUniqueId())).setScore(2);
        obj.getScore(ChatColor.STRIKETHROUGH + "----------------").setScore(1);
        obj.getScore(QWERTZcore.CORE_ICON + ChatColor.GOLD + " QWERTZ Core").setScore(0);
    }

    private void startScoreboardUpdater() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Scoreboard board = playerScoreboards.get(player.getUniqueId());
                if (board != null) {
                    updateScoreboard(player, board, board.getObjective("qwertzcore"));
                }
            }
        }, 20L, 20L); // Update every second
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
}