package app.qwertz.qwertzcore.papi;

import app.qwertz.qwertzcore.QWERTZcore;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Placeholders extends PlaceholderExpansion {

    private final QWERTZcore plugin;

    public Placeholders(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "qwertzcore";
    }

    @Override
    public @NotNull String getAuthor() {
        return "QWERTZ_EXE";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // This is important to keep the expansion loaded
    }


    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        switch (identifier) {
            case "alive":
                return String.valueOf(plugin.getEventManager().getAlivePlayers().size());
            case "dead":
                return String.valueOf(plugin.getEventManager().getDeadPlayers().size());
            case "isdead":
                return String.valueOf(plugin.getEventManager().isPlayerDead(player));
            case "isalive":
                return String.valueOf(!plugin.getEventManager().isPlayerDead(player));
            case "revives":
                return String.valueOf(plugin.getDatabaseManager().getReviveTokens(player.getUniqueId()));
            case "wins":
                return String.valueOf(plugin.getDatabaseManager().getWins(player.getUniqueId()));
            case "status":
                return plugin.getEventManager().isPlayerDead(player) ?
                        ChatColor.RED + "DEAD" : ChatColor.GREEN + "ALIVE";
            case "startingin":
                return String.valueOf(plugin.getScoreboardManager().eventcountdown);
            case "server":
                return String.valueOf(plugin.getConfigManager().getServerName());
            case "event":
                return String.valueOf(plugin.getConfigManager().getEventName());
        }

        return null; // Placeholder is unknown by the expansion
    }
}