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

package app.qwertz.qwertzcore.commands;

import app.qwertz.qwertzcore.QWERTZcore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class EventCountdownCommand implements CommandExecutor {

    private final QWERTZcore plugin;
    private BukkitRunnable countdownTask;
    private int remainingSeconds;

    public EventCountdownCommand(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /eventcountdown <time|cancel>");
            return true;
        }

        if (args[0].equalsIgnoreCase("cancel")) {
            cancelCountdown();
            plugin.getScoreboardManager().updateCountdown("...");
            return true;
        }
        String timeArg = args[0].toLowerCase();
        int minutes = 0;
        int seconds = 0;

        if (timeArg.endsWith("sec")) {
            seconds = Integer.parseInt(timeArg.substring(0, timeArg.length() - 1));
        } else if (timeArg.endsWith("min")) {
            minutes = Integer.parseInt(timeArg.substring(0, timeArg.length() - 3));
        } else {
            minutes = Integer.parseInt(timeArg);
        }

        remainingSeconds = minutes * 60 + seconds;

        if (remainingSeconds <= 0 || remainingSeconds > 60 * 60) { // Max 1 hour
            if (countdownTask != null) {
                countdownTask.cancel();
            }
            cancelCountdown();
            plugin.getScoreboardManager().updateCountdown("...");
            sender.sendMessage(ChatColor.RED + "Invalid time. Please specify a time between 1 second and 60 minutes.");
            return true;
        }

        if (countdownTask != null) {
            countdownTask.cancel();
        }

        startCountdown();
        sender.sendMessage(ChatColor.GREEN + "Event countdown started for " + formatTime(remainingSeconds));
        return true;
    }

    private void startCountdown() {
        countdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (remainingSeconds <= 0) {
                    Bukkit.broadcastMessage(QWERTZcore.CORE_ICON + ChatColor.GREEN + " Event " + plugin.getConfigManager().getEventName() + " is starting now!");
                    updateScoreboard(0);
                    this.cancel();
                    return;
                }

                if (remainingSeconds <= 10 ||
                        (remainingSeconds <= 60 && remainingSeconds % 10 == 0) ||
                        remainingSeconds % 60 == 0) {
                    broadcastCountdown();
                }

                updateScoreboard(remainingSeconds);
                remainingSeconds--;
            }
        };

        countdownTask.runTaskTimer(plugin, 0L, 20L); // Run every second
    }

    private void broadcastCountdown() {
        String timeLeft = formatTime(remainingSeconds);
        Bukkit.broadcastMessage(QWERTZcore.CORE_ICON + ChatColor.YELLOW + " Event " + plugin.getConfigManager().getEventName() +
                " starts in " + ChatColor.RED + timeLeft + ChatColor.YELLOW + "!");
    }

    private String formatTime(int seconds) {
        if (seconds >= 60) {
            return (seconds / 60) + "min";
        } else {
            return seconds + "sec";
        }
    }

    private void updateScoreboard(int seconds) {
        plugin.getScoreboardManager().updateCountdown(formatTime(seconds));
    }

    public void cancelCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        updateScoreboard(0);
    }
}
