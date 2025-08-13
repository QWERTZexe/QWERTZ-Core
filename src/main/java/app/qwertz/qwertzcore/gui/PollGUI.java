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
import app.qwertz.qwertzcore.commands.PollCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class PollGUI implements Listener {

    private static final Map<UUID, PendingInput> pendingInputs = new HashMap<>();
    private final QWERTZcore plugin;
    private final Player player;
    private final PollCommand pollCommand;
    private Inventory inventory;

    public PollGUI(QWERTZcore plugin, Player player, PollCommand pollCommand) {
        this.plugin = plugin;
        this.player = player;
        this.pollCommand = pollCommand;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.inventory = buildGUI();
    }

    public void open() {
        player.openInventory(inventory);
    }

    private Inventory buildGUI() {
        Inventory gui = Bukkit.createInventory(null, 54, QWERTZcore.CORE_ICON + " " + ChatColor.DARK_PURPLE + "Poll Creator");

        // Fill with black glass panes
        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < 54; i++) {
            gui.setItem(i, blackPane);
        }

        // Get current state from PollCommand
        String question = pollCommand.getPlayerQuestion(player.getUniqueId());
        int duration = pollCommand.getPlayerDuration(player.getUniqueId());
        List<String> options = pollCommand.getPlayerOptions(player.getUniqueId());

        // Question section
        gui.setItem(13, createItem(Material.PAPER, "§eQuestion",
            "§7Current: " + (question.isEmpty() ? "§cNot set" : "§a" + question),
            "§7Click to set the poll question"));
        
        // Duration section
        gui.setItem(12, createItem(Material.CLOCK, "§eDuration", 
            "§7Current: §a" + duration + " seconds",
            "§7Click to change duration"));
        
        // Options section
        gui.setItem(14, createItem(Material.BOOK, "§eOptions", 
            "§7Current: §a" + options.size() + " options",
            "§7Click to add options"));
        
        // Create poll button
        gui.setItem(19, createItem(Material.EMERALD_BLOCK, "§aCreate Poll",
            "§7Click to create the poll",
            "§7Question: " + (question.isEmpty() ? "§cNot set" : "§a" + question),
            "§7Duration: §a" + duration + " seconds",
            "§7Options: §a" + options.size()));
        
        // Cancel button
        gui.setItem(25, createItem(Material.REDSTONE_BLOCK, "§cCancel", 
            "§7Click to cancel poll creation"));

        // Display current options
        if (!options.isEmpty()) {
            for (int i = 0; i < Math.min(options.size(), 9); i++) {
                gui.setItem(36 + i, createItem(Material.NAME_TAG, "§bOption " + (i + 1), 
                    "§7" + options.get(i),
                    "§cClick to remove!"));
            }
        }

        return gui;
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                meta.setLore(Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory() != inventory) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;

        int slot = event.getRawSlot();

        switch (slot) {
            case 13: // Question
                requestInput(clicker, PollCommand.PollCreationState.QUESTION, "Enter the poll question:");
                break;
            case 12: // Duration
                requestInput(clicker, PollCommand.PollCreationState.DURATION, "Enter the poll duration in seconds:");
                break;
            case 14: // Options
                requestInput(clicker, PollCommand.PollCreationState.OPTIONS, "Enter a poll option:");
                break;
            case 19: // Create Poll
                createPoll(clicker);
                break;
            case 25: // Cancel
                pollCommand.clearPlayerState(player.getUniqueId());
                PollGUI.removePendingInput(player.getUniqueId());
                clicker.closeInventory();
                break;
            default:
                // Handle option removal (slots 36-44)
                if (slot >= 36 && slot <= 44) {
                    int optionIndex = slot - 36;
                    List<String> currentOptions = pollCommand.getPlayerOptions(player.getUniqueId());
                    if (optionIndex < currentOptions.size()) {
                        String removedOption = currentOptions.remove(optionIndex);
                        pollCommand.setPlayerOptions(player.getUniqueId(), currentOptions);
                        plugin.getMessageManager().sendMessage(clicker, "pollgui.option-removed", 
                            new HashMap<String, String>() {{ put("%option%", removedOption); }});
                        
                        // Refresh the GUI
                        clicker.closeInventory();
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            pollCommand.createPollGUI(clicker).open();
                        });
                    }
                }
                break;
        }
    }

    private void requestInput(Player player, PollCommand.PollCreationState state, String message) {
        pollCommand.setPlayerState(player.getUniqueId(), state);
        pendingInputs.put(player.getUniqueId(), new PendingInput(state));
        player.closeInventory();
        plugin.getMessageManager().sendMessage(player, "pollgui.enter-value", 
            new HashMap<String, String>() {{ put("%message%", message); }});
    }

    private void createPoll(Player player) {
        String question = pollCommand.getPlayerQuestion(player.getUniqueId());
        int duration = pollCommand.getPlayerDuration(player.getUniqueId());
        List<String> options = pollCommand.getPlayerOptions(player.getUniqueId());
        
        if (question.isEmpty()) {
            plugin.getMessageManager().sendMessage(player, "pollgui.no-question");
            return;
        }
        
        if (options.size() < 2) {
            plugin.getMessageManager().sendMessage(player, "pollgui.not-enough-options");
            return;
        }

        // Create the poll using the existing PollCommand logic
        pollCommand.createPollFromGUI(player, duration, question, options);
        pollCommand.clearPlayerState(player.getUniqueId());
        player.closeInventory();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory() != inventory) return;
        if (!(event.getPlayer() instanceof Player)) return;
        Player closer = (Player) event.getPlayer();
        if (!closer.equals(player)) return;

        // Only unregister if there's no pending input
        if (!pendingInputs.containsKey(player.getUniqueId())) {
            // Don't unregister the listener here - let it persist for chat input
            // But we can clean up the GUI listener since the GUI is closed
            org.bukkit.event.HandlerList.unregisterAll(this);
        }
    }

    // Static method to handle chat input - this will be called from a separate listener
    public static void handleChatInput(Player player, String input, QWERTZcore plugin, PollCommand pollCommand) {
        PendingInput pending = pendingInputs.get(player.getUniqueId());
        if (pending == null) return;

        switch (pending.getState()) {
            case QUESTION:
                pollCommand.setPlayerQuestion(player.getUniqueId(), input);
                plugin.getMessageManager().sendMessage(player, "pollgui.question-set", 
                    new HashMap<String, String>() {{ put("%question%", input); }});
                break;
            case DURATION:
                try {
                    int duration = Integer.parseInt(input);
                    if (duration <= 0) {
                        plugin.getMessageManager().sendMessage(player, "pollgui.invalid-duration");
                        return;
                    }
                    pollCommand.setPlayerDuration(player.getUniqueId(), duration);
                    plugin.getMessageManager().sendMessage(player, "pollgui.duration-set", 
                        new HashMap<String, String>() {{ put("%duration%", String.valueOf(duration)); }});
                } catch (NumberFormatException e) {
                    plugin.getMessageManager().sendMessage(player, "pollgui.invalid-duration");
                    return;
                }
                break;
            case OPTIONS:
                List<String> currentOptions = pollCommand.getPlayerOptions(player.getUniqueId());
                if (currentOptions.size() >= 9) {
                    plugin.getMessageManager().sendMessage(player, "pollgui.too-many-options");
                    return;
                }
                currentOptions.add(input);
                pollCommand.setPlayerOptions(player.getUniqueId(), currentOptions);
                plugin.getMessageManager().sendMessage(player, "pollgui.option-added", 
                    new HashMap<String, String>() {{ put("%option%", input); }});
                break;
        }
        
        pendingInputs.remove(player.getUniqueId());
        
        // Reopen the GUI
        Bukkit.getScheduler().runTask(plugin, () -> {
            pollCommand.createPollGUI(player).open();
        });
    }

    public static void removePendingInput(UUID playerId) {
        pendingInputs.remove(playerId);
    }
    
    public static boolean hasPendingInput(UUID playerId) {
        return pendingInputs.containsKey(playerId);
    }

    private static class PendingInput {
        private final PollCommand.PollCreationState state;

        public PendingInput(PollCommand.PollCreationState state) {
            this.state = state;
        }

        public PollCommand.PollCreationState getState() {
            return state;
        }
    }
}
