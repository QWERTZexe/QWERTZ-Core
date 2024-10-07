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
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Objects;

public class TablistManager {
    private final QWERTZcore plugin;

    public TablistManager(QWERTZcore plugin) {
        this.plugin = plugin;
        startTablistUpdater();
    }

    public void updateTablist(Player player) {

        if (plugin.getConfigManager().getTabList()) {
        String header = ChatColor.GOLD + "" + ChatColor.BOLD + plugin.getConfigManager().getServerName() + "\n\n" +
                ChatColor.YELLOW + "Event: " + plugin.getConfigManager().getEventName() + "\n" +
                ChatColor.AQUA + "Players: " + plugin.getVanishManager().getNonVanishedPlayerCount() + "\n";

        String footer = "\n" + QWERTZcore.CORE_ICON + ChatColor.GOLD + " QWERTZ Core";

        player.setPlayerListHeaderFooter(header, footer);

        updatePlayerListName(player);
        }
    }

    private void updatePlayerListName(Player player) {
        String prefix = plugin.getRankManager().getPrefix(player);
        String suffix = plugin.getRankManager().getSuffix(player);
        String pingColor = getPingColor(player.getPing());
        String listName;
        if (!(Objects.equals(prefix, ""))) {
            listName = prefix + " " + player.getName() + suffix + " " + pingColor + player.getPing();
        }
        else {
            listName = prefix + player.getName() + suffix + " " + pingColor + player.getPing();
        }
        Scoreboard scoreboard = player.getScoreboard();
        Team team = scoreboard.getTeam(player.getName());
        if (team == null) {
            team = scoreboard.registerNewTeam(player.getName());
        }

        team.setPrefix(prefix);
        team.setSuffix(suffix + " " + pingColor + player.getPing());
        team.addEntry(player.getName());

        player.setPlayerListName(listName);
    }

    private String getPingColor(int ping) {
        if (ping <= 40) return ChatColor.DARK_GREEN + "";
        if (ping <= 100) return ChatColor.GREEN + "";
        if (ping <= 160) return ChatColor.YELLOW + "";
        if (ping <= 250) return ChatColor.RED + "";
        return ChatColor.DARK_RED + "";
    }

    private void startTablistUpdater() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateTablist(player);
            }
        }, 20L, 20L); // Update every second
    }
}