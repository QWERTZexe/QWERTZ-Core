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
import app.qwertz.qwertzcore.blocks.QWERTZcoreBlockType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class EventBlockCommand implements CommandExecutor {
    private final QWERTZcore plugin;

    public EventBlockCommand(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getColor("colorError") + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(plugin.getConfigManager().getColor("colorError") + "Usage: /eventblock <blocktype> [material]");
            player.sendMessage(plugin.getConfigManager().getColor("colorPrimary") + "Available block types: " + Arrays.toString(QWERTZcoreBlockType.values()));
            plugin.getSoundManager().playSound(player);
            return true;
        }

        String blockType = args[0].toUpperCase();
        Material material = null;

        if (args.length > 1) {
            material = Material.matchMaterial(args[1]);
            if (material == null || !material.isBlock()) {
                player.sendMessage(plugin.getConfigManager().getColor("colorError") + "Invalid material specified!");
                plugin.getSoundManager().playSound(player);
                return true;
            }
        }

        if (!plugin.getBlockManager().isValidBlockType(blockType)) {
            player.sendMessage(plugin.getConfigManager().getColor("colorError") + "Invalid block type! " + plugin.getConfigManager().getColor("colorPrimary") + "Available types: " + Arrays.toString(QWERTZcoreBlockType.values()));
            plugin.getSoundManager().playSound(player);
            return true;
        }

        ItemStack item = new ItemStack(material != null ? material : QWERTZcoreBlockType.valueOf(blockType).getDefaultMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(QWERTZcore.CORE_ICON + ChatColor.GOLD + " QWERTZ Core " + blockType);
            meta.setLore(Arrays.asList(ChatColor.GRAY + "Place this block to create a special event block!"));
            item.setItemMeta(meta);

            player.getInventory().addItem(item);
            plugin.getSoundManager().playSound(player);
            player.sendMessage(QWERTZcore.CORE_ICON + plugin.getConfigManager().getColor("colorSuccess") + " You have received a " + ChatColor.GOLD + "QWERTZ Core " + plugin.getConfigManager().getColor("colorPrimary") + blockType + plugin.getConfigManager().getColor("colorSuccess") + " with material " + plugin.getConfigManager().getColor("colorPrimary") + item.getType().name() + plugin.getConfigManager().getColor("colorSuccess") + "!");
        }

        return true;
    }
}