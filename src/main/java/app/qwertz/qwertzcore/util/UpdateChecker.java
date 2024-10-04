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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {

    private final QWERTZcore plugin;
    private String latestVersion;
    private String downloadUrl;
    private boolean updateAvailable = false;

    public UpdateChecker(QWERTZcore plugin) {
        this.plugin = plugin;
        checkForUpdates();
    }

    public void checkForUpdates() {
        try {
            String currentVersion = QWERTZcore.VERSION;
            URL url = new URL("https://api.modrinth.com/v2/project/qwertz-core/version");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                JsonArray versions = JsonParser.parseReader(reader).getAsJsonArray();

                if (versions.size() > 0) {
                    JsonObject latestVersionObj = versions.get(0).getAsJsonObject();
                    latestVersion = latestVersionObj.get("version_number").getAsString();
                    downloadUrl = latestVersionObj.get("files").getAsJsonArray().get(0).getAsJsonObject().get("url").getAsString();

                    if (isNewerVersion(latestVersion, currentVersion)) {
                        updateAvailable = true;
                        plugin.getLogger().info("A new version of QWERTZcore is available: " + latestVersion);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to check for updates: " + e.getMessage());
        }
    }

    private boolean isNewerVersion(String latestVersion, String currentVersion) {
        String[] latest = latestVersion.split("\\.");
        String[] current = currentVersion.split("\\.");

        for (int i = 0; i < Math.min(latest.length, current.length); i++) {
            int l = Integer.parseInt(latest[i]);
            int c = Integer.parseInt(current[i]);
            if (l > c) return true;
            if (l < c) return false;
        }

        return latest.length > current.length;
    }

    public void notifyPlayer(Player player) {
        if (updateAvailable) {
            player.sendMessage(QWERTZcore.CORE_ICON + ChatColor.YELLOW + " A new version of " + ChatColor.GOLD + ChatColor.BOLD + "QWERTZ Core" + ChatColor.YELLOW + " is available: " + ChatColor.AQUA + latestVersion);
            player.sendMessage(QWERTZcore.CORE_ICON + ChatColor.YELLOW + " Download it from: " + ChatColor.AQUA + ChatColor.UNDERLINE + "https://modrinth.com/plugin/qwertz-core/version/" + latestVersion);
        }
    }
}