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
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MessageManager {
    private final QWERTZcore plugin;
    private final Map<UUID, UUID> lastMessageSender;

    public MessageManager(QWERTZcore plugin) {
        this.plugin = plugin;
        this.lastMessageSender = new HashMap<>();
    }

    public boolean canReceiveMessages(Player player) {
        return plugin.getDatabaseManager().isMessageToggleEnabled(player.getUniqueId());
    }

    public void toggleMessages(Player player) {
        boolean currentState = plugin.getDatabaseManager().isMessageToggleEnabled(player.getUniqueId());
        plugin.getDatabaseManager().setMessageToggleEnabled(player.getUniqueId(), !currentState);
    }

    public void setReplyTarget(Player sender, Player recipient) {
        lastMessageSender.put(recipient.getUniqueId(), sender.getUniqueId());
    }

    public Player getReplyTarget(Player player) {
        UUID lastSenderUUID = lastMessageSender.get(player.getUniqueId());
        return lastSenderUUID != null ? plugin.getServer().getPlayer(lastSenderUUID) : null;
    }
}