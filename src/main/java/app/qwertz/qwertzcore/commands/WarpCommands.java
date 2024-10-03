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
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
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
                return handleWarps(player);
            default:
                return false;
        }
    }

    private boolean handleSetWarp(Player player, String[] args) {
        if (!player.hasPermission("qwertzcore.setwarp")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to set warps.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /setwarp <name>");
            return true;
        }

        String warpName = args[0].toLowerCase();
        Location location = player.getLocation();
        plugin.getConfigManager().addWarp(warpName, location);
        player.sendMessage(ChatColor.GREEN + "Warp '" + warpName + "' has been set at your current location.");
        return true;
    }

    private boolean handleWarp(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /warp <name>");
            return true;
        }

        String warpName = args[0].toLowerCase();
        Location warpLocation = plugin.getConfigManager().getWarp(warpName);

        if (warpLocation == null) {
            player.sendMessage(ChatColor.RED + "Warp '" + warpName + "' does not exist.");
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
                player.sendMessage(ChatColor.RED + "WARNING: " + ChatColor.YELLOW + "You are still alive! Type /warp " + warpName + " again within 10 seconds to confirm teleportation.");
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
            player.sendMessage(ChatColor.RED + "You don't have permission to delete warps.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /delwarp <name>");
            return true;
        }

        String warpName = args[0].toLowerCase();
        if (plugin.getConfigManager().getWarp(warpName) == null) {
            player.sendMessage(ChatColor.RED + "Warp '" + warpName + "' does not exist.");
            return true;
        }

        plugin.getConfigManager().removeWarp(warpName);
        player.sendMessage(ChatColor.GREEN + "Warp '" + warpName + "' has been deleted.");
        return true;
    }

    private boolean handleWarps(Player player) {
        Set<String> warpNames = plugin.getConfigManager().getWarpNames();
        if (warpNames.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "There are no warps set.");
        } else {
            player.sendMessage(ChatColor.GREEN + "Available warps: " + String.join(", ", warpNames));
        }
        return true;
    }

    private void unrevivePlayer(Player player) {
        plugin.getEventManager().handlePlayerDeath(player);
        player.sendMessage(ChatColor.RED + "You have been unrevived as you chose to teleport while alive.");
    }

    private void teleportToWarp(Player player, Location location, boolean wasAlive) {
        player.teleport(location);
        player.sendMessage(ChatColor.GREEN + "Teleported to warp.");
        if (wasAlive) {
            unrevivePlayer(player);
        }
    }
}