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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WarpTabCompleter implements TabCompleter {

    private final QWERTZcore plugin;

    public WarpTabCompleter(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("warp") || command.getName().equalsIgnoreCase("delwarp")) {
            if (args.length == 1) {
                String partialWarpName = args[0].toLowerCase();
                Set<String> warpNames = plugin.getConfigManager().getWarpNames();
                completions.addAll(warpNames.stream()
                        .filter(warp -> warp.toLowerCase().startsWith(partialWarpName))
                        .collect(Collectors.toList()));
            }
        } else if (command.getName().equalsIgnoreCase("setwarp")) {
            if (args.length == 1) {
                completions.add("name");
            }
        }

        return completions;
    }
}