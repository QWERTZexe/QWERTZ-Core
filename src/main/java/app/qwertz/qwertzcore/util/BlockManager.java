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
import app.qwertz.qwertzcore.blocks.*;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class BlockManager {
    private final QWERTZcore plugin;
    private final Map<String, Class<? extends QWERTZcoreBlock>> blockTypes;

    public BlockManager(QWERTZcore plugin) {
        this.plugin = plugin;
        this.blockTypes = new HashMap<>();
        registerBlockTypes();
    }
    

    private void registerBlockTypes() {
        blockTypes.put("DAMAGE_BLOCK", DamageBlock.class);
        blockTypes.put("INSTANT_DEATH_BLOCK", InstantDeathBlock.class);
        blockTypes.put("RANDOM_DROP_BLOCK", RandomDropBlock.class);
        blockTypes.put("GRAVITY_FLIP_BLOCK", GravityFlipBlock.class);
    }

    /**
     * Get the special block handler for a given material
     * @param material The material to check
     * @return The special block handler, or null if the material is not registered
     */
    public QWERTZcoreBlock getSpecialBlock(Material material) {
        if (material == null) {
            return null;
        }
        
        String materialKey = material.getKey().toString();
        Map<String, String> specialBlocks = plugin.getConfigManager().getSpecialBlocks();
        
        // Find which block type uses this material
        for (Map.Entry<String, String> entry : specialBlocks.entrySet()) {
            String blockType = entry.getKey();
            String configuredMaterial = entry.getValue();
            
            // Only process if material is actually configured (not null)
            if (configuredMaterial != null && !configuredMaterial.isEmpty() && configuredMaterial.equals(materialKey)) {
                // Found a match - create and return the appropriate block handler
                try {
                    QWERTZcoreBlockType type = QWERTZcoreBlockType.valueOf(blockType);
                    return createBlock(type, material);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid block type: " + blockType);
                }
            }
        }
        
        return null;
    }
    
    private QWERTZcoreBlock createBlock(QWERTZcoreBlockType type, Material material) {
        switch (type) {
            case DAMAGE_BLOCK:
                return new DamageBlock(material);
            case INSTANT_DEATH_BLOCK:
                return new InstantDeathBlock(material);
            case RANDOM_DROP_BLOCK:
                return new RandomDropBlock(material);
            case GRAVITY_FLIP_BLOCK:
                return new GravityFlipBlock(material);
            default:
                throw new IllegalArgumentException("Unknown block type: " + type);
        }
    }
    
    public boolean isValidBlockType(String blockType) {
        return blockTypes.containsKey(blockType.toUpperCase());
    }

    public String[] getAvailableBlockTypes() {
        return blockTypes.keySet().toArray(new String[0]);
    }
}