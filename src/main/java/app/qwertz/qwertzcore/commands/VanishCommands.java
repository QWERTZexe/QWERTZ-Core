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

public class VanishCommands implements CommandExecutor {
    private final QWERTZcore plugin;

    public VanishCommands(QWERTZcore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (label.toLowerCase()) {
            case "vanish":
                return vanish(sender);
            case "unvanish":
                return unVanish(sender);
        }
        return false;
    }

    public boolean vanish(CommandSender sender) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "general.only-player-execute");
            return true;
        }
        if (!plugin.getVanishManager().getVanishedPlayers().contains(((Player) sender).getUniqueId())) {
                if (plugin.getConfigManager().getMsgsOnVanish()) {
                int fakeCount = plugin.getVanishManager().getNonVanishedPlayerCount();
                int newCount = fakeCount - 1;

                HashMap<String, String> localMap = new HashMap<>();
                localMap.put("%name%", sender.getName());
                localMap.put("%fakeCount%", String.valueOf(fakeCount));
                localMap.put("%newCount%", String.valueOf(newCount));
                plugin.getMessageManager().broadcastMessage("vanish.leave-msg", localMap);
                plugin.getSoundManager().broadcastConfigSound();
            }
            plugin.getMessageManager().sendMessage(sender, "vanish.you-got-vanished");
            plugin.getVanishManager().addVanishedPlayer((Player) sender);
            for (Player loop : Bukkit.getOnlinePlayers()) {
                loop.hidePlayer((Player) sender);
            }
        } else {
            plugin.getMessageManager().sendMessage(sender, "vanish.already-vanished");
            plugin.getSoundManager().playSoundToSender(sender);
        }

        return true;
    }

    public boolean unVanish(CommandSender sender) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "general.only-player-execute");
            return true;
        }
        if (plugin.getVanishManager().getVanishedPlayers().contains(((Player) sender).getUniqueId())) {
            if (plugin.getConfigManager().getMsgsOnVanish()) {
                int fakeCount = plugin.getVanishManager().getNonVanishedPlayerCount();
                int newCount = fakeCount + 1;
                HashMap<String, String> localMap = new HashMap<>();
                localMap.put("%name%", sender.getName());
                localMap.put("%fakeCount%", String.valueOf(fakeCount));
                localMap.put("%newCount%", String.valueOf(newCount));
                plugin.getMessageManager().broadcastMessage("vanish.join-msg", localMap);
                plugin.getSoundManager().broadcastConfigSound();
            }
            plugin.getMessageManager().sendMessage(sender, "vanish.you-got-unvanished");
            plugin.getSoundManager().playSoundToSender(sender);
            plugin.getVanishManager().removeVanishedPlayer((Player) sender);
            for (Player loop : Bukkit.getOnlinePlayers()) {
                loop.showPlayer((Player) sender);
            }
        } else {
            plugin.getMessageManager().sendMessage(sender, "vanish.not-vanished");
            plugin.getSoundManager().playSoundToSender(sender);
        }

        return true;
    }
}