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

package app.qwertz.qwertzcore.listeners;

import app.qwertz.qwertzcore.QWERTZcore;
import app.qwertz.qwertzcore.blocks.QWERTZcoreBlock;
import app.qwertz.qwertzcore.util.BlockManager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class BlockEventListener implements Listener {
    private final QWERTZcore plugin;

    public BlockEventListener(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        BlockManager blockManager = plugin.getBlockManager();
        QWERTZcoreBlock specialBlock = blockManager.getSpecialBlock(block.getType());

        if (specialBlock != null) {
            // Handle special block actions before the event might be cancelled
            specialBlock.onMine(event.getPlayer(), block);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only check if player actually moved to a different block
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        Player player = event.getPlayer();
        Location playerLoc = event.getTo();
        Block blockInside = playerLoc.getBlock();
        // Clone location before modifying to avoid corrupting the event
        Block blockBelow = playerLoc.clone().subtract(0, 1, 0).getBlock();

        checkAndTriggerBlock(player, blockInside);
        checkAndTriggerBlock(player, blockBelow);
    }

    private void checkAndTriggerBlock(Player player, Block block) {
        BlockManager blockManager = plugin.getBlockManager();
        QWERTZcoreBlock specialBlock = blockManager.getSpecialBlock(block.getType());

        if (specialBlock != null) {
            specialBlock.onTouch(player, block);
        }
    }
}