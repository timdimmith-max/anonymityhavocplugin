package com.anonymoushavoc.task;

import com.anonymoushavoc.AnonymousHavocPlugin;
import com.anonymoushavoc.relic.RelicData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class ItemDropTask extends BukkitRunnable {
    
    private final AnonymousHavocPlugin plugin;
    
    public ItemDropTask(AnonymousHavocPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void run() {
        List<RelicData> allRelics = plugin.getDatabaseManager().getAllRelics();
        long thresholdMillis = plugin.getConfigManager().getOfflineThresholdHours() * 60 * 60 * 1000L;
        long currentTime = System.currentTimeMillis();
        
        for (RelicData relicData : allRelics) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(relicData.getOwnerUuid());
            
            if (!owner.isOnline()) {
                long offlineTime = currentTime - owner.getLastPlayed();
                
                if (offlineTime > thresholdMillis) {
                    dropRelic(relicData);
                }
            }
        }
    }
    
    private void dropRelic(RelicData relicData) {
        if (relicData.getLastLocationWorld() == null) {
            plugin.getLogger().warning("Cannot drop relic - no last location stored");
            return;
        }
        
        World world = Bukkit.getWorld(relicData.getLastLocationWorld());
        if (world == null) {
            plugin.getLogger().warning("Cannot drop relic - world not found: " + relicData.getLastLocationWorld());
            return;
        }
        
        Location dropLocation = new Location(
                world,
                relicData.getLastLocationX(),
                relicData.getLastLocationY(),
                relicData.getLastLocationZ()
        );
        
        ItemStack relicItem = plugin.getRelicManager().createRelicItem(relicData.getType());
        world.dropItemNaturally(dropLocation, relicItem);
        
        plugin.getDatabaseManager().deleteRelic(relicData.getType());
        
        Bukkit.broadcastMessage("§c§l⚠ §7A relic has been dropped due to inactivity!");
        
        plugin.getLogger().info("Dropped " + relicData.getType() + " at " + 
                dropLocation.getBlockX() + ", " + dropLocation.getBlockY() + ", " + dropLocation.getBlockZ());
    }
}