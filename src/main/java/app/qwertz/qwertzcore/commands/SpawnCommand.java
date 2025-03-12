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
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpawnCommand implements CommandExecutor {

    private final QWERTZcore plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, BukkitTask> pendingTeleports = new HashMap<>();

    public SpawnCommand(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendConsole(sender, "general.only-player-execute");
            return true;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();

        if (plugin.getEventManager().isPlayerDead(player)) {
            teleportToSpawn(player, false);
        } else {
            long currentTime = System.currentTimeMillis();
            if (cooldowns.containsKey(playerUUID) && currentTime - cooldowns.get(playerUUID) < 10000) {
                // Player confirmed within 10 seconds
                teleportToSpawn(player, true);
                cooldowns.remove(playerUUID);
                if (pendingTeleports.containsKey(playerUUID)) {
                    pendingTeleports.get(playerUUID).cancel();
                    pendingTeleports.remove(playerUUID);
                }
            } else {
                // First time or cooldown expired, show warning
                player.sendTitle(plugin.getMessageManager().prepareMessage(plugin.getMessageManager().getMessage("spawn.alive-title.title"), new HashMap<>()), plugin.getMessageManager().prepareMessage(plugin.getMessageManager().getMessage("spawn.alive-title.subtitle"), new HashMap<>()), 10, 70, 20);
                plugin.getMessageManager().sendMessage(player, "spawn.alive-message");
                plugin.getSoundManager().playSound(player);
                cooldowns.put(playerUUID, currentTime);

                // Schedule a task to remove the cooldown after 10 seconds
                BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    cooldowns.remove(playerUUID);
                    pendingTeleports.remove(playerUUID);
                }, 200L); // 200 ticks = 10 seconds
                pendingTeleports.put(playerUUID, task);
            }
        }

        return true;
    }
    private void unrevivePlayer(Player player) {
        plugin.getEventManager().handlePlayerDeath(player, true);
        plugin.getMessageManager().sendMessage(player, "spawn.spawn-while-alive");
        plugin.getSoundManager().playSound(player);
    }
    private void teleportToSpawn(Player player, boolean wasAlive) {
        Location spawnLocation = plugin.getConfigManager().getSpawnLocation();
        if (spawnLocation != null) {
            player.teleport(spawnLocation);
            plugin.getMessageManager().sendMessage(player, "spawn.success");
            plugin.getSoundManager().playSound(player);
            if (wasAlive) {
                unrevivePlayer(player);
            }
        } else {
            plugin.getMessageManager().sendMessage(player, "spawn.no-spawn");
            plugin.getSoundManager().playSound(player);
        }
    }
}