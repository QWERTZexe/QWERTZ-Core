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
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;

import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.DyeColor;
import org.bukkit.potion.PotionType;
import org.bukkit.potion.PotionData;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ConfigManager {
    private final QWERTZcore plugin;
    private final File configFile;
    private final File warpsFile; // New file for warps
    private final File kitsFile; // New file for kits (JSON - legacy)
    private final File kitsYamlFile; // New file for kits (YAML - new format)
    private final Gson gson;
    private Map<String, Object> config;
    private Set<String> keep;
    private Map<String, Map<String, Object>> warps; // Separate map for warps
    private Map<String, List<Map<String, Object>>> kits; // Separate map for kits (legacy JSON)
    private YamlConfiguration kitsYaml; // YAML configuration for new kit format
    public ConfigManager(QWERTZcore plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.json");
        this.warpsFile = new File(plugin.getDataFolder(), "warps.json"); // Initialize warps file
        this.kitsFile = new File(plugin.getDataFolder(), "kits.json");
        this.kitsYamlFile = new File(plugin.getDataFolder(), "kits.yml");
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Optional.class, new OptionalTypeAdapter())
                .enableComplexMapKeySerialization()
                .setPrettyPrinting()
                .create();
        this.config = new HashMap<>();
        this.warps = new HashMap<>();
        this.kits = new HashMap<>();
        this.kitsYaml = new YamlConfiguration();
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
        Set<String> keysToRemove = new HashSet<>(config.keySet()); // Track keys to remove
        keep = new HashSet<>();
        addDefault("spawn", null);
        addDefault("tpOnRevive", true);
        addDefault("tpOnUnrevive", true);
        addDefault("tpOnDeath", true);
        addDefault("tpOnJoin", true);
        addDefault("server", "My Server");
        addDefault("event", "Event");
        addDefault("sound", true);
        addDefault("soundEffect", "BLOCK_NOTE_BLOCK_PLING");
        addDefault("soundPitch", 1);
        addDefault("soundVolume", 100);
        addDefault("reviveTokensEnabled", true);
        addDefault("discord", QWERTZcore.DISCORD_LINK);
        addDefault("youtube", "https://youtube.com");
        addDefault("store", "https://yourstore.com");
        addDefault("tiktok", "https://tiktok.com");
        addDefault("twitch", "https://twitch.tv");
        addDefault("website", QWERTZcore.WEBSITE);
        addDefault("other", "https://example.com");
        addDefault("chat", true);
        addDefault("doScoreboard", true);
        addDefault("doTabList", true);
        addDefault("doChat", true);
        addDefault("checkForUpdates", true);
        addDefault("specialBlockOutput", false);
        addDefault("joinLeaveMsgsOnVanish", true);
        addDefault("suppressVanilla", true);
        addDefault("biggerMessages", true);
        addDefault("chatTimer", true);
         addDefault("reviveStaff", false);
         addDefault("emojis", true);
         addDefault("coloredChat", true);
         addDefault("allowRejoining", true);
         addDefault("rejoinTime", 30);

        // Garbage collect: Remove keys that are in the file but not defined as defaults
        System.out.print(keep);
        keysToRemove.removeAll(keep); // Remove all defaults that were already in the map
        for (String key : keysToRemove) {
            config.remove(key); // Remove the key if it's not a default
            plugin.getLogger().info("Removed deprecated config key: " + key);
        }

        saveConfig();
    }

    private void addDefault(String key, Object defaultValue) {
        keep.add(key);
        if (!config.containsKey(key)) {
            if (key.equals("spawn")) {
                setDefaultSpawnLocation();
            }
            else {
                config.put(key, defaultValue);
            }
        }
    }

    // Helper method to get the default world name from server.properties
    private String getDefaultWorldName() {
        File serverProperties = new File(plugin.getServer().getWorldContainer(), "server.properties");
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(serverProperties)) {
            props.load(fis);
            return props.getProperty("level-name", "world"); // Default to "world" if not found
        } catch (IOException e) {
            plugin.getLogger().warning("Could not read server.properties: " + e.getMessage());
            return "world"; // Default to "world" on error
        }
    }

    private void setDefaultSpawnLocation() {
        Map<String, Object> spawnMap = new HashMap<>();
        spawnMap.put("world", getDefaultWorldName());
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

    public void set(String key, Object value) {
        config.put(key, value);
        saveConfig();
    }
    private void loadKits() {
        // Load YAML kits first (new format)
        if (kitsYamlFile.exists()) {
            try {
                kitsYaml = YamlConfiguration.loadConfiguration(kitsYamlFile);
                plugin.getLogger().info("Loaded YAML kits from kits.yml");
            } catch (Exception e) {
                plugin.getLogger().warning("Could not load YAML kits file: " + e.getMessage());
                kitsYaml = new YamlConfiguration();
            }
        } else {
            kitsYaml = new YamlConfiguration();
        }

        // Load legacy JSON kits for backward compatibility
        if (kitsFile.exists()) {
            try (Reader reader = new FileReader(kitsFile)) {
                kits = gson.fromJson(
                        reader, new TypeToken<Map<String, List<Map<String, Object>>>>() {}.getType());
                if (kits != null) {
                    plugin.getLogger().info("Loaded legacy JSON kits from kits.json");
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Could not load legacy kits file: " + e.getMessage());
                kits = new HashMap<>();
            }
        } else {
            kits = new HashMap<>();
        }
    }

    private void saveKitsYaml() {
        try {
            kitsYaml.save(kitsYamlFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save YAML kits file: " + e.getMessage());
        }
    }

    private void saveKits() {
        // Keep legacy method for backward compatibility, but it's no longer used for new kits
        try (Writer writer = new FileWriter(kitsFile)) {
            gson.toJson(kits, writer);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save legacy kits file: " + e.getMessage());
        }
    }

    public void saveKit(String kitName, List<ItemStack> items) {
        // Save using YAML format with Bukkit's built-in ItemStack serialization
        try {
            // Create a map to store the kit data
            Map<String, Object> kitData = new HashMap<>();
            
            // Store items by slot
            for (int i = 0; i < items.size(); i++) {
                ItemStack item = items.get(i);
                if (item != null) {
                    // Use Bukkit's built-in serialization
                    kitData.put("slot" + i, item.serialize());
                }
            }
            
            // Save to YAML
            kitsYaml.set("kits." + kitName, kitData);
            saveKitsYaml();
            
            plugin.getLogger().info("Saved kit '" + kitName + "' in YAML format");
        } catch (Exception e) {
            plugin.getLogger().warning("Error saving kit " + kitName + " in YAML format: " + e.getMessage());
            // Fallback to legacy JSON format
            saveKitLegacy(kitName, items);
        }
    }

    private void saveKitLegacy(String kitName, List<ItemStack> items) {
        // Legacy JSON saving method for fallback
        List<Map<String, Object>> serializedItems = new ArrayList<>();

        for (ItemStack item : items) {
            try {
                if (item != null) {
                    Map<String, Object> serializedItem = serializeItemStack(item);
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
        // First try to load from YAML format (new format)
        if (kitsYaml.contains("kits." + kitName)) {
            try {
                Map<String, Object> kitData = kitsYaml.getConfigurationSection("kits." + kitName).getValues(false);
                List<ItemStack> items = new ArrayList<>();
                
                // Create a list with 41 slots (36 main inventory + 4 armor + 1 offhand)
                for (int i = 0; i < 41; i++) {
                    items.add(null);
                }
                
                // Load items from their slots
                for (Map.Entry<String, Object> entry : kitData.entrySet()) {
                    if (entry.getKey().startsWith("slot")) {
                        try {
                            String slotStr = entry.getKey().substring(4); // Remove "slot" prefix
                            int slot = Integer.parseInt(slotStr);
                            if (slot >= 0 && slot < 41) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> itemData = (Map<String, Object>) entry.getValue();
                                ItemStack item = ItemStack.deserialize(itemData);
                                items.set(slot, item);
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Error deserializing item in slot " + entry.getKey() + " for kit " + kitName + ": " + e.getMessage());
                        }
                    }
                }
                
                return items;
            } catch (Exception e) {
                plugin.getLogger().warning("Error loading kit " + kitName + " from YAML: " + e.getMessage());
            }
        }
        
        // Fallback to legacy JSON format
        List<Map<String, Object>> serializedItems = kits.get(encodeString(kitName));
        if (serializedItems == null) {
            return null;
        }

        List<ItemStack> items = new ArrayList<>();
        for (Map<String, Object> serializedItem : serializedItems) {
            if (serializedItem != null) {
                try {
                    items.add(deserializeItemStack(serializedItem));
                } catch (Exception e) {
                    plugin.getLogger().warning("Error deserializing item in kit " + kitName + ": " + e.getMessage());
                    items.add(null);
                }
            } else {
                items.add(null);
            }
        }
        return items;
    }

    public void deleteKit(String kitName) {
        // Try to delete from YAML first
        if (kitsYaml.contains("kits." + kitName)) {
            kitsYaml.set("kits." + kitName, null);
            saveKitsYaml();
            plugin.getLogger().info("Deleted kit '" + kitName + "' from YAML format");
            return;
        }
        
        // Fallback to legacy JSON deletion
        kits.remove(encodeString(kitName)); // Encode kit name for removal
        saveKits();
    }

    public Set<String> getKitNames() {
        Set<String> kitNames = new HashSet<>();
        
        // Get kit names from YAML format
        if (kitsYaml.contains("kits")) {
            try {
                for (String key : kitsYaml.getConfigurationSection("kits").getKeys(false)) {
                    kitNames.add(key);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error getting kit names from YAML: " + e.getMessage());
            }
        }
        
        // Get kit names from legacy JSON format
        Set<String> encodedNames = kits.keySet();
        for (String encodedName : encodedNames) {
            kitNames.add(decodeString(encodedName));
        }

        return kitNames;
    }

    // Helper method to encode strings using Base64
    private String encodeString(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_16));
    }

    // Helper method to decode strings from Base64
    private String decodeString(String input) {
        return new String(Base64.getDecoder().decode(input), StandardCharsets.UTF_16);
    }

    // Enhanced item serialization that preserves all item data
    private Map<String, Object> serializeItemStack(ItemStack item) {
        Map<String, Object> serialized = new HashMap<>();

        // Basic item data
        serialized.put("type", item.getType().name());
        serialized.put("amount", item.getAmount());
        serialized.put("durability", item.getDurability());

        // ItemMeta data
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            Map<String, Object> metaData = new HashMap<>();

            // Display name
            if (meta.hasDisplayName()) {
                metaData.put("displayName", meta.getDisplayName());
            }

            // Lore
            if (meta.hasLore()) {
                metaData.put("lore", meta.getLore());
            }

                         // Enchantments
             if (meta.hasEnchants()) {
                 Map<String, Object> enchants = new HashMap<>();
                 for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                     enchants.put(entry.getKey().getName(), entry.getValue());
                 }
                 metaData.put("enchants", enchants);
             }

            // Leather armor color
            if (meta instanceof LeatherArmorMeta) {
                LeatherArmorMeta leatherMeta = (LeatherArmorMeta) meta;
                if (leatherMeta.getColor() != null) {
                    Map<String, Integer> color = new HashMap<>();
                    color.put("red", leatherMeta.getColor().getRed());
                    color.put("green", leatherMeta.getColor().getGreen());
                    color.put("blue", leatherMeta.getColor().getBlue());
                    metaData.put("leatherColor", color);
                }
            }
                         // Potion data
             if (meta instanceof PotionMeta) {
                 PotionMeta potionMeta = (PotionMeta) meta;
                 // Save base potion data using deprecated API (suppressed)
                 @SuppressWarnings("all")
                 PotionData basePotionData = potionMeta.getBasePotionData();
                 if (basePotionData != null) {
                     Map<String, Object> potionInfo = new HashMap<>();
                     potionInfo.put("type", basePotionData.getType().name());
                     potionInfo.put("extended", basePotionData.isExtended());
                     potionInfo.put("upgraded", basePotionData.isUpgraded());
                     metaData.put("potionData", potionInfo);
                 }
                 // Save custom effects as PotionEffect objects
                 if (potionMeta.hasCustomEffects()) {
                     List<org.bukkit.potion.PotionEffect> customEffects = new ArrayList<>();
                     for (org.bukkit.potion.PotionEffect effect : potionMeta.getCustomEffects()) {
                         customEffects.add(effect);
                     }
                     metaData.put("customEffects", customEffects);
                 }
                 // Save color if present
                 if (potionMeta.hasColor()) {
                     Color color = potionMeta.getColor();
                     Map<String, Integer> colorData = new HashMap<>();
                     colorData.put("red", color.getRed());
                     colorData.put("green", color.getGreen());
                     colorData.put("blue", color.getBlue());
                     metaData.put("potionColor", colorData);
                 }
             }

            // Book data
            if (meta instanceof BookMeta) {
                BookMeta bookMeta = (BookMeta) meta;
                if (bookMeta.hasTitle()) {
                    metaData.put("bookTitle", bookMeta.getTitle());
                }
                if (bookMeta.hasAuthor()) {
                    metaData.put("bookAuthor", bookMeta.getAuthor());
                }
                if (bookMeta.hasPages()) {
                    metaData.put("bookPages", bookMeta.getPages());
                }
                metaData.put("bookGeneration", bookMeta.getGeneration() != null ? bookMeta.getGeneration().name() : null);
            }

            // Skull data
            if (meta instanceof SkullMeta) {
                SkullMeta skullMeta = (SkullMeta) meta;
                if (skullMeta.hasOwner()) {
                    metaData.put("skullOwner", skullMeta.getOwner());
                }
            }

            // Banner data
            if (meta instanceof BannerMeta) {
                BannerMeta bannerMeta = (BannerMeta) meta;
                if (bannerMeta.getPatterns() != null && !bannerMeta.getPatterns().isEmpty()) {
                    List<Map<String, String>> patterns = new ArrayList<>();
                    for (Pattern pattern : bannerMeta.getPatterns()) {
                        Map<String, String> patternData = new HashMap<>();
                        patternData.put("type", pattern.getPattern().name());
                        patternData.put("color", pattern.getColor().name());
                        patterns.add(patternData);
                    }
                    metaData.put("bannerPatterns", patterns);
                }
            }

            // Firework data
            if (meta instanceof FireworkMeta) {
                FireworkMeta fireworkMeta = (FireworkMeta) meta;
                if (fireworkMeta.hasEffects()) {
                    List<Map<String, Object>> effects = new ArrayList<>();
                    for (org.bukkit.FireworkEffect effect : fireworkMeta.getEffects()) {
                        Map<String, Object> effectData = new HashMap<>();
                        effectData.put("type", effect.getType().name());
                        effectData.put("flicker", effect.hasFlicker());
                        effectData.put("trail", effect.hasTrail());

                        List<String> colors = new ArrayList<>();
                        for (Color color : effect.getColors()) {
                            colors.add(color.asRGB() + "");
                        }
                        effectData.put("colors", colors);

                        List<String> fadeColors = new ArrayList<>();
                        for (Color color : effect.getFadeColors()) {
                            fadeColors.add(color.asRGB() + "");
                        }
                        effectData.put("fadeColors", fadeColors);

                        effects.add(effectData);
                    }
                    metaData.put("fireworkEffects", effects);
                }
                metaData.put("fireworkPower", fireworkMeta.getPower());
            }

            // Compass data
            if (meta instanceof CompassMeta) {
                CompassMeta compassMeta = (CompassMeta) meta;
                if (compassMeta.hasLodestone()) {
                    Map<String, Object> lodestoneData = new HashMap<>();
                    lodestoneData.put("world", compassMeta.getLodestone().getWorld().getName());
                    lodestoneData.put("x", compassMeta.getLodestone().getX());
                    lodestoneData.put("y", compassMeta.getLodestone().getY());
                    lodestoneData.put("z", compassMeta.getLodestone().getZ());
                    metaData.put("lodestone", lodestoneData);
                }
                metaData.put("tracking", compassMeta.isLodestoneTracked());
            }

            // Map data
            if (meta instanceof MapMeta) {
                MapMeta mapMeta = (MapMeta) meta;
                if (mapMeta.hasMapId()) {
                    metaData.put("mapId", mapMeta.getMapId());
                }
                if (mapMeta.hasLocationName()) {
                    metaData.put("locationName", mapMeta.getLocationName());
                }
                metaData.put("scaling", mapMeta.isScaling());
            }

            // Custom model data
            if (meta.hasCustomModelData()) {
                metaData.put("customModelData", meta.getCustomModelData());
            }

            // Unbreakable
            if (meta.isUnbreakable()) {
                metaData.put("unbreakable", true);
            }

            // Hide flags
            if (meta.getItemFlags() != null && !meta.getItemFlags().isEmpty()) {
                List<String> hideFlags = new ArrayList<>();
                for (org.bukkit.inventory.ItemFlag flag : meta.getItemFlags()) {
                    hideFlags.add(flag.name());
                }
                metaData.put("hideFlags", hideFlags);
            }

            // Note: Localized name methods are deprecated and removed

            serialized.put("meta", metaData);
        }

        return serialized;
    }

    // Enhanced item deserialization that restores all item data
    private ItemStack deserializeItemStack(Map<String, Object> serialized) {
        String typeName = (String) serialized.get("type");
        int amount = ((Number) serialized.get("amount")).intValue();
        short durability = ((Number) serialized.get("durability")).shortValue();

        ItemStack item = new ItemStack(org.bukkit.Material.valueOf(typeName), amount);
        item.setDurability(durability);

        // Restore ItemMeta data
        if (serialized.containsKey("meta")) {
            Map<String, Object> metaData = (Map<String, Object>) serialized.get("meta");
            ItemMeta meta = item.getItemMeta();

            // Display name
            if (metaData.containsKey("displayName")) {
                meta.setDisplayName((String) metaData.get("displayName"));
            }

            // Lore
            if (metaData.containsKey("lore")) {
                @SuppressWarnings("unchecked")
                List<String> lore = (List<String>) metaData.get("lore");
                meta.setLore(lore);
            }

                         // Enchantments
             if (metaData.containsKey("enchants")) {
                 @SuppressWarnings("unchecked")
                 Map<String, Object> enchants = (Map<String, Object>) metaData.get("enchants");
                 for (Map.Entry<String, Object> entry : enchants.entrySet()) {
                     Enchantment enchant = Enchantment.getByName(entry.getKey());
                     if (enchant != null) {
                         // Handle both Integer and Double values
                         int level;
                         if (entry.getValue() instanceof Integer) {
                             level = (Integer) entry.getValue();
                         } else if (entry.getValue() instanceof Double) {
                             level = ((Double) entry.getValue()).intValue();
                         } else {
                             level = ((Number) entry.getValue()).intValue();
                         }
                         meta.addEnchant(enchant, level, true);
                     }
                 }
             }

            // Leather armor color
            if (metaData.containsKey("leatherColor") && meta instanceof LeatherArmorMeta) {
                @SuppressWarnings("unchecked")
                Map<String, Integer> colorData = (Map<String, Integer>) metaData.get("leatherColor");
                LeatherArmorMeta leatherMeta = (LeatherArmorMeta) meta;
                Color color = Color.fromRGB(colorData.get("red"), colorData.get("green"), colorData.get("blue"));
                leatherMeta.setColor(color);
                meta = leatherMeta;
            }

                                                 // Potion data
             if (metaData.containsKey("customEffects") && meta instanceof PotionMeta) {
                 PotionMeta potionMeta = (PotionMeta) meta;
                 
                 // Restore base potion data using deprecated API (suppressed)
                 if (metaData.containsKey("potionData")) {
                     @SuppressWarnings("unchecked")
                     Map<String, Object> potionData = (Map<String, Object>) metaData.get("potionData");
                     try {
                         String potionTypeName = (String) potionData.get("type");
                         boolean extended = (Boolean) potionData.get("extended");
                         boolean upgraded = (Boolean) potionData.get("upgraded");
                         
                         PotionType potionType = PotionType.valueOf(potionTypeName);
                         @SuppressWarnings("deprecation")
                         PotionData basePotionData = new PotionData(potionType, extended, upgraded);
                         potionMeta.setBasePotionData(basePotionData);
                     } catch (Exception e) {
                         plugin.getLogger().warning("Could not restore potion base data: " + e.getMessage());
                     }
                 }
                 
                 // Restore custom effects as PotionEffect objects
                 @SuppressWarnings("unchecked")
                 List<org.bukkit.potion.PotionEffect> customEffects = (List<org.bukkit.potion.PotionEffect>) metaData.get("customEffects");
                 for (org.bukkit.potion.PotionEffect effect : customEffects) {
                     potionMeta.addCustomEffect(effect, true);
                 }
                 
                 // Restore potion color
                 if (metaData.containsKey("potionColor")) {
                     @SuppressWarnings("unchecked")
                     Map<String, Integer> colorData = (Map<String, Integer>) metaData.get("potionColor");
                     Color color = Color.fromRGB(colorData.get("red"), colorData.get("green"), colorData.get("blue"));
                     potionMeta.setColor(color);
                 }
                 
                 meta = potionMeta;
             }

            // Custom model data
            if (metaData.containsKey("customModelData")) {
                meta.setCustomModelData(((Number) metaData.get("customModelData")).intValue());
            }

            // Unbreakable
            if (metaData.containsKey("unbreakable") && (Boolean) metaData.get("unbreakable")) {
                meta.setUnbreakable(true);
            }

            // Hide flags
            if (metaData.containsKey("hideFlags")) {
                @SuppressWarnings("unchecked")
                List<String> hideFlags = (List<String>) metaData.get("hideFlags");
                for (String flagName : hideFlags) {
                    try {
                        org.bukkit.inventory.ItemFlag flag = org.bukkit.inventory.ItemFlag.valueOf(flagName);
                        meta.addItemFlags(flag);
                    } catch (IllegalArgumentException e) {
                        // Skip invalid flags
                    }
                }
            }

            // Book data
            if (metaData.containsKey("bookTitle") && meta instanceof BookMeta) {
                BookMeta bookMeta = (BookMeta) meta;
                bookMeta.setTitle((String) metaData.get("bookTitle"));
                if (metaData.containsKey("bookAuthor")) {
                    bookMeta.setAuthor((String) metaData.get("bookAuthor"));
                }
                if (metaData.containsKey("bookPages")) {
                    @SuppressWarnings("unchecked")
                    List<String> pages = (List<String>) metaData.get("bookPages");
                    bookMeta.setPages(pages);
                }
                if (metaData.containsKey("bookGeneration") && metaData.get("bookGeneration") != null) {
                    bookMeta.setGeneration(org.bukkit.inventory.meta.BookMeta.Generation.valueOf((String) metaData.get("bookGeneration")));
                }
                meta = bookMeta;
            }

            // Skull data
            if (metaData.containsKey("skullOwner") && meta instanceof SkullMeta) {
                SkullMeta skullMeta = (SkullMeta) meta;
                skullMeta.setOwner((String) metaData.get("skullOwner"));
                meta = skullMeta;
            }

            // Banner data
            if (metaData.containsKey("bannerPatterns") && meta instanceof BannerMeta) {
                BannerMeta bannerMeta = (BannerMeta) meta;
                @SuppressWarnings("unchecked")
                List<Map<String, String>> patterns = (List<Map<String, String>>) metaData.get("bannerPatterns");
                for (Map<String, String> patternData : patterns) {
                    PatternType type = PatternType.valueOf(patternData.get("type"));
                    DyeColor color = DyeColor.valueOf(patternData.get("color"));
                    bannerMeta.addPattern(new Pattern(color, type));
                }
                meta = bannerMeta;
            }

            // Firework data
            if (metaData.containsKey("fireworkEffects") && meta instanceof FireworkMeta) {
                FireworkMeta fireworkMeta = (FireworkMeta) meta;
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> effects = (List<Map<String, Object>>) metaData.get("fireworkEffects");
                for (Map<String, Object> effectData : effects) {
                    org.bukkit.FireworkEffect.Type type = org.bukkit.FireworkEffect.Type.valueOf((String) effectData.get("type"));
                    boolean flicker = (Boolean) effectData.get("flicker");
                    boolean trail = (Boolean) effectData.get("trail");

                    @SuppressWarnings("unchecked")
                    List<String> colorStrings = (List<String>) effectData.get("colors");
                    List<Color> colors = new ArrayList<>();
                    for (String colorStr : colorStrings) {
                        colors.add(Color.fromRGB(Integer.parseInt(colorStr)));
                    }

                    @SuppressWarnings("unchecked")
                    List<String> fadeColorStrings = (List<String>) effectData.get("fadeColors");
                    List<Color> fadeColors = new ArrayList<>();
                    for (String colorStr : fadeColorStrings) {
                        fadeColors.add(Color.fromRGB(Integer.parseInt(colorStr)));
                    }

                    fireworkMeta.addEffect(org.bukkit.FireworkEffect.builder()
                            .with(type)
                            .flicker(flicker)
                            .trail(trail)
                            .withColor(colors)
                            .withFade(fadeColors)
                            .build());
                }
                if (metaData.containsKey("fireworkPower")) {
                    fireworkMeta.setPower(((Number) metaData.get("fireworkPower")).intValue());
                }
                meta = fireworkMeta;
            }

            // Compass data
            if (metaData.containsKey("lodestone") && meta instanceof CompassMeta) {
                CompassMeta compassMeta = (CompassMeta) meta;
                @SuppressWarnings("unchecked")
                Map<String, Object> lodestoneData = (Map<String, Object>) metaData.get("lodestone");
                String worldName = (String) lodestoneData.get("world");
                double x = ((Number) lodestoneData.get("x")).doubleValue();
                double y = ((Number) lodestoneData.get("y")).doubleValue();
                double z = ((Number) lodestoneData.get("z")).doubleValue();
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    compassMeta.setLodestone(new Location(world, x, y, z));
                }
                if (metaData.containsKey("tracking")) {
                    compassMeta.setLodestoneTracked((Boolean) metaData.get("tracking"));
                }
                meta = compassMeta;
            }

            // Map data
            if (metaData.containsKey("mapId") && meta instanceof MapMeta) {
                MapMeta mapMeta = (MapMeta) meta;
                int mapId = ((Number) metaData.get("mapId")).intValue();
                mapMeta.setMapId(mapId);
                if (metaData.containsKey("locationName")) {
                    mapMeta.setLocationName((String) metaData.get("locationName"));
                }
                if (metaData.containsKey("scaling")) {
                    mapMeta.setScaling((Boolean) metaData.get("scaling"));
                }
                meta = mapMeta;
            }

            // Note: Localized name methods are deprecated and removed

            item.setItemMeta(meta);
        }

        return item;
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


    public boolean isReviveTokensEnabled() {
        return (boolean) config.getOrDefault("reviveTokensEnabled", true);
    }
    public String getDiscordLink() {
        return (String) config.getOrDefault("discord", QWERTZcore.DISCORD_LINK);
    }
    public boolean getChat() {
        return (boolean) config.getOrDefault("chat", true);
    }

     public boolean getColoredChat() {
         return (boolean) config.getOrDefault("coloredChat", true);
     }

     public boolean getAllowRejoining() {
         return (boolean) config.getOrDefault("allowRejoining", true);
     }

     public int getRejoinTime() {
         return ((Number) config.getOrDefault("rejoinTime", 30)).intValue();
     }
}