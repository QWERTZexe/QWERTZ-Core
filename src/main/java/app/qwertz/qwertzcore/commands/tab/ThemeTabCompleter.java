package app.qwertz.qwertzcore.commands.tab;

import app.qwertz.qwertzcore.QWERTZcore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;
import java.util.stream.Collectors;

public class ThemeTabCompleter implements TabCompleter {
    private final QWERTZcore plugin;

    public ThemeTabCompleter(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("theme") && args.length == 1) {
            return plugin.getMessageManager().getThemes().stream()
                    .filter(theme -> theme.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return null;
    }
}
