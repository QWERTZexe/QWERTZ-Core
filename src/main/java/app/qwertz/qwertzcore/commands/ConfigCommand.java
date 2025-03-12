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
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ConfigCommand implements CommandExecutor {

    private final QWERTZcore plugin;

    public ConfigCommand(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            plugin.getMessageManager().sendInvalidUsage((Player) sender, "/config <key> <value>");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        String key = args[0];
        String value = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        if (!plugin.getConfigManager().hasKey(key) && !key.equals("spawn")) {
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%key%", key);
            plugin.getMessageManager().sendMessage((Player) sender, "config.key-not-found", localMap);
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        if (key.equals("spawn")) {
            if (!(sender instanceof Player)) {
                plugin.getMessageManager().sendMessage((Player) sender, "general.only-player-execute");
                return true;
            }
            if (value.equalsIgnoreCase("currentpos")) {
                Player player = (Player) sender;
                Location loc = player.getLocation();
                Map<String, Object> spawnMap = new HashMap<>();
                spawnMap.put("world", loc.getWorld().getName());
                spawnMap.put("x", loc.getX());
                spawnMap.put("y", loc.getY());
                spawnMap.put("z", loc.getZ());
                spawnMap.put("yaw", (double) loc.getYaw());
                spawnMap.put("pitch", (double) loc.getPitch());
                plugin.getConfigManager().set("spawn", spawnMap);
                plugin.getMessageManager().sendMessage((Player) sender, "config.set-spawn");
                plugin.getSoundManager().playSoundToSender(sender);
            } else {
                plugin.getMessageManager().sendMessage((Player) sender, "config.invalid-spawn");
            }
        } else {
            Object currentValue = plugin.getConfigManager().get(key);
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("%key%", key);
            localMap.put("%value%", String.valueOf(currentValue));


            if (currentValue instanceof Boolean) {
                boolean boolValue = Boolean.parseBoolean(value);
                plugin.getConfigManager().set(key, boolValue);
                plugin.getMessageManager().sendMessage((Player) sender, "config.set-key", localMap);
                plugin.getSoundManager().playSoundToSender(sender);
            } else if (currentValue instanceof String) {
                plugin.getConfigManager().set(key, value);
                plugin.getMessageManager().sendMessage((Player) sender, "config.set-key", localMap);
                plugin.getSoundManager().playSoundToSender(sender);
            } else if (currentValue instanceof Number) {
                try {
                    if (value.contains(".")) {
                        double doubleValue = Double.parseDouble(value);
                        plugin.getConfigManager().set(key, doubleValue);
                        plugin.getMessageManager().sendMessage((Player) sender, "config.set-key", localMap);
                        plugin.getSoundManager().playSoundToSender(sender);
                    } else {
                        int intValue = Integer.parseInt(value);
                        plugin.getConfigManager().set(key, intValue);
                        plugin.getMessageManager().sendMessage((Player) sender, "config.set-key", localMap);
                        plugin.getSoundManager().playSoundToSender(sender);
                    }
                } catch (NumberFormatException e) {
                    HashMap<String, String> localMap2 = new HashMap<>();
                    localMap2.put("%value%", value);
                    plugin.getMessageManager().sendMessage((Player) sender, "config.invalid-number-format", localMap2);
                    plugin.getSoundManager().playSoundToSender(sender);
                    return true;
                }
            } else {
                HashMap<String, String> localMap3 = new HashMap<>();
                localMap3.put("%key%", key);
                plugin.getMessageManager().sendMessage((Player) sender, "config.invalid-type", localMap3);
                plugin.getSoundManager().playSoundToSender(sender);
                return true;
            }
        }

        plugin.getConfigManager().saveConfig();
        return true;
    }
}