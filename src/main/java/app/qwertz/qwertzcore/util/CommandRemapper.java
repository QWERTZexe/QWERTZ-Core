package app.qwertz.qwertzcore.util;

import app.qwertz.qwertzcore.QWERTZcore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

public class CommandRemapper implements CommandExecutor {

    private final QWERTZcore plugin;
    private final CommandExecutor originalExecutor;
    private final String commandName;

    public CommandRemapper(QWERTZcore plugin, CommandExecutor originalExecutor, String commandName) {
        this.plugin = plugin;
        this.originalExecutor = originalExecutor;
        this.commandName = commandName;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.toLowerCase().startsWith("qwertzcore:")) {
            plugin.getLogger().warning("The command /" + label + " is deprecated. Please use /" + commandName + " instead.");
            // Remap to the original command
            PluginCommand originalCommand = plugin.getCommand(commandName);
            if (originalCommand != null) {
                return originalCommand.execute(sender, commandName, args);
            }
        }
        // Execute the original command
        return originalExecutor.onCommand(sender, command, label, args);
    }
}