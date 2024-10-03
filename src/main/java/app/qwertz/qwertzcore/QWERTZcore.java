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
import app.qwertz.qwertzcore.commands.tab.ConfigTabCompleter;
import app.qwertz.qwertzcore.papi.Placeholders;
import app.qwertz.qwertzcore.util.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class QWERTZcore extends JavaPlugin {

    public static final String CORE_ICON = ChatColor.GREEN + "❇" + ChatColor.RESET;
    public static final String VERSION = "1.0";
    public static final String AUTHORS = "QWERTZ_EXE";
    public static final String DISCORD_LINK = "https://discord.gg/Vp6Q4FHCzf";
    public static final String WEBSITE = "https://qwertz.app";

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

    @Override
    public void onEnable() {
        getLogger().info("QWERTZ Core has been enabled!");

        this.configManager = new ConfigManager(this);
        this.configManager.loadConfig();
        this.eventManager = new EventManager(this);
        this.rankManager = new RankManager(this);
        if (rankManager.isUsingLuckPerms()) {
            getLogger().info("[EXTENSION] LuckPerms found and hooked successfully.");
        } else {
            getLogger().warning("[EXTENSION] LuckPerms not found. Using default rank system.");
        }
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("[EXTENSION] PlaceholderAPI found and added placeholders successfully.");
            new Placeholders(this).register();
        } else {
            getLogger().warning("[EXTENSION] PlaceholderAPI not found. No placeholders will be provided.");
        }
        this.scoreboardManager = new ScoreboardManager(this, eventManager, configManager);
        this.tablistManager = new TablistManager(this);
        this.chatManager = new ChatManager(this);
        this.databaseManager = new DatabaseManager(this);
        this.messageManager = new MessageManager(this);

        registerCommands();
        registerListeners();
        PluginCommand configCommand = this.getCommand("config");
        if (configCommand != null) {
            configCommand.setExecutor(new ConfigCommand(this));
            configCommand.setTabCompleter(new ConfigTabCompleter(this));
        }
        // BSTATS - INIT
        int pluginId = 23512;
        Metrics metrics = new Metrics(this, pluginId);
        // BSTATS - INIT DONE
        printAsciiArt();
    }

    private void printAsciiArt() {
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        String[] asciiArt = {
                "§6⣼⣿⣿⠟⠁⠀⠀⢀⣤⣾⣿⡿⠟⠉⠀⠀⠀⠀⠀⠀⣀⣤⣴⡾⠿⠛⠛⠋⠉⠀⠀⠀⠀⠀⠀⠀⠉⠉⠙⠛⠿⠿⣿⣶⣦⣄⣀⠀⠀⠀⠉⠛⠿⣿⣿⣶⣄⡀⠀⠀⠈⠻⢿⣿⣦",
                "§5⣿⠟⠁⠀⠀⢀⣴⣿⣿⠿⠋⠀⠀⠀⠀⠀⢀⣤⣶⣿⠿⠋⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣀⣀⡀⠀⠀⠀⠀⠀⠀⠀⠉⠙⠻⢿⣿⣶⣤⡀⠀⠀⠈⠙⢿⣿⣿⣶⣄⠀⠀⠀⠘⢿",
                "§4⠋⠀⠀⢠⣴⣿⣿⠟⠁⠀⠀⠀⠀⣀⣴⣾⡿⠛⠉⠀⠀⠀⣀⣠⣤⣴⣶⣶⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⣦⣤⣄⡀⠀⠀⠀⠀⠈⠙⠿⣿⣷⣦⡀⠀⠀⠈⠻⣿⣿⣷⣄⠀⠀⠈",
        "§3⠀⢀⣴⣿⣿⠟⠁⠀⠀⠀⢀⣠⣾⣿⠟⠁⠀⠀⢀⣤⣶⣿⣿⣿⣿⠿⠟⠛⠉⠁⠀⠀⠀⠀⠀⠀⠈⠉⠙⠛⠿⢿⣿⣷⣦⣄⡀⠀⠀⠀⠀⠙⢿⣿⣶⣄⠀⠀⠀⠛⢿⣿⣷⡀⠀",
        "§2⢀⣾⣿⡿⠃⠀⠀⠀⢀⣴⣿⡿⠋⠀⠀⢀⣴⣾⣿⣿⣿⠿⠛⠉⠀⣀⣤⣴⣶⣾⣶⣿⣿⣿⣿⣶⣶⣶⣦⣤⣀⡀⠀⠉⠛⠿⣿⣷⣦⣄⡀⠀⠀⠈⠻⣿⣷⣄⠀⠀⠀⠛⢿⣿⣄",
        "§1⣿⣿⠟⠁⠀⠀⢀⣴⣿⠟⠁⠀⠀⣠⣶⣿⣿⡿⠟⠋⠀⢀⣤⣶⣿⣿⡿⠿⠛⠛⠛⠉⠉⠙⠛⠛⠻⠿⠿⣿⣿⣿⣷⣦⣄⡀⠀⠙⠻⣿⣿⣦⣀⠀⠀⠉⢻⣿⣷⡄⠀⠀⠀⠹⣿",
        "§r⣿⠋⠀⠀⠀⣰⣿⡟⠁⠀⠀⣠⣾⣿⣿⠟⠉⠀⢀⣤⣾⣿⠿⠛⠉⠀⣀⣤⣤⣶⣶⣶⣿⣶⣶⣶⣦⣤⣤⣀⡀⠈⠙⠿⣿⣿⣷⣤⡀⠀⠙⢿⣿⣦⡀⠀⠀⠹⣿⣿⣆⠀⠀⠀⠘",
        "§9⠇⠀⠀⢀⣾⡿⠋⠀⢀⣰⣿⣿⣿⠟⠁⢀⣠⣾⣿⠿⠋⠁⢀⣠⣴⣾⣿⠿⠟⠛⠛⠛⠉⠉⠉⠙⠛⠛⠛⠿⠿⣷⣦⣄⡀⠙⠻⣿⣿⣦⡀⠀⠙⣿⣷⡄⠀⠀⠘⣿⣿⣧⠀⠀⠀",
        "§8⠀⠀⢀⣿⡿⠃⠀⠀⣼⣿⣿⡟⠃⠀⣠⣼⣿⠟⠃⠀⢀⣤⣿⣿⠿⠛⠀⣀⣤⣤⣼⣿⣿⣿⣿⣿⣿⣿⣤⣀⡀⠀⠛⢿⣿⣧⡀⠀⠻⣿⣿⣄⠀⠘⣿⣿⡄⠀⠀⠸⣿⣿⣧⠀⠀",
        "§7⠀⢠⣿⡿⠁⠀⢀⣾⣿⡿⠏⠀⣠⣾⣿⠟⠁⠀⣠⣶⣿⠿⠋⣡⣴⣾⣿⡿⠿⠿⠛⠛⠛⠛⠛⠛⠻⠿⢿⣿⣿⣶⣤⡀⠈⠻⣿⣦⣀⠈⠻⣿⣧⡀⠈⢻⣿⡄⠀⠀⠙⣿⣿⣆⠀",
        "§a⢠⣿⡟⠀⠀⢠⣿⣿⡟⠁⢀⣾⣿⡿⠁⠀⣠⣾⣿⠟⢁⣴⣿⣿⠟⠋⠀⠀⠀⣀⣠⣤⣤⣤⣤⣤⣀⠀⠀⠈⠙⠻⣿⣿⣶⣄⠈⠻⣿⣦⡀⠈⢿⣷⡀⠀⢻⣿⡄⠀⠀⠘⣿⣿⡄",
        "§b⡟⠀⠀⢠⣿⣿⠏⠀⣰⣿⣿⠋⠀⢀⣼⣿⡿⠁⣴⣿⡿⠋⠀⠀⣀⣴⣾⣿⡿⠟⠛⠛⠛⠛⠿⠿⣿⣷⣦⣄⡀⠀⠹⣿⣿⣦⡀⠉⢿⣷⡄⠀⢻⣷⡀⠀⢻⣿⡄⠀⠀⠘⣿⣿",
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
        getLogger().info("QWERTZ Core has been disabled!");
    }

    private void registerCommands() {
        getCommand("core").setExecutor(new CoreCommand());
        getCommand("timer").setExecutor(new TimerCommand(this));
        getCommand("gmc").setExecutor(new GamemodeCommand(this));
        getCommand("gms").setExecutor(new GamemodeCommand(this));
        getCommand("gmsp").setExecutor(new GamemodeCommand(this));
        getCommand("gma").setExecutor(new GamemodeCommand(this));
        getCommand("gm").setExecutor(new GamemodeCommand(this));
        getCommand("chatrevive").setExecutor(new ChatReviveCommand(this));
        EventCommands eventCommands = new EventCommands(this, eventManager);
        getCommand("revive").setExecutor(eventCommands);
        getCommand("unrevive").setExecutor(eventCommands);
        getCommand("reviveall").setExecutor(eventCommands);
        getCommand("listalive").setExecutor(eventCommands);
        getCommand("listdead").setExecutor(eventCommands);
        getCommand("givedead").setExecutor(eventCommands);
        getCommand("givealive").setExecutor(eventCommands);
        getCommand("tpalive").setExecutor(eventCommands);
        getCommand("tpdead").setExecutor(eventCommands);
        getCommand("tphere").setExecutor(eventCommands);
        getCommand("revivelast").setExecutor(eventCommands);
        getCommand("healalive").setExecutor(eventCommands);
        getCommand("healdead").setExecutor(eventCommands);
        getCommand("config").setExecutor(new ConfigCommand(this));
        getCommand("spawn").setExecutor(new SpawnCommand(this));
        ClearInventoryCommand clearInventoryCommand = new ClearInventoryCommand(this);
        getCommand("clearalive").setExecutor(clearInventoryCommand);
        getCommand("cleardead").setExecutor(clearInventoryCommand);
        hideCommand = new HideCommand(this);
        getCommand("hide").setExecutor(hideCommand);
        eventCountdownCommand = new EventCountdownCommand(this);
        this.getCommand("eventcountdown").setExecutor(eventCountdownCommand);
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
    }
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerEventListener(eventManager, configManager, scoreboardManager, tablistManager, hideCommand), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(eventManager, configManager), this);
    }
    public EventManager getEventManager() {
        return eventManager;
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
}