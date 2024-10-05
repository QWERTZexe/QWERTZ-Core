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
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class BlockManager {
    private final QWERTZcore plugin;
    private final Map<Location, QWERTZcoreBlock> specialBlocks;
    private final Map<String, Class<? extends QWERTZcoreBlock>> blockTypes;

    public BlockManager(QWERTZcore plugin) {
        this.plugin = plugin;
        this.specialBlocks = new HashMap<>();
        this.blockTypes = new HashMap<>();
        registerBlockTypes();
        loadSpecialBlocks();
    }

    private void registerBlockTypes() {
        blockTypes.put("DAMAGE_BLOCK", DamageBlock.class);
        blockTypes.put("INSTANT_DEATH_BLOCK", InstantDeathBlock.class);
        blockTypes.put("RANDOM_DROP_BLOCK", RandomDropBlock.class);
        blockTypes.put("GRAVITY_FLIP_BLOCK", GravityFlipBlock.class);
    }

    public void setSpecialBlock(Location location, String blockType, Material material) {
        try {
            QWERTZcoreBlockType type = QWERTZcoreBlockType.valueOf(blockType);
            QWERTZcoreBlock block = createBlock(type, material);
            specialBlocks.put(location, block);
            saveSpecialBlocks();
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Failed to create special block: " + e.getMessage());
        }
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
    public QWERTZcoreBlock getSpecialBlock(Location location) {
        return specialBlocks.get(location);
    }
    public void removeSpecialBlock(Location location) {
        if (specialBlocks.remove(location) != null) {
            saveSpecialBlocks();
            System.out.println("Removed special block at: " + location);
        }
    }



    private void loadSpecialBlocks() {

        Map<String, String> savedBlocks = plugin.getDatabaseManager().getSpecialBlocks();
        for (Map.Entry<String, String> entry : savedBlocks.entrySet()) {
            Location location = stringToLocation(entry.getKey());
            String blockType = entry.getValue();

            setSpecialBlock(location, blockType, null);
        }
    }

    public void saveSpecialBlocks() {
        Map<String, String> blocksToSave = new HashMap<>();
        for (Map.Entry<Location, QWERTZcoreBlock> entry : specialBlocks.entrySet()) {
            QWERTZcoreBlockType blockType = QWERTZcoreBlockType.fromBlock(entry.getValue());
            blocksToSave.put(locationToString(entry.getKey()), blockType.name());
        }
        plugin.getDatabaseManager().saveSpecialBlocks(blocksToSave);
    }

    private String locationToString(Location location) {
        return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ();
    }

    private Location stringToLocation(String str) {
        String[] parts = str.split(",");
        if (parts.length == 4) {
            return new Location(plugin.getServer().getWorld(parts[0]),
                    Double.parseDouble(parts[1]),
                    Double.parseDouble(parts[2]),
                    Double.parseDouble(parts[3]));
        }
        return null;
    }

    public boolean isValidBlockType(String blockType) {
        return blockTypes.containsKey(blockType);
    }

    public String[] getAvailableBlockTypes() {
        return blockTypes.keySet().toArray(new String[0]);
    }
}