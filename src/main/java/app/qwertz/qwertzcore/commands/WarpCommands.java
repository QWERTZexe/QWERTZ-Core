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
            plugin.getMessageManager().sendConsole(sender, "general.only-player-execute");
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
            plugin.getMessageManager().sendMessage(player, "warps.cannot-set");
            plugin.getSoundManager().playSound(player);
            return true;
        }

        if (args.length != 1) {
            plugin.getMessageManager().sendInvalidUsage(player, "/setwarp <name>");
            plugin.getSoundManager().playSound(player);
            return true;
        }

        String warpName = args[0].toLowerCase();
        Location location = player.getLocation();
        plugin.getConfigManager().addWarp(warpName, location);
        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%warp%", warpName);
        plugin.getMessageManager().sendMessage(player, "warps.set", localMap);
        plugin.getSoundManager().playSound(player);
        return true;
    }

    private boolean handleWarp(Player player, String[] args) {
        if (args.length != 1) {
            plugin.getMessageManager().sendInvalidUsage(player, "/warp <name>");
            plugin.getSoundManager().playSound(player);
            return true;
        }

        String warpName = args[0].toLowerCase();
        Location warpLocation = plugin.getConfigManager().getWarp(warpName);

        if (warpLocation == null) {
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%warp%", warpName);
            plugin.getMessageManager().sendMessage(player, "warps.nonexistent", localMap);
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
                player.sendTitle(plugin.getMessageManager().prepareMessage(plugin.getMessageManager().getMessage("warps.alive-title.title"), new HashMap<>()), plugin.getMessageManager().prepareMessage(plugin.getMessageManager().getMessage("warps.alive-title.subtitle"), new HashMap<>()), 10, 70, 20);
                plugin.getMessageManager().sendMessage(player, "warps.alive-message");
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
            plugin.getMessageManager().sendMessage(player, "warps.cannot-delete");
            plugin.getSoundManager().playSound(player);
            return true;
        }

        if (args.length != 1) {
            plugin.getMessageManager().sendInvalidUsage(player, "/delwarp <name>");
            plugin.getSoundManager().playSound(player);
            return true;
        }

        String warpName = args[0].toLowerCase();
        if (plugin.getConfigManager().getWarp(warpName) == null) {
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%warp%", warpName);
            plugin.getMessageManager().sendMessage(player, "warps.nonexistent", localMap);
            plugin.getSoundManager().playSound(player);
            return true;
        }

        plugin.getConfigManager().removeWarp(warpName);
        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%warp%", warpName);
        plugin.getMessageManager().sendMessage(player, "warps.delete", localMap);
        plugin.getSoundManager().playSound(player);
        return true;
    }

    private boolean handleWarps(Player player) {
        Set<String> warpNames = plugin.getConfigManager().getWarpNames();
        if (warpNames.isEmpty()) {
            plugin.getMessageManager().sendMessage(player, "warps.no-warps");
            plugin.getSoundManager().playSound(player);
        } else {
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%list%", String.join(", ", warpNames));
            plugin.getMessageManager().sendMessage(player, "warps.list", localMap);
            plugin.getSoundManager().playSound(player);
        }
        return true;
    }

    private void unrevivePlayer(Player player) {
        plugin.getEventManager().handlePlayerDeath(player, true);
        plugin.getMessageManager().sendMessage(player, "warps.warp-while-alive");
        plugin.getSoundManager().playSound(player);
    }

    private void teleportToWarp(Player player, Location location, boolean wasAlive) {
        player.teleport(location);
        plugin.getMessageManager().sendMessage(player, "warps.success");
        plugin.getSoundManager().playSound(player);
        if (wasAlive) {
            unrevivePlayer(player);
        }
    }
}