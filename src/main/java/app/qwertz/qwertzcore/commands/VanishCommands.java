package app.qwertz.qwertzcore.commands;

import app.qwertz.qwertzcore.QWERTZcore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static org.bukkit.Bukkit.broadcastMessage;

public class VanishCommands implements CommandExecutor {
    private final QWERTZcore plugin;

    public VanishCommands(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (label.toLowerCase()) {
            case "vanish":
                return vanish(sender);
            case "unvanish":
                return unVanish(sender);
        }
        return false;
    }

    public boolean vanish(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (!plugin.getVanishManager().getVanishedPlayers().contains(((Player) sender).getUniqueId())) {
            if (plugin.getConfigManager().getMsgsOnVanish()) {
                int fakeCount = plugin.getVanishManager().getNonVanishedPlayerCount();
                int newCount = fakeCount - 1;
                broadcastMessage(QWERTZcore.CORE_ICON + ChatColor.YELLOW + " " + sender.getName() +
                        ChatColor.RED + " just left us! " + ChatColor.GRAY +
                        "[" + ChatColor.AQUA + fakeCount + ChatColor.GRAY +
                        " -> " + ChatColor.AQUA + newCount + ChatColor.GRAY + "]");
                plugin.getSoundManager().broadcastConfigSound();
            }
            sender.sendMessage(QWERTZcore.CORE_ICON + ChatColor.YELLOW + " You have been vanished!");
            plugin.getSoundManager().playSound(player);
            plugin.getVanishManager().addVanishedPlayer((Player) sender);
            for (Player loop : Bukkit.getOnlinePlayers()) {
                loop.hidePlayer((Player) sender);
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You are already vanished!");
            plugin.getSoundManager().playSound(player);
        }

        return true;
    }

    public boolean unVanish(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (plugin.getVanishManager().getVanishedPlayers().contains(((Player) sender).getUniqueId())) {
            if (plugin.getConfigManager().getMsgsOnVanish()) {
                int fakeCount = plugin.getVanishManager().getNonVanishedPlayerCount();
                int newCount = fakeCount + 1;
                broadcastMessage(QWERTZcore.CORE_ICON + ChatColor.YELLOW + " " + sender.getName() +
                        ChatColor.GREEN + " just joined! " + ChatColor.GRAY + "[" +
                        ChatColor.AQUA + fakeCount + ChatColor.GRAY + " -> " +
                        ChatColor.AQUA + newCount + ChatColor.GRAY + "]");
                plugin.getSoundManager().broadcastConfigSound();
            }
            sender.sendMessage(QWERTZcore.CORE_ICON + ChatColor.YELLOW + " You have been unvanished!");
            plugin.getSoundManager().playSound(player);
            plugin.getSoundManager().playSound(player);
            plugin.getVanishManager().removeVanishedPlayer((Player) sender);
            for (Player loop : Bukkit.getOnlinePlayers()) {
                loop.showPlayer((Player) sender);
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You are not vanished!");
            plugin.getSoundManager().playSound(player);
        }

        return true;
    }
}