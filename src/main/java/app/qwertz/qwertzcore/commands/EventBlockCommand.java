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
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;

public class EventBlockCommand implements CommandExecutor {
    private final QWERTZcore plugin;

    public EventBlockCommand(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "general.only-player-execute");
            return true;
        }
        
        HashMap<String, String> localMap = new HashMap<>();
        String types = Arrays.toString(QWERTZcoreBlockType.values());
        localMap.put("%types%", types);
        Player player = (Player) sender;

        if (args.length < 1) {
            plugin.getMessageManager().sendMessage(player, "specialblocks.list-header");
            
            java.util.Map<String, String> specialBlocks = plugin.getConfigManager().getSpecialBlocks();
            for (java.util.Map.Entry<String, String> entry : specialBlocks.entrySet()) {
                String blockType = entry.getKey();
                String material = entry.getValue();
                if (material == null || material.isEmpty()) {
                    material = "null";
                }
                
                HashMap<String, String> entryMap = new HashMap<>();
                entryMap.put("%blockType%", blockType);
                entryMap.put("%material%", material);
                plugin.getMessageManager().sendMessage(player, "specialblocks.list-entry", entryMap);
            }
            
            plugin.getSoundManager().playSound(player);
            return true;
        }

        String blockType = args[0].toUpperCase();
        
        // Validate block type
        if (!plugin.getBlockManager().isValidBlockType(blockType)) {
            plugin.getMessageManager().sendMessage(player, "specialblocks.invalid-type", localMap);
            plugin.getSoundManager().playSound(player);
            return true;
        }

        // If no material provided or "null" is specified, set to null
        if (args.length == 1 || (args.length == 2 && args[1].equalsIgnoreCase("null"))) {
            plugin.getConfigManager().setSpecialBlockMaterial(blockType, null);
            HashMap<String, String> localMap2 = new HashMap<>();
            localMap2.put("%blockType%", blockType);
            localMap2.put("%material%", "null");
            plugin.getMessageManager().sendMessage(player, "specialblocks.set", localMap2);
            plugin.getSoundManager().playSound(player);
            return true;
        }

        // Material provided - validate and set
        String materialInput = args[1];
        Material material = Material.matchMaterial(materialInput);
        
        if (material == null || !material.isBlock()) {
            plugin.getMessageManager().sendMessage(player, "specialblocks.invalid-material");
            plugin.getSoundManager().playSound(player);
            return true;
        }

        // Convert to namespaced format (minecraft:material_name)
        String materialName = material.getKey().toString();
        plugin.getConfigManager().setSpecialBlockMaterial(blockType, materialName);
        
        HashMap<String, String> localMap3 = new HashMap<>();
        localMap3.put("%blockType%", blockType);
        localMap3.put("%material%", materialName);
        plugin.getMessageManager().sendMessage(player, "specialblocks.set", localMap3);
        plugin.getSoundManager().playSound(player);
        
        return true;
    }
}