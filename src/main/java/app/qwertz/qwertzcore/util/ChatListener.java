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
import app.qwertz.qwertzcore.commands.ChatReviveCommand;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;

public class ChatListener implements Listener {

    private final QWERTZcore plugin;
    private final Object answer;
    private final String gameType;
    private final ChatReviveCommand chatReviveCommand;
    private boolean gameOver = false;

    public ChatListener(QWERTZcore plugin, Object answer, String gameType, ChatReviveCommand chatReviveCommand) {
        this.plugin = plugin;
        this.answer = answer;
        this.gameType = gameType;
        this.chatReviveCommand = chatReviveCommand;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {

        if (gameOver) return;

        String message = event.getMessage().trim();

        if (gameType.equals("guess") || gameType.equals("math")) {
            try {
                int guessedNumber = Integer.parseInt(message);
                if (guessedNumber == (Integer) answer) {
                    announceWinner(event.getPlayer().getName());
                }
            } catch (NumberFormatException ignored) {}
        } else if (gameType.equals("typer") || gameType.equals("custom")) {
            if (message.equalsIgnoreCase((String) answer)) {
                announceWinner(event.getPlayer().getName());
            }
        }
    }

    private void announceWinner(String playerName) {
        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%player%", playerName);
        plugin.getMessageManager().broadcastMessage("chatrevival.winner", localMap);
        plugin.getSoundManager().broadcastConfigSound();
        if (gameType.equals("guess") || gameType.equals("math")) {
            HashMap<String, String> localMap2 = new HashMap<>();
            localMap2.put("%answer%", String.valueOf(answer));
            plugin.getMessageManager().broadcastMessage("chatrevival.correct-answer", localMap2);
            plugin.getSoundManager().broadcastConfigSound();
        }

        endGame();
    }

    public void cancelGame() {
        if (gameType.equals("guess") || gameType.equals("math")) {
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%answer%", String.valueOf(answer));
            plugin.getMessageManager().broadcastMessage("chatrevival.correct-answer", localMap);
            plugin.getSoundManager().broadcastConfigSound();
        } else if (gameType.equals("typer")) {
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%sentence%", String.valueOf(answer));
            plugin.getMessageManager().broadcastMessage("chatrevival.correct-sentence", localMap);
            plugin.getSoundManager().broadcastConfigSound();
        } else if (gameType.equals("custom")) {
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%answer%", String.valueOf(answer));
            plugin.getMessageManager().broadcastMessage("chatrevival.correct-answer", localMap);
            plugin.getSoundManager().broadcastConfigSound();
        }
        endGame();
    }

    private void endGame() {
        gameOver = true;
        HandlerList.unregisterAll(this);
        chatReviveCommand.setActiveGameNull();
    }

    public boolean isGameOver() {
        return gameOver;
    }
}
