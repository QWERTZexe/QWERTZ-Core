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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ConfigManager {
    private final QWERTZcore plugin;
    private final File configFile;
    private final File warpsFile; // New file for warps
    private final File kitsFile; // New file for kits
    private final Gson gson;
    private Map<String, Object> config;
    private Map<String, Map<String, Object>> warps; // Separate map for warps
    private Map<String, List<Map<String, Object>>> kits; // Separate map for kits

    @Deprecated(since = "2.0", forRemoval = true)
    public static final String DEFAULT_FONT = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    @Deprecated(since = "2.0", forRemoval = true)
    public static final String QWERTZ_FONT = "ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ";
    @Deprecated(since = "2.0", forRemoval = true)
    public static final String MODERN_FONT = "ᴀʙᴄᴅᴇғɢʜɪᴊᴋʟᴍɴᴏᴘᴏʀsᴛᴜᴠᴡxʏᴢᴀʙᴄᴅᴇғɢʜɪᴊᴋʟᴍɴᴏᴘᴏʀsᴛᴜᴠᴡxʏᴢ";
    @Deprecated(since = "2.0", forRemoval = true)
    public static final String BLOCKY_FONT = "🄰🄱🄲🄳🄴🄵🄶🄷🄸🄹🄺🄻🄼🄽🄾🄿🅀🅁🅂🅃🅄🅅🅆🅇🅈🅉🄰🄱🄲🄳🄴🄵🄶🄷🄸🄹🄺🄻🄼🄽🄾🄿🅀🅁🅂🅃🅄🅅🅆🅇🅈🅉";

    public ConfigManager(QWERTZcore plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.json");
        this.warpsFile = new File(plugin.getDataFolder(), "warps.json"); // Initialize warps file
        this.kitsFile = new File(plugin.getDataFolder(), "kits.json");
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Optional.class, new OptionalTypeAdapter())
                .enableComplexMapKeySerialization()
                .setPrettyPrinting()
                .create();
        this.config = new HashMap<>();
        this.warps = new HashMap<>();
        this.kits = new HashMap<>();
        ensurePluginFolder();
        loadConfig();
        loadWarps();
        loadKits();
    }


    public void loadConfig() {
        if (!configFile.exists()) {
            createDefaultConfig();
        }

        try (Reader reader = new FileReader(configFile)) {
            config = gson.fromJson(reader, Map.class);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not load config file: " + e.getMessage());
            createDefaultConfig();
        }

        if (config == null) {
            config = new HashMap<>();
        }

        // Ensure all required settings exist
        ensureConfigDefaults();
    }
    public void loadWarps() {
        if (!warpsFile.exists()) {
            saveWarps(); // Create an empty warps file if it doesn't exist
        }

        try (Reader reader = new FileReader(warpsFile)) {
            warps = gson.fromJson(reader, Map.class);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not load warps file: " + e.getMessage());
            saveWarps();
        }

        if (warps == null) {
            warps = new HashMap<>();
        }
    }
    private void createDefaultConfig() {
        config = new HashMap<>();
        ensureConfigDefaults();
        saveConfig();
        plugin.getLogger().info("Created default config.json file");
    }


    private void ensureConfigDefaults() {

        if (!config.containsKey("spawn")) {
            setDefaultSpawnLocation();
        }
        if (!config.containsKey("tpOnRevive")) {
            config.put("tpOnRevive", true);
        }
        if (!config.containsKey("tpOnUnrevive")) {
            config.put("tpOnUnrevive", true);
        }
        if (!config.containsKey("tpOnDeath")) {
            config.put("tpOnDeath", true);
        }
        if (!config.containsKey("tpOnJoin")) {
            config.put("tpOnJoin", true);
        }
        if (!config.containsKey("server")) {
            config.put("server", "My Server");
        }
        if (!config.containsKey("event")) {
            config.put("event", "Event");
        }
        if (!config.containsKey("scoreboardUpperCase")) {
            config.put("scoreboardUpperCase", true);
        }
        if (!config.containsKey("font")) {
            config.put("font", "modern");
        }
        if (!config.containsKey("sound")) {
            config.put("sound", true);
        }
        if (!config.containsKey("soundEffect")) {
            config.put("soundEffect", "BLOCK_NOTE_BLOCK_PLING");
        }
        if (!config.containsKey("soundPitch")) {
            config.put("soundPitch", 1);
        }
        if (!config.containsKey("soundVolume")) {
            config.put("soundVolume", 100);
        }
        if (!config.containsKey("reviveTokensEnabled")) {
            config.put("reviveTokensEnabled", true);
        }
        if (!config.containsKey("discord")) {
            config.put("discord", QWERTZcore.DISCORD_LINK);
        }
        if (!config.containsKey("youtube")) {
            config.put("youtube", "https://youtube.com");
        }
        if (!config.containsKey("store")) {
            config.put("store", "https://yourstore.com");
        }
        if (!config.containsKey("tiktok")) {
            config.put("tiktok", "https://tiktok.com");
        }
        if (!config.containsKey("twitch")) {
            config.put("twitch", "https://twitch.tv");
        }
        if (!config.containsKey("website")) {
            config.put("website", QWERTZcore.WEBSITE);
        }
        if (!config.containsKey("other")) {
            config.put("other", "https://example.com");
        }
        if (!config.containsKey("chat")) {
            config.put("chat", true);
        }
        if (!config.containsKey("doScoreboard")) {
            config.put("doScoreboard", true);
        }
        if (!config.containsKey("doTabList")) {
            config.put("doTabList", true);
        }
        if (!config.containsKey("doChat")) {
            config.put("doChat", true);
        }
        if (!config.containsKey("checkForUpdates")) {
            config.put("checkForUpdates", true);
        }
        if (!config.containsKey("specialBlockOutput")) {
            config.put("specialBlockOutput", false);
        }
        if (!config.containsKey("joinLeaveMsgsOnVanish")) {
            config.put("joinLeaveMsgsOnVanish", true);
        }
        if (!config.containsKey("suppressVanilla")) {
            config.put("suppressVanilla", true);
        }
        if (!config.containsKey("biggerMessages")) {
            config.put("biggerMessages", true);
        }
        if (!config.containsKey("chatTimer")) {
            config.put("chatTimer", true);
        }
        if (!config.containsKey("reviveStaff")) {
            config.put("reviveStaff", false);
        }
        if (!config.containsKey("emojis")) {
            config.put("emojis", true);
        }
    }


    private void setDefaultSpawnLocation() {
        Map<String, Object> spawnMap = new HashMap<>();
        spawnMap.put("world", "world");
        spawnMap.put("x", 0.0);
        spawnMap.put("y", 64.0);
        spawnMap.put("z", 0.0);
        spawnMap.put("yaw", 0.0f);
        spawnMap.put("pitch", 0.0f);
        config.put("spawn", spawnMap);
        saveConfig();
    }

    public void saveConfig() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
            gson.toJson(config, writer);
            writer.flush();
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save config file: " + e.getMessage());
        }
    }


    public Location getSpawnLocation() {
        Map<String, Object> spawnMap = (Map<String, Object>) config.get("spawn");
        if (spawnMap != null) {
            World world = Bukkit.getWorld((String) spawnMap.get("world"));
            double x = ((Number) spawnMap.get("x")).doubleValue();
            double y = ((Number) spawnMap.get("y")).doubleValue();
            double z = ((Number) spawnMap.get("z")).doubleValue();
            float yaw = ((Number) spawnMap.get("yaw")).floatValue();
            float pitch = ((Number) spawnMap.get("pitch")).floatValue();
            return new Location(world, x, y, z, yaw, pitch);
        }
        return plugin.getServer().getWorlds().get(0).getSpawnLocation();
    }

    public boolean getTpOnRevive() {
        return (boolean) config.getOrDefault("tpOnRevive", true);
    }

    private void ensurePluginFolder() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getLogger().info("Creating plugin folder: " + plugin.getDataFolder().getPath());
            if (plugin.getDataFolder().mkdirs()) {
                plugin.getLogger().info("Plugin folder created successfully.");
            } else {
                plugin.getLogger().warning("Failed to create plugin folder.");
            }
        }
    }

    public Set<String> getKeys() {
        return config.keySet();
    }

    public boolean hasKey(String key) {
        return config.containsKey(key);
    }

    public Object get(String key) {
        return config.get(key);
    }

    @Deprecated(since = "2.0", forRemoval = true)
    public String getColor(String key) {
        String unformatted = (String) config.get(key);
        if (unformatted == null) {
            unformatted = "§a";
        }
        unformatted = unformatted.replace("&", "§");
        StringBuilder colorCodes = new StringBuilder();

        for (int i = 0; i < unformatted.length(); i++) {
            if (unformatted.charAt(i) == '§' && i + 1 < unformatted.length()) {
                char colorChar = unformatted.charAt(i + 1);
                if (isValidColorCode(colorChar)) {
                    colorCodes.append('§').append(colorChar);
                }
                i++; // Skip the next character as it is part of the color code
            }
        }

        return colorCodes.toString();
    }

    private boolean isValidColorCode(char c) {
        return "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(c) > -1;
    }

    public void set(String key, Object value) {
        config.put(key, value);
        saveConfig();
    }
    private void loadKits() {
        if (!kitsFile.exists()) {
            saveKits();
            return;
        }

        try (Reader reader = new FileReader(kitsFile)) {
            kits =
                    gson.fromJson(
                            reader, new TypeToken<Map<String, List<Map<String, Object>>>>() {}.getType());
        } catch (IOException e) {
            plugin.getLogger().warning("Could not load kits file: " + e.getMessage());
            kits = new HashMap<>();
        }
    }

    private void saveKits() {
        try (Writer writer = new FileWriter(kitsFile)) {
            gson.toJson(kits, writer);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save kits file: " + e.getMessage());
        }
    }

    public void saveKit(String kitName, List<ItemStack> items) {
        List<Map<String, Object>> serializedItems = new ArrayList<>();

        for (ItemStack item : items) {
            try {
                if (item != null) {
                    Map<String, Object> serializedItem = encodeStrings(item.serialize());
                    serializedItems.add(serializedItem);
                } else {
                    serializedItems.add(null);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error serializing item in kit " + kitName + ": " + e.getMessage());
                serializedItems.add(null);
            }
        }

        kits.put(encodeString(kitName), serializedItems); // Encode kit name
        saveKits();
    }

    public List<ItemStack> getKit(String kitName) {
        List<Map<String, Object>> serializedItems = kits.get(encodeString(kitName)); // Encode kit name for lookup

        if (serializedItems == null) {
            return null;
        }

        List<ItemStack> items = new ArrayList<>();
        for (Map<String, Object> serializedItem : serializedItems) {
            if (serializedItem != null) {
                items.add(ItemStack.deserialize(decodeStrings(serializedItem)));
            } else {
                items.add(null);
            }
        }
        return items;
    }

    public void deleteKit(String kitName) {
        kits.remove(encodeString(kitName)); // Encode kit name for removal
        saveKits();
    }

    public Set<String> getKitNames() {
        Set<String> encodedNames = kits.keySet();
        Set<String> decodedNames = new HashSet<>();

        for (String encodedName : encodedNames) {
            decodedNames.add(decodeString(encodedName));
        }

        return decodedNames;
    }

    // Helper method to encode strings using Base64
    private String encodeString(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_16));
    }

    // Helper method to decode strings from Base64
    private String decodeString(String input) {
        return new String(Base64.getDecoder().decode(input), StandardCharsets.UTF_16);
    }

    // Helper method to encode all string values in a map
    private Map<String, Object> encodeStrings(Map<String, Object> map) {
        Map<String, Object> encodedMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof String) {
                encodedMap.put(entry.getKey(), encodeString((String) entry.getValue()));
            } else {
                encodedMap.put(entry.getKey(), entry.getValue());
            }
        }

        return encodedMap;
    }

    // Helper method to decode all string values in a map
    private Map<String, Object> decodeStrings(Map<String, Object> map) {
        Map<String, Object> decodedMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof String) {
                decodedMap.put(entry.getKey(), decodeString((String) entry.getValue()));
            } else {
                decodedMap.put(entry.getKey(), entry.getValue());
            }
        }

        return decodedMap;
    }
    public void saveWarps() {
        try (Writer writer = new FileWriter(warpsFile)) {
            gson.toJson(warps, writer);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save warps file: " + e.getMessage());
        }
    }

    public void addWarp(String name, Location location) {
        Map<String, Object> warpMap = new HashMap<>();
        warpMap.put("world", location.getWorld().getName());
        warpMap.put("x", location.getX());
        warpMap.put("y", location.getY());
        warpMap.put("z", location.getZ());
        warpMap.put("yaw", location.getYaw());
        warpMap.put("pitch", location.getPitch());

        warps.put(name, warpMap);
        saveWarps(); // Save changes to the separate warps file
    }

    public void removeWarp(String name) {
        warps.remove(name);
        saveWarps(); // Save changes to the separate warps file
    }

    public Location getWarp(String name) {
        Map<String, Object> warpMap = warps.get(name);
        if (warpMap != null) {
            World world = Bukkit.getWorld((String) warpMap.get("world"));
            double x = ((Number) warpMap.get("x")).doubleValue();
            double y = ((Number) warpMap.get("y")).doubleValue();
            double z = ((Number) warpMap.get("z")).doubleValue();
            float yaw = ((Number) warpMap.get("yaw")).floatValue();
            float pitch = ((Number) warpMap.get("pitch")).floatValue();
            return new Location(world, x, y, z, yaw, pitch);
        }
        return null;
    }

    public Set<String> getWarpNames() {
        return warps.keySet();
    }
    public String getServerName() {
        return (String) config.getOrDefault("server", "My Server");
    }

    public String getEventName() {
        return (String) config.getOrDefault("event", "Event");
    }
    public boolean getTpOnUnrevive() {
        return (boolean) config.getOrDefault("tpOnUnrevive", true);
    }

    public boolean getMsgsOnVanish() {
        return (boolean) config.getOrDefault("joinLeaveMsgsOnVanish", true);
    }

    public boolean getTpOnDeath() {
        return (boolean) config.getOrDefault("tpOnDeath", true);
    }

    public boolean getTpOnJoin() {
        return (boolean) config.getOrDefault("tpOnJoin", true);
    }
    public boolean getScoreboardUpperCase() {
        return (boolean) config.getOrDefault("scoreboardUpperCase", true);
    }
    public boolean getScoreBoard() {
        return (boolean) config.getOrDefault("doScoreboard", true);
    }

    public boolean getTabList() {
        return (boolean) config.getOrDefault("doTabList", true);
    }

    public boolean getChatFormatting() {
        return (boolean) config.getOrDefault("doChat", true);
    }
    public String getFont() {
        return (String) config.getOrDefault("font", "modern");
    }
    public String getConfigSound() {
        return (String) config.getOrDefault("soundEffect", "BLOCK_NOTE_BLOCK_PLING");
    }
    public double getSoundVolume() {
        return (double) config.getOrDefault("soundVolume", 100);
    }
    public double getSoundPitch() {
        return (double) config.getOrDefault("soundPitch", 1);
    }
    public boolean playSounds() {
        return (boolean) config.getOrDefault("sound", true);
    }

    @Deprecated(since = "2.0", forRemoval = true)
    public String getFontString() {
        String fontType = getFont();
        switch (fontType.toLowerCase()) {
            case "qwertz":
                return QWERTZ_FONT;
            case "modern":
                return MODERN_FONT;
            case "blocky":
                return BLOCKY_FONT;
            default:
                return DEFAULT_FONT;
        }
    }

    @Deprecated(since = "2.0", forRemoval = true)
    public String formatScoreboardText(String text) {
        boolean upperCase = getScoreboardUpperCase();
        String fontString = getFontString();

        if (upperCase) {
            text = text.toUpperCase();
        }

        if (fontString.equals(DEFAULT_FONT)) {
            return text; // No conversion needed for default font
        }

        StringBuilder formattedText = new StringBuilder();
        for (char c : text.toCharArray()) {
            int index = DEFAULT_FONT.indexOf(c);
            if (index != -1) {
                if (fontString.equals(BLOCKY_FONT)) {
                    // For blocky font and for Math, we need to handle surrogate pairs
                    formattedText.append(fontString.substring(index * 2, (index * 2) + 2));
                } else {
                    formattedText.append(fontString.charAt(index));
                }
            } else {
                formattedText.append(c);
            }
        }

        return formattedText.toString();
    }

    public boolean isReviveTokensEnabled() {
        return (boolean) config.getOrDefault("reviveTokensEnabled", true);
    }
    public String getDiscordLink() {
        return (String) config.getOrDefault("discord", QWERTZcore.DISCORD_LINK);
    }
    public boolean getChat() {
        return (boolean) config.getOrDefault("chat", true);
    }
}