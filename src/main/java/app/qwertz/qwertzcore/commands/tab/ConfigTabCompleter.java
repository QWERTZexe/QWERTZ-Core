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

import app.qwertz.qwertzcore.QWERTZcore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigTabCompleter implements TabCompleter {

    private final QWERTZcore plugin;
    private final List<String> fontOptions = Arrays.asList("default", "qwertz", "modern", "blocky");
    private final List<String> booleanOptions = Arrays.asList("true", "false");
    private final List<String> colorList = Arrays.asList("&1", "&2", "&3", "&4", "&5", "&6", "&7", "&8", "&9", "&0", "&a", "&b", "&c", "&d", "&e", "&f", "&k", "&l", "&m", "&n", "&o", "&r");
    public ConfigTabCompleter(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> keys = new ArrayList<>(plugin.getConfigManager().getKeys());
            keys.add("spawn");  // Add "spawn" as a valid key
            return keys.stream()
                    .filter(key -> key.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            String key = args[0].toLowerCase();
            if (key.equals("spawn")) {
                return List.of("currentpos");
            } else if (key.equals("font")) {
                return fontOptions.stream()
                        .filter(font -> font.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (key.contains("color")) {
                return colorList.stream()
                        .filter(option -> option.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
            else {
                Object value = plugin.getConfigManager().get(key);
                if (value == null || value instanceof Boolean) {
                    return booleanOptions.stream()
                            .filter(option -> option.startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                } else {
                    return List.of(value.toString());
                }
            }
        }
        return new ArrayList<>();
    }
}