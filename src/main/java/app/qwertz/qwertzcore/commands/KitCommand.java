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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class KitCommand implements CommandExecutor {
    private final QWERTZcore plugin;

    public KitCommand(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getColor("colorError") + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (label.equalsIgnoreCase("createkit")) {
            if (args.length != 1) {
                player.sendMessage(plugin.getConfigManager().getColor("colorError") + "Usage: /createkit <kitname>");
                return true;
            }
            createKit(player, args[0]);
        } else if (label.equalsIgnoreCase("kit")) {
            if (args.length != 2) {
                player.sendMessage(plugin.getConfigManager().getColor("colorError") + "Usage: /kit <kitname> <player|alive|dead|all>");
                return true;
            }
            giveKit(player, args[0], args[1]);
        } else if (label.equalsIgnoreCase("delkit")) {
            if (args.length != 1) {
                player.sendMessage(plugin.getConfigManager().getColor("colorError") + "Usage: /delkit <kitname>");
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
        player.sendMessage(QWERTZcore.CORE_ICON + plugin.getConfigManager().getColor("colorSuccess") + " Kit " + plugin.getConfigManager().getColor("colorPrimary") + "'" + kitName + "'" + plugin.getConfigManager().getColor("colorSuccess") + " has been created!");
    }

    private void giveKit(Player sender, String kitName, String target) {
        List<ItemStack> items = plugin.getConfigManager().getKit(kitName);
        if (items == null) {
            sender.sendMessage(QWERTZcore.CORE_ICON + plugin.getConfigManager().getColor("colorError") + "Kit " + plugin.getConfigManager().getColor("colorPrimary") + "'" + kitName + "'" + plugin.getConfigManager().getColor("colorError") + " does not exist!");
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
                    sender.sendMessage(QWERTZcore.CORE_ICON + plugin.getConfigManager().getColor("colorError") + " Player " + plugin.getConfigManager().getColor("colorPrimary") + "'" + target + "'" + plugin.getConfigManager().getColor("colorError") + " not found!");
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

            targetPlayer.sendMessage(QWERTZcore.CORE_ICON + plugin.getConfigManager().getColor("colorSuccess") + " You have received the kit " + plugin.getConfigManager().getColor("colorPrimary") + "'" + kitName + "'" + plugin.getConfigManager().getColor("colorSuccess") + "!");
        }

        Bukkit.broadcastMessage(QWERTZcore.CORE_ICON + plugin.getConfigManager().getColor("colorSuccess") + " Kit " + plugin.getConfigManager().getColor("colorPrimary") + "'" + kitName + "'" + plugin.getConfigManager().getColor("colorSuccess") + " has been given to " + plugin.getConfigManager().getColor("colorPrimary") + targetPlayers.size() + plugin.getConfigManager().getColor("colorSuccess") + " players!");
    }

    private void deleteKit(Player player, String kitName) {
        if (plugin.getConfigManager().getKit(kitName) == null) {
            player.sendMessage(QWERTZcore.CORE_ICON + plugin.getConfigManager().getColor("colorError") + " Kit " + plugin.getConfigManager().getColor("colorPrimary") + "'" + kitName + "'" + plugin.getConfigManager().getColor("colorError") + " does not exist.");
            return;
        }

        plugin.getConfigManager().deleteKit(kitName);
        player.sendMessage(QWERTZcore.CORE_ICON + plugin.getConfigManager().getColor("colorDead") + " Kit " + plugin.getConfigManager().getColor("colorPrimary") + "'" + kitName + "'" + plugin.getConfigManager().getColor("colorError") + " has been deleted.");
    }

    private void listKits(Player player) {
        Set<String> kitNames = plugin.getConfigManager().getKitNames();
        if (kitNames.isEmpty()) {
            player.sendMessage(QWERTZcore.CORE_ICON + plugin.getConfigManager().getColor("colorPrimary") + " There are no kits available.");
        } else {
            player.sendMessage(QWERTZcore.CORE_ICON + plugin.getConfigManager().getColor("colorSuccess") + " Available kits: " + plugin.getConfigManager().getColor("colorPrimary") + String.join(", ", kitNames));
        }
    }
}