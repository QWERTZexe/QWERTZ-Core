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

public class ReviveTokenCommands implements CommandExecutor {

    private final QWERTZcore plugin;
    private final Map<UUID, Long> pendingReviveRequests = new HashMap<>();

    public ReviveTokenCommands(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (label.toLowerCase()) {
            case "userevive":
                return handleUseRevive(sender);
            case "reviveaccept":
                return handleReviveAccept(sender, args);
            case "revivedeny":
                return handleReviveDeny(sender, args);
            case "addrevive":
                return handleAddRevive(sender, args);
            case "removerevive":
                return handleRemoveRevive(sender, args);
            case "revives":
                return handleRevivals(sender, args);
        }
        return false;
    }

    private boolean handleRevivals(CommandSender sender, String[] args) {
        Player target;
        if (args.length == 0 && sender instanceof Player) {
            target = (Player) sender;
        } else if (args.length == 1) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                plugin.getMessageManager().sendMessage((Player) sender, "general.player-not-found");

                plugin.getSoundManager().playSoundToSender(sender);
                return true;
            }
        } else {
            plugin.getMessageManager().sendInvalidUsage((Player) sender, "/revives [player]");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        int reviveTokens = plugin.getDatabaseManager().getReviveTokens(target.getUniqueId());
        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%name%", target.getName());
        localMap.put("%tokens%", String.valueOf(reviveTokens));
        plugin.getMessageManager().sendMessage((Player) sender, "revivaltokens.showrevives", localMap);
        plugin.getSoundManager().playSoundToSender(sender);
        return true;
    }

    private boolean handleUseRevive(CommandSender sender) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendConsole(sender, "general.only-player-execute");
            return true;
        }

        Player player = (Player) sender;
        if (!plugin.getConfigManager().isReviveTokensEnabled()) {
            plugin.getMessageManager().sendMessage(player, "revivaltokens.tokens-disabled");
            plugin.getSoundManager().playSound(player);
            return true;
        }

        long currentTime = System.currentTimeMillis();
        long cooldownEndTime = plugin.getDatabaseManager().getReviveRequestCooldown(player.getUniqueId());

        if (currentTime < cooldownEndTime) {
            long remainingCooldown = (cooldownEndTime - currentTime) / 1000;
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%time%", String.valueOf(remainingCooldown));
            plugin.getMessageManager().sendMessage(player, "revivaltokens.cooldown", localMap);
            plugin.getSoundManager().playSound(player);
            return true;
        }

        int tokens = plugin.getDatabaseManager().getReviveTokens(player.getUniqueId());
        if (tokens <= 0) {
            plugin.getMessageManager().sendMessage(player, "revivaltokens.no-tokens");
            plugin.getSoundManager().playSound(player);
            return true;
        }
        if (plugin.getEventManager().isPlayerAlive(player)) {
            plugin.getMessageManager().sendMessage(player, "revivaltokens.already-alive");
            plugin.getSoundManager().playSound(player);
            return true;
        }
        pendingReviveRequests.put(player.getUniqueId(), currentTime + 30000); // 20 seconds expiry
        plugin.getDatabaseManager().setReviveRequestCooldown(player.getUniqueId(), currentTime + 30000);

        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%name%", player.getName());
        plugin.getMessageManager().broadcastMessage("revivaltokens.request-revive", localMap);
        plugin.getSoundManager().broadcastConfigSound();

        return true;
    }

    private boolean handleReviveAccept(CommandSender sender, String[] args) {

        if (args.length != 1) {
            plugin.getMessageManager().sendInvalidUsage((Player) sender, "/reviveaccept <player>");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            plugin.getMessageManager().sendMessage((Player) sender, "general.player-not-found");

            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        if (!pendingReviveRequests.containsKey(target.getUniqueId()) ||
                pendingReviveRequests.get(target.getUniqueId()) < System.currentTimeMillis()) {
            plugin.getMessageManager().sendMessage((Player) sender, "revivaltokens.no-pending-request");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        pendingReviveRequests.remove(target.getUniqueId());
        plugin.getDatabaseManager().removeReviveToken(target.getUniqueId());
        plugin.getEventManager().revivePlayer(target, (Player) sender);

        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%name%", target.getName());
        plugin.getMessageManager().broadcastMessage("revivaltokens.revive-accepted", localMap);
        plugin.getSoundManager().broadcastConfigSound();
        return true;
    }

    private boolean handleReviveDeny(CommandSender sender, String[] args) {


        if (args.length != 1) {
            plugin.getMessageManager().sendInvalidUsage((Player) sender, "/revivedeny <player>");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            plugin.getMessageManager().sendMessage((Player) sender, "general.player-not-found");

            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        pendingReviveRequests.remove(target.getUniqueId());
        plugin.getDatabaseManager().setReviveRequestCooldown(target.getUniqueId(), System.currentTimeMillis() + 120000); // 2 minutes cooldown

        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%name%", target.getName());
        plugin.getMessageManager().broadcastMessage("revivaltokens.revive-denied", localMap);
        plugin.getSoundManager().broadcastConfigSound();
        return true;
    }

    private boolean handleAddRevive(CommandSender sender, String[] args) {

        if (args.length != 1) {
            plugin.getMessageManager().sendInvalidUsage((Player) sender, "/addrevive <player>");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            plugin.getMessageManager().sendMessage((Player) sender, "general.player-not-found");

            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        plugin.getDatabaseManager().addReviveToken(target.getUniqueId());
        int tokens = plugin.getDatabaseManager().getReviveTokens(target.getUniqueId());

        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%name%", target.getName());
        localMap.put("%tokens%", String.valueOf(tokens));
        plugin.getMessageManager().broadcastMessage("revivaltokens.token-given", localMap);
        plugin.getSoundManager().broadcastConfigSound();
        return true;
    }

    private boolean handleRemoveRevive(CommandSender sender, String[] args) {

        if (args.length != 1) {
            plugin.getMessageManager().sendInvalidUsage((Player) sender, "/removerevive <player>");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            plugin.getMessageManager().sendMessage((Player) sender, "general.player-not-found");

            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        plugin.getDatabaseManager().removeReviveToken(target.getUniqueId());
        int tokens = plugin.getDatabaseManager().getReviveTokens(target.getUniqueId());

        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%name%", target.getName());
        localMap.put("%tokens%", String.valueOf(tokens));
        plugin.getMessageManager().broadcastMessage("revivaltokens.token-removed", localMap);
        plugin.getSoundManager().broadcastConfigSound();
        return true;
    }
}