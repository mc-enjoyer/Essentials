package com.earth2me.essentials.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.HashMap;
import java.util.Map;

public class SpawnerNamingUtil
{
    // Mob spawner name mapping based on ID and data values
    private static final Map<String, String> SPAWNER_NAMES = new HashMap<>();
    
    static {
        SPAWNER_NAMES.put("52:50", "Creeper Spawner");
        SPAWNER_NAMES.put("52:51", "Skeleton Spawner");
        SPAWNER_NAMES.put("52:52", "Spider Spawner");
        SPAWNER_NAMES.put("52:53", "Giant Spawner");
        SPAWNER_NAMES.put("52:54", "Zombie Spawner");
        SPAWNER_NAMES.put("52:55", "Slime Spawner");
        SPAWNER_NAMES.put("52:56", "Ghast Spawner");
        SPAWNER_NAMES.put("52:57", "Pig Zombie Spawner");
        SPAWNER_NAMES.put("52:58", "Enderman Spawner");
        SPAWNER_NAMES.put("52:59", "Cave Spider Spawner");
        SPAWNER_NAMES.put("52:60", "Silverfish Spawner");
        SPAWNER_NAMES.put("52:61", "Blaze Spawner");
        SPAWNER_NAMES.put("52:62", "Magma Cube Spawner");
        SPAWNER_NAMES.put("52:63", "Ender Dragon Spawner");
        SPAWNER_NAMES.put("52:64", "Wither Spawner");
        SPAWNER_NAMES.put("52:65", "Bat Spawner");
        SPAWNER_NAMES.put("52:66", "Witch Spawner");
        SPAWNER_NAMES.put("52:90", "Pig Spawner");
        SPAWNER_NAMES.put("52:91", "Sheep Spawner");
        SPAWNER_NAMES.put("52:92", "Cow Spawner");
        SPAWNER_NAMES.put("52:93", "Chicken Spawner");
        SPAWNER_NAMES.put("52:94", "Squid Spawner");
        SPAWNER_NAMES.put("52:95", "Wolf Spawner");
        SPAWNER_NAMES.put("52:96", "Mooshroom Spawner");
        SPAWNER_NAMES.put("52:97", "Snowman Spawner");
        SPAWNER_NAMES.put("52:98", "Ocelot Spawner");
        SPAWNER_NAMES.put("52:99", "Iron Golem Spawner");
        SPAWNER_NAMES.put("52:100", "Horse Spawner");
    }
    
    /**
     * Checks if an item is a mob spawner and applies the appropriate display name
     * @param item The ItemStack to check and potentially rename
     * @return The modified ItemStack with spawner name if applicable, or the original item
     */
    public static ItemStack applySpawnerNaming(ItemStack item)
    {
        if (item == null || item.getType() != Material.MOB_SPAWNER)
        {
            return item;
        }
        
        // Get the spawner's ID and data value
        short durability = item.getDurability();
        String spawnerKey = "52:" + durability;
        
        // Check if we have a mapping for this spawner type
        String newName = SPAWNER_NAMES.get(spawnerKey);
        if (newName != null)
        {
            ItemMeta meta = item.getItemMeta();
            if (meta != null)
            {
                meta.setDisplayName(ChatColor.RED + newName);
                item.setItemMeta(meta);
            }
        }
        
        return item;
    }
    
    /**
     * Checks if an item is a mob spawner
     * @param item The ItemStack to check
     * @return true if the item is a mob spawner
     */
    public static boolean isSpawner(ItemStack item)
    {
        return item != null && item.getType() == Material.MOB_SPAWNER;
    }
} 