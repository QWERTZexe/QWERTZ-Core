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

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ChatReviveTabCompleter implements TabCompleter {

    private final List<String> gameTypes = Arrays.asList("math", "typer", "guess");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("chatrevive")) {
            if (args.length == 1) {
                String partialGameType = args[0].toLowerCase();
                completions.addAll(gameTypes.stream()
                        .filter(type -> type.startsWith(partialGameType))
                        .collect(Collectors.toList()));
            } else if (args.length == 2 && args[0].equalsIgnoreCase("guess")) {
                completions.add("40");
            }
        }

        return completions;
    }
}