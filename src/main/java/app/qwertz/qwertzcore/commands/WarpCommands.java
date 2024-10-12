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
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WarpCommands implements CommandExecutor {

    private final QWERTZcore plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, BukkitTask> pendingTeleports = new HashMap<>();

    public WarpCommands(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        switch (label.toLowerCase()) {
            case "setwarp":
                return handleSetWarp(player, args);
            case "warp":
                return handleWarp(player, args);
            case "delwarp":
                return handleDelWarp(player, args);
            case "warps":
            case "listwarps":
                return handleWarps(player);
            default:
                return false;
        }
    }

    private boolean handleSetWarp(Player player, String[] args) {
        if (!player.hasPermission("qwertzcore.setwarp")) {
            player.sendMessage(QWERTZcore.CORE_ICON + ChatColor.RED + " You don't have permission to set warps!");
            plugin.getSoundManager().playSound(player);
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /setwarp <name>");
            plugin.getSoundManager().playSound(player);
            return true;
        }

        String warpName = args[0].toLowerCase();
        Location location = player.getLocation();
        plugin.getConfigManager().addWarp(warpName, location);
        player.sendMessage(QWERTZcore.CORE_ICON + ChatColor.GREEN + " Warp " + ChatColor.YELLOW + "'" + warpName + "'" + ChatColor.GREEN + " has been set at your current location!");
        plugin.getSoundManager().playSound(player);
        return true;
    }

    private boolean handleWarp(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /warp <name>");
            plugin.getSoundManager().playSound(player);
            return true;
        }

        String warpName = args[0].toLowerCase();
        Location warpLocation = plugin.getConfigManager().getWarp(warpName);

        if (warpLocation == null) {
            player.sendMessage(QWERTZcore.CORE_ICON + ChatColor.RED + " Warp " + ChatColor.YELLOW + "'" + warpName + "'" + ChatColor.RED + " does not exist!");
            plugin.getSoundManager().playSound(player);
            return true;
        }

        UUID playerUUID = player.getUniqueId();

        if (plugin.getEventManager().isPlayerDead(player)) {
            teleportToWarp(player, warpLocation, false);
        } else {
            long currentTime = System.currentTimeMillis();
            if (cooldowns.containsKey(playerUUID) && currentTime - cooldowns.get(playerUUID) < 10000) {
                teleportToWarp(player, warpLocation, true);
                cooldowns.remove(playerUUID);
                if (pendingTeleports.containsKey(playerUUID)) {
                    pendingTeleports.get(playerUUID).cancel();
                    pendingTeleports.remove(playerUUID);
                }
            } else {
                player.sendTitle(ChatColor.RED + "WARNING", ChatColor.YELLOW + "You are still alive!", 10, 70, 20);
                player.sendMessage(ChatColor.RED + "WARNING: " + ChatColor.YELLOW + "You are still alive! Type /warp " + warpName + " again within 10 seconds to confirm teleportation!");
                plugin.getSoundManager().playSound(player);
                cooldowns.put(playerUUID, currentTime);

                BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    cooldowns.remove(playerUUID);
                    pendingTeleports.remove(playerUUID);
                }, 200L);
                pendingTeleports.put(playerUUID, task);
            }
        }

        return true;
    }

    private boolean handleDelWarp(Player player, String[] args) {
        if (!player.hasPermission("qwertzcore.delwarp")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to delete warps!");
            plugin.getSoundManager().playSound(player);
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /delwarp <name>");
            plugin.getSoundManager().playSound(player);
            return true;
        }

        String warpName = args[0].toLowerCase();
        if (plugin.getConfigManager().getWarp(warpName) == null) {
            player.sendMessage(QWERTZcore.CORE_ICON + ChatColor.RED + " Warp " + ChatColor.YELLOW + "'" + warpName + "'" + ChatColor.RED + " does not exist!");
            plugin.getSoundManager().playSound(player);
            return true;
        }

        plugin.getConfigManager().removeWarp(warpName);
        player.sendMessage(QWERTZcore.CORE_ICON + ChatColor.RED + " Warp " + ChatColor.YELLOW + "'" + warpName + "'" + ChatColor.RED +  " has been deleted!");
        plugin.getSoundManager().playSound(player);
        return true;
    }

    private boolean handleWarps(Player player) {
        Set<String> warpNames = plugin.getConfigManager().getWarpNames();
        if (warpNames.isEmpty()) {
            player.sendMessage(QWERTZcore.CORE_ICON + ChatColor.YELLOW + " There are no warps set!");
            plugin.getSoundManager().playSound(player);
        } else {
            player.sendMessage(QWERTZcore.CORE_ICON + ChatColor.YELLOW + " Available warps: " + String.join(", ", warpNames));
            plugin.getSoundManager().playSound(player);
        }
        return true;
    }

    private void unrevivePlayer(Player player) {
        plugin.getEventManager().handlePlayerDeath(player, true);
        player.sendMessage(ChatColor.RED + "You have been unrevived as you chose to teleport while alive!");
        plugin.getSoundManager().playSound(player);
    }

    private void teleportToWarp(Player player, Location location, boolean wasAlive) {
        player.teleport(location);
        player.sendMessage(QWERTZcore.CORE_ICON + ChatColor.GREEN + " Teleported to warp!");
        plugin.getSoundManager().playSound(player);
        if (wasAlive) {
            unrevivePlayer(player);
        }
    }
}