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
import net.luckperms.api.model.user.User;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class RankManager {
    private final QWERTZcore plugin;
    private LuckPerms luckPerms;
    private boolean usingLuckPerms;

    public RankManager(QWERTZcore plugin) {
        this.plugin = plugin;
        this.usingLuckPerms = setupLuckPerms();
    }

    private boolean setupLuckPerms() {
        if (plugin.getServer().getPluginManager().getPlugin("LuckPerms") == null) {
            return false;
        }
        RegisteredServiceProvider<LuckPerms> provider = plugin.getServer().getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
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
        }
        return "";
    }
    public String getPrefix(Player player) {
        if (usingLuckPerms) {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                String prefix = user.getCachedData().getMetaData().getPrefix();
                if (prefix != null) {
                    prefix = ChatColor.translateAlternateColorCodes('&', prefix);
                    return prefix;
                }
            }
        }
        if (player.isOp()) {
            return "§6[§6§lHOST§6]";
        }
        else {
            return "§a[PLAYER]";
        }
    }

    public String getSuffix(Player player) {
        if (usingLuckPerms) {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                String suffix = user.getCachedData().getMetaData().getSuffix();
                if (suffix != null) {
                    suffix = ChatColor.translateAlternateColorCodes('&', suffix);
                    return suffix;
                }
            }
        }
        return "";
    }

    public boolean isUsingLuckPerms() {
        return usingLuckPerms;
    }
}