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

    public RestrictedCommandsListener() {
        restrictedCommands.put("/minecraft:me", "qwertzcore.chat.bypassme");
        restrictedCommands.put("/me", "qwertzcore.chat.bypassme");
        restrictedCommands.put("/minecraft:tm", "qwertzcore.chat.bypasstm");
        restrictedCommands.put("/tm", "qwertzcore.chat.bypasstm");
        restrictedCommands.put("/teammsg", "qwertzcore.chat.bypasstm");
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().split(" ")[0].toLowerCase();

        if (restrictedCommands.containsKey(command)) {
            String bypassPermission = restrictedCommands.get(command);
            if (!player.isOp() && !player.hasPermission(bypassPermission)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            }
        }
    }
}