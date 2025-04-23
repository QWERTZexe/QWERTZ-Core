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

import java.util.HashMap;

public class TimerCommand implements CommandExecutor {

    private final QWERTZcore plugin;
    private BukkitTask currentTimer;

    public TimerCommand(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            plugin.getMessageManager().sendInvalidUsage(sender, "/timer <seconds> or /timer cancel");
            plugin.getSoundManager().playSoundToSender(sender);
            return false;
        }

        if (args[0].equalsIgnoreCase("cancel")) {
            cancelTimer();
            plugin.getMessageManager().sendMessage(sender, "timer.cancelled");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        try {
            int seconds = Integer.parseInt(args[0]);
            if (seconds <= 0) {
                plugin.getMessageManager().sendMessage(sender, "timer.no-number");
                plugin.getSoundManager().playSoundToSender(sender);
                return false;
            }
            startTimer(seconds);
            return true;
        } catch (NumberFormatException e) {
            plugin.getMessageManager().sendMessage(sender, "timer.invalid-number");
            plugin.getSoundManager().playSoundToSender(sender);
            return false;
        }
    }

    private void startTimer(int seconds) {
        cancelTimer(); // Cancel any existing timer
        String startMessage = plugin.getMessageManager().prepareMessage(plugin.getMessageManager().getMessage("timer.started"), new HashMap<>());

        broadcastMessage(startMessage);
        broadcastActionBar(startMessage);

        currentTimer = new BukkitRunnable() {
            int timeLeft = seconds;

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    String msg = plugin.getMessageManager().prepareMessage(plugin.getMessageManager().getMessage("timer.time-up"), new HashMap<>());
                    broadcastMessage(msg);
                    broadcastActionBar(msg);
                    currentTimer = null;
                    cancel();
                    return;
                }

                HashMap<String, String> localMap = new HashMap<>();
                localMap.put("%timeLeft%", String.valueOf(timeLeft));
                String message = plugin.getMessageManager().prepareMessage(plugin.getMessageManager().getMessage("timer.countdown"), localMap);
                if ((Boolean) plugin.getConfigManager().get("chatTimer")) {
                    Bukkit.broadcastMessage(message);
                }
                broadcastActionBar(message);

                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second (20 ticks)
    }

    private void cancelTimer() {
        if (currentTimer != null) {
            currentTimer.cancel();
            currentTimer = null;
            plugin.getMessageManager().broadcastMessage("timer.broadcast-cancel");
            plugin.getSoundManager().broadcastConfigSound();
        }
    }

    private void broadcastMessage(String message) {
        Bukkit.broadcastMessage(message);
        plugin.getSoundManager().broadcastConfigSound();
    }

    private void broadcastActionBar(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
        }
    }
}