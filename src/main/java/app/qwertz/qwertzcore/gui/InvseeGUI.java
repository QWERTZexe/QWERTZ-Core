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

package app.qwertz.qwertzcore.gui;

import app.qwertz.qwertzcore.QWERTZcore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class InvseeGUI implements Listener {
    private final QWERTZcore plugin;
    private final Player viewer;
    private final Player target;
    private final Inventory gui;
    private BukkitTask updateTask;

    public InvseeGUI(QWERTZcore plugin, Player viewer, Player target) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.target = target;
        this.gui = Bukkit.createInventory(null, 54, QWERTZcore.CORE_ICON + " " + ChatColor.DARK_PURPLE + target.getName() + "'s Inventory");

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        updateGUI();
        viewer.openInventory(gui);
        startUpdateTask();
    }

    private void updateGUI() {
        // Set black glass panes
        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 9; i < 18; i++) {
            gui.setItem(i, blackPane);
        }
        for (int i = 1; i < 5; i++) {
            gui.setItem(i, blackPane);
        }
        // Set inventory contents
        for (int i = 0; i < 36; i++) {
            gui.setItem(i + 18, target.getInventory().getItem(i+9));
        }

        // Set hotbar
        for (int i = 0; i < 9; i++) {
            gui.setItem(i + 45, target.getInventory().getItem(i));
        }

        // Set offhand
        gui.setItem(0, target.getInventory().getItemInOffHand());

        // Set armor
        ItemStack[] armor = target.getInventory().getArmorContents();
        for (int i = 0; i < 4; i++) {
            gui.setItem(i + 5, armor[i]);
        }
    }

    private void startUpdateTask() {
        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateGUI, 1L, 1L);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory() != gui) return;

        if (event.getRawSlot() >= 0 && event.getRawSlot() < 54) {
            if ((event.getRawSlot() >= 9 && event.getRawSlot() < 18) || (event.getRawSlot() >= 1 && event.getRawSlot() < 5)) {
                event.setCancelled(true);
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (event.getRawSlot() >= 18 && event.getRawSlot() < 54) {
                    int targetSlot = event.getRawSlot() - 18;
                    int column = targetSlot % 9;
                    if (targetSlot < 9) {
                        target.getInventory().setItem(9 + column, event.getCurrentItem());
                    }
                    else if (targetSlot >= 9 && targetSlot < 18) {
                        target.getInventory().setItem(18 + column, event.getCurrentItem());
                        }
                    else if (targetSlot >= 18 && targetSlot < 27) {
                        target.getInventory().setItem(27 + column, event.getCurrentItem());
                    }
                    else if (targetSlot >= 27 && targetSlot < 36) {
                        target.getInventory().setItem(column, event.getCurrentItem());
                    }
                } else if (event.getRawSlot() >= 5 && event.getRawSlot() < 9) {
                    int armorSlot = event.getRawSlot() - 5;
                    target.getInventory().setArmorContents(new ItemStack[]{
                            gui.getItem(5), gui.getItem(6), gui.getItem(7), gui.getItem(8)
                    });
                } else if (event.getRawSlot() == 0) {
                    target.getInventory().setItemInOffHand(event.getCurrentItem());
                }
                target.updateInventory();
            });
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory() != gui) return;

        for (int slot : event.getRawSlots()) {
            if ((slot >= 9 && slot < 18) || (slot >= 1 && slot < 5)) {
                event.setCancelled(true);
                return;
            }
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            for (int slot : event.getRawSlots()) {
                if (slot >= 18 && slot < 54) {
                    int targetSlot = slot - 18;
                    int column = targetSlot % 9;
                    if (targetSlot < 9) {
                        target.getInventory().setItem(9 + column, event.getNewItems().get(slot));
                    }
                    else if (targetSlot >= 9 && targetSlot < 18) {
                        target.getInventory().setItem(18 + column, event.getNewItems().get(slot));
                    }
                    else if (targetSlot >= 18 && targetSlot < 27) {
                        target.getInventory().setItem(27 + column, event.getNewItems().get(slot));
                    }
                    else if (targetSlot >= 27 && targetSlot < 36) {
                        target.getInventory().setItem(column, event.getNewItems().get(slot));
                    }
                } else if (slot >= 5 && slot < 9) {
                    target.getInventory().setArmorContents(new ItemStack[]{
                            gui.getItem(5), gui.getItem(6), gui.getItem(7), gui.getItem(8)
                    });
                } else if (slot == 0) {
                    target.getInventory().setItemInOffHand(event.getNewItems().get(slot));
                }
            }
            target.updateInventory();
        });
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory() != gui) return;

        updateTask.cancel();
        InventoryCloseEvent.getHandlerList().unregister(this);
        InventoryClickEvent.getHandlerList().unregister(this);
        InventoryDragEvent.getHandlerList().unregister(this);
        PlayerDropItemEvent.getHandlerList().unregister(this);
        PlayerPickupItemEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.getPlayer() == target) {
            Bukkit.getScheduler().runTask(plugin, this::updateGUI);
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (event.getPlayer() == target) {
            Bukkit.getScheduler().runTask(plugin, this::updateGUI);
        }
    }
}