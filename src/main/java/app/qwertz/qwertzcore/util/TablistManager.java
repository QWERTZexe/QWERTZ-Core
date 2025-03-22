package app.qwertz.qwertzcore.util;

import app.qwertz.qwertzcore.QWERTZcore;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class TablistManager {
    private final QWERTZcore plugin;
    private FileConfiguration tabConfig;
    private FileConfiguration fileTabConfig;
    private FileConfiguration internalTabConfig;
    private final Map<Integer, String> pingColors = new TreeMap<>(Comparator.naturalOrder());
    private int tabTaskID = -1; // Store the task ID.  -1 indicates no task is running.

    public TablistManager(QWERTZcore plugin) {
        this.plugin = plugin;
        loadTabConfig();
        this.tabConfig = getConfigToUse();
        setupPingColors();
        setupPingColors();
        startTablistUpdater();
    }
    private void setupPingColors() {
        // Load ping colors
        ConfigurationSection pingSection = tabConfig.getConfigurationSection("ping-colors");
        if (pingSection != null) {
            pingSection.getKeys(false).forEach(key -> {
                try {
                    if (!key.equals("default")) {
                        int threshold = Integer.parseInt(key);
                        pingColors.put(threshold, pingSection.getString(key));
                    }
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid ping threshold in tab.yml: " + key);
                }
            });
        }
    }
    // Helper method to determine which config to use
    private FileConfiguration getConfigToUse() {
        String activeTheme = plugin.getMessageManager().messagesConfig.getString("active-theme");
        if (Objects.equals(activeTheme, "file")) {
            return fileTabConfig;
        } else if (Objects.equals(activeTheme, "internal")) {
            return internalTabConfig;
        } else {
            // Attempt to load from repo
            FileConfiguration repoConfig = plugin.getMessageManager().loadFromRepo(activeTheme, "tab");
            if (repoConfig != null) {
                return repoConfig;
            } else {
                plugin.getLogger().warning("Failed to load theme from repo, using internal.");
                return internalTabConfig; // Fallback to default
            }
        }
    }
    private void loadTabConfig() {
        File tabFile = new File(plugin.getDataFolder(), "tab.yml");
        if (!tabFile.exists()) {
            plugin.saveResource("tab.yml", false);
        }
        fileTabConfig = YamlConfiguration.loadConfiguration(tabFile);
        // Load default messages from JAR
        InputStream defaultStream = plugin.getResource("tab.yml");
        if (defaultStream != null) {
            internalTabConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
        }
    }

    public void updateTablist(Player player) {
        if (plugin.getConfigManager().getTabList()) {
            String header = buildHeaderFooter("header");
            String footer = buildHeaderFooter("footer");
            player.setPlayerListHeaderFooter(header, footer);
            updatePlayerListName(player);
        }
    }

    private String buildHeaderFooter(String path) {
        List<String> lines = tabConfig.getStringList(path);
        StringBuilder builder = new StringBuilder();

        for (String line : lines) {
            builder.append(prepareLine(line))
                    .append("\n");
        }
        return builder.toString();
    }

    private String prepareLine(String line) {
        return plugin.getMessageManager().prepareMessage(line, new HashMap<>()).replace("%server%", plugin.getConfigManager().getServerName())
                .replace("%event%", plugin.getConfigManager().getEventName())
                .replace("%discord%", plugin.getConfigManager().getDiscordLink())
                .replace("%player%", String.valueOf(plugin.getVanishManager().getNonVanishedPlayerCount()));
    }

    private void updatePlayerListName(Player player) {
        String format = tabConfig.getString("player-list-name", "%prefix%%player_name%%suffix% %ping_color%%ping%ms");
        String pingColor = getPingColor(player.getPing());

        String listName = format
                .replace("%prefix%", plugin.getRankManager().getPrefix(player))
                .replace("%suffix%", plugin.getRankManager().getSuffix(player))
                .replace("%player%", player.getName())
                .replace("%ping_color%", pingColor)
                .replace("%ping%", String.valueOf(player.getPing()));

        Scoreboard scoreboard = player.getScoreboard();
        Team team = scoreboard.getTeam(player.getName());
        if (team == null) {
            team = scoreboard.registerNewTeam(player.getName());
        }

        team.setPrefix(plugin.getRankManager().getPrefix(player));
        team.setSuffix(plugin.getRankManager().getSuffix(player) + " " + pingColor + player.getPing());
        team.addEntry(player.getName());

        player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', listName));
    }

    private String getPingColor(int ping) {
        for (Map.Entry<Integer, String> entry : pingColors.entrySet()) {
            if (ping <= entry.getKey()) {
                return entry.getValue();
            }
        }
        return tabConfig.getString("ping-colors.default", "&4");
    }

    private void startTablistUpdater() {
        stopTabUpdater();
        tabTaskID = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateTablist(player);
            }
        }, 20L, 20L).getTaskId();
    }

    public void stopTabUpdater() {
        if (tabTaskID != -1) {
            Bukkit.getScheduler().cancelTask(tabTaskID);
            tabTaskID = -1;
        }
    }
}
