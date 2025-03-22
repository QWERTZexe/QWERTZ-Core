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
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static app.qwertz.qwertzcore.QWERTZcore.*;

public class CoreCommand implements CommandExecutor {

    private final QWERTZcore plugin;

    public CoreCommand(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("qwertzcore.host.reloadcore")) {
            if (args[0].equalsIgnoreCase("reload")) {
                plugin.reloadCore(sender);
                return true;
            }
        }
        sender.sendMessage(String.format(
                "%s %s%sQWERTZ Core %sv%s\n" +
                        "%sMade by: %s%s\n" +
                        "%sSupport | Discord: %s%s",
                CORE_ICON,
                ChatColor.GOLD, ChatColor.BOLD, ChatColor.YELLOW, VERSION,
                ChatColor.AQUA, ChatColor.LIGHT_PURPLE, AUTHORS,
                ChatColor.BLUE, ChatColor.UNDERLINE, DISCORD_LINK
        ));
        plugin.getSoundManager().playSoundToSender(sender);
        return true;
    }
}