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

package app.qwertz.qwertzcore;

import app.qwertz.qwertzcore.bstats.Metrics;
import app.qwertz.qwertzcore.commands.*;
import app.qwertz.qwertzcore.commands.tab.*;
import app.qwertz.qwertzcore.gui.ConfigGUI;
import app.qwertz.qwertzcore.listeners.PollChatListener;
import app.qwertz.qwertzcore.listeners.ChatRevivalChatListener;
import app.qwertz.qwertzcore.packets.PacketManager;
import app.qwertz.qwertzcore.papi.Placeholders;
import app.qwertz.qwertzcore.util.*;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class QWERTZcore extends JavaPlugin {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    public static final String CORE_ICON_RAW = "❇";
    public static final String CORE_ICON_COLOR = "#FF6700";
    public static final String CORE_ICON = translateHexColorCodes("&" + CORE_ICON_COLOR + CORE_ICON_RAW) + ChatColor.RESET;
    public static final String VERSION = "3.4";
    public static final String AUTHORS = "QWERTZ_EXE";
    public static final String DISCORD_LINK = "https://discord.gg/Vp6Q4FHCzf";
    public static final String WEBSITE = "https://qwertz.app";

    private Metrics metrics;
    public Boolean isUsingWorldGuard = false;
    public Boolean isUsingPacketEvents = false;
    private VanishManager vanishManager;
    private SoundManager soundManager;
    private EventManager eventManager;
    private ConfigManager configManager;
    private RankManager rankManager;
    private ScoreboardManager scoreboardManager;
    private TablistManager tablistManager;
    private ChatManager chatManager;
    private DatabaseManager databaseManager;
    private HideCommand hideCommand;
    private EventCountdownCommand eventCountdownCommand;
    private MessageManager messageManager;
    private UpdateChecker updateChecker;
    private BlockManager blockManager;
    private ReloadCoreCommand reloadCoreCommand;
    private PollCommand pollCommand;
    private ChatReviveCommand chatReviveCommand;
    private RejoinManager rejoinManager;
    private PacketManager packetManager = null;

    @Override
    public void onEnable() {
        getLogger().info("Enabling QWERTZ Core...");

        this.configManager = new ConfigManager(this);
        this.configManager.loadConfig();
        this.vanishManager = new VanishManager(this);
        this.eventManager = new EventManager(this);
        this.rankManager = new RankManager(this);
        if (rankManager.isUsingLuckPerms()) {
            getLogger().info("[EXTENSION] LuckPerms found and hooked successfully.");
        } else {
            if (rankManager.isUsingPowerRanks()) {
                getLogger().info("[EXTENSION] PowerRanks found and hooked successfully.");
            }
            else {
                getLogger().warning("[EXTENSION] LuckPerms not found. Using default rank system.");
            }
        }
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("[EXTENSION] PlaceholderAPI found and added placeholders successfully.");
            new Placeholders(this).register();
        } else {
            getLogger().warning("[EXTENSION] PlaceholderAPI not found. No placeholders will be provided.");
        }
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            getLogger().info("[EXTENSION] WorldGuard found and added security commands successfully.");
            this.isUsingWorldGuard = true;
        } else {
            getLogger().warning("[EXTENSION] WorldGuard not found. No security commands will be provided.");
        }

        if (Bukkit.getPluginManager().getPlugin("packetevents") != null) {
            this.isUsingPacketEvents = true;
            getLogger().info("[EXTENSION] packetevents found and added packet-based features successfully.");
        } else {
            getLogger().warning("[EXTENSION] packetevents not found. No packet-based features will be provided.");
        }
        this.messageManager = new MessageManager(this);
        this.databaseManager = new DatabaseManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        this.tablistManager = new TablistManager(this);
        this.chatManager = new ChatManager(this);
        this.databaseManager.initializeSpecialBlocks();
        this.soundManager = new SoundManager(this);
        this.updateChecker = new UpdateChecker(this);
        this.blockManager = new BlockManager(this);
        this.rejoinManager = new RejoinManager(this);
        if (this.packetManager == null && isUsingPacketEvents) {
            this.packetManager = new PacketManager(SpigotPacketEventsBuilder.build(this), this);
        }
        registerCommands();
        registerListeners();
        PluginCommand configCommand = this.getCommand("config");
        if (configCommand != null) {
            configCommand.setExecutor(new ConfigCommand(this));
            configCommand.setTabCompleter(new ConfigTabCompleter(this));
        }
        // BSTATS - INIT
        int pluginId = 23512;
        metrics = new Metrics(this, pluginId);
        // BSTATS - INIT DONE
        for (String commandName : getDescription().getCommands().keySet()) {
            PluginCommand command = getCommand(commandName);
            if (command != null) {
                CommandExecutor originalExecutor = command.getExecutor();
                command.setExecutor(new CommandRemapper(this, originalExecutor, commandName));
            }
        }
        printAsciiArt();
        getLogger().info("QWERTZ Core has been enabled!");
        Bukkit.getScheduler().runTask(this, this::onDone);
    }

    public void onDone() {
        getLogger().info("\u001B[33mServer started successfully with QWERTZ Core!");
        if (packetManager != null) {
            packetManager.register();
        }
    }

    private void printAsciiArt() {
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        String[] asciiArt = {
            "§6⣼⣿⣿⠟⠁⠀⠀⢀⣤⣾⣿⡿⠟⠉⠀⠀⠀⠀⠀⠀⣀⣤⣴⡾⠿⠛⠛⠋⠉⠀⠀⠀⠀⠀⠀⠀⠉⠉⠙⠛⠿⠿⣿⣶⣦⣄⣀⠀⠀⠀⠉⠛⠿⣿⣿⣶⣄⡀⠀⠀⠈⠻⢿⣿⣦",
            "§5⣿⠟⠁⠀⠀⢀⣴⣿⣿⠿⠋⠀⠀⠀⠀⠀⢀⣤⣶⣿⠿⠋⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣀⣀⡀⠀ ⠀⠀⠀⠀⠀⠉⠙⠻⢿⣿⣶⣤⡀⠀⠀⠈⠙⢿⣿⣿⣶⣄⠀⠀⠀⠘⢿",
            "§4⠋⠀⠀⢠⣴⣿⣿⠟⠁⠀⠀⠀⠀⣀⣴⣾⡿⠛⠉⠀⠀⠀⣀⣠⣤⣴⣶⣶⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⣦⣤⣄⡀⠀⠀⠀⠀⠈⠙⠿⣿⣷⣦⡀⠀⠀⠈⠻⣿⣿⣷⣄⠀⠀⠈",
            "§3⠀⢀⣴⣿⣿⠟⠁⠀⠀⠀⢀⣠⣾⣿⠟⠁⠀⠀⢀⣤⣶⣿⣿⣿⣿⠿⠟⠛⠉⠁⠀⠀⠀⠀⠀⠀⠈⠉⠙⠛⠿⢿⣿⣷⣦⣄⡀⠀⠀⠀⠀⠙⢿⣿⣶⣄⠀⠀⠀⠛⢿⣿⣷⡀⠀",
            "§2⢀⣾⣿⡿⠃⠀⠀⠀⢀⣴⣿⡿⠋⠀⠀⢀⣴⣾⣿⣿⣿⠿⠛⠉⠀⣀⣤⣴⣶⣾⣶⣿⣿⣿⣿⣶⣶⣶⣦⣤⣀⡀⠀⠉⠛⠿⣿⣷⣦⣄⡀⠀⠀⠈⠻⣿⣷⣄⠀⠀⠀⠛⢿⣿⣄",
            "§1⣿⣿⠟⠁⠀⠀⢀⣴⣿⠟⠁⠀⠀⣠⣶⣿⣿⡿⠟⠋⠀⢀⣤⣶⣿⣿⡿⠿⠛⠛⠛⠉⠉⠙⠛⠛⠻⠿⠿⣿⣿⣿⣷⣦⣄⡀⠀⠙⠻⣿⣿⣦⣀⠀⠀⠉⢻⣿⣷⡄⠀⠀⠀⠹⣿",
            "§r⣿⠋⠀⠀⠀⣰⣿⡟⠁⠀⠀⣠⣾⣿⣿⠟⠉⠀⢀⣤⣾⣿⠿⠛⠉⠀⣀⣤⣤⣶⣶⣶⣿⣶⣶⣶⣦⣤⣤⣀⡀⠈⠙⠿⣿⣿⣷⣤⡀⠀⠙⢿⣿⣦⡀⠀⠀⠹⣿⣿⣆⠀⠀⠀⠘",
            "§9⠇⠀⠀⢀⣾⡿⠋⠀⢀⣰⣿⣿⣿⠟⠁⢀⣠⣾⣿⠿⠋⠁⢀⣠⣴⣾⣿⠿⠟⠛⠛⠛⠉⠉⠉⠙⠛⠛⠛⠿⠿⣷⣦⣄⡀⠙⠻⣿⣿⣦⡀⠀⠙⣿⣷⡄⠀⠀⠘⣿⣿⣧⠀⠀⠀",
            "§8⠀⠀⢀⣿⡿⠃⠀⠀⣼⣿⣿⡟⠃⠀⣠⣼⣿⠟⠃⠀⢀⣤⣿⣿⠿⠛⠀⣀⣤⣤⣼⣿⣿⣿⣿⣿⣿⣿⣤⣀⡀⠀⠛⢿⣿⣧⡀⠀⠻⣿⣿⣄⠀⠘⣿⣿⡄⠀⠀⠸⣿⣿⣧⠀⠀",
            "§7⠀⢠⣿⡿⠁⠀⢀⣾⣿⡿⠏⠀⣠⣾⣿⠟⠁⠀⣠⣶⣿⠿⠋⣡⣴⣾⣿⡿⠿⠿⠛⠛⠛⠛⠛⠛⠻⠿⢿⣿⣿⣶⣤⡀⠈⠻⣿⣦⣀⠈⠻⣿⣧⡀⠈⢻⣿⡄⠀⠀⠙⣿⣿⣆⠀",
            "§a⢠⣿⡟⠀⠀⢠⣿⣿⡟⠁⢀⣾⣿⡿⠁⠀⣠⣾⣿⠟⢁⣴⣿⣿⠟⠋⠀⠀⠀⣀⣠⣤⣤⣤⣤⣤⣀⠀⠀⠈⠙⠻⣿⣿⣶⣄⠈⠻⣿⣦⡀⠈⢿⣷⡀⠀⢻⣿⡄⠀⠀⠘⣿⣿⡄",
            "§b⡟⠀⠀⢠⣿⣿⠏⠀⣰⣿⣿⠋⠀⢀⣼⣿⡿⠁⣴⣿⡿⠋⠀⠀⣀⣴⣾⣿⡿⠟⠛⠛⠛⠛⠿⠿⣿⣷⣦⣄⡀⠀ ⠹⣿⣿⣦⡀⠉⢿⣷⡄⠀⢻⣷⡀⠀⢻⣿⡄⠀⠀⠘⣿⣿",
            "§c⡿⠀⠀⢀⣾⣿⠋⠀⢸⣿⡿⠃⠀⢠⣾⣿⠋⢀⣾⣿⠟⠀⠀⣠⣾⣿⡿⠟⢉⣀⣠⣤⣤⣤⣤⣀⡀⠀⠉⠻⢿⣿⣦⠀⠈⢿⣿⣷⡄⠈⢻⣿⡆⠀⢻⣧⠀⠀⢻⣿⠀⠀⠀⠘⣿",
            "§d⠃⠀⠀⣾⣿⠇⠀⢠⣿⣿⠁⠀⣰⣿⣿⠃⢠⣾⡿⠃⠀⢀⣾⣿⡿⢋⣤⣾⣿⣿⡿⠿⠿⠿⠿⢿⣿⣿⣦⣄⠀⠹⣿⣷⣄⠈⢻⣿⣿⡄⠀⢻⣿⡀⠀⢿⡆⠀⠈⢿⣇⠀⠀⠀⢻",
            "§e⠀⠀⣼⣿⣟⠀⢀⣾⣿⠇⠀⢠⣿⣿⠏⢠⣿⡿⠁⠀⣠⣿⣿⠏⣠⣾⣿⠟⢋⣡⣤⣶⣾⣿⣶⣦⣄⠙⢿⣿⣷⡄⠘⣿⣿⣆⠀⢻⣿⣿⡀⠀⢿⣧⠀⠘⣷⠀⠀⢸⣿⠀⠀⠀⠘",
            "§f⠀⠠⣿⣿⠇⠀⣼⣿⡟⠀⠀⣼⣿⡟⠀⣺⣿⠃⠀⢰⣿⣿⠃⣰⣿⠿⢁⣴⣿⡿⠛⢉⣉⠉⠉⠻⣿⣧⡀⢻⣿⣿⡆⠘⣿⣿⡄⠀⢿⣿⣧⠀⠘⣿⡆⠀⢻⡇⠀⠀⢿⡇⠀⠀⠀",
            "§6⠀⢸⣿⡿⠀⣸⣿⣿⠃⠀⢰⣿⡟⠀⢰⣿⡏⠀⢠⣿⣿⠇⣰⣿⠏⣴⣿⠟§6/¯¯¯¯¯\\§6⣄⠘⢿⣧⠀⢻⣿⣇⠀⢹⣿⣷⠀⢸⣿⣿⠀⠰⣿⣇⠀⢹⣿⠀⠀⢸⣇⠀⠀⠀",
            "§5⠀⣾⣿⠇⠀⣽⣿⡿⠀⠀⣿⣿⠃⠀⣼⣿⠀⠀⣺⣿⡟⢠⣿⡟⣼⡿⠁⣼§6|     |§5⣿⡆⢸⣿⠆⠈⣿⣿⠀⢸⣿⣿⠀⠀⣿⣿⠀⢨⣿⣿⠀⢸⣿⠀⠀⢸⣿⠀⠀⢠",
            "§4⠀⣿⣿⠀⠠⣿⣿⡇⠀⠀⣿⡟⠀⠀⣿⡏⠀⢠⣿⣿⠁⣼⣿⢠⣿⡇⢰⣿§6|     |§4⣿⡇⢸⣿⡇⠀⣿⣟⠀⢸⣿⣿⠀⢸⣿⣿⠀⢰⣿⡇⠀⢻⣿⠀⠀⢸⣿⠀⠀⢸",
            "§3⠀⣿⣿⠀⢀⣿⣿⡇⠀⠀⣿⡟⠀⠀⣿⡧⠀⢸⣿⡏⠀⣿⡏⢸⣿⡇⢸⣿§6|     |§3⠟⢡⣿⣿⠀⣼⣿⡏⠀⣼⣿⡟⢀⣾⣿⡇⠀⣾⣿⡇⠀⣿⣿⠀⠀⣿⣿⠀⠀⢸",
            "§2⠀⣿⣿⠀⠠⣿⣿⡇⠀⠀⣿⡇⠀⠀⣿⡇⠀⢸⣿⠁⠀⣿⣇⠘⣿⣧⠘⣿§6\\_____/¯\\§2⡿⠁⣴⣿⣿⠃⣼⣿⣿⠃⣼⣿⡿⠀⣼⣿⡿⠀⣼⣿⡏⠀⢸⣿⡿⠀⠀⣼",
            "§1⠀⢿⣿⡀⠀⢿⣿⣧⠀⠀⣿⡆⠀⠀⣿⡆⠀⢸⣿⡀⠀⣿⣿⡀⠹⣿⣦⡘⠿⣿⣿⣿§6\\    \\§1⣾⣿⡿⠋⣼⣿⡿⠃⣼⣿⣿⠃⣼⣿⣿⠁⣰⣿⡿⠁⢀⣿⣿⡇⠀⠀⣾",
            "§r⠀⢸⣿⡇⠀⢹⣿⣿⠀⠀⢸⣟⠀⠀⢹⣇⠀⢸⣿⣧⠀⠹⣿⣧⡀⠹⣿⣿⣷⣦⣬⣭⣤§6\\____\\§r⠋⣠⣾⣿⠟⢀⣼⣿⡿⠃⣰⣿⣿⠃⣰⣿⣿⠁⠀⣼⣿⣿⠀⠀⢰⣿",
            "§9⠀⠈⣿⣿⠀⠈⣿⣿⡄⠀⠈⣿⡆⠀⠸⣿⡀⠀⢿⣿⣧⡀⠹⣿⣷⣄⡈⠙⠿⠿⢿⣿⣿⡿⠿⠟⠛⣁⣤⣾⡿⠟⠁⣰⣾⣿⠟⢡⣾⣿⡿⠃⢠⣿⣿⠃⠀⣰⣿⣿⠇⠀⠀⣼⡿",
            "§8⠀⠀⣿⣿⡇⠀⢹⣿⣧⠀⠀⢻⣿⠀⠀⢻⣷⠀⠈⣿⣿⣿⣦⠈⢻⣿⣿⣶⣦⣤⣤⡄⢠⣤⣤⣴⣾⣿⡟⠋⠀⢠⣾⣿⡟⠁⣴⣿⣿⡟⠀⢰⣿⣿⠃⠀⢰⣿⣿⡏⠀⠀⣼⣿⠃",
            "§7⠀⠀⠘⣿⣷⠀⠀⢿⣿⡄⠀⠘⣿⣧⠀⠘⣿⣧⡀⠈⠻⣿⣿⣷⣤⡉⠛⠿⣿⣿⣿⣿⣿⣿⡿⠟⠋⠉⢀⣤⣶⣿⠟⠉⢀⣴⣿⡿⠋⢀⣴⣿⡿⠃⠀⢠⣿⡿⡿⠁⠀⣰⣿⠏⠀",
            "§a⣇⠀⠀⠹⣿⣧⠀⠈⣿⣿⣆⠀⠹⣿⣷⡀⠘⢿⣿⣦⡀⠈⠛⢿⣿⣿⣷⣦⣄⣀⣈⣁⣀⣀⣠⣤⣶⣾⡿⠟⠋⠀⣠⣶⣿⡿⠋⢀⣤⣿⣿⠟⠁⠀⣰⣿⣿⠟⠁⠀⣼⣿⠏⠀⠀",
            "§b⣿⡆⠀⠀⢻⣿⣷⡀⠈⢿⣿⣦⠀⠘⢿⣷⣄⠀⠹⢿⣿⣶⣄⡀⠀⠉⠛⠛⠿⠿⠿⠿⠿⠿⠛⠋⠉⠀⢀⣠⣴⣾⡿⠟⠉⣠⣶⣿⣿⡿⠉⠀⣠⣾⣿⣿⠃⠀⢀⣾⣿⠋⠀⠀⣼",
            "§c⢻⣿⣆⠀⠀⠻⣿⣷⡀⠈⠻⣿⣿⣄⠀⠙⠿⣷⣤⡀⠉⠛⠿⣿⣷⣦⣤⣀⣀⣀⣀⣀⣀⣀⣤⣤⣶⣾⠿⠟⠛⢉⣠⣴⣿⣿⣿⠟⠋⠀⣠⣶⣿⡿⠛⠁⠀⣴⣿⠟⠁⠀⢠⣾⢿",
            "§d⠀⢻⣿⣧⡀⠀⠙⣿⣿⣄⠀⠈⢿⣿⣷⣄⠀⠈⠻⢿⣷⣦⣄⡀⠉⠙⠛⠛⠻⠿⠛⠛⠛⠛⠉⠉⠀⢀⣠⣴⣾⣿⣿⣿⡿⠟⠁⠀⣠⣾⣿⡿⠋⠀⠀⣠⣾⡿⠋⠀⢀⣴⣿⡟⠁",
            "§e⠀⠀⠻⣿⣷⡄⠀⠘⢿⣿⣦⡄⠀⠉⠿⣿⣷⣤⣀⠀⠈⠙⠻⠿⣿⣶⣶⣴⣦⣴⣶⣴⣤⣶⣶⣾⣿⣿⠿⠿⠟⠛⠉⠀⠀⣀⣴⣿⡿⠟⠁⠀⠀⣠⣾⡿⠋⠀⢀⣴⣿⣿⠃⠀⠀",
            "§f⡄⠀⠀⠙⢿⣿⣦⡀⠀⠙⢿⣿⣦⡀⠀⠈⠛⢿⣿⣿⣶⣤⣀⡀⠀⠈⠉⠉⠉⠛⠛⠛⠛⠋⠉⠉⠀⠀⠀⠀⢀⣀⣤⣶⣿⡿⠟⠋⠀⠀⢀⣠⣾⠿⠋⠀⣠⣴⣿⡿⠛⠀⠀⢀⣴",
            "§6⣿⣆⠀⠀⠈⠛⣿⣿⣦⡀⠀⠙⠿⣿⣷⣤⣀⡀⠈⠙⠻⢿⣿⣿⣿⣷⣶⣶⣶⣤⣤⣤⣤⣤⣴⣶⣶⣿⣾⣿⠿⠟⠛⠉⠀⠀⣀⣠⣴⣶⡿⠋⠁⣀⣤⣾⣿⡿⠋⠀⠀⣠⣴⣿⡿"
        };
        console.sendMessage("");
        for (String line : asciiArt) {
            console.sendMessage(ChatColor.translateAlternateColorCodes('§', line));
        }
        console.sendMessage("");
    }
    @Override
    public void onDisable() {
        getLogger().info("Disabling QWERTZ Core...");
        if (metrics != null) {
            metrics.shutdown();
        }
        metrics = null;
        HandlerList.unregisterAll(this);
        configManager = null;
        vanishManager = null;
        soundManager = null;
        eventManager = null;
        rankManager = null;
        if (scoreboardManager != null) {
            scoreboardManager.stopScoreboardUpdater();
            scoreboardManager.removeScoreboardFromAllPlayers();
        }
        scoreboardManager = null;
        if (tablistManager != null) {
            tablistManager.stopTabUpdater();
        }
        tablistManager = null;
        chatManager = null;
        databaseManager = null;
        messageManager = null;
        blockManager = null;
        Bukkit.getScheduler().cancelTasks(this);
        getLogger().info("QWERTZ Core has been disabled!");
    }

    private void registerCommands() {
        getCommand("core").setExecutor(new CoreCommand(this));
        getCommand("timer").setExecutor(new TimerCommand(this));
        getCommand("timer").setTabCompleter(new TimerTabCompleter());
        getCommand("gmc").setExecutor(new GamemodeCommand(this));
        getCommand("gms").setExecutor(new GamemodeCommand(this));
        getCommand("gmsp").setExecutor(new GamemodeCommand(this));
        getCommand("gma").setExecutor(new GamemodeCommand(this));
        getCommand("gm").setExecutor(new GamemodeCommand(this));
        getCommand("gm").setTabCompleter(new GameModeTabCompleter());
        this.chatReviveCommand = new ChatReviveCommand(this);
        getCommand("chatrevival").setExecutor(chatReviveCommand);
        getCommand("chatrevival").setTabCompleter(new ChatReviveTabCompleter());
        EventCommands eventCommands = new EventCommands(this);
        getCommand("revive").setExecutor(eventCommands);
        getCommand("unrevive").setExecutor(eventCommands);
        getCommand("reviveall").setExecutor(eventCommands);
        getCommand("reviverandom").setExecutor(eventCommands);
        getCommand("unreviveall").setExecutor(eventCommands);
        getCommand("listalive").setExecutor(eventCommands);
        getCommand("listdead").setExecutor(eventCommands);
        getCommand("givedead").setExecutor(eventCommands);
        getCommand("givealive").setExecutor(eventCommands);
        getCommand("givedead").setTabCompleter(new GiveCommandTabCompleter());
        getCommand("givealive").setTabCompleter(new GiveCommandTabCompleter());
        getCommand("tpalive").setExecutor(eventCommands);
        getCommand("tpdead").setExecutor(eventCommands);
        getCommand("tpall").setExecutor(eventCommands);
        getCommand("tphere").setExecutor(eventCommands);
        getCommand("revivelast").setExecutor(eventCommands);
        getCommand("revivelast").setTabCompleter(new ReviveLastTabCompleter());
        getCommand("healalive").setExecutor(eventCommands);
        getCommand("healdead").setExecutor(eventCommands);
        getCommand("vanish").setExecutor(new VanishCommands(this));
        getCommand("unvanish").setExecutor(new VanishCommands(this));
        getCommand("config").setExecutor(new ConfigCommand(this));
        getCommand("spawn").setExecutor(new SpawnCommand(this));
        ClearInventoryCommand clearInventoryCommand = new ClearInventoryCommand(this);
        getCommand("clearalive").setExecutor(clearInventoryCommand);
        getCommand("cleardead").setExecutor(clearInventoryCommand);
        hideCommand = new HideCommand(this);
        getCommand("hide").setExecutor(hideCommand);
        getCommand("hide").setTabCompleter(new HideTabCompleter());
        eventCountdownCommand = new EventCountdownCommand(this);
        this.getCommand("eventcountdown").setExecutor(eventCountdownCommand);
        getCommand("eventcountdown").setTabCompleter(new TimerTabCompleter());
        ReviveTokenCommands reviveCommands = new ReviveTokenCommands(this);
        this.getCommand("userevive").setExecutor(reviveCommands);
        this.getCommand("reviveaccept").setExecutor(reviveCommands);
        this.getCommand("revivedeny").setExecutor(reviveCommands);
        this.getCommand("addrevive").setExecutor(reviveCommands);
        this.getCommand("removerevive").setExecutor(reviveCommands);
        this.getCommand("revives").setExecutor(reviveCommands);
        DiscordCommand discordCommand = new DiscordCommand(this);
        this.getCommand("dc").setExecutor(discordCommand);
        this.getCommand("disc").setExecutor(discordCommand);
        this.getCommand("discord").setExecutor(discordCommand);
        ChatMuteCommand chatMuteCommand = new ChatMuteCommand(this);
        this.getCommand("mutechat").setExecutor(chatMuteCommand);
        this.getCommand("unmutechat").setExecutor(chatMuteCommand);
        AdvertisementCommand adCommand = new AdvertisementCommand(this);
        this.getCommand("ad").setExecutor(adCommand);
        this.getCommand("setad").setExecutor(adCommand);
        AdTabCompleter adTabCompleter = new AdTabCompleter();
        this.getCommand("ad").setTabCompleter(adTabCompleter);
        this.getCommand("setad").setTabCompleter(adTabCompleter);
        MessageCommands messageCommands = new MessageCommands(this);
        this.getCommand("message").setExecutor(messageCommands);
        this.getCommand("reply").setExecutor(messageCommands);
        this.getCommand("messagetoggle").setExecutor(messageCommands);
        WinCommands winCommands = new WinCommands(this);
        this.getCommand("addwin").setExecutor(winCommands);
        this.getCommand("removewin").setExecutor(winCommands);
        this.getCommand("wins").setExecutor(winCommands);
        WarpCommands warpCommands = new WarpCommands(this);
        this.getCommand("setwarp").setExecutor(warpCommands);
        this.getCommand("warp").setExecutor(warpCommands);
        this.getCommand("delwarp").setExecutor(warpCommands);
        this.getCommand("warps").setExecutor(warpCommands);
        WarpTabCompleter warpTabCompleter = new WarpTabCompleter(this);
        this.getCommand("setwarp").setTabCompleter(warpTabCompleter);
        this.getCommand("warp").setTabCompleter(warpTabCompleter);
        this.getCommand("delwarp").setTabCompleter(warpTabCompleter);
        this.getCommand("eventblock").setExecutor(new EventBlockCommand(this));
        this.getCommand("eventblock").setTabCompleter(new EventBlockTabCompleter());
        getCommand("createkit").setExecutor(new KitCommand(this));
        getCommand("kit").setExecutor(new KitCommand(this));
        getCommand("delkit").setExecutor(new KitCommand(this));
        getCommand("createkit").setTabCompleter(new KitTabCompleter(this));
        getCommand("kit").setTabCompleter(new KitTabCompleter(this));
        getCommand("delkit").setTabCompleter(new KitTabCompleter(this));
        getCommand("kits").setExecutor(new KitCommand(this));
        getCommand("invsee").setExecutor(new InvseeCommand(this));
        if (isUsingWorldGuard) {
            WorldGuardCommands worldGuardCommands = new WorldGuardCommands(this);
            getCommand("pvp").setExecutor(worldGuardCommands);
            getCommand("flow").setExecutor(worldGuardCommands);
            getCommand("break").setExecutor(worldGuardCommands);
            getCommand("place").setExecutor(worldGuardCommands);
            getCommand("hunger").setExecutor(worldGuardCommands);
            getCommand("falldamage").setExecutor(worldGuardCommands);
            WorldGuardTabCompleter worldGuardTabCompleter = new WorldGuardTabCompleter();
            getCommand("pvp").setTabCompleter(worldGuardTabCompleter);
            getCommand("flow").setTabCompleter(worldGuardTabCompleter);
            getCommand("break").setTabCompleter(worldGuardTabCompleter);
            getCommand("place").setTabCompleter(worldGuardTabCompleter);
            getCommand("hunger").setTabCompleter(worldGuardTabCompleter);
            getCommand("falldamage").setTabCompleter(worldGuardTabCompleter);
        }
        this.pollCommand = new PollCommand(this);
        getCommand("poll").setExecutor(pollCommand);
        getCommand("pollvote").setExecutor(new PollVoteCommand(this, pollCommand));
        getCommand("setspawn").setExecutor(new SetterCommands(this));
        getCommand("setserver").setExecutor(new SetterCommands(this));
        getCommand("setevent").setExecutor(new SetterCommands(this));

        this.reloadCoreCommand = new ReloadCoreCommand(this);
        getCommand("reloadcore").setExecutor(reloadCoreCommand);
        getCommand("theme").setExecutor(new ThemeCommand(this));
        getCommand("theme").setTabCompleter(new ThemeTabCompleter(this));
        getCommand("emojis").setExecutor(new EmojiCommand(this));
        getCommand("speed").setExecutor(new SpeedCommand(this));
        getCommand("speed").setTabCompleter(new SpeedTabCompleter());
        getCommand("skull").setExecutor(new SkullCommand(this));
        getCommand("broadcast").setExecutor(new BroadcastCommand(this));
        getCommand("rejoin").setExecutor(new RejoinCommand(this));
        getCommand("rejoin").setTabCompleter(new RejoinTabCompleter());
    }
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerEventListener(this, hideCommand, updateChecker), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new RestrictedCommandsListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockEventListener(this), this);
        getServer().getPluginManager().registerEvents(new ConfigGUI.ChatInputListener(this), this);
        getServer().getPluginManager().registerEvents(new PollChatListener(this, pollCommand), this);
        getServer().getPluginManager().registerEvents(new ChatRevivalChatListener(this, chatReviveCommand), this);
    }
    public EventManager getEventManager() {
        return eventManager;
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }
    public ConfigManager getConfigManager() {
        return configManager;
    }
    public RankManager getRankManager() {
        return rankManager;
    }
    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
    public TablistManager getTablistManager() {
        return tablistManager;
    }
    public ChatManager getChatManager() {
        return chatManager;
    }
    public void cancelEventCountdown() {
        if (eventCountdownCommand != null) {
            eventCountdownCommand.cancelCountdown();
        }
    }
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    public MessageManager getMessageManager() {
        return messageManager;
    }
    public BlockManager getBlockManager() {
        return blockManager;
    }
    public VanishManager getVanishManager() {
        return vanishManager;
    }

    public PollCommand getPollCommand() {
        return pollCommand;
    }

    public ChatReviveCommand getChatReviveCommand() {
        return chatReviveCommand;
    }

    public RejoinManager getRejoinManager() {
        return rejoinManager;
    }

    public void reloadCore(CommandSender sender) {
        reloadCoreCommand.reload(sender);
    }

    public static String translateHexColorCodes(final String message) {
        final char colorChar = ChatColor.COLOR_CHAR;

        final Matcher matcher = HEX_PATTERN.matcher(message);
        final StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);

        while (matcher.find()) {
            final String group = matcher.group(1);

            matcher.appendReplacement(buffer, colorChar + "x"
                    + colorChar + group.charAt(0) + colorChar + group.charAt(1)
                    + colorChar + group.charAt(2) + colorChar + group.charAt(3)
                    + colorChar + group.charAt(4) + colorChar + group.charAt(5));
        }

        return matcher.appendTail(buffer).toString();
    }
}