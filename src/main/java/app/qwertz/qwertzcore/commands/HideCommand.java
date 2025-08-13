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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class HideCommand implements CommandExecutor {

    private final QWERTZcore plugin;
    private final Map<UUID, String> playerHideStatus = new HashMap<>();
    private final Map<UUID, Integer> playerHideNumber = new HashMap<>();

    public HideCommand(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "general.only-player-execute");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            plugin.getMessageManager().sendInvalidUsage(player, "/hide <number|host|staff|all|off>");
            plugin.getSoundManager().playSound(player);
            return true;
        }

        String arg = args[0].toLowerCase();
        
        // Check if the argument is a number
        try {
            int number = Integer.parseInt(arg);
            if (number < 0) {
                plugin.getMessageManager().sendMessage(player, "hide.invalid-number");
                plugin.getSoundManager().playSound(player);
                return true;
            }
            setHideNumber(player, number);
            return true;
        } catch (NumberFormatException e) {
            // Not a number, treat as mode
        }

        // Handle existing modes
        switch (arg) {
            case "host":
            case "staff":
            case "all":
                setHideMode(player, arg);
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

    private void setHideNumber(Player player, int number) {
        playerHideNumber.put(player.getUniqueId(), number);
        playerHideStatus.remove(player.getUniqueId()); // Clear any existing mode
        updatePlayerVisibility(player);

        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%number%", String.valueOf(number));
        plugin.getMessageManager().sendMessage(player, "hide.number-set", localMap);
        plugin.getSoundManager().playSound(player);
    }

    private void setHideMode(Player player, String mode) {
        playerHideStatus.put(player.getUniqueId(), mode);
        playerHideNumber.remove(player.getUniqueId()); // Clear any existing number
        updatePlayerVisibility(player);

        if (mode == null) {
            plugin.getMessageManager().sendMessage(player, "hide.mode-off");
            plugin.getSoundManager().playSound(player);
        } else {
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%mode%", mode);
            plugin.getMessageManager().sendMessage(player, "hide.mode-on", localMap);
            plugin.getSoundManager().playSound(player);
        }
    }

    public void updatePlayerVisibility(Player player) {
        String mode = playerHideStatus.get(player.getUniqueId());
        Integer number = playerHideNumber.get(player.getUniqueId());

        List<Player> otherPlayers = Bukkit.getOnlinePlayers().stream()
                .filter(p -> !p.equals(player))
                .collect(Collectors.toList());

        if (mode == null && number == null) {
            // Show all players
            for (Player otherPlayer : otherPlayers) {
                player.showPlayer(plugin, otherPlayer);
            }
        } else if (number != null) {
            // Hide all players first
            for (Player otherPlayer : otherPlayers) {
                player.hidePlayer(plugin, otherPlayer);
            }
            
            // Show only the specified number of players
            List<Player> playersToShow = otherPlayers.stream()
                    .limit(number)
                    .collect(Collectors.toList());
            
            for (Player otherPlayer : playersToShow) {
                player.showPlayer(plugin, otherPlayer);
            }
        } else {
            // Handle existing modes
            for (Player otherPlayer : otherPlayers) {
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
        Integer number = playerHideNumber.get(newPlayer.getUniqueId());
        if (mode != null || number != null) {
            updatePlayerVisibility(newPlayer);
        }
    }

    public void updateVisibilityForNewPlayer(Player newPlayer) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String mode = playerHideStatus.get(player.getUniqueId());
            Integer number = playerHideNumber.get(player.getUniqueId());
            
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
            } else if (number != null) {
                // For number-based hiding, we need to recalculate visibility
                // This is a simplified approach - in practice, you might want to maintain a list of visible players
                List<Player> visiblePlayers = Bukkit.getOnlinePlayers().stream()
                        .filter(p -> !p.equals(player))
                        .limit(number)
                        .collect(Collectors.toList());
                
                if (!visiblePlayers.contains(newPlayer)) {
                    player.hidePlayer(plugin, newPlayer);
                }
            }
        }
    }
}