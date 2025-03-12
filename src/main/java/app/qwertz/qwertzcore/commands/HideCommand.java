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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HideCommand implements CommandExecutor {

    private final QWERTZcore plugin;
    private final Map<UUID, String> playerHideStatus = new HashMap<>();

    public HideCommand(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendConsole(sender, "general.only-player-execute");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        Player player = (Player) sender;


        if (args.length != 1) {
            plugin.getMessageManager().sendInvalidUsage(player, "/hide <host|staff|all|off>");
            plugin.getSoundManager().playSound(player);
            return true;
        }

        String mode = args[0].toLowerCase();
        switch (mode) {
            case "host":
            case "staff":
            case "all":
                setHideMode(player, mode);
                break;
            case "off":
                setHideMode(player, null);
                break;
            default:
                plugin.getMessageManager().sendMessage(player, "hide.invalid");
                plugin.getSoundManager().playSound(player);
                return true;
        }

        return true;
    }

    private void setHideMode(Player player, String mode) {
        playerHideStatus.put(player.getUniqueId(), mode);
        updatePlayerVisibility(player);

        if (mode == null) {
            plugin.getMessageManager().sendMessage(player, "hide.off");
            plugin.getSoundManager().playSound(player);
        } else {
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%mode%", mode);
            plugin.getMessageManager().sendMessage(player, "hide.on", localMap);
            plugin.getSoundManager().playSound(player);
        }
    }

    public void updatePlayerVisibility(Player player) {
        String mode = playerHideStatus.get(player.getUniqueId());

        for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
            if (player.equals(otherPlayer)) continue;

            if (mode == null) {
                player.showPlayer(plugin, otherPlayer);
            } else {
                switch (mode) {
                    case "host":
                        if (otherPlayer.hasPermission("qwertzcore.host")) {
                            player.showPlayer(plugin, otherPlayer);
                        } else {
                            player.hidePlayer(plugin, otherPlayer);
                        }
                        break;
                    case "staff":
                        if (otherPlayer.hasPermission("qwertzcore.staff")) {
                            player.showPlayer(plugin, otherPlayer);
                        } else {
                            player.hidePlayer(plugin, otherPlayer);
                        }
                        break;
                    case "all":
                        player.hidePlayer(plugin, otherPlayer);
                        break;
                }
            }
        }
    }

    public void handlePlayerJoin(Player newPlayer) {
        // Update visibility for the new player
        updateVisibilityForNewPlayer(newPlayer);

        // Update the new player's view of others
        String mode = playerHideStatus.get(newPlayer.getUniqueId());
        if (mode != null) {
            updatePlayerVisibility(newPlayer);
        }
    }

    public void updateVisibilityForNewPlayer(Player newPlayer) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String mode = playerHideStatus.get(player.getUniqueId());
            if (mode != null) {
                switch (mode) {
                    case "host":
                        if (!newPlayer.hasPermission("qwertzcore.host")) {
                            player.hidePlayer(plugin, newPlayer);
                        }
                        break;
                    case "staff":
                        if (!newPlayer.hasPermission("qwertzcore.staff") && !newPlayer.hasPermission("qwertzcore.host")) {
                            player.hidePlayer(plugin, newPlayer);
                        }
                        break;
                    case "all":
                        player.hidePlayer(plugin, newPlayer);
                        break;
                }
            }
        }
    }
}