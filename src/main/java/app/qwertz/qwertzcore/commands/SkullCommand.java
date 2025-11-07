/*
        Copyright (C) 2025 QWERTZ_EXE

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
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.HashMap;

public class SkullCommand implements CommandExecutor {
    private final QWERTZcore plugin;

    public SkullCommand(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "general.only-player-execute");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            plugin.getMessageManager().sendInvalidUsage(sender, "/" + label + " <player>");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        String targetName = args[0];

        // Create player head item
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        
        if (skullMeta != null) {
            // Set the owner of the skull
            // Try to use newer API if available, fallback to deprecated setOwner
            try {
                org.bukkit.OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(targetName);
                skullMeta.setOwningPlayer(offlinePlayer);
            } catch (Exception e) {
                // Fallback to deprecated method for older versions
                skullMeta.setOwner(targetName);
            }
            skull.setItemMeta(skullMeta);
        }

        // Give the skull to the player
        HashMap<Integer, ItemStack> excess = player.getInventory().addItem(skull);
        
        if (!excess.isEmpty()) {
            // Inventory is full, drop the item
            player.getWorld().dropItemNaturally(player.getLocation(), skull);
        }

        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%name%", targetName);
        plugin.getMessageManager().sendMessage(sender, "skull.received", localMap);
        plugin.getSoundManager().playSound(player);
        return true;
    }
}

