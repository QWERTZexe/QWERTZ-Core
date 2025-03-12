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
            plugin.getMessageManager().sendConsole(sender, "general.only-player-execute");
            return true;
        }

        if (args.length < 2 || args.length == 3) {
            plugin.getMessageManager().sendInvalidUsage((Player) sender, " /poll <duration> <question> [<answer1> <answer2>] [answer3] ...");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        if (pollActive) {
            plugin.getMessageManager().sendMessage((Player) sender, "poll.already-active");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        int duration;
        try {
            duration = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            plugin.getMessageManager().sendMessage((Player) sender, "poll.invalid-duration");
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
        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%question%", question);
        Bukkit.broadcastMessage(plugin.getMessageManager().prepareMessage(plugin.getMessageManager().getMessage("poll.new-poll"), localMap));
        Bukkit.broadcastMessage("");
        for (int i = 0; i < options.size(); i++) {
            HashMap<String, String> tempMap = new HashMap<>();
            tempMap.put("%index%", String.valueOf(i + 1));
            tempMap.put("%option%", options.get(i));
            TextComponent message = new TextComponent(plugin.getMessageManager().prepareMessage(plugin.getMessageManager().getMessage("poll.option"), tempMap));
            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pollvote " + i));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(plugin.getMessageManager().prepareMessage(plugin.getMessageManager().getMessage("poll.hover"), new HashMap<>())).create()));
            Bukkit.spigot().broadcast(message);
        }
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(plugin.getMessageManager().prepareMessage(plugin.getMessageManager().getMessage("poll.click-to-vote"), new HashMap<>()));
        Bukkit.broadcastMessage("");
        plugin.getSoundManager().broadcastConfigSound();
    }

    private void endPoll() {
        pollActive = false;
        plugin.getSoundManager().broadcastConfigSound();
        Bukkit.broadcastMessage(plugin.getMessageManager().prepareMessage(plugin.getMessageManager().getMessage("poll.result-title"), new HashMap<>()));
        Bukkit.broadcastMessage("");

        Map<Integer, Long> voteCount = votes.values().stream()
                .collect(Collectors.groupingBy(i -> i, Collectors.counting()));

        List<Map.Entry<Integer, Long>> sortedResults = voteCount.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toList());

        for (Map.Entry<Integer, Long> entry : sortedResults) {
            int optionIndex = entry.getKey();
            long count = entry.getValue();
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%index%", String.valueOf(optionIndex + 1));
            localMap.put("%option%", options.get(optionIndex));
            localMap.put("%amount%", String.valueOf(count));
            Bukkit.broadcastMessage(plugin.getMessageManager().prepareMessage(plugin.getMessageManager().getMessage("poll.result-option"), localMap));
        }
        Bukkit.broadcastMessage("");
    }

    public void vote(Player player, int option) {
        if (!pollActive) {
            plugin.getMessageManager().sendMessage(player, "poll.no-active-poll");
            plugin.getSoundManager().playSound(player);
            return;
        }

        if (option < 0 || option >= options.size()) {
            plugin.getMessageManager().sendMessage(player, "poll.invalid-option");
            plugin.getSoundManager().playSound(player);
            return;
        }

        votes.put(player.getUniqueId(), option);
        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%option%", options.get(option));
        plugin.getMessageManager().sendMessage(player, "poll.vote-success", localMap);
        plugin.getSoundManager().playSound(player);
    }
}