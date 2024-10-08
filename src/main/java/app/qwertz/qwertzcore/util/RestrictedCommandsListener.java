package app.qwertz.qwertzcore.util;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestrictedCommandsListener implements Listener {

    private final Map<String, String> restrictedCommands = new HashMap<>();

    private final ConfigManager configManager;

    public RestrictedCommandsListener(ConfigManager configManager) {
        restrictedCommands.put("/minecraft:me", "qwertzcore.chat.bypassme");
        restrictedCommands.put("/me", "qwertzcore.chat.bypassme");
        restrictedCommands.put("/minecraft:tm", "qwertzcore.chat.bypasstm");
        restrictedCommands.put("/tm", "qwertzcore.chat.bypasstm");
        restrictedCommands.put("/teammsg", "qwertzcore.chat.bypasstm");
        this.configManager = configManager;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().split(" ")[0].toLowerCase();

        if (restrictedCommands.containsKey(command)) {
            String bypassPermission = restrictedCommands.get(command);
            if (!player.isOp() && !player.hasPermission(bypassPermission)) {
                event.setCancelled(true);
                player.sendMessage(configManager.getColor("colorError") + "You don't have permission to use this command.");
            }
        }
    }
}