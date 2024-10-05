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

package app.qwertz.qwertzcore.util;

import app.qwertz.qwertzcore.QWERTZcore;
import app.qwertz.qwertzcore.blocks.QWERTZcoreBlock;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

public class BlockEventListener implements Listener {
    private final QWERTZcore plugin;

    public BlockEventListener(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();
        ItemStack item = event.getItemInHand();
        ItemMeta meta = item.getItemMeta();

        if (meta != null && meta.hasDisplayName()) {
            String displayName = ChatColor.stripColor(meta.getDisplayName());
            if (displayName.startsWith(QWERTZcore.CORE_ICON_RAW + " QWERTZ Core ")) {
                String blockType = displayName.substring((QWERTZcore.CORE_ICON_RAW + " QWERTZ Core ").length());
                BlockManager blockManager = plugin.getBlockManager();
                if (blockManager.isValidBlockType(blockType)) {
                    blockManager.setSpecialBlock(block.getLocation(), blockType, block.getType());
                    if ((Boolean) plugin.getConfigManager().get("specialBlockOutput")) {
                      player.sendMessage(QWERTZcore.CORE_ICON + ChatColor.YELLOW + " Placed a " + ChatColor.GOLD + blockType + ChatColor.YELLOW + " at X: " + ChatColor.GOLD + block.getLocation().getBlockX() + ChatColor.YELLOW + " Y: " +  ChatColor.GOLD + block.getLocation().getBlockY() + ChatColor.YELLOW + " Z: " + ChatColor.GOLD + block.getLocation().getBlockZ() + ChatColor.YELLOW + " WORLD: " + ChatColor.GOLD + block.getWorld().getName());
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        BlockManager blockManager = plugin.getBlockManager();
        QWERTZcoreBlock specialBlock = blockManager.getSpecialBlock(block.getLocation());

        if (specialBlock != null) {
            // Handle special block actions before the event might be cancelled
            specialBlock.onMine(event.getPlayer(), block);

            // Schedule a task to run after the event has been processed by all plugins
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!event.isCancelled()) {
                    // The block was successfully broken
                    blockManager.removeSpecialBlock(block.getLocation());
                    if ((Boolean) plugin.getConfigManager().get("specialBlockOutput")) {
                        event.getPlayer().sendMessage(QWERTZcore.CORE_ICON + ChatColor.RED + " Removed special block at X: " + ChatColor.GOLD + block.getLocation().getBlockX() + ChatColor.RED +  " Y: " + ChatColor.GOLD + block.getLocation().getBlockY() + ChatColor.RED + " Z: " + ChatColor.GOLD + block.getLocation().getBlockZ() + ChatColor.RED + " WORLD: " + ChatColor.GOLD + Objects.requireNonNull(block.getLocation().getWorld()).getName());
                    }
                }
            });
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location playerLoc = player.getLocation();
        Block blockInside = playerLoc.getBlock();
        Block blockBelow = playerLoc.subtract(0, 1, 0).getBlock();

        checkAndTriggerBlock(player, blockInside);
        checkAndTriggerBlock(player, blockBelow);
    }

    private void checkAndTriggerBlock(Player player, Block block) {
        BlockManager blockManager = plugin.getBlockManager();
        QWERTZcoreBlock specialBlock = blockManager.getSpecialBlock(block.getLocation());

        if (specialBlock != null) {
            specialBlock.onTouch(player, block);
        }
    }
}