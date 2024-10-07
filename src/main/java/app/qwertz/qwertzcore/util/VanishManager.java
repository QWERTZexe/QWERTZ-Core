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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class VanishManager {
    private final QWERTZcore plugin;
    List<UUID> vanishedPlayers = new ArrayList<>();
    
    public VanishManager(QWERTZcore plugin) {
        this.plugin = plugin;
    }
    
    public void hideVanishedPlayers(Player player) {
        for (Player loop : Bukkit.getOnlinePlayers()) {
            if(vanishedPlayers.contains(loop.getUniqueId())) {
                player.hidePlayer(loop);
            }
        }
    }
    
    public void removeVanishedPlayer(Player player) {
        vanishedPlayers.remove(player.getUniqueId());
    }
    
    public void addVanishedPlayer(Player player) {
        vanishedPlayers.add(player.getUniqueId());
    }
    
    public int getNonVanishedPlayerCount() {
        return Bukkit.getOnlinePlayers().size() - vanishedPlayers.size();
    }
    
    public List getVanishedPlayers() {
        return(vanishedPlayers);
    }
}
