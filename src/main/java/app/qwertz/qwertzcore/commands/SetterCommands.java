package app.qwertz.qwertzcore.commands;

import app.qwertz.qwertzcore.QWERTZcore;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SetterCommands implements CommandExecutor {
    private final QWERTZcore plugin;

    public SetterCommands(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("setspawn")) {
            setSpawn(sender);
        } else if (command.getName().equalsIgnoreCase("setserver")) {
            setServer(sender, args);
        } else if (command.getName().equalsIgnoreCase("setevent")) {
            setEvent(sender, args);

        }
        return true;
    }

    public void setSpawn(CommandSender sender) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendConsole(sender, "general.only-player-execute");
        }
        Player player = (Player) sender;
        Location loc = player.getLocation();
        Map<String, Object> spawnMap = new HashMap<>();
        spawnMap.put("world", loc.getWorld().getName());
        spawnMap.put("x", loc.getX());
        spawnMap.put("y", loc.getY());
        spawnMap.put("z", loc.getZ());
        spawnMap.put("yaw", (double) loc.getYaw());
        spawnMap.put("pitch", (double) loc.getPitch());
        plugin.getConfigManager().set("spawn", spawnMap);
        plugin.getMessageManager().sendMessage((Player) sender, "config.set-spawn");
        plugin.getSoundManager().playSoundToSender(sender);
    }

    public void setServer(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendConsole(sender, "general.only-player-execute");
        }

        if (args.length != 1) {
            plugin.getMessageManager().sendInvalidUsage((Player) sender, "/setserver <server>");
            plugin.getSoundManager().playSoundToSender(sender);
            return;
        }
        String server = args[0];
        plugin.getConfigManager().set("server", server);
        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%value%", server);
        localMap.put("%key%", "server");
        plugin.getMessageManager().sendMessage((Player) sender, "config.set-key", localMap);
        plugin.getSoundManager().playSoundToSender(sender);
    }
    public void setEvent(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendConsole(sender, "general.only-player-execute");
        }

        if (args.length != 1) {
            plugin.getMessageManager().sendInvalidUsage((Player) sender, "/setevent <server>");
            plugin.getSoundManager().playSoundToSender(sender);
            return;
        }
        String event = args[0];
        plugin.getConfigManager().set("event", event);
        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%value%", event);
        localMap.put("%key%", "event");
        plugin.getMessageManager().sendMessage((Player) sender, "config.set-key", localMap);
        plugin.getSoundManager().playSoundToSender(sender);
    }
}
