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

import java.util.HashMap;

public class GamemodeCommand implements CommandExecutor {

    private final QWERTZcore plugin;

    public GamemodeCommand(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "general.only-player-execute");
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
                    plugin.getMessageManager().sendInvalidUsage(sender, "/gm <creative|survival|adventure|spectator>");
                    plugin.getSoundManager().playSoundToSender(sender);
                    return false;
                }
                targetGameMode = parseGameMode(args[0]);
                if (targetGameMode == null) {
                    plugin.getMessageManager().sendMessage(sender, "gamemode.invalid");
                    plugin.getSoundManager().playSoundToSender(sender);
                    return false;
                }
                break;
            default:
                return false;
        }

        player.setGameMode(targetGameMode);
        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%gamemode%", targetGameMode.name());
        plugin.getMessageManager().sendMessage(player, "gamemode.success", localMap);
        plugin.getSoundManager().playSound(player);
        return true;
    }

    private GameMode parseGameMode(String mode) {
        switch (mode.toLowerCase()) {
            case "1":
            case "c":
            case "creative":
                return GameMode.CREATIVE;
            case "0":
            case "s":
            case "survival":
                return GameMode.SURVIVAL;
            case "2":
            case "a":
            case "adventure":
                return GameMode.ADVENTURE;
            case "3":
            case "sp":
            case "spectator":
                return GameMode.SPECTATOR;
            default:
                return null;
        }
    }
}