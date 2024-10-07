package app.qwertz.qwertzcore.util;

import app.qwertz.qwertzcore.QWERTZcore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class VanishManager {
    private final QWERTZcore plugin;
    List<UUID> vanishedPlayers = new ArrayList<>();
    
    public VanishManager(QWERTZcore plugin) {
        this.plugin = plugin;
    }
    
    public void hideVanishedPlayers(Player player) {
        for (Player loop : Bukkit.getOnlinePlayers()) {
            if(vanishedPlayers.contains(loop.getUniqueId())) {
                player.hidePlayer(loop);
            }
        }
    }
    
    public void removeVanishedPlayer(Player player) {
        vanishedPlayers.remove(player.getUniqueId());
    }
    
    public void addVanishedPlayer(Player player) {
        vanishedPlayers.add(player.getUniqueId());
    }
    
    public int getNonVanishedPlayerCount() {
        return Bukkit.getOnlinePlayers().size() - vanishedPlayers.size();
    }
    
    public List getVanishedPlayers() {
        return(vanishedPlayers);
    }
}
