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

public enum QWERTZcoreBlockType {
    DAMAGE_BLOCK("DAMAGE_BLOCK", Material.RED_WOOL),
    INSTANT_DEATH_BLOCK("INSTANT_DEATH_BLOCK", Material.REDSTONE_BLOCK),
    RANDOM_DROP_BLOCK("RANDOM_DROP_BLOCK", Material.GRASS_BLOCK),
    GRAVITY_FLIP_BLOCK("GRAVITY_FLIP_BLOCK", Material.END_STONE);

    private final String name;
    private final Material defaultMaterial;

    QWERTZcoreBlockType(String name, Material defaultMaterial) {
        this.name = name;
        this.defaultMaterial = defaultMaterial;
    }
    public static QWERTZcoreBlockType fromBlock(QWERTZcoreBlock block) {
        if (block instanceof DamageBlock) return DAMAGE_BLOCK;
        if (block instanceof InstantDeathBlock) return INSTANT_DEATH_BLOCK;
        if (block instanceof RandomDropBlock) return RANDOM_DROP_BLOCK;
        if (block instanceof GravityFlipBlock) return GRAVITY_FLIP_BLOCK;
        throw new IllegalArgumentException("Unknown block type: " + block.getClass().getSimpleName());
    }

    public String getName() {
        return name;
    }

    public Material getDefaultMaterial() {
        return defaultMaterial;
    }
}