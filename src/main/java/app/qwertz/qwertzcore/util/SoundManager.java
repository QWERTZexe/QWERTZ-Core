package app.qwertz.qwertzcore.util;

import app.qwertz.qwertzcore.QWERTZcore;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SoundManager {

    private final QWERTZcore plugin;

    public SoundManager(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    public void broadcastConfigSound() {
        if(plugin.getConfigManager().playSounds()) {
            for (Player loop : Bukkit.getOnlinePlayers()) {
                loop.playSound(loop, Sound.valueOf(getConfigSound()), getSoundVolume(), getSoundPitch());
            }
        }
    }

    public void playSoundToSender(CommandSender sender) {
        if(plugin.getConfigManager().playSounds()) {
            try {
                if (sender instanceof Player player) {
                    player.playSound(player, Sound.valueOf(getConfigSound()), getSoundVolume(), getSoundPitch());}
            }
            catch(IllegalArgumentException e) {
                System.out.println("The sound (or pitch/volume) in the config of QWERTZ Core is invalid. Please change it/them or the sounds won't play.");
            }
        }
    }

    public void playSound(Player player) {
        if(plugin.getConfigManager().playSounds()) {
            try {
                player.playSound(player, Sound.valueOf(getConfigSound()), getSoundVolume(), getSoundPitch());
            }
            catch(IllegalArgumentException e) {
                System.out.println("The sound (or pitch/volume) in the config of QWERTZ Core is invalid. Please change it/them or the sounds won't play.");
            }
        }
    }

    public String getConfigSound() {
        return plugin.getConfigManager().getConfigSound();
    }

    public float getSoundVolume() {
        return (float) plugin.getConfigManager().getSoundVolume();
    }

    public float getSoundPitch() {
        return (float) plugin.getConfigManager().getSoundPitch();
    }

    public boolean playSounds() {
        return plugin.getConfigManager().playSounds();
    }
}
