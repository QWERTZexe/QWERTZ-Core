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
import app.qwertz.qwertzcore.commands.ChatReviveCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ChatRevivalGUI implements Listener {

    private static final Map<UUID, PendingInput> pendingInputs = new HashMap<>();
    private final QWERTZcore plugin;
    private final Player player;
    private final ChatReviveCommand chatReviveCommand;
    private Inventory inventory;

    public ChatRevivalGUI(QWERTZcore plugin, Player player, ChatReviveCommand chatReviveCommand) {
        this.plugin = plugin;
        this.player = player;
        this.chatReviveCommand = chatReviveCommand;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.inventory = buildGUI();
    }

    public void open() {
        player.openInventory(inventory);
    }

    private Inventory buildGUI() {
        Inventory gui = Bukkit.createInventory(null, 27, QWERTZcore.CORE_ICON + " " + ChatColor.DARK_PURPLE + "Chat Revival");

        // Fill with black glass panes
        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, blackPane);
        }

        // Get current state from ChatReviveCommand
        String question = chatReviveCommand.getPlayerQuestion(player.getUniqueId());
        String answer = chatReviveCommand.getPlayerAnswer(player.getUniqueId());
        String selectedGame = chatReviveCommand.getPlayerSelectedGame(player.getUniqueId());
        int currentGuessMax = chatReviveCommand.getPlayerGuessMax(player.getUniqueId());

        // Math game option
        ItemStack mathItem = createItem(Material.PAPER, "§eMath Game",
            "§7Start a math question game",
            "§7Players solve math problems");
        if (selectedGame.equals("math")) {
            mathItem.addUnsafeEnchantment(Enchantment.values()[0], 1);
        }
        gui.setItem(10, mathItem);
        
        // Typer game option
        ItemStack typerItem = createItem(Material.BOOK, "§eTyper Game",
            "§7Start a typing game",
            "§7Players type random sentences");
        if (selectedGame.equals("typer")) {
            typerItem.addUnsafeEnchantment(Enchantment.values()[0], 1);
        }
        gui.setItem(11, typerItem);
        
        // Guess game option
        ItemStack guessItem = createItem(Material.COMPASS, "§eGuess Game",
            "§7Start a number guessing game",
            "§7Max number: §a" + currentGuessMax,
            "§7Click to set max number or start game");
        if (selectedGame.equals("guess")) {
            guessItem.addUnsafeEnchantment(Enchantment.values()[0], 1);
        }
        gui.setItem(12, guessItem);
        
        // Custom game option
        ItemStack customItem = createItem(Material.NAME_TAG, "§eCustom Game",
            "§7Create a custom question",
            "§7Question: " + (question.isEmpty() ? "§cNot set" : "§a" + question),
            "§7Answer: " + (answer.isEmpty() ? "§cNot set" : "§a" + answer),
            "§7Click to set question and answer");
        if (selectedGame.equals("custom")) {
            customItem.addUnsafeEnchantment(Enchantment.values()[0], 1);
        }
        gui.setItem(13, customItem);
        
        // Start button (emerald block) - only show if a game is selected
        if (!selectedGame.isEmpty()) {
            String startLore = "";
            switch (selectedGame) {
                case "math":
                    startLore = "§7Click to start the math game";
                    break;
                case "typer":
                    startLore = "§7Click to start the typer game";
                    break;
                case "guess":
                    startLore = "§7Click to start the guess game\n§7Max number: §a" + currentGuessMax;
                    break;
                case "custom":
                    if (!question.isEmpty() && !answer.isEmpty()) {
                        startLore = "§7Click to start the custom game\n§7Question: §a" + question + "\n§7Answer: §a" + answer;
                    } else {
                        startLore = "§cSet question and answer first";
                    }
                    break;
            }
            gui.setItem(16, createItem(Material.EMERALD_BLOCK, "§aStart " + selectedGame.substring(0, 1).toUpperCase() + selectedGame.substring(1) + " Game", startLore.split("\n")));
        }
        
        // Cancel button
        gui.setItem(22, createItem(Material.REDSTONE_BLOCK, "§cCancel", 
            "§7Click to close the GUI"));

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
            case 10: // Math Game
                selectGame("math");
                break;
            case 11: // Typer Game
                selectGame("typer");
                break;
            case 12: // Guess Game
                selectGame("guess");
                break;
            case 13: // Custom Game
                selectGame("custom");
                break;
            case 16: // Start Game
                startSelectedGame();
                break;
            case 22: // Cancel
                chatReviveCommand.clearPlayerState(player.getUniqueId());
                ChatRevivalGUI.removePendingInput(player.getUniqueId());
                clicker.closeInventory();
                break;
        }
    }

    private void selectGame(String gameType) {
        chatReviveCommand.setPlayerSelectedGame(player.getUniqueId(), gameType);
        
        // For guess and custom games, we need to set up the configuration first
        if (gameType.equals("guess")) {
            requestGuessMaxInput(player);
        } else if (gameType.equals("custom")) {
            requestQuestionInput(player);
        } else {
            // For math and typer, we can start immediately
            refreshGUI();
        }
    }

    private void startSelectedGame() {
        String selectedGame = chatReviveCommand.getPlayerSelectedGame(player.getUniqueId());
        
        switch (selectedGame) {
            case "math":
                startMathGame(player);
                break;
            case "typer":
                startTyperGame(player);
                break;
            case "guess":
                startGuessGame(player);
                break;
            case "custom":
                startCustomGame(player);
                break;
        }
    }

    private void startMathGame(Player player) {
        player.closeInventory();
        chatReviveCommand.onCommand(player, null, "chatrevival", new String[]{"math"});
    }

    private void startTyperGame(Player player) {
        player.closeInventory();
        chatReviveCommand.onCommand(player, null, "chatrevival", new String[]{"typer"});
    }

    private void startGuessGame(Player player) {
        chatReviveCommand.startGuessGameFromGUI(player);
        player.closeInventory();
    }

    private void startCustomGame(Player player) {
        chatReviveCommand.startCustomGameFromGUI(player);
        player.closeInventory();
    }

    private void requestQuestionInput(Player player) {
        chatReviveCommand.setPlayerState(player.getUniqueId(), ChatReviveCommand.ChatRevivalState.QUESTION);
        pendingInputs.put(player.getUniqueId(), new PendingInput(ChatReviveCommand.ChatRevivalState.QUESTION));
        player.closeInventory();
        plugin.getMessageManager().sendMessage(player, "chatrevivalgui.enter-question");
    }

    private void requestGuessMaxInput(Player player) {
        chatReviveCommand.setPlayerState(player.getUniqueId(), ChatReviveCommand.ChatRevivalState.GUESS_MAX);
        pendingInputs.put(player.getUniqueId(), new PendingInput(ChatReviveCommand.ChatRevivalState.GUESS_MAX));
        player.closeInventory();
        plugin.getMessageManager().sendMessage(player, "chatrevivalgui.enter-guess-max");
    }

    private void refreshGUI() {
        this.inventory = buildGUI();
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory() != inventory) return;
        if (!(event.getPlayer() instanceof Player)) return;
        Player closer = (Player) event.getPlayer();
        if (!closer.equals(player)) return;

        // Only unregister if there's no pending input
        if (!pendingInputs.containsKey(player.getUniqueId())) {
            org.bukkit.event.HandlerList.unregisterAll(this);
        }
    }

    // Static method to handle chat input - this will be called from a separate listener
    public static void handleChatInput(Player player, String input, QWERTZcore plugin, ChatReviveCommand chatReviveCommand) {
        PendingInput pending = pendingInputs.get(player.getUniqueId());
        if (pending == null) return;

        switch (pending.getState()) {
            case QUESTION:
                chatReviveCommand.setPlayerQuestion(player.getUniqueId(), input);
                plugin.getMessageManager().sendMessage(player, "chatrevivalgui.question-set", 
                    new HashMap<String, String>() {{ put("%question%", input); }});
                
                // Now request the answer
                chatReviveCommand.setPlayerState(player.getUniqueId(), ChatReviveCommand.ChatRevivalState.ANSWER);
                pendingInputs.put(player.getUniqueId(), new PendingInput(ChatReviveCommand.ChatRevivalState.ANSWER));
                plugin.getMessageManager().sendMessage(player, "chatrevivalgui.enter-answer");
                break;
            case ANSWER:
                chatReviveCommand.setPlayerAnswer(player.getUniqueId(), input);
                plugin.getMessageManager().sendMessage(player, "chatrevivalgui.answer-set", 
                    new HashMap<String, String>() {{ put("%answer%", input); }});
                pendingInputs.remove(player.getUniqueId());
                
                // Reopen the GUI with the selected game enchanted
                Bukkit.getScheduler().runTask(plugin, () -> {
                    chatReviveCommand.createChatRevivalGUI(player).open();
                });
                break;
            case GUESS_MAX:
                try {
                    int max = Integer.parseInt(input);
                    if (max > 0) {
                        chatReviveCommand.setPlayerGuessMax(player.getUniqueId(), max);
                        plugin.getMessageManager().sendMessage(player, "chatrevivalgui.guess-max-set", 
                            new HashMap<String, String>() {{ put("%max%", input); }});
                        pendingInputs.remove(player.getUniqueId());
                        
                        // Reopen the GUI with the selected game enchanted
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            chatReviveCommand.createChatRevivalGUI(player).open();
                        });
                    } else {
                        plugin.getMessageManager().sendMessage(player, "chatrevivalgui.invalid-number");
                    }
                } catch (NumberFormatException e) {
                    plugin.getMessageManager().sendMessage(player, "chatrevivalgui.invalid-number");
                }
                break;
        }
    }

    public static void removePendingInput(UUID playerId) {
        pendingInputs.remove(playerId);
    }
    
    public static boolean hasPendingInput(UUID playerId) {
        return pendingInputs.containsKey(playerId);
    }

    private static class PendingInput {
        private final ChatReviveCommand.ChatRevivalState state;

        public PendingInput(ChatReviveCommand.ChatRevivalState state) {
            this.state = state;
        }

        public ChatReviveCommand.ChatRevivalState getState() {
            return state;
        }
    }
}
