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

package app.qwertz.qwertzcore.gui;

import app.qwertz.qwertzcore.QWERTZcore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class ConfigGUI implements Listener {

    private static final UUID RANDOM_UUID = UUID.fromString("92864445-51c5-4c3b-9039-517c9927d1b4");
    private static final String LEFT_ARROW_URL = "http://textures.minecraft.net/texture/a2f0425d64fdc8992928d608109810c1251fe243d60d175bed427c651cbe";
    private static final String RIGHT_ARROW_URL = "http://textures.minecraft.net/texture/6d865aae2746a9b8e9a4fe629fb08d18d0a9251e5ccbe5fa7051f53eab9b94";
    private static final Map<UUID, PendingInput> pendingInputs = new HashMap<>();
    private final QWERTZcore plugin;
    private final Player player;
    private int page;
    private Inventory inventory;

    public ConfigGUI(QWERTZcore plugin, Player player, int page) {
        this.plugin = plugin;
        this.player = player;
        this.page = page;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.inventory = buildGUI(); // Build the GUI only once
    }

    public void open() {
        player.openInventory(inventory);
    }

    private Inventory buildGUI() {
        Inventory gui = Bukkit.createInventory(null, 54, "Config Editor");

        ItemStack pearl = createItem(Material.ENDER_PEARL, "§bConfig Menu");
        fillRow(gui, 0, pearl);
        fillRow(gui, 5, pearl);

        setupNavigation(gui);
        setupConfigItems(gui);

        return gui;
    }

    private void setupNavigation(Inventory gui) {
        boolean hasPrevious = page > 0;
        boolean hasNext = page < (int) Math.ceil(plugin.getConfigManager().getKeys().size() / 5.0) - 1;

        if (hasPrevious) {
            gui.setItem(18, createSkull(LEFT_ARROW_URL, "§aPrevious Page"));
            gui.setItem(27, createSkull(LEFT_ARROW_URL, "§aPrevious Page"));

        } else {
            gui.setItem(18, null); // Clear the slot if no previous page
            gui.setItem(27, null); // Clear the slot if no previous page
        }

        if (hasNext) {
            gui.setItem(26, createSkull(RIGHT_ARROW_URL, "§aNext Page"));
            gui.setItem(35, createSkull(RIGHT_ARROW_URL, "§aNext Page"));
        } else {
            gui.setItem(26, null); // Clear the slot if no next page
            gui.setItem(35, null); // Clear the slot if no next page
        }
    }

    private void setupConfigItems(Inventory gui) {
        int[] keySlots = {20, 21, 22, 23, 24};
        int[] valueSlots = {29, 30, 31, 32, 33};
        List<String> keys = new ArrayList<>(plugin.getConfigManager().getKeys());

        // Clear the item slots first to avoid displaying old config
        for (int i = 0; i < 5; i++) {
            if (keySlots.length > i) {
                gui.setItem(keySlots[i], null); // Clear Key Items
                gui.setItem(valueSlots[i], null); // Clear Value Items
            }
        }

        for (int i = 0; i < 5; i++) {
            int index = page * 5 + i;
            if (index >= keys.size()) break;

            String key = keys.get(index);
            Object value = plugin.getConfigManager().get(key);

            gui.setItem(keySlots[i], createKeyItem(key));
            gui.setItem(valueSlots[i], createValueItem(key, value));
        }
    }

    private void handleNavigationClick(int slot) {
        if ((slot == 18 || slot == 27) && page > 0) { // Left arrow
            this.page--;
            updateGUI();
        } else if ((slot == 26 || slot == 35) && page < (int) Math.ceil(plugin.getConfigManager().getKeys().size() / 5.0) - 1) { // Right arrow
            this.page++;
            updateGUI();
        }
    }

    // Method to update the GUI contents
    private void updateGUI() {
        setupNavigation(inventory);
        setupConfigItems(inventory);
        player.updateInventory(); // Refresh the inventory for the player
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Config Editor") && event.getInventory().equals(inventory)) {
            event.setCancelled(true);

            if (!(event.getWhoClicked() instanceof Player)) return;

            ItemStack item = event.getCurrentItem();
            if (item == null) return;

            handleEnderPearlAnimation(inventory);
            handleNavigationClick(event.getSlot());
            handleConfigItemClick(event.getSlot());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals("Config Editor") && event.getInventory().equals(inventory)) {
            ConfigGUI.unregister(this);
        }
    }

    public static void unregister(ConfigGUI gui) {
        HandlerList.unregisterAll(gui);
    }

    private void handleConfigItemClick(int slot) {
        int[] valueSlots = {29, 30, 31, 32, 33};
        for (int i = 0; i < valueSlots.length; i++) {
            if (slot == valueSlots[i]) {
                int keyIndex = page * 5 + i;
                List<String> keys = new ArrayList<>(plugin.getConfigManager().getKeys());
                if (keyIndex >= keys.size()) return;

                String key = keys.get(keyIndex);
                Object value = plugin.getConfigManager().get(key);

                if (value instanceof Boolean) {
                    plugin.getConfigManager().set(key, !(Boolean) value);
                    plugin.getConfigManager().saveConfig();
                    updateGUI();
                } else if (value instanceof Number || value instanceof String) {
                    promptForInput(key);
                } else {
                    plugin.getMessageManager().sendMessage(player, "configgui.unsupported-type");
                }
            }
        }
    }

    private void promptForInput(String key) {
        player.closeInventory();
        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%key%", key);
        plugin.getMessageManager().sendMessage(player, "configgui.enter-new-value", localMap);
        pendingInputs.put(player.getUniqueId(), new PendingInput(key, page));
    }

    private ItemStack createKeyItem(String key) {
        Material mat = switch (key.toLowerCase()) {
            case "spawn" -> Material.COMPASS;
            case "sound", "soundvolume", "soundpitch", "soundeffect" -> Material.NOTE_BLOCK;
            case "server", "event" -> Material.NAME_TAG;
            case "specialblockoutput" -> Material.COMMAND_BLOCK;
            case "suppressvanilla" -> Material.LAVA_BUCKET;
            case "doscoreboard", "dotablist", "dochat" -> Material.WRITTEN_BOOK;
            case "biggermessages" -> Material.STICKY_PISTON;
            case "revivetokensenabled", "revivestaff" -> Material.HEART_OF_THE_SEA;
            case "joinleavemsgsonvanish" -> Material.SPECTRAL_ARROW;
            case "chat" -> Material.ENCHANTED_BOOK;
            case "allowrejoining" -> Material.REPEATING_COMMAND_BLOCK;
            case "rejointime" -> Material.CLOCK;
            case "clearonjoin" -> Material.CHEST;
            case "clearontp" -> Material.ENDER_CHEST;
            default -> Material.PAPER;
        };

        ItemStack item = switch (key.toLowerCase()) {
            case "server", "event" -> createSkull("http://textures.minecraft.net/texture/622872342d2cf20754b9e1bae9c0902912dcae12e63b520b6fe8bd911b91018b", "");
            case "youtube" -> createSkull("http://textures.minecraft.net/texture/103b1bb8452626fa9ed6b8b756561df7ad5e98eea1fa4386b48f196469e9e2", "");
            case "tiktok" -> createSkull("http://textures.minecraft.net/texture/bcf2105bb737638833033dd8244071e75870e2e11c2617e542e8924fb2b90180", "");
            case "other" -> createSkull("http://textures.minecraft.net/texture/46ba63344f49dd1c4f5488e926bf3d9e2b29916a6c50d610bb40a5273dc8c82", "");
            case "website" -> createSkull("http://textures.minecraft.net/texture/16439d2e306b225516aa9a6d007a7e75edd2d5015d113b42f44be62a517e574f", "");
            case "twitch" -> createSkull("http://textures.minecraft.net/texture/8028a09221f50145165c4a3435e5fc0469fde0ec93e3dd10a92f1ba0c811feae", "");
            case "discord" -> createSkull("http://textures.minecraft.net/texture/ad833b51566565658f9011de8784e90c1ad9ba5d3337f8c069213bbdee986523", "");
            case "store" -> createSkull("http://textures.minecraft.net/texture/c9487f33f5500d1ed50a05818c26aeffb314edddcac6815996dfa2692a182cfc", "");
            case "tponjoin" -> createSkull("http://textures.minecraft.net/texture/cbfb41f866e7e8e593659986c9d6e88cd37677b3f7bd44253e5871e66d1d424", "");
            case "tponrevive" -> createSkull("http://textures.minecraft.net/texture/32fa8f38c7b22096619c3a6d6498b405530e48d5d4f91e2aacea578844d5c67", "");
            case "tponunrevive" -> createSkull("http://textures.minecraft.net/texture/faff2eb498e5c6a04484f0c9f785b448479ab213df95ec91176a308a12add70", "");
            case "tpondeath" -> createSkull("http://textures.minecraft.net/texture/14d844fee24d5f27ddb669438528d83b684d901b75a6889fe7488dfc4cf7a1c", "");
            case "checkforupdates" -> createSkull("http://textures.minecraft.net/texture/9cdb8f43656c06c4e8683e2e6341b4479f157f48082fea4aff09b37ca3c6995b", "");
            case "emojis" -> createSkull("http://textures.minecraft.net/texture/de4e994eacf90b60ee87b10c50acb80dd1daf9ee6f2c63a79b01154b16dcf0cf", "");
            case "chattimer" -> createSkull("http://textures.minecraft.net/texture/dbcb230a410e93b7d4b5c289631d614b90453843d6ed03daf5e4015a2fe1f56b", "");
            case "coloredchat" -> createSkull("http://textures.minecraft.net/texture/f4e94273c727b1f2c9376b5cae4ed9a48d5851bd2ab2fd83d5f81a6e6aff193d", "");
            default -> new ItemStack(mat);
        };
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e" + key);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createValueItem(String key, Object value) {
        Material mat = Material.BARRIER;
        String name = "§cUnsupported Type";

        if (value instanceof Boolean) {
            mat = (Boolean) value ? Material.LIME_WOOL : Material.RED_WOOL;
            name = (Boolean) value ? "§aTrue" : "§cFalse";
        } else if (value instanceof Number) {
            mat = Material.OAK_SIGN;
            name = "§b" + value;
        } else if (value instanceof String) {
            mat = Material.NAME_TAG;
            name = "§f" + value;
        }

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createSkull(String textureUrl, String name) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();

        PlayerProfile profile = Bukkit.createPlayerProfile(RANDOM_UUID);
        PlayerTextures textures = profile.getTextures();
        try {
            textures.setSkin(new URL(textureUrl));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid texture URL", e);
        }
        profile.setTextures(textures);
        meta.setOwnerProfile(profile);

        meta.setDisplayName(name);
        skull.setItemMeta(meta);
        return skull;
    }

    private void handleEnderPearlAnimation(Inventory inv) {
        ItemStack eye = createItem(Material.ENDER_EYE, "§bConfig Menu");
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, eye);
            inv.setItem(i + 45, eye);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            ItemStack pearl = createItem(Material.ENDER_PEARL, "§bConfig Menu");
            for (int i = 0; i < 9; i++) {
                inv.setItem(i, pearl);
                inv.setItem(i + 45, pearl);
            }
        }, 20L);
    }

    // Helper methods
    private void fillRow(Inventory inv, int row, ItemStack item) {
        for (int i = 0; i < 9; i++) {
            inv.setItem(row * 9 + i, item);
        }
    }

    private ItemStack createItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    // Static nested class for chat input handling
    public static class ChatInputListener implements Listener {
        private final QWERTZcore plugin;

        public ChatInputListener(QWERTZcore plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void onChat(AsyncPlayerChatEvent event) {
            UUID uuid = event.getPlayer().getUniqueId();
            if (!pendingInputs.containsKey(uuid)) return;

            event.setCancelled(true);
            PendingInput input = pendingInputs.remove(uuid);
            String key = input.key;
            String value = event.getMessage();
            Player player = event.getPlayer();

            Bukkit.getScheduler().runTask(plugin, () -> {
                Object currentValue = plugin.getConfigManager().get(key);
                try {
                    if (currentValue instanceof Integer) {
                        plugin.getConfigManager().set(key, Integer.parseInt(value));
                    } else if (currentValue instanceof Double) {
                        plugin.getConfigManager().set(key, Double.parseDouble(value));
                    } else if (currentValue instanceof Number) {
                        // Fallback for other Number types (e.g., Long, Float)
                        if (value.contains(".")) {
                            plugin.getConfigManager().set(key, Double.parseDouble(value));
                        } else {
                            plugin.getConfigManager().set(key, Integer.parseInt(value));
                        }
                    } else if (currentValue instanceof String) {
                        plugin.getConfigManager().set(key, value);
                    } else {
                        plugin.getMessageManager().sendMessage(player, "configgui.unsupported-type");
                        return;
                    }
                    plugin.getConfigManager().saveConfig();
                    HashMap<String, String> localMap = new HashMap<>();
                    localMap.put("%key%", key);
                    localMap.put("%value%", value);
                    plugin.getMessageManager().sendMessage(player, "configgui.set-key-to-value", localMap);

                    // Reopen the GUI at the correct page
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        new ConfigGUI(plugin, player, input.page).open();
                    });
                } catch (NumberFormatException e) {
                    plugin.getMessageManager().sendMessage(player, "configgui.invalid-number-format");
                }
            });
        }
    }

    // Helper class to store what the player is editing
    private static class PendingInput {
        final String key;
        final int page;

        PendingInput(String key, int page) {
            this.key = key;
            this.page = page;
        }
    }
}
