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
            sender.sendMessage(plugin.getConfigManager().getColor("colorError") + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(player.getWorld()));

        if (regions == null) {
            player.sendMessage(plugin.getConfigManager().getColor("colorError") + "WorldGuard regions are not available in this world.");
            plugin.getSoundManager().playSound(player);
            return true;
        }

        ProtectedRegion region;
        boolean isGlobal;

        if (args.length > 0) {
            String regionName = args[0];
            region = regions.getRegion(regionName);
            if (region == null) {
                player.sendMessage(plugin.getConfigManager().getColor("colorError") + "Region '" + regionName + "' not found.");
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
                flagName = "FLOWING";
                break;
            case "pvp":
            case "togglepvp":
                flag = Flags.PVP;
                flagName = "PVP";
                break;
            case "break":
            case "togglebreak":
                flag = Flags.BLOCK_BREAK;
                flagName = "BLOCK BREAKING";
                break;
            case "place":
            case "toggleplace":
                flag = Flags.BLOCK_PLACE;
                flagName = "BLOCK PLACING";
                break;
            case "falldamage":
            case "togglefalldamage":
                flag = Flags.FALL_DAMAGE;
                flagName = "FALL DAMAGE";
                break;
            case "hunger":
            case "togglehunger":
                flag = Flags.HUNGER_DRAIN;
                flagName = "HUNGER";
                break;
            default:
                return false;
        }

        boolean currentState = region.getFlag(flag) != StateFlag.State.DENY;
        if (flagName != "FLOWING") {
            region.setFlag(flag, currentState ? StateFlag.State.DENY : StateFlag.State.ALLOW);
        }else{
            StateFlag[] FlowFlags = {Flags.WATER_FLOW, Flags.LAVA_FLOW};
            enableHighFrequencyFlags();
            for (StateFlag loop : FlowFlags) {
                region.setFlag(loop, currentState ? StateFlag.State.DENY : StateFlag.State.ALLOW);
            }
        }
        String scope = isGlobal ? plugin.getConfigManager().getColor("colorSecondary") + " GLOBALLY" : " in region " + plugin.getConfigManager().getColor("colorSecondary") + "'" + region.getId() + "'";
        String newState = currentState ? plugin.getConfigManager().getColor("colorDead") + "DISABLED" : plugin.getConfigManager().getColor("colorAlive") + "ENABLED";
        plugin.getMessageManager().broadcastMessage(QWERTZcore.CORE_ICON + " " + plugin.getConfigManager().getColor("colorPrimary") + sender.getName() + plugin.getConfigManager().getColor("colorSuccess") + " just " + newState + " " + plugin.getConfigManager().getColor("colorPrimary") + flagName + plugin.getConfigManager().getColor("colorSuccess") + scope + plugin.getConfigManager().getColor("colorSuccess") + "!");
        plugin.getSoundManager().broadcastConfigSound();
        return true;
    }
}