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
    public void hideVanishedPlayers(Player p) {
        for (Player loop : Bukkit.getOnlinePlayers()) {
            if(vanishedPlayers.contains(loop.getName())) {
                p.hidePlayer(loop);
            }
        }
    }
    public void removeVanishedPlayer(Player p) {
        vanishedPlayers.remove(p.getName());
    }
    public void addVanishedPlayer(Player p) {
        vanishedPlayers.add(p.getName());
    }
    public int getFakePlayerCount() {
        return Bukkit.getOnlinePlayers().size() - vanishedPlayers.size();
    }
    public List getVanishedPlayers() {
        return(vanishedPlayers);
    }
}
