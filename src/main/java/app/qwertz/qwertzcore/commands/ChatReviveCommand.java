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
import app.qwertz.qwertzcore.gui.ChatRevivalGUI;
import app.qwertz.qwertzcore.listeners.ChatListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Random;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChatReviveCommand implements CommandExecutor {

    private final QWERTZcore plugin;
    private final Random random = new Random();
    private ChatListener activeGame;

    // State management for GUI
    private Map<UUID, ChatRevivalState> playerStates = new HashMap<>();
    private Map<UUID, String> playerQuestions = new HashMap<>();
    private Map<UUID, String> playerAnswers = new HashMap<>();
    private Map<UUID, Integer> playerGuessMax = new HashMap<>();
    private Map<UUID, String> playerSelectedGame = new HashMap<>();

    private static final List<String> WORDS = Arrays.asList(
            "apple", "banana", "cat", "dog", "elephant", "frog", "giraffe", "house", "ice", "jump",
            "kite", "lemon", "monkey", "nest", "orange", "penguin", "queen", "rabbit", "sun", "tree",
            "umbrella", "violin", "water", "xylophone", "yellow", "zebra", "book", "car", "door", "egg", "qwertz"
    );

    public enum ChatRevivalState {
        QUESTION, ANSWER, GUESS_MAX
    }

    public ChatReviveCommand(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "player-only");
            return false;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            // Open GUI
            createChatRevivalGUI(player).open();
            return true;
        }

        if (args[0].equalsIgnoreCase("cancel")) {
            cancelGame(sender);
            return true;
        }

        if (activeGame != null && !activeGame.isGameOver()) {
            plugin.getMessageManager().sendMessage(sender, "chatrevival.already-going");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "math":
                startMathGame();
                break;
            case "typer":
                startTyperGame();
                break;
            case "guess":
                int max = 40;
                if (args.length >= 2) {
                    try {
                        max = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        plugin.getMessageManager().sendMessage(sender, "chatrevival.invalid-number");
                        plugin.getSoundManager().playSoundToSender(sender);
                    }
                }
                startGuessGame(max);
                break;
            case "custom":
                if (args.length < 3) {
                    plugin.getMessageManager().sendInvalidUsage(sender, "/chatrevival custom <question> <answer>");
                    plugin.getSoundManager().playSoundToSender(sender);
                    return false;
                }
                String question = args[1];
                String answer = args[2];
                startCustomGame(question, answer);
                break;
            default:
                plugin.getMessageManager().sendMessage(sender, "chatrevival.invalid-game");
                plugin.getSoundManager().playSoundToSender(sender);
                return false;
        }

        return true;
    }

    private void startMathGame() {
        int num1 = random.nextInt(20) + 1;
        int num2 = random.nextInt(20) + 1;
        int num3 = random.nextInt(20) + 1;
        char[] operators = {'+', '-', '*'};
        char op1 = operators[random.nextInt(operators.length)];
        char op2 = operators[random.nextInt(operators.length)];

        String question = String.format("%d %c %d %c %d", num1, op1, num2, op2, num3);
        int answer = evaluateExpression(num1, num2, num3, op1, op2);
        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%question%", question);
        plugin.getMessageManager().broadcastMessage("chatrevival.math-question", localMap);
        plugin.getMessageManager().broadcastMessage("chatrevival.math-howto");
        plugin.getSoundManager().broadcastConfigSound();

        activeGame = new ChatListener(plugin, answer, "math", this);
        plugin.getServer().getPluginManager().registerEvents(activeGame, plugin);
    }

    private void startTyperGame() {
        int wordCount = random.nextInt(5) + 3; // 3 to 7 words
        StringBuilder sentence = new StringBuilder();
        for (int i = 0; i < wordCount; i++) {
            sentence.append(WORDS.get(random.nextInt(WORDS.size()))).append(" ");
        }
        String finalSentence = sentence.toString().trim();
        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%sentence%", finalSentence);
        plugin.getMessageManager().broadcastMessage("chatrevival.typer-question", localMap);
        plugin.getSoundManager().broadcastConfigSound();
        activeGame = new ChatListener(plugin, finalSentence, "typer", this);
        plugin.getServer().getPluginManager().registerEvents(activeGame, plugin);
    }

    private void startGuessGame(int max) {
        int target = random.nextInt(max) + 1;

        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%number%", String.valueOf(max));
        plugin.getMessageManager().broadcastMessage("chatrevival.guess-question", localMap);
        plugin.getSoundManager().broadcastConfigSound();
        activeGame = new ChatListener(plugin, target, "guess", this);
        plugin.getServer().getPluginManager().registerEvents(activeGame, plugin);
    }

    private int evaluateExpression(int num1, int num2, int num3, char op1, char op2) {
        // First, handle multiplication
        if (op1 == '*') {
            return performOperation(num1 * num2, num3, op2);
        } else if (op2 == '*') {
            return performOperation(num1, num2 * num3, op1);
        } else {
            // If no multiplication, perform operations from left to right
            return performOperation(performOperation(num1, num2, op1), num3, op2);
        }
    }

    private int performOperation(int a, int b, char op) {
        switch (op) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            default:
                throw new IllegalArgumentException("Invalid operator: " + op);
        }
    }

    private void startCustomGame(String question, String answer) {
        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%question%", question);
        plugin.getMessageManager().broadcastMessage("chatrevival.custom-question", localMap);
        plugin.getSoundManager().broadcastConfigSound();
        activeGame = new ChatListener(plugin, answer, "custom", this);
        plugin.getServer().getPluginManager().registerEvents(activeGame, plugin);
    }

    // State management methods
    public void setPlayerState(UUID playerId, ChatRevivalState state) {
        playerStates.put(playerId, state);
    }

    public ChatRevivalState getPlayerState(UUID playerId) {
        return playerStates.get(playerId);
    }

    public void setPlayerQuestion(UUID playerId, String question) {
        playerQuestions.put(playerId, question);
    }

    public String getPlayerQuestion(UUID playerId) {
        return playerQuestions.getOrDefault(playerId, "");
    }

    public void setPlayerAnswer(UUID playerId, String answer) {
        playerAnswers.put(playerId, answer);
    }

    public String getPlayerAnswer(UUID playerId) {
        return playerAnswers.getOrDefault(playerId, "");
    }

    public void setPlayerGuessMax(UUID playerId, int max) {
        playerGuessMax.put(playerId, max);
    }

    public int getPlayerGuessMax(UUID playerId) {
        return playerGuessMax.getOrDefault(playerId, 40);
    }

    public void setPlayerSelectedGame(UUID playerId, String gameType) {
        playerSelectedGame.put(playerId, gameType);
    }

    public String getPlayerSelectedGame(UUID playerId) {
        return playerSelectedGame.getOrDefault(playerId, "");
    }

    public void clearPlayerState(UUID playerId) {
        playerStates.remove(playerId);
        playerQuestions.remove(playerId);
        playerAnswers.remove(playerId);
        playerGuessMax.remove(playerId);
        playerSelectedGame.remove(playerId);
    }

    public ChatRevivalGUI createChatRevivalGUI(Player player) {
        return new ChatRevivalGUI(plugin, player, this);
    }

    public void startCustomGameFromGUI(Player player) {
        String question = getPlayerQuestion(player.getUniqueId());
        String answer = getPlayerAnswer(player.getUniqueId());
        
        if (question.isEmpty() || answer.isEmpty()) {
            plugin.getMessageManager().sendMessage(player, "chatrevivalgui.incomplete");
            return;
        }

        startCustomGame(question, answer);
        clearPlayerState(player.getUniqueId());
    }

    public void startGuessGameFromGUI(Player player) {
        int max = getPlayerGuessMax(player.getUniqueId());
        startGuessGame(max);
        clearPlayerState(player.getUniqueId());
    }

    private void cancelGame(CommandSender sender) {
        if (activeGame != null) {
            activeGame.cancelGame();
            activeGame = null;
            plugin.getMessageManager().broadcastMessage("chatrevival.cancelled");
            plugin.getSoundManager().broadcastConfigSound();
        } else {
            plugin.getMessageManager().sendMessage(sender, "chatrevival.no-active-game");
            plugin.getSoundManager().playSoundToSender(sender);
        }
    }

    public void setActiveGameNull() {
        this.activeGame = null;
    }
}