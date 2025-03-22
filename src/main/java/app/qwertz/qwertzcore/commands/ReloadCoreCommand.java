package app.qwertz.qwertzcore.commands;

import app.qwertz.qwertzcore.QWERTZcore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCoreCommand implements CommandExecutor {
    private final QWERTZcore plugin;

    public ReloadCoreCommand(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        reload(sender);
        return true;
    }

    public void reload(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(QWERTZcore.CORE_ICON + " §6Reloading §aQWERTZ Core§6...");

        long startTime = System.currentTimeMillis();

        plugin.onDisable();
        plugin.onEnable();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        sender.sendMessage("");

        sender.sendMessage(QWERTZcore.CORE_ICON + " §aDone! §6Reload completed in §e" + duration + "ms§6.");
        sender.sendMessage("");

    }
}
