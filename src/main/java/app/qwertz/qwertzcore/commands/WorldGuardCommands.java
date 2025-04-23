package app.qwertz.qwertzcore.commands;

import app.qwertz.qwertzcore.QWERTZcore;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

public class WorldGuardCommands implements CommandExecutor {

    private final QWERTZcore plugin;
    public WorldGuardCommands(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    public boolean enableHighFrequencyFlags() {
        File configFile = new File(WorldGuard.getInstance().getPlatform().getGlobalStateManager().getDataFolder(), "config.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        config.set("regions.high-frequency-flags", true);
        try {
            config.save(configFile);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wg reload");
            return true;
        } catch (IOException e) {
            this.plugin.getLogger().warning("We were unable to enable 'high-frequency-flags' option in the WorldGuard config. /flow will be broken. ");
            return false;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "general.only-player-execute");
            return true;
        }

        Player player = (Player) sender;
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(player.getWorld()));

        if (regions == null) {
            plugin.getMessageManager().sendMessage(player, "worldguard.no-regions");
            plugin.getSoundManager().playSound(player);
            return true;
        }

        ProtectedRegion region;
        boolean isGlobal;

        if (args.length > 0) {
            String regionName = args[0];
            region = regions.getRegion(regionName);
            if (region == null) {
                HashMap<String, String> localMap = new HashMap<>();
                localMap.put("%name%", regionName);
                plugin.getMessageManager().sendMessage(player, "worldguard.not-found", localMap);
                plugin.getSoundManager().playSound(player);
                return true;
            }
            isGlobal = false;
        } else {
            ApplicableRegionSet applicableRegions = regions.getApplicableRegions(BukkitAdapter.asBlockVector(player.getLocation()));
            region = null;
            for (ProtectedRegion r : applicableRegions) {
                if (region == null || r.getPriority() > region.getPriority()) {
                    region = r;
                }
            }
            isGlobal = region == null;
            if (isGlobal) {
                region = regions.getRegion("__global__");
            }
        }

        StateFlag flag;
        String flagName;

        switch (command.getName().toLowerCase()) {
            case "flow":
            case "toggleflow":
                flag = Flags.WATER_FLOW;
                flagName = plugin.getMessageManager().prepareMessage(plugin.getMessageManager().getMessage("worldguard.flags.flow"), new HashMap<>());
                break;
            case "pvp":
            case "togglepvp":
                flag = Flags.PVP;
                flagName = plugin.getMessageManager().prepareMessage(plugin.getMessageManager().getMessage("worldguard.flags.pvp"), new HashMap<>());
                break;
            case "break":
            case "togglebreak":
                flag = Flags.BLOCK_BREAK;
                flagName = plugin.getMessageManager().prepareMessage(plugin.getMessageManager().getMessage("worldguard.flags.break"), new HashMap<>());
                break;
            case "place":
            case "toggleplace":
                flag = Flags.BLOCK_PLACE;
                flagName = plugin.getMessageManager().prepareMessage(plugin.getMessageManager().getMessage("worldguard.flags.place"), new HashMap<>());
                break;
            case "falldamage":
            case "togglefalldamage":
                flag = Flags.FALL_DAMAGE;
                flagName = plugin.getMessageManager().prepareMessage(plugin.getMessageManager().getMessage("worldguard.flags.falldamage"), new HashMap<>());
                break;
            case "hunger":
            case "togglehunger":
                flag = Flags.HUNGER_DRAIN;
                flagName = plugin.getMessageManager().prepareMessage(plugin.getMessageManager().getMessage("worldguard.flags.hunger"), new HashMap<>());
                break;
            default:
                return false;
        }
        if (region == null) {
            plugin.getMessageManager().sendMessage(player, "worldguard.invalid-region");
            return true;
        }
        boolean currentState = region.getFlag(flag) != StateFlag.State.DENY;
        if (!flag.equals(Flags.WATER_FLOW)) {
            region.setFlag(flag, currentState ? StateFlag.State.DENY : StateFlag.State.ALLOW);
        }else{
            StateFlag[] FlowFlags = {Flags.WATER_FLOW, Flags.LAVA_FLOW};
            for (StateFlag loop : FlowFlags) {
                region.setFlag(loop, currentState ? StateFlag.State.DENY : StateFlag.State.ALLOW);
            }
            enableHighFrequencyFlags();
        }
        String newState = currentState ? "DISABLED" : "ENABLED";
        String stateColor = currentState ? "%colorDead%" : "%colorAlive%";
        HashMap<String, String> localMap = new HashMap<>();
        localMap.put("%name%", sender.getName());
        localMap.put("%state%", newState);
        localMap.put("%stateLower%", newState.toLowerCase(Locale.ROOT));
        localMap.put("%stateColor%", stateColor);
        localMap.put("%flag%", flagName);


        if (isGlobal) {
            plugin.getMessageManager().broadcastMessage("worldguard.globally", localMap);
        } else {
            localMap.put("%region%", region.getId());
            plugin.getMessageManager().broadcastMessage("worldguard.regionally", localMap);
        }
        plugin.getSoundManager().broadcastConfigSound();
        return true;
    }
}