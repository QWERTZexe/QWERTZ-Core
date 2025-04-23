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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class KitCommand implements CommandExecutor {
    private final QWERTZcore plugin;

    public KitCommand(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "general.only-player-execute");
            return true;
        }

        Player player = (Player) sender;

        if (label.equalsIgnoreCase("createkit")) {
            if (args.length != 1) {
                plugin.getMessageManager().sendInvalidUsage(player, "/createkit <kitname>");
                plugin.getSoundManager().playSound(player);
                return true;
            }
            createKit(player, args[0]);
        } else if (label.equalsIgnoreCase("kit")) {
            if (args.length != 2) {
                plugin.getMessageManager().sendInvalidUsage(player, "/kit <kitname> <player|alive|dead|all>");
                plugin.getSoundManager().playSound(player);
                return true;
            }
            giveKit(player, args[0], args[1]);
        } else if (label.equalsIgnoreCase("delkit")) {
            if (args.length != 1) {
                plugin.getMessageManager().sendInvalidUsage(player, "/delkit <kitname>");
                plugin.getSoundManager().playSound(player);
                return true;
            }
            deleteKit(player, args[0]);
        } else if (label.equalsIgnoreCase("kits")) {
            listKits(player);
        }

        return true;
    }

    private void createKit(Player player, String kitName) {
        List<ItemStack> items = new ArrayList<>();

        // Add main inventory items
        for (ItemStack item : player.getInventory().getContents()) {
            items.add(item != null ? item.clone() : null);
        }

        // Add armor contents
        for (ItemStack item : player.getInventory().getArmorContents()) {
            items.add(item != null ? item.clone() : null);
        }

        // Add offhand item
        items.add(player.getInventory().getItemInOffHand().clone());

        plugin.getConfigManager().saveKit(kitName, items);
        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%name%", kitName);
        plugin.getMessageManager().sendMessage(player, "kit.created", localMap);
        plugin.getSoundManager().playSound(player);
    }

    private void giveKit(Player sender, String kitName, String target) {
        List<ItemStack> items = plugin.getConfigManager().getKit(kitName);
        if (items == null) {
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%name%", kitName);
            plugin.getMessageManager().sendMessage(sender, "kit.nonexistent", localMap);
            plugin.getSoundManager().playSoundToSender(sender);
            return;
        }

        List<Player> targetPlayers = new ArrayList<>();

        switch (target.toLowerCase()) {
            case "alive":
                for (UUID uuid : plugin.getEventManager().getAlivePlayers()) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        targetPlayers.add(player);
                    }
                }
                break;
            case "dead":
                for (UUID uuid : plugin.getEventManager().getDeadPlayers()) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        targetPlayers.add(player);
                    }
                }
                break;
            case "all":
                targetPlayers.addAll(Bukkit.getOnlinePlayers());
                break;
            default:
                Player targetPlayer = Bukkit.getPlayer(target);
                if (targetPlayer != null) {
                    targetPlayers.add(targetPlayer);
                } else {
                    plugin.getMessageManager().sendMessage(sender, "general.player-not-found");
                    plugin.getSoundManager().playSoundToSender(sender);
                    return;
                }
        }


        for (Player targetPlayer : targetPlayers) {
            // Clear inventory first
            targetPlayer.getInventory().clear();

            // Apply main inventory items
            for (int i = 0; i < 36 && i < items.size(); i++) {
                if (items.get(i) != null) {
                    targetPlayer.getInventory().setItem(i, items.get(i).clone());
                }
            }

            // Apply armor
            if (items.size() > 36) {
                ItemStack[] armorContents = new ItemStack[4];
                for (int i = 0; i < 4 && i + 36 < items.size(); i++) {
                    armorContents[i] = items.get(i + 36) != null ? items.get(i + 36).clone() : null;
                }
                targetPlayer.getInventory().setArmorContents(armorContents);
            }

            // Apply offhand item
            if (items.size() > 40) {
                targetPlayer.getInventory().setItemInOffHand(items.get(40) != null ? items.get(40).clone() : null);
            }
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%name%", kitName);
            plugin.getMessageManager().sendMessage(targetPlayer, "kit.received", localMap);
            plugin.getSoundManager().playSound(targetPlayer);
        }
        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%name%", kitName);
        localMap.put("%amount%", String.valueOf(targetPlayers.size()));
        plugin.getMessageManager().broadcastMessage("kit.broadcast", localMap);
        plugin.getSoundManager().broadcastConfigSound();
    }

    private void deleteKit(Player player, String kitName) {
        if (plugin.getConfigManager().getKit(kitName) == null) {
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%name%", kitName);
            plugin.getMessageManager().sendMessage(player, "kit.nonexistent", localMap);
            plugin.getSoundManager().playSound(player);
            return;
        }

        plugin.getConfigManager().deleteKit(kitName);
        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%name%", kitName);
        plugin.getMessageManager().sendMessage(player, "kit.deleted", localMap);
        plugin.getSoundManager().playSound(player);
    }

    private void listKits(Player player) {
        Set<String> kitNames = plugin.getConfigManager().getKitNames();
        if (kitNames.isEmpty()) {
            plugin.getMessageManager().sendMessage(player, "kit.no-kits");
            plugin.getSoundManager().playSound(player);
        } else {
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%list%", String.join(", ", kitNames));

            plugin.getMessageManager().sendMessage(player, "kit.list", localMap);
            plugin.getSoundManager().playSound(player);
        }
    }
}