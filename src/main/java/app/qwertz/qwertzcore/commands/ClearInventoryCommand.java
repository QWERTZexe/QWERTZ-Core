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

public class ClearInventoryCommand implements CommandExecutor {

    private final QWERTZcore plugin;

    public ClearInventoryCommand(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean clearAlive = label.equalsIgnoreCase("clearalive");
        int clearedCount = 0;

        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean isDead = plugin.getEventManager().isPlayerDead(player);
            if ((clearAlive && !isDead) || (!clearAlive && isDead)) {
                player.getInventory().clear();
                player.sendMessage(ChatColor.RED + "Your inventory has been cleared by an admin.");
                clearedCount++;
            }
        }

        String playerType = clearAlive ? "alive" : "dead";
        ChatColor playerTypeColor = clearAlive ? ChatColor.GREEN : ChatColor.RED;

        String message = String.format("%s %s%d %s%s %splayers have had their inventories cleared",
                QWERTZcore.CORE_ICON,
                ChatColor.GREEN,
                clearedCount,
                playerTypeColor,
                playerType,
                ChatColor.GREEN);

        Bukkit.broadcastMessage(message);

        return true;
    }
}