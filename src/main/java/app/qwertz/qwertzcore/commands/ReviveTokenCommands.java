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
import org.bukkit.ChatColor;
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
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Usage: /revives [player]");
            return true;
        }

        int reviveTokens = plugin.getDatabaseManager().getReviveTokens(target.getUniqueId());
        sender.sendMessage(QWERTZcore.CORE_ICON + ChatColor.YELLOW + " " + target.getName() + ChatColor.GREEN + " has " + reviveTokens + " revival tokens.");

        return true;
    }

    private boolean handleUseRevive(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        if (!plugin.getConfigManager().isReviveTokensEnabled()) {
            player.sendMessage(ChatColor.RED + "Revive tokens are currently disabled!");
            return true;
        }

        long currentTime = System.currentTimeMillis();
        long cooldownEndTime = plugin.getDatabaseManager().getReviveRequestCooldown(player.getUniqueId());

        if (currentTime < cooldownEndTime) {
            long remainingCooldown = (cooldownEndTime - currentTime) / 1000;
            player.sendMessage(ChatColor.RED + "You must wait " + remainingCooldown + " seconds before requesting a revive!");
            return true;
        }

        int tokens = plugin.getDatabaseManager().getReviveTokens(player.getUniqueId());
        if (tokens <= 0) {
            player.sendMessage(ChatColor.RED + "You don't have any revive tokens!");
            return true;
        }
        if (plugin.getEventManager().isPlayerAlive(player)) {
            player.sendMessage(ChatColor.RED + "You are already alive!");
            return true;
        }
        pendingReviveRequests.put(player.getUniqueId(), currentTime + 30000); // 20 seconds expiry
        plugin.getDatabaseManager().setReviveRequestCooldown(player.getUniqueId(), currentTime + 30000);

        String message = String.format("%s %s%s %sis requesting to use a revive token! (%s/reviveaccept %s%s%s or %s/revivedeny %s%s%s)",
                QWERTZcore.CORE_ICON, ChatColor.YELLOW, player.getName(), ChatColor.GREEN,
                ChatColor.GREEN, ChatColor.YELLOW, player.getName(), ChatColor.GREEN,
                ChatColor.RED, ChatColor.YELLOW, player.getName(), ChatColor.GREEN);
        Bukkit.broadcastMessage(message);

        return true;
    }

    private boolean handleReviveAccept(CommandSender sender, String[] args) {

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /reviveaccept <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        if (!pendingReviveRequests.containsKey(target.getUniqueId()) ||
                pendingReviveRequests.get(target.getUniqueId()) < System.currentTimeMillis()) {
            sender.sendMessage(ChatColor.RED + "There is no pending revive request for this player!");
            return true;
        }

        pendingReviveRequests.remove(target.getUniqueId());
        plugin.getDatabaseManager().removeReviveToken(target.getUniqueId());
        plugin.getEventManager().revivePlayer(target, (Player) sender);

        String message = String.format("%s %s%s's %srevive request has been accepted!",
                QWERTZcore.CORE_ICON, ChatColor.YELLOW, target.getName(), ChatColor.GREEN);
        Bukkit.broadcastMessage(message);

        return true;
    }

    private boolean handleReviveDeny(CommandSender sender, String[] args) {


        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /revivedeny <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        pendingReviveRequests.remove(target.getUniqueId());
        plugin.getDatabaseManager().setReviveRequestCooldown(target.getUniqueId(), System.currentTimeMillis() + 120000); // 2 minutes cooldown

        String message = String.format("%s %s%s's %srevive request has been denied!",
                QWERTZcore.CORE_ICON, ChatColor.YELLOW, target.getName(), ChatColor.RED);
        Bukkit.broadcastMessage(message);

        return true;
    }

    private boolean handleAddRevive(CommandSender sender, String[] args) {

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /addrevive <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        plugin.getDatabaseManager().addReviveToken(target.getUniqueId());
        int tokens = plugin.getDatabaseManager().getReviveTokens(target.getUniqueId());

        String message = String.format("%s %s%s %shas been given a revive token! They now have %s%d %stokens.",
                QWERTZcore.CORE_ICON, ChatColor.YELLOW, target.getName(), ChatColor.GREEN, ChatColor.YELLOW, tokens, ChatColor.GREEN);
        Bukkit.broadcastMessage(message);

        return true;
    }

    private boolean handleRemoveRevive(CommandSender sender, String[] args) {

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /removerevive <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        plugin.getDatabaseManager().removeReviveToken(target.getUniqueId());
        int tokens = plugin.getDatabaseManager().getReviveTokens(target.getUniqueId());

        String message = String.format("%s %sA revive token has been removed from %s%s%s! They now have %s%d %stokens.",
                QWERTZcore.CORE_ICON, ChatColor.RED, ChatColor.YELLOW, target.getName(), ChatColor.RED, ChatColor.YELLOW, tokens, ChatColor.RED);
        Bukkit.broadcastMessage(message);

        return true;
    }
}