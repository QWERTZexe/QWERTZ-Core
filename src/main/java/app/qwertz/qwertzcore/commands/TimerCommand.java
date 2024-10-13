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
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class TimerCommand implements CommandExecutor {

    private final QWERTZcore plugin;
    private BukkitTask currentTimer;

    public TimerCommand(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(plugin.getConfigManager().getColor("colorError") + "Usage: /timer <seconds> or /timer cancel");
            return false;
        }

        if (args[0].equalsIgnoreCase("cancel")) {
            cancelTimer();
            sender.sendMessage(plugin.getConfigManager().getColor("colorPrimary") + "Timer cancelled.");
            return true;
        }

        try {
            int seconds = Integer.parseInt(args[0]);
            if (seconds <= 0) {
                sender.sendMessage(plugin.getConfigManager().getColor("colorError") + "Please provide a positive number of seconds.");
                return false;
            }
            startTimer(seconds);
            return true;
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getConfigManager().getColor("colorError") + "Invalid number format. Please provide a valid number of seconds.");
            return false;
        }
    }

    private void startTimer(int seconds) {
        cancelTimer(); // Cancel any existing timer

        String startMessage = String.format("%s %s%s%sA timer just got started!",
                QWERTZcore.CORE_ICON, plugin.getConfigManager().getColor("colorPrimary"), plugin.getConfigManager().getColor("colorSuccess"), plugin.getConfigManager().getColor("colorPrimary"));
        broadcastMessage(startMessage);
        broadcastActionBar(startMessage);

        currentTimer = new BukkitRunnable() {
            int timeLeft = seconds;

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    broadcastMessage(plugin.getConfigManager().getColor("colorSecondary") + "Time's up!");
                    broadcastActionBar(plugin.getConfigManager().getColor("colorSecondary") + "Time's up!");
                    currentTimer = null;
                    cancel();
                    return;
                }

                String message = String.format("%s %s%d %sseconds",
                        QWERTZcore.CORE_ICON, plugin.getConfigManager().getColor("colorSuccess"), timeLeft, plugin.getConfigManager().getColor("colorPrimary"));

                broadcastMessage(message);
                broadcastActionBar(message);

                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second (20 ticks)
    }

    private void cancelTimer() {
        if (currentTimer != null) {
            currentTimer.cancel();
            currentTimer = null;
            broadcastMessage(plugin.getConfigManager().getColor("colorPrimary") + "Timer has been cancelled.");
        }
    }

    private void broadcastMessage(String message) {
        Bukkit.broadcastMessage(message);
    }

    private void broadcastActionBar(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
        }
    }
}