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
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class MessageManager {
    private final QWERTZcore plugin;
    private final Map<UUID, UUID> lastMessageSender;
    public FileConfiguration messagesConfig;
    private File messagesFile;
    private FileConfiguration defaultMessagesConfig;
    private List<String> themes;
    // Fields to store cached configuration and last load time
    private FileConfiguration cachedRepoConfig = null;
    private long lastRepoLoadTime = 0;
    private static final long REPO_LOAD_INTERVAL = 60 * 5 * 1000; // 5 minutes in milliseconds


    public MessageManager(QWERTZcore plugin) {
        this.plugin = plugin;
        this.lastMessageSender = new HashMap<>();
        themes = new ArrayList<>();
        themes.add("internal");
        themes.add("file");
        loadMessages();

        // Fetch themes from GitHub
        fetchThemesFromGitHub();
    }

    private void fetchThemesFromGitHub() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL("https://api.github.com/repos/QWERTZexe/QWERTZ-Core/contents/themes");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

                if (conn.getResponseCode() != 200) {
                    throw new RuntimeException("HTTP error code: " + conn.getResponseCode());
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();
                conn.disconnect();

                Gson gson = new Gson();
                List<Map<String, Object>> items = gson.fromJson(response.toString(), new TypeToken<List<Map<String, Object>>>(){}.getType());

                for (Map<String, Object> item : items) {
                    if ("dir".equals(item.get("type"))) {
                        themes.add((String) item.get("name"));
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to fetch themes from GitHub: " + e.getMessage());
            }
        });
    }


    public List<String> getThemes() {
        return themes;
    }

    public void setTheme(String theme) {
        try {
            messagesConfig.set("active-theme", theme);
            messagesConfig.save(messagesFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        message = QWERTZcore.translateHexColorCodes(message);
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
            // Check if it's time to reload from the repo
            long currentTime = System.currentTimeMillis();
            if (cachedRepoConfig == null || (currentTime - lastRepoLoadTime > REPO_LOAD_INTERVAL)) {
                FileConfiguration repoConfig = loadFromRepo(activeTheme, "messages");
                if (repoConfig != null) {
                    cachedRepoConfig = repoConfig;
                    lastRepoLoadTime = currentTime; // Update last load time
                } else {
                    plugin.getLogger().warning("Failed to load theme from repo, using internal.");
                    cachedRepoConfig = defaultMessagesConfig; // Fallback to default
                }
            }
            return cachedRepoConfig;
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

    public void sendMessage(CommandSender recipent, String message, HashMap<String, String> localPlaceholders) {
        message = getMessage(message);
        message = prepareMessage(message, localPlaceholders);
        recipent.sendMessage(message);
    }

    public void sendMessage(Player recipent, String message, HashMap<String, String> localPlaceholders) {
        message = getMessage(message);
        message = prepareMessage(message, localPlaceholders);
        recipent.sendMessage(message);
    }

    public void sendMessage(CommandSender recipent, String message) {
        message = getMessage(message);
        message = prepareMessage(message, new HashMap<>());
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

    public void sendInvalidUsage(CommandSender recipent, String usage) {
        String message = "general.invalid-usage";
        message = getMessage(message);
        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%usage%", usage);
        message = prepareMessage(message, localMap);
        recipent.sendMessage(message);
    }

    public List<String> getStringList(String path) {
        FileConfiguration config = getConfigToUse();
        return config.getStringList(path);
    }
}