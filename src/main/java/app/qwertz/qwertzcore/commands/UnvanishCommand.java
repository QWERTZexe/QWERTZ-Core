package app.qwertz.qwertzcore.commands;

import app.qwertz.qwertzcore.QWERTZcore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static org.bukkit.Bukkit.broadcastMessage;

public class UnvanishCommand implements CommandExecutor {
    private final QWERTZcore plugin;

    public UnvanishCommand(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (sender instanceof Player p) {
            if (plugin.getVanishManager().getVanishedPlayers().contains(p.getName())) {
                if (plugin.getConfigManager().getMsgsOnVanish()) {
                    int fakeCount = plugin.getVanishManager().getFakePlayerCount();
                    int newCount = fakeCount + 1;
                    broadcastMessage(QWERTZcore.CORE_ICON + ChatColor.YELLOW + " " + p.getName() +
                            ChatColor.RED + " Just joined us! " + ChatColor.GRAY + "(" +
                            ChatColor.AQUA + fakeCount + ChatColor.GRAY + "->" +
                            ChatColor.AQUA + newCount + ChatColor.GRAY + ")");
                }
                p.sendMessage(QWERTZcore.CORE_ICON + ChatColor.YELLOW + " You have been unvanished.");
                plugin.getVanishManager().removeVanishedPlayer(p);
                for (Player loop : Bukkit.getOnlinePlayers()) {
                    loop.showPlayer(p);
                }
            }else{
                p.sendMessage(ChatColor.RED + "You aren't vanished!");
            }
        }
        return true;
    }
}
