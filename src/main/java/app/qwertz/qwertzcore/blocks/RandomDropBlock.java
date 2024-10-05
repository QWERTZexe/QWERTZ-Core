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
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class RandomDropBlock extends QWERTZcoreBlock {
    private static final List<Material> VALID_MATERIALS;
    private static final Random RANDOM = new Random();

    static {
        VALID_MATERIALS = Arrays.stream(Material.values())
                .filter(Material::isItem)
                .collect(Collectors.toList());
    }

    public RandomDropBlock(Material material) {
        super(material != null ? material : QWERTZcoreBlockType.RANDOM_DROP_BLOCK.getDefaultMaterial());
    }

    @Override
    public void onTouch(Player player, Block block) {
        // Do nothing on touch
    }

    @Override
    public void onMine(Player player, Block block) {
        if (!VALID_MATERIALS.isEmpty()) {
            Material randomMaterial = VALID_MATERIALS.get(RANDOM.nextInt(VALID_MATERIALS.size()));
            ItemStack randomItem = new ItemStack(randomMaterial);
            block.getWorld().dropItemNaturally(block.getLocation(), randomItem);
        }
    }
}