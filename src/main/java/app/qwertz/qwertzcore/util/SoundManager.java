/*
        Copyright (C) 2024 QWERTZ_EXE

        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License
        as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
        without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
        See the GNU Affero General Public License for more details.

        You should have received a copy of the GNU Affero General Public License along with this program.
        If not, see <http://www.gnu.org/licenses/>.
*/

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
                plugin.getLogger().warning("The sound (or pitch/volume) in the config of QWERTZ Core is invalid. Please change it/them or the sounds won't play.");
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
