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
import org.bukkit.inventory.Inventory;

import java.util.HashMap;

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
                if (player.hasPermission("qwertzcore.staff.bypassclear") && !clearAlive) {
                } else {
                clearPlayerInventory(player);
                plugin.getMessageManager().sendMessage(player, "clearinv.got-cleared");
                plugin.getSoundManager().playSound(player);
                clearedCount++;
                }
            }
        }

        String playerType = clearAlive ? "alive" : "dead";
        String playerTypeColor = clearAlive ? "%colorAlive%" : "%colorDead%";
        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%amount%", String.valueOf(clearedCount));
        localMap.put("%group%", playerType);
        localMap.put("%groupColor%", playerTypeColor);
        plugin.getMessageManager().broadcastMessage("clearinv.broadcast", localMap);
        plugin.getSoundManager().broadcastConfigSound();

        return true;
    }
    private void clearPlayerInventory(Player player) {
        // Clear main inventory
        Inventory inventory = player.getInventory();
        inventory.clear();

        // Clear cursor item (item being held by the mouse)
        player.setItemOnCursor(null);

        // Clear crafting grid (if applicable)
        player.getOpenInventory();
        Inventory topInventory = player.getOpenInventory().getTopInventory();
        topInventory.clear();
    }
}