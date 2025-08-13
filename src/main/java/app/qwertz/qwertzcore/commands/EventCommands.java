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

import app.qwertz.qwertzcore.util.EventManager;
import app.qwertz.qwertzcore.QWERTZcore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class EventCommands implements CommandExecutor {
    private final QWERTZcore plugin;

    public EventCommands(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName().toLowerCase()) {
            case "revive":
                return handleRevive(sender, args);
            case "unrevive":
                return handleUnrevive(sender, args);
            case "reviveall":
                return handleReviveAll(sender);
            case "unreviveall":
                return handleUnReviveAll(sender);
            case "listalive":
                return handleListAlive(sender);
            case "listdead":
                return handleListDead(sender);
            case "givedead":
                return handleGive(sender, args, true);
            case "givealive":
                return handleGive(sender, args, false);
            case "tpalive":
                return handleTeleport(sender, false, true);
            case "tpdead":
                return handleTeleport(sender, true, true);
            case "tpall":
                return handleTeleport(sender, true, false);
            case "tphere":
                return handleTpHere(sender, args);
            case "revivelast":
                return handleReviveLast(sender, args);
            case "healalive":
                return handleHeal(sender, true, args);
            case "healdead":
                return handleHeal(sender, false, args);
            default:
                return false;
        }
    }

    private boolean handleRevive(CommandSender sender, String[] args) {
        if (args.length != 1) {
            plugin.getMessageManager().sendInvalidUsage(sender, "/revive <player>");
            plugin.getSoundManager().playSoundToSender(sender);
            return false;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            plugin.getMessageManager().sendMessage(sender, "general.player-not-found");
            plugin.getSoundManager().playSoundToSender(sender);
            return false;
        }

        if  (!plugin.getEventManager().revivePlayer(target, (Player) sender)) {
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%player%", target.getName());
            plugin.getMessageManager().sendMessage(sender, "event.alreadyalive", localMap);
            plugin.getSoundManager().playSoundToSender(sender);
        }
        return true;
    }

    private boolean handleUnrevive(CommandSender sender, String[] args) {
        if (args.length != 1) {
            plugin.getMessageManager().sendInvalidUsage(sender, "/unrevive <player>");
            plugin.getSoundManager().playSoundToSender(sender);
            return false;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            plugin.getMessageManager().sendMessage(sender, "general.player-not-found");
            plugin.getSoundManager().playSoundToSender(sender);
            return false;
        }

        if (!plugin.getEventManager().unrevivePlayer(target, (Player) sender)) {
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%player%", target.getName());
            plugin.getMessageManager().sendMessage(sender, "event.alreadydead", localMap);
            plugin.getSoundManager().playSoundToSender(sender);
        }
        return true;
    }

    private boolean handleReviveAll(CommandSender sender) {
        plugin.getEventManager().reviveAll((Player) sender);
        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%player%", sender.getName());
        plugin.getMessageManager().broadcastMessage("event.revivedall", localMap);
        plugin.getSoundManager().broadcastConfigSound();
        return true;
    }

    private boolean handleUnReviveAll(CommandSender sender) {
        plugin.getEventManager().unReviveAll();
        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%player%", sender.getName());
        plugin.getMessageManager().broadcastMessage("event.unrevivedall", localMap);
        plugin.getSoundManager().broadcastConfigSound();
        return true;
    }

    private boolean handleListAlive(CommandSender sender) {
        String aliveList = plugin.getEventManager().getAlivePlayers().stream()
                .map(Bukkit::getPlayer)
                .filter(player -> player != null)
                .map(Player::getName)
                .collect(Collectors.joining(", "));

        if (aliveList.isEmpty()) {
            plugin.getMessageManager().sendMessage(sender, "event.noalive");
            plugin.getSoundManager().playSoundToSender(sender);
        } else {
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%list%", aliveList);
            plugin.getMessageManager().sendMessage(sender, "event.listalive", localMap);
            plugin.getSoundManager().playSoundToSender(sender);
        }
        return true;
    }

    private boolean handleListDead(CommandSender sender) {
        String deadList = plugin.getEventManager().getDeadPlayers().stream()
                .map(Bukkit::getPlayer)
                .filter(player -> player != null)
                .map(Player::getName)
                .collect(Collectors.joining(", "));

        if (deadList.isEmpty()) {
            plugin.getMessageManager().sendMessage(sender, "event.nodead");
            plugin.getSoundManager().playSoundToSender(sender);
        } else {
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%list%", deadList);
            plugin.getMessageManager().sendMessage(sender, "event.listdead", localMap);
            plugin.getSoundManager().playSoundToSender(sender);
        }
        return true;
    }

    private boolean handleGive(CommandSender sender, String[] args, boolean isDead) {
        if (args.length < 1 || args.length > 3) {
            plugin.getMessageManager().sendInvalidUsage(sender, "/" + (isDead ? "givedead" : "givealive") + " <item> [amount] [data]");
            plugin.getSoundManager().playSoundToSender(sender);
            return false;
        }

        Material material = Material.matchMaterial(args[0]);
        if (material == null) {
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%item%", args[0]);
            plugin.getMessageManager().sendMessage(sender, "event.invalid-item", localMap);
            plugin.getSoundManager().playSoundToSender(sender);
            return false;
        }

        int amount = 1;
        if (args.length >= 2) {
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                HashMap<String, String> localMap = new HashMap<>();
                localMap.put("%amount%", args[1]);
                plugin.getMessageManager().sendMessage(sender, "event.invalid-amount", localMap);
                plugin.getSoundManager().playSoundToSender(sender);
                return false;
            }
        }

        // Data will be removed from command soon
        @Deprecated(since = "2.0", forRemoval = true)
        short data = 0;
        if (args.length == 3) {
            try {
                data = Short.parseShort(args[2]);
            } catch (NumberFormatException e) {
                // dont use new system here, will be removed soon anyways
                sender.sendMessage("§cInvalid data value: " + args[2]);
                plugin.getSoundManager().playSoundToSender(sender);
                return false;
            }
        }

        ItemStack itemStack = new ItemStack(material, amount, data);
        int playersAffected = 0;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if ((isDead && plugin.getEventManager().isPlayerDead(player)) || (!isDead && plugin.getEventManager().isPlayerAlive(player))) {
                player.getInventory().addItem(itemStack.clone());
                playersAffected++;
            }
        }

        String playerType = isDead ? "dead" : "alive";
        String playerTypeColor = isDead ? "%colorDead%" : "%colorAlive%";
        String itemName = itemStack.getType().toString().toLowerCase().replace("_", " ");

        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%affected%", String.valueOf(playersAffected));
        localMap.put("%type%", playerType);
        localMap.put("%typeColor%", playerTypeColor);
        localMap.put("%amount%", String.valueOf(amount));
        localMap.put("%item%", itemName);
        // Broadcast the message to all players
        plugin.getMessageManager().broadcastMessage("event.give-broadcast", localMap);
        plugin.getSoundManager().broadcastConfigSound();
        return true;
    }

    private boolean handleTeleport(CommandSender sender, boolean isDead, boolean filter) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "general.only-player-execute");
            return true;
        }

        Player executor = (Player) sender;
        int teleportedCount = 0;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player != executor) {
                if (filter) {
                    if ((isDead && plugin.getEventManager().isPlayerDead(player)) || (!isDead && plugin.getEventManager().isPlayerAlive(player))) {
                        player.teleport(executor.getLocation());
                        teleportedCount++;
                    }
                } else {
                    player.teleport(executor.getLocation());
                    teleportedCount++;
                }
            }
        }

        String playerType = isDead ? "dead" : "alive";
        String playerTypeColor = isDead ? "%colorDead%" : "%colorAlive%";
        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%name%", executor.getName());
        if (filter) {
            localMap.put("%typeColor%", playerTypeColor);
            localMap.put("%type%", playerType);
            plugin.getMessageManager().broadcastMessage("event.tp-group", localMap);
        } else {
            plugin.getMessageManager().broadcastMessage("event.tpall", localMap);
        }
        plugin.getSoundManager().broadcastConfigSound();

        return true;
    }
    private boolean handleTpHere(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "general.only-player-execute");
            return true;
        }

        if (args.length != 1) {
            plugin.getMessageManager().sendInvalidUsage(sender, "/tphere <player>");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        Player commandSender = (Player) sender;
        Player targetPlayer = Bukkit.getPlayer(args[0]);

        if (targetPlayer == null) {
            plugin.getMessageManager().sendMessage(sender, "general.player-not-found");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        if (targetPlayer == commandSender) {
            plugin.getMessageManager().sendMessage(sender, "event.canttptoyourself");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        targetPlayer.teleport(commandSender.getLocation());
        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%name%", targetPlayer.getName());
        HashMap<String, String> localMap2 = new HashMap<>();
        localMap2.put("%name%", commandSender.getName());
        plugin.getMessageManager().sendMessage(commandSender, "event.tphere-sender-msg", localMap);
        plugin.getSoundManager().playSound(commandSender);
        plugin.getMessageManager().sendMessage(targetPlayer, "event.tphere-target-msg", localMap2);
        plugin.getSoundManager().playSound(targetPlayer);

        return true;
    }
    private boolean handleReviveLast(CommandSender sender, String[] args) {
        int seconds = 30; // Default to 30 seconds if no argument is provided

        if (args.length > 0) {
            String timeArg = args[0].toLowerCase();
            seconds = parseTimeArgument(timeArg);
            
            if (seconds == -1) {
                plugin.getMessageManager().sendMessage(sender, "event.revivelast.invalid-format");
                plugin.getSoundManager().playSoundToSender(sender);
                return true;
            }
            
            if (seconds <= 0 || seconds > 300) {
                plugin.getMessageManager().sendMessage(sender, "event.revivelast.no-number");
                plugin.getSoundManager().playSoundToSender(sender);
                return true;
            }
        }

        List<Player> recentlyDeadPlayers = plugin.getEventManager().getRecentlyDeadPlayers(seconds);
        int revivedCount = 0;

        for (Player player : recentlyDeadPlayers) {
            plugin.getEventManager().revivePlayer(player, (Player) sender);
            revivedCount++;
        }

        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%amount%", String.valueOf(revivedCount));
        localMap.put("%seconds%", String.valueOf(seconds));

        plugin.getMessageManager().broadcastMessage("event.revivelast.broadcast", localMap);
        plugin.getSoundManager().broadcastConfigSound();
        return true;
    }

    /**
     * Parses time arguments like "1min", "1s", "2m", "30sec", etc.
     * @param timeArg The time argument string
     * @return The number of seconds, or -1 if invalid format
     */
    private int parseTimeArgument(String timeArg) {
        // Remove any whitespace
        timeArg = timeArg.trim();
        
        // Check if it's just a number (default to minutes)
        if (timeArg.matches("^\\d+$")) {
            int minutes = Integer.parseInt(timeArg);
            if (minutes < 1 || minutes > 5) {
                return -1; // Invalid range for minutes (1-5)
            }
            return minutes * 60; // Convert to seconds
        }
        
        // Check for minutes format: "1min", "2m", etc.
        if (timeArg.matches("^\\d+(min|m)$")) {
            int minutes = Integer.parseInt(timeArg.replaceAll("(min|m)", ""));
            if (minutes < 1 || minutes > 5) {
                return -1; // Invalid range for minutes (1-5)
            }
            return minutes * 60; // Convert to seconds
        }
        
        // Check for seconds format: "1s", "30sec", etc.
        if (timeArg.matches("^\\d+(s|sec)$")) {
            int secs = Integer.parseInt(timeArg.replaceAll("(s|sec)", ""));
            if (secs < 1 || secs > 300) {
                return -1; // Invalid range for seconds (1-300)
            }
            return secs;
        }
        
        // If none of the patterns match, return -1 (invalid format)
        return -1;
    }
    public boolean handleHeal(CommandSender sender, Boolean alive, String[] args) {
        boolean healAlive = alive;
        int healedCount = 0;

        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean isDead = plugin.getEventManager().isPlayerDead(player);
            if ((healAlive && !isDead) || (!healAlive && isDead)) {
                healPlayer(player);
                healedCount++;
            }
        }

        String playerType = healAlive ? "alive" : "dead";
        String playerTypeColor = healAlive ? "%colorAlive%" : "%colorDead%";
        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%amount%", String.valueOf(healedCount));
        localMap.put("%type%", playerType);
        localMap.put("%typeColor%", playerTypeColor);
        localMap.put("%name%", sender.getName());

        plugin.getMessageManager().broadcastMessage("event.heal-broadcast", localMap);
        plugin.getSoundManager().broadcastConfigSound();

        return true;
    }
    private void healPlayer(Player player) {
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setHealth(maxHealth);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setFireTicks(0);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
    }
}