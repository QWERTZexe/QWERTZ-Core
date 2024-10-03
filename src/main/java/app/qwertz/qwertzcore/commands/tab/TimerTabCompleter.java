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

public class TimerTabCompleter implements TabCompleter {

    private final List<String> timerOptions = Arrays.asList("cancel");
    private final List<String> eventCountdownOptions = Arrays.asList("cancel");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("timer")) {
            if (args.length == 1) {
                if (args[0].isEmpty()) {
                    completions.add("10");
                    completions.addAll(timerOptions);
                } else {
                    String partialArg = args[0].toLowerCase();
                    if ("cancel".startsWith(partialArg)) {
                        completions.add("cancel");
                    }
                    if (partialArg.matches("\\d*")) {
                        completions.add("10");
                    }
                }
            }
        } else if (command.getName().equalsIgnoreCase("eventcountdown")) {
            if (args.length == 1) {
                if (args[0].isEmpty()) {
                    completions.add("15min");
                    completions.addAll(eventCountdownOptions);
                } else {
                    String partialArg = args[0].toLowerCase();
                    if ("cancel".startsWith(partialArg)) {
                        completions.add("cancel");
                    }
                    completions.add("15min");
                }
            }
        }

        return completions;
    }
}