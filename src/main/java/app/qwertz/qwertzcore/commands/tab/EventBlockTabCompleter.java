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

package app.qwertz.qwertzcore.commands.tab;

import app.qwertz.qwertzcore.blocks.QWERTZcoreBlockType;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EventBlockTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First argument: block types
            String partialBlockType = args[0].toUpperCase();
            completions.addAll(Arrays.stream(QWERTZcoreBlockType.values())
                    .map(Enum::name)
                    .filter(name -> name.startsWith(partialBlockType))
                    .collect(Collectors.toList()));
        } else if (args.length == 2) {
            // Second argument: materials (with "null" as first option)
            String partialMaterial = args[1].toLowerCase();
            
            // Add "null" as first option if it matches
            if ("null".startsWith(partialMaterial)) {
                completions.add("null");
            }
            
            // Add all block materials
            completions.addAll(Arrays.stream(Material.values())
                    .filter(Material::isBlock)
                    .map(material -> material.getKey().getKey())
                    .filter(name -> name.startsWith(partialMaterial))
                    .collect(Collectors.toList()));
        }

        // Use copyPartialMatches but ensure "null" stays first if present
        List<String> result = StringUtil.copyPartialMatches(args[args.length - 1], completions, new ArrayList<>());
        
        // Ensure "null" is first if it's in the results
        if (args.length == 2 && result.contains("null")) {
            result.remove("null");
            result.add(0, "null");
        }
        
        return result;
    }
}