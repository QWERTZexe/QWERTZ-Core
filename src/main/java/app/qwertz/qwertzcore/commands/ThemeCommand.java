package app.qwertz.qwertzcore.commands;

import app.qwertz.qwertzcore.QWERTZcore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ThemeCommand implements CommandExecutor {
    private QWERTZcore plugin;

    public ThemeCommand(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            plugin.getMessageManager().sendInvalidUsage(sender, "/theme <theme>");
            return true;
        }

        String theme = args[0].toLowerCase();
        plugin.getMessageManager().setTheme(theme);

        if (plugin.getMessageManager().getThemes().contains(theme)) {
            plugin.getMessageManager().sendMessage(sender, "messages.successful");
        } else {
            plugin.getMessageManager().sendMessage(sender, "messages.invalid-theme");
        }
        return true;
    }
}
