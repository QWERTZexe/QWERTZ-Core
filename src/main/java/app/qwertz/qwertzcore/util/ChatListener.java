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
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

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

    @EventHandler
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
        } else if (gameType.equals("typer")) {
            if (message.equalsIgnoreCase((String) answer)) {
                announceWinner(event.getPlayer().getName());
            }
        }
    }

    private void announceWinner(String playerName) {
        Bukkit.broadcastMessage(String.format("%s %s%s %shas won the chat revival game!",
                QWERTZcore.CORE_ICON, plugin.getConfigManager().getColor("colorAlive"), playerName, plugin.getConfigManager().getColor("colorPrimary")));

        if (gameType.equals("guess") || gameType.equals("math")) {
            Bukkit.broadcastMessage(String.format("%s %sThe correct answer was: %s%d",
                    QWERTZcore.CORE_ICON, plugin.getConfigManager().getColor("colorPrimary"), plugin.getConfigManager().getColor("colorAlive"), answer));
        }

        endGame();
    }

    public void cancelGame() {
        if (gameType.equals("guess") || gameType.equals("math")) {
            Bukkit.broadcastMessage(String.format("%s %sThe correct answer was: %s%d",
                    QWERTZcore.CORE_ICON, plugin.getConfigManager().getColor("colorPrimary"), plugin.getConfigManager().getColor("colorAlive"), answer));
        } else if (gameType.equals("typer")) {
            Bukkit.broadcastMessage(String.format("%s %sThe correct sentence was: %s%s",
                    QWERTZcore.CORE_ICON, plugin.getConfigManager().getColor("colorPrimary"), plugin.getConfigManager().getColor("colorAlive"), answer));
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