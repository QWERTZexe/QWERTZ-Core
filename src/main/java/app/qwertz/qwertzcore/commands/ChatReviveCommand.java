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
import app.qwertz.qwertzcore.util.ChatListener;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Random;
import java.util.Arrays;
import java.util.List;

public class ChatReviveCommand implements CommandExecutor {

    private final QWERTZcore plugin;
    private final Random random = new Random();
    private ChatListener activeGame;

    private static final List<String> WORDS = Arrays.asList(
            "apple", "banana", "cat", "dog", "elephant", "frog", "giraffe", "house", "ice", "jump",
            "kite", "lemon", "monkey", "nest", "orange", "penguin", "queen", "rabbit", "sun", "tree",
            "umbrella", "violin", "water", "xylophone", "yellow", "zebra", "book", "car", "door", "egg", "qwertz"
    );

    public ChatReviveCommand(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(plugin.getConfigManager().getColor("colorError") + "Usage: /chatrevive <math|typer|guess|cancel> [max]");
            plugin.getSoundManager().playSoundToSender(sender);
            return false;
        }

        if (args[0].equalsIgnoreCase("cancel")) {
            cancelGame(sender);
            return true;
        }

        if (activeGame != null && !activeGame.isGameOver()) {
            sender.sendMessage(plugin.getConfigManager().getColor("colorError") + "A game is already in progress. Use /chatrevive cancel to end it.");
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
                        sender.sendMessage(plugin.getConfigManager().getColor("colorError") + "Invalid number format for max value. Using default of 40.");
                        plugin.getSoundManager().playSoundToSender(sender);
                    }
                }
                startGuessGame(max);
                break;
            default:
                sender.sendMessage(plugin.getConfigManager().getColor("colorError") + "Invalid game type. Use math, typer, or guess.");
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

        broadcastMessage(String.format("%s %sMath Question: %s%s",
                QWERTZcore.CORE_ICON, plugin.getConfigManager().getColor("colorPrimary"), plugin.getConfigManager().getColor("colorSuccess"), question));
        broadcastMessage(plugin.getConfigManager().getColor("colorPrimary") + "Type the correct answer in chat!");
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

        broadcastMessage(String.format("%s %sType this sentence: %s%s",
                QWERTZcore.CORE_ICON, plugin.getConfigManager().getColor("colorPrimary"), plugin.getConfigManager().getColor("colorSuccess"), finalSentence));

        plugin.getSoundManager().broadcastConfigSound();
        activeGame = new ChatListener(plugin, finalSentence, "typer", this);
        plugin.getServer().getPluginManager().registerEvents(activeGame, plugin);
    }

    private void startGuessGame(int max) {
        int target = random.nextInt(max) + 1;

        broadcastMessage(String.format("%s %sGuess a number between %s1 %sand %s%d",
                QWERTZcore.CORE_ICON, plugin.getConfigManager().getColor("colorPrimary"), plugin.getConfigManager().getColor("colorSuccess"), plugin.getConfigManager().getColor("colorPrimary"), plugin.getConfigManager().getColor("colorSuccess"), max));

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

    private void broadcastMessage(String message) {
        this.plugin.getMessageManager().broadcastMessage(QWERTZcore.CORE_ICON + " " + message);
    }

    private void cancelGame(CommandSender sender) {
        if (activeGame != null) {
            activeGame.cancelGame();
            activeGame = null;
            broadcastMessage(plugin.getConfigManager().getColor("colorPrimary") + "The chat revival game has been cancelled.");
            plugin.getSoundManager().broadcastConfigSound();
        } else {
            sender.sendMessage(QWERTZcore.CORE_ICON + plugin.getConfigManager().getColor("colorError") + " There is no active chat revival game to cancel.");
            plugin.getSoundManager().playSoundToSender(sender);
        }
    }

    public void setActiveGameNull() {
        this.activeGame = null;
    }
}