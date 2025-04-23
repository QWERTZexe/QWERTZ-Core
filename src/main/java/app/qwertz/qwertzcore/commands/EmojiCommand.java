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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

public class EmojiCommand implements CommandExecutor {

    private final QWERTZcore plugin;

    public EmojiCommand(QWERTZcore plugin) {
            this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "general.only-player-execute");
            return true;
        }

        plugin.getMessageManager().sendMessage(sender, "emojis.list");
        plugin.getMessageManager().sendMessage(sender, "emojis.reminder");
        List<String> emojis = plugin.getMessageManager().getStringList("emojis.emojis");
        for (String emoji : emojis) {
            HashMap<String, String> localMap = new HashMap<>();
            String[] parts = emoji.split("\\|");  // Escape pipe character
            localMap.put("%code%", parts[0]);     // Use array index instead of .first()
            localMap.put("%emoji%", parts.length > 1 ? parts[1] : "");  // Handle missing emoji
            plugin.getMessageManager().sendMessage(sender, "emojis.item", localMap);  // Use correct message key
        }
        return true;
    }
}
