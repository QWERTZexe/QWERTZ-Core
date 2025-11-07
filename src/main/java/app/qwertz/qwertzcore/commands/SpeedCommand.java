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
import java.util.Map;

public class SpeedCommand implements CommandExecutor {
    private final QWERTZcore plugin;

    public SpeedCommand(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "general.only-player-execute");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            plugin.getMessageManager().sendInvalidUsage(sender, "/speed <speed>");
            return true;
        }

        int speed;
        try {
            speed = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            plugin.getMessageManager().sendMessage(sender, "configgui.invalid-number");
            return true;
        }

        if (speed < 1 || speed > 10) {
            plugin.getMessageManager().sendMessage(sender, "speed.out-of-range");
            return true;
        }

        float speedValue = speed / 10.0f; // Converts 1-10 to 0.1-1.0

        player.setWalkSpeed(speedValue);
        player.setFlySpeed(speedValue/2);
        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%speed%", String.valueOf(speed));
        plugin.getMessageManager().sendMessage(sender, "speed.changed", localMap);
        return true;
    }
}
