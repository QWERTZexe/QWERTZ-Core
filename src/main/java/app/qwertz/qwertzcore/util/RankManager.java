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
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.WeightNode;
import nl.svenar.powerranks.common.structure.PRPlayerRank;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import nl.svenar.powerranks.api.PowerRanksAPI;

import java.util.List;
import java.util.Set;

public class RankManager {
    private final QWERTZcore plugin;
    private LuckPerms luckPerms;
    private PowerRanksAPI powerRanksAPI;
    private boolean usingLuckPerms;
    private boolean usingPowerRanks;

    public RankManager(QWERTZcore plugin) {
        this.plugin = plugin;
        this.usingLuckPerms = setupLuckPerms();
        this.usingPowerRanks = setupPowerRanks();
    }

    private boolean setupLuckPerms() {
        if (plugin.getServer().getPluginManager().getPlugin("LuckPerms") != null) {
            RegisteredServiceProvider<LuckPerms> provider = plugin.getServer().getServicesManager().getRegistration(LuckPerms.class);
            if (provider != null) {
                luckPerms = provider.getProvider();
                return true;
            }
        }
        return false;
    }

    private boolean setupPowerRanks() {
        if (plugin.getServer().getPluginManager().getPlugin("PowerRanks") != null) {
            powerRanksAPI = new PowerRanksAPI();
            return true;
        }
        return false;
    }

    public String getRank(Player player) {
        if (usingLuckPerms) {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                return user.getPrimaryGroup();
            }
        } else if (usingPowerRanks) {
            Set<PRPlayerRank> playerRanks = powerRanksAPI.getPlayersAPI().getRanks(player.getUniqueId());
            if (!playerRanks.isEmpty()) {
                PRPlayerRank primaryRank = playerRanks.iterator().next(); // Get the first rank
                return primaryRank.getName();
            }
        }
        return "default";

    }

    public String getTag(Player player) {
        if (usingLuckPerms) {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                String tag = user.getCachedData().getMetaData().getMetaValue("tag");
                return tag != null ? tag : "";
            }
        } else if (usingPowerRanks) {
            Set<PRPlayerRank> playerRanks = powerRanksAPI.getPlayersAPI().getRanks(player.getUniqueId());
            if (!playerRanks.isEmpty()) {
                PRPlayerRank primaryRank = playerRanks.iterator().next(); // Get the first rank
                return powerRanksAPI.getRanksAPI().getPrefix(primaryRank.getName());
            }
        }
        return "";
    }

    public String getPrefix(Player player) {
        if (usingLuckPerms) {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                String prefix = user.getCachedData().getMetaData().getPrefix();
                if (prefix != null) {
                    return ChatColor.translateAlternateColorCodes('&', prefix);
                }
            }
        } else if (usingPowerRanks) {
            Set<PRPlayerRank> playerRanks = powerRanksAPI.getPlayersAPI().getRanks(player.getUniqueId());
            if (!playerRanks.isEmpty()) {
                PRPlayerRank primaryRank = playerRanks.iterator().next(); // Get the first rank
                String prefix = powerRanksAPI.getRanksAPI().getPrefix(primaryRank.getName());
                if (prefix != null && !prefix.isEmpty()) {
                    return ChatColor.translateAlternateColorCodes('&', prefix);
                }
            }

        }

        if (player.isOp()) {
            return "§6[§6§lHOST§6] ";
        } else {
            return "§a[PLAYER] ";
        }
    }

    public String getSuffix(Player player) {
        if (usingLuckPerms) {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                String suffix = user.getCachedData().getMetaData().getSuffix();
                if (suffix != null) {
                    return ChatColor.translateAlternateColorCodes('&', suffix);
                }
            }
        } else if (usingPowerRanks) {
            Set<PRPlayerRank> playerRanks = powerRanksAPI.getPlayersAPI().getRanks(player.getUniqueId());
            if (!playerRanks.isEmpty()) {
                PRPlayerRank primaryRank = playerRanks.iterator().next(); // Get the first rank
                String suffix = powerRanksAPI.getRanksAPI().getSuffix(primaryRank.getName());
                if (suffix != null && !suffix.isEmpty()) {
                    return ChatColor.translateAlternateColorCodes('&', suffix);
                }
            }
        }
        return "";
    }
    public int getWeight(Player player) {
        if (usingLuckPerms) {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                CachedMetaData metaData = user.getCachedData().getMetaData();
                if (metaData != null) {
                    return user.getNodes(NodeType.WEIGHT).stream()
                        .mapToInt(WeightNode::getWeight)
                        .max()
                        .orElse(0);
                }
            }
        }
        if (player.isOp()) {
            return 100;
        } else {
            return 0;
        }
    }


    public boolean isUsingLuckPerms() {
        return usingLuckPerms;
    }

    public boolean isUsingPowerRanks() {
        return usingPowerRanks;
    }
}
