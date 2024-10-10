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

public class ChatMuteCommand implements CommandExecutor {

    private final QWERTZcore plugin;

    public ChatMuteCommand(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean isMuting = label.equalsIgnoreCase("mutechat");
        plugin.getConfigManager().set("chat", !isMuting);

        String message = String.format("%s %sChat has been %s%s",
                QWERTZcore.CORE_ICON,
                plugin.getConfigManager().getColor("colorPrimary"),
                isMuting ? plugin.getConfigManager().getColor("colorDead") + "muted" : plugin.getConfigManager().getColor("colorAlive") + "unmuted",
                plugin.getConfigManager().getColor("colorPrimary") + "!");
        Bukkit.broadcastMessage(message);
        return true;
    }
}