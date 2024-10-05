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

package app.qwertz.qwertzcore.blocks;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class GravityFlipBlock extends QWERTZcoreBlock {
    private static final int GRAVITY_FLIP_DURATION = 100; // 5 seconds (20 ticks per second)

    public GravityFlipBlock(Material material) {
        super(material != null ? material : QWERTZcoreBlockType.GRAVITY_FLIP_BLOCK.getDefaultMaterial());
    }

    @Override
    public void onTouch(Player player, Block block) {
        flipGravity(player);
    }

    @Override
    public void onMine(Player player, Block block) {
        // Do nothing when mined
    }

    private void flipGravity(Player player) {
        // Apply levitation effect to simulate reversed gravity
        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, GRAVITY_FLIP_DURATION, 10));

    }
}