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
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class AdvertisementCommand implements CommandExecutor {

    private final QWERTZcore plugin;
    private final Map<String, ChatColor> platformColors;

    public AdvertisementCommand(QWERTZcore plugin) {
        this.plugin = plugin;
        this.platformColors = new HashMap<>();
        initializePlatformColors();
    }

    private void initializePlatformColors() {
        platformColors.put("twitch", ChatColor.DARK_PURPLE);
        platformColors.put("tiktok", ChatColor.LIGHT_PURPLE);
        platformColors.put("youtube", ChatColor.RED);
        platformColors.put("discord", ChatColor.BLUE);
        platformColors.put("store", ChatColor.GREEN);
        platformColors.put("website", ChatColor.YELLOW);
        platformColors.put("other", ChatColor.GRAY);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("ad")) {
            return handleAdCommand(sender, args);
        } else if (label.equalsIgnoreCase("setad")) {
            return handleSetAdCommand(sender, args);
        }
        return false;
    }

    private boolean handleAdCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("qwertzcore.host.ad")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /ad <platform>");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        String platform = args[0].toLowerCase();
        if (!platformColors.containsKey(platform)) {
            sender.sendMessage(ChatColor.RED + "Invalid platform. Available platforms: twitch, tiktok, youtube, discord, store, website, other");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        // Get the advertisement message
        String adMessage = (String) plugin.getConfigManager().get(platform);
        if (adMessage == null || adMessage.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No advertisement set for this platform.");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        // Split the message into text and link parts
        String[] parts = adMessage.split(" ");
        StringBuilder messageBuilder = new StringBuilder();

        // Assume the last part is the URL
        String link = parts[parts.length - 1];

        // Check if the last part is a valid URL
        if (!link.startsWith("http://") && !link.startsWith("https://")) {
            sender.sendMessage(ChatColor.RED + "The last part of your advertisement must be a valid URL.");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        // Build the main message without the link
        for (int i = 0; i < parts.length - 1; i++) {
            messageBuilder.append(parts[i]).append(" ");
        }

        // Create the formatted advertisement message with clickable link
        TextComponent advertisement = new TextComponent(QWERTZcore.CORE_ICON + " ");

        // Add the text part
        advertisement.addExtra(new TextComponent(messageBuilder.toString().trim() + " "));

        // Add the clickable link part
        TextComponent clickableLink = new TextComponent(link);
        clickableLink.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));

        // Color the link if desired
        clickableLink.setColor(ChatColor.AQUA);

        advertisement.addExtra(clickableLink);

        // Broadcast the advertisement with colored separators
        ChatColor platformColor = platformColors.get(platform);
        String separator = platformColor + "-----------------------------------------------------";

        Bukkit.broadcastMessage(separator);

        Bukkit.spigot().broadcast(advertisement);

        Bukkit.broadcastMessage(separator);

        plugin.getSoundManager().broadcastConfigSound();

        return true;
    }

    private boolean handleSetAdCommand(CommandSender sender, String[] args) {

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /setad <platform> <message>");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        String platform = args[0].toLowerCase();

        // Validate platform
        if (!platformColors.containsKey(platform)) {
            sender.sendMessage(ChatColor.RED + "Invalid platform. Available platforms: twitch, tiktok, youtube, discord, store, website, other");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        // Join all remaining arguments as the message
        String message = String.join(" ", args).substring(platform.length() + 1);
        plugin.getConfigManager().set(platform, message);

        sender.sendMessage(ChatColor.GREEN + "Advertisement for " + platform + " has been set.");
        plugin.getSoundManager().playSoundToSender(sender);
        return true;
    }
}