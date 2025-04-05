package app.qwertz.qwertzcore.commands;

import app.qwertz.qwertzcore.QWERTZcore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

public class EmojiCommand implements CommandExecutor {

    private final QWERTZcore plugin;

    public EmojiCommand(QWERTZcore plugin) {
            this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendConsole(sender, "general.only-player-execute");
            return true;
        }

        plugin.getMessageManager().sendMessage((Player) sender, "emojis.list");
        plugin.getMessageManager().sendMessage((Player) sender, "emojis.reminder");
        List<String> emojis = plugin.getMessageManager().getStringList("emojis.emojis");
        for (String emoji : emojis) {
            HashMap<String, String> localMap = new HashMap<>();
            String[] parts = emoji.split("\\|");  // Escape pipe character
            localMap.put("%code%", parts[0]);     // Use array index instead of .first()
            localMap.put("%emoji%", parts.length > 1 ? parts[1] : "");  // Handle missing emoji
            plugin.getMessageManager().sendMessage((Player) sender, "emojis.item", localMap);  // Use correct message key
        }
        return true;
    }
}
