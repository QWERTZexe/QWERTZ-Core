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
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerDeathListener implements Listener {
    private QWERTZcore plugin;

    public PlayerDeathListener(QWERTZcore plugin) {

        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        plugin.getEventManager().handlePlayerDeath(player, false);
        if (plugin.getConfigManager().get("suppressVanilla").equals(true)) {
            event.setDeathMessage(null);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (plugin.getConfigManager().getTpOnDeath()) {
            // Teleport them to the configured spawn location
            Location spawnLocation = plugin.getConfigManager().getSpawnLocation();
            if (spawnLocation != null) {
                event.setRespawnLocation(spawnLocation);
            }
        }
    }
}