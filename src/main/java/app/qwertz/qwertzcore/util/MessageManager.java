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
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class MessageManager {
    private final QWERTZcore plugin;
    private final Map<UUID, UUID> lastMessageSender;
    public FileConfiguration messagesConfig;
    private File messagesFile;
    private FileConfiguration defaultMessagesConfig;


    public MessageManager(QWERTZcore plugin) {
        this.plugin = plugin;
        this.lastMessageSender = new HashMap<>();
        loadMessages();
    }

    private void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        // Load default messages from JAR
        InputStream defaultStream = plugin.getResource("messages.yml");
        if (defaultStream != null) {
            defaultMessagesConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            updateMessages();
        } else {
            plugin.getLogger().warning("Default messages.yml not found in JAR!");
        }
    }

    private void updateMessages() {
        Set<String> keys = defaultMessagesConfig.getKeys(true);
        for (String key : keys) {
            if (!messagesConfig.contains(key)) {
                messagesConfig.set(key, defaultMessagesConfig.get(key));
                try {
                    messagesConfig.save(messagesFile);
                } catch (IOException e) {
                    plugin.getLogger().warning("Unable to save messages.yml!");
                }
            }
        }
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


    public String prepareMessage(String message, HashMap<String, String> localPlaceholders) {
        for (Map.Entry<String, String> entry : localPlaceholders.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }
        FileConfiguration config = getConfigToUse();

        if (config.contains("placeholders")) {
            ConfigurationSection placeholders = config.getConfigurationSection("placeholders");
            if (placeholders != null) {
                for (String key : placeholders.getKeys(false)) {
                    String placeholder = "%" + key + "%";
                    String value = placeholders.getString(key);
                    assert value != null: "Invalid placeholder";
                    message = message.replace(placeholder, value);
                }
            }
        }
        message = message.replace("%colorPrimary%", "§e");
        message = message.replace("%colorSecondary%", "§6");
        message = message.replace("%colorTertiary%", "§b");
        message = message.replace("%colorError%", "§c");
        message = message.replace("%colorSuccess%", "§a");
        message = message.replace("%colorAlive%", "§a");
        message = message.replace("%colorDead%", "§c");
        message = message.replace("%CORE_ICON_RAW%", QWERTZcore.CORE_ICON_RAW);
        message = message.replace("%CORE_ICON%", QWERTZcore.CORE_ICON);
        message = ChatColor.translateAlternateColorCodes('&', message);
        return message;
    }
    public FileConfiguration loadFromRepo(String themeName, String type) {
        String repoUrl = "https://raw.githubusercontent.com/QWERTZexe/QWERTZ-Core/refs/heads/main/themes/" + themeName + "/" + type + ".yml";

        try {
            URL url = new URL(repoUrl);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(5000); // Set connection timeout
            connection.setReadTimeout(5000);    // Set read timeout
            try (InputStream inputStream = connection.getInputStream()) {
                return YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream));
            } catch (IOException e) {
                plugin.getLogger().warning("Error reading messages.yml from repo: " + e.getMessage());
                return null;
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Error connecting to repo: " + e.getMessage());
            return null;
        }
    }
    // Helper method to determine which config to use
    private FileConfiguration getConfigToUse() {
        String activeTheme = messagesConfig.getString("active-theme");
        if (Objects.equals(activeTheme, "file")) {
            return messagesConfig;
        } else if (Objects.equals(activeTheme, "internal")) {
            return defaultMessagesConfig;
        } else {
            // Attempt to load from repo
            FileConfiguration repoConfig = loadFromRepo(activeTheme, "messages");
            if (repoConfig != null) {
                return repoConfig;
            } else {
                plugin.getLogger().warning("Failed to load theme from repo, using internal.");
                return defaultMessagesConfig; // Fallback to default
            }
        }
    }

    public String getMessage(String path) {
        FileConfiguration config = getConfigToUse();
        String message = config.getString(path);
        if (message == null) {
            return QWERTZcore.CORE_ICON + " %colorError%Message not found: " + path;
        }
        return message;
    }

    public void broadcastMessage(String message, HashMap<String, String> localPlaceholders) {
        message = getMessage(message);
        message = prepareMessage(message, localPlaceholders);
        if ((Boolean) plugin.getConfigManager().get("biggerMessages")) {
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage(message);
            Bukkit.broadcastMessage("");
        }
        else {
            Bukkit.broadcastMessage(message);
        }
    }

    public void broadcastMessage(String message) {
        message = getMessage(message);
        message = prepareMessage(message, new HashMap<>());
        if ((Boolean) plugin.getConfigManager().get("biggerMessages")) {
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage(message);
            Bukkit.broadcastMessage("");
        }
        else {
            Bukkit.broadcastMessage(message);
        }
    }

    public void sendMessage(Player recipent, String message, HashMap<String, String> localPlaceholders) {
        message = getMessage(message);
        message = prepareMessage(message, localPlaceholders);
        recipent.sendMessage(message);
    }

    public void sendMessage(Player recipent, String message) {
        message = getMessage(message);
        message = prepareMessage(message, new HashMap<>());
        recipent.sendMessage(message);
    }

    // HELPERS FOR COMMON STUFF

    public void sendInvalidUsage(Player recipent, String usage) {
        String message = "general.invalid-usage";
        message = getMessage(message);
        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%usage%", usage);
        message = prepareMessage(message, localMap);
        recipent.sendMessage(message);
    }

    public void sendConsole(CommandSender sender, String message) {
        message = getMessage(message);
        message = prepareMessage(message, new HashMap<>());
        sender.sendMessage(message);
    }
}