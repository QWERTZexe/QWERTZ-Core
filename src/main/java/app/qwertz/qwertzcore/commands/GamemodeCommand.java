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
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GamemodeCommand implements CommandExecutor {

    private final QWERTZcore plugin;

    public GamemodeCommand(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getColor("colorError") + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        GameMode targetGameMode;

        switch (command.getName().toLowerCase()) {
            case "gmc":
                targetGameMode = GameMode.CREATIVE;
                break;
            case "gms":
                targetGameMode = GameMode.SURVIVAL;
                break;
            case "gmsp":
                targetGameMode = GameMode.SPECTATOR;
                break;
            case "gma":
                targetGameMode = GameMode.ADVENTURE;
                break;
            case "gm":
                if (args.length == 0) {
                    sender.sendMessage(plugin.getConfigManager().getColor("colorError") + "Usage: /gm <creative|survival|adventure|spectator>");
                    return false;
                }
                targetGameMode = parseGameMode(args[0]);
                if (targetGameMode == null) {
                    sender.sendMessage(plugin.getConfigManager().getColor("colorError") + "Invalid gamemode. Use creative, survival, adventure, or spectator.");
                    return false;
                }
                break;
            default:
                return false;
        }

        player.setGameMode(targetGameMode);
        player.sendMessage(String.format("%s %sYour gamemode has been set to %s%s%s.",
                QWERTZcore.CORE_ICON, plugin.getConfigManager().getColor("colorPrimary"), plugin.getConfigManager().getColor("colorSuccess"), targetGameMode.name(), plugin.getConfigManager().getColor("colorPrimary")));
        return true;
    }

    private GameMode parseGameMode(String mode) {
        switch (mode.toLowerCase()) {
            case "c":
            case "creative":
                return GameMode.CREATIVE;
            case "s":
            case "survival":
                return GameMode.SURVIVAL;
            case "a":
            case "adventure":
                return GameMode.ADVENTURE;
            case "sp":
            case "spectator":
                return GameMode.SPECTATOR;
            default:
                return null;
        }
    }
}