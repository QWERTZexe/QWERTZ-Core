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
import org.bukkit.entity.Player;

import java.util.*;

public class FreezeCommand implements CommandExecutor {
    private final QWERTZcore plugin;

    public FreezeCommand(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "general.only-player-execute");
            return true;
        }

        Player executor = (Player) sender;
        boolean isUnfreeze = label.equalsIgnoreCase("unfreeze");

        if (args.length != 1) {
            String usage = isUnfreeze ? "/unfreeze <alive|dead|all|player>" : "/freeze <alive|dead|all|player>";
            plugin.getMessageManager().sendInvalidUsage(executor, usage);
            plugin.getSoundManager().playSound(executor);
            return true;
        }

        String target = args[0];
        List<Player> targetPlayers = new ArrayList<>();

        switch (target.toLowerCase()) {
            case "alive":
                for (UUID uuid : plugin.getEventManager().getAlivePlayers()) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        targetPlayers.add(player);
                    }
                }
                break;
            case "dead":
                for (UUID uuid : plugin.getEventManager().getDeadPlayers()) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        targetPlayers.add(player);
                    }
                }
                break;
            case "all":
                targetPlayers.addAll(Bukkit.getOnlinePlayers());
                break;
            default:
                Player targetPlayer = Bukkit.getPlayer(target);
                if (targetPlayer != null) {
                    targetPlayers.add(targetPlayer);
                } else {
                    plugin.getMessageManager().sendMessage(sender, "general.player-not-found");
                    plugin.getSoundManager().playSoundToSender(sender);
                    return true;
                }
        }

        if (isUnfreeze) {
            for (Player targetPlayer : targetPlayers) {
                if (plugin.getEventManager().isPlayerFrozen(targetPlayer)) {
                    plugin.getEventManager().unfreezePlayer(targetPlayer);
                }
            }
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%amount%", String.valueOf(targetPlayers.size()));
            plugin.getMessageManager().sendMessage(executor, "freeze.unfrozen", localMap);
        } else {
            for (Player targetPlayer : targetPlayers) {
                if (!plugin.getEventManager().isPlayerFrozen(targetPlayer)) {
                    plugin.getEventManager().freezePlayer(targetPlayer);
                }
            }
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%amount%", String.valueOf(targetPlayers.size()));
            plugin.getMessageManager().sendMessage(executor, "freeze.frozen", localMap);
        }

        plugin.getSoundManager().playSound(executor);
        return true;
    }
}

