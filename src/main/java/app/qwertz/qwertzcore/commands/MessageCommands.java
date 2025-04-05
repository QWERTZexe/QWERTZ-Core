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

public class MessageCommands implements CommandExecutor {

    private final QWERTZcore plugin;

    public MessageCommands(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendConsole(sender, "general.only-player-execute");
            return true;
        }

        Player player = (Player) sender;

        switch (label.toLowerCase()) {
            case "message":
            case "msg":
            case "tell":
                return handleMessageCommand(player, args);
            case "reply":
            case "r":
                return handleReplyCommand(player, args);
            case "messagetoggle":
            case "msgtoggle":
            case "togglemsgs":
                return handleMessageToggleCommand(player);
        }

        return false;
    }

    private boolean handleMessageCommand(Player sender, String[] args) {
        if (args.length < 2) {
            plugin.getMessageManager().sendInvalidUsage(sender, "/message <player> <message>");
            plugin.getSoundManager().playSound(sender);
            return true;
        }

        Player recipient = Bukkit.getPlayer(args[0]);
        if (recipient == null) {
            plugin.getMessageManager().sendMessage(sender, "general.player-not-found");
            plugin.getSoundManager().playSound(sender);
            return true;
        }

        if (!plugin.getMessageManager().canReceiveMessages(recipient)) {
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%name%", recipient.getName());
            plugin.getMessageManager().sendMessage(sender, "messaging.cannot-message", localMap);
            plugin.getSoundManager().playSound(sender);
            return true;
        }

        String message = String.join(" ", args).substring(args[0].length() + 1);
        sendPrivateMessage(sender, recipient, message);
        plugin.getSoundManager().playSound(sender);
        plugin.getSoundManager().playSound(recipient);
        return true;
    }

    private boolean handleReplyCommand(Player sender, String[] args) {
        if (args.length < 1) {
            plugin.getMessageManager().sendInvalidUsage(sender, "/reply <message>");
            return true;
        }

        Player recipient = plugin.getMessageManager().getReplyTarget(sender);
        if (recipient == null) {
            plugin.getMessageManager().sendMessage(sender, "messaging.cannot-reply");
            plugin.getSoundManager().playSound(sender);
            return true;
        }

        if (!plugin.getMessageManager().canReceiveMessages(recipient)) {
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%name%", recipient.getName());
            plugin.getMessageManager().sendMessage(sender, "messaging.cannot-message", localMap);
            return true;
        }

        String message = String.join(" ", args);
        sendPrivateMessage(sender, recipient, message);
        plugin.getSoundManager().playSound(sender);
        plugin.getSoundManager().playSound(recipient);
        return true;
    }

    private boolean handleMessageToggleCommand(Player player) {
        plugin.getMessageManager().toggleMessages(player);
        boolean enabled = plugin.getMessageManager().canReceiveMessages(player);
        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%state%", enabled ? "enabled" : "disabled");
        localMap.put("%stateColor%", enabled ? "%colorAlive%" : "%colorDead%");
        plugin.getMessageManager().sendMessage(player, "messaging.togglemsg", localMap);
        plugin.getSoundManager().playSound(player);
        return true;
    }

    private void sendPrivateMessage(Player sender, Player recipient, String message) {
        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%sender%", sender.getName());
        localMap.put("%recipient%", recipient.getName());
        message = plugin.getChatManager().translateEmojis(message, sender);
        localMap.put("%message%", message);
        plugin.getMessageManager().sendMessage(sender, "messaging.direct-message", localMap);
        plugin.getMessageManager().sendMessage(recipient, "messaging.direct-message", localMap);
        plugin.getSoundManager().playSound(sender);
        plugin.getSoundManager().playSound(recipient);
        plugin.getMessageManager().setReplyTarget(sender, recipient);
    }
}