package app.qwertz.qwertzcore.util;

import app.qwertz.qwertzcore.QWERTZcore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class VanishManager {
    private final QWERTZcore plugin;
    List<String> vanishedPlayers = new ArrayList<>();
    
    public VanishManager(QWERTZcore plugin) {
        this.plugin = plugin;
    }
    
    public void hideVanishedPlayers(Player player) {
        for (Player loop : Bukkit.getOnlinePlayers()) {
            if(vanishedPlayers.contains(loop.getName())) {
                player.hidePlayer(loop);
            }
        }
    }
    
    public void removeVanishedPlayer(Player player) {
        vanishedPlayers.remove(player.getName());
    }
    
    public void addVanishedPlayer(Player player) {
        vanishedPlayers.add(player.getName());
    }
    
    public int getNonVanishedPlayerCount() {
        return Bukkit.getOnlinePlayers().size() - vanishedPlayers.size();
    }
    
    public List getVanishedPlayers() {
        return(vanishedPlayers);
    }
}
