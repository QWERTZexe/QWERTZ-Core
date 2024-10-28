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
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class PollCommand implements CommandExecutor {

    private final QWERTZcore plugin;
    private Map<UUID, Integer> votes;
    private List<String> options;
    private String question;
    private boolean pollActive = false;

    public PollCommand(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getColor("colorError") + "This command can only be used by players.");
            return true;
        }

        if (args.length < 2 || args.length == 3) {
            sender.sendMessage(plugin.getConfigManager().getColor("colorError") + "Usage: /poll <duration> <question> [<answer1> <answer2>] [answer3] ...");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        if (pollActive) {
            sender.sendMessage(plugin.getConfigManager().getColor("colorError") + "A poll is already active.");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        int duration;
        try {
            duration = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getConfigManager().getColor("colorError") + "Invalid duration. Please provide a number in seconds.");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        question = args[1].replace("-", " ");
        options = new ArrayList<>();
        for (int i = 2; i < args.length; i++) {
            options.add(args[i].replace("-", " "));
        }

        votes = new HashMap<>();
        pollActive = true;

        displayPoll();

        Bukkit.getScheduler().runTaskLater(plugin, this::endPoll, duration * 20L);

        return true;
    }

    private void displayPoll() {
        Bukkit.broadcastMessage(QWERTZcore.CORE_ICON + plugin.getConfigManager().getColor("colorPrimary") + " §lNew poll: " + plugin.getConfigManager().getColor("colorSecondary") + question);
        Bukkit.broadcastMessage("");
        for (int i = 0; i < options.size(); i++) {
            TextComponent message = new TextComponent(plugin.getConfigManager().getColor("colorSecondary") + (i + 1) + ". " + plugin.getConfigManager().getColor("colorTertiary") + options.get(i));
            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pollvote " + i));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to vote for this option!").create()));
            Bukkit.spigot().broadcast(message);
        }
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§7§oClick to vote!");
        Bukkit.broadcastMessage("");
        plugin.getSoundManager().broadcastConfigSound();
    }

    private void endPoll() {
        pollActive = false;
        plugin.getSoundManager().broadcastConfigSound();
        Bukkit.broadcastMessage(QWERTZcore.CORE_ICON + plugin.getConfigManager().getColor("colorPrimary") + " §lPoll ended! " + plugin.getConfigManager().getColor("colorPrimary") + "Results:");
        Bukkit.broadcastMessage("");

        Map<Integer, Long> voteCount = votes.values().stream()
                .collect(Collectors.groupingBy(i -> i, Collectors.counting()));

        List<Map.Entry<Integer, Long>> sortedResults = voteCount.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toList());

        for (Map.Entry<Integer, Long> entry : sortedResults) {
            int optionIndex = entry.getKey();
            long count = entry.getValue();
            Bukkit.broadcastMessage(plugin.getConfigManager().getColor("colorSecondary") + (optionIndex + 1) + ". " + plugin.getConfigManager().getColor("colorTertiary") + options.get(optionIndex) + ": " + plugin.getConfigManager().getColor("colorPrimary") + count + " votes");
        }
        Bukkit.broadcastMessage("");
    }

    public void vote(Player player, int option) {
        if (!pollActive) {
            player.sendMessage(plugin.getConfigManager().getColor("colorError") + "There is no active poll.");
            plugin.getSoundManager().playSound(player);
            return;
        }

        if (option < 0 || option >= options.size()) {
            player.sendMessage(plugin.getConfigManager().getColor("colorError") + "Invalid option.");
            plugin.getSoundManager().playSound(player);
            return;
        }

        votes.put(player.getUniqueId(), option);
        player.sendMessage(plugin.getConfigManager().getColor("colorSuccess") + "You voted for: " + options.get(option));
        plugin.getSoundManager().playSound(player);
    }
}