package com.anonymoushavoc.task;

import com.anonymoushavoc.AnonymousHavocPlugin;
import com.anonymoushavoc.relic.Relic;
import com.anonymoushavoc.relic.RelicData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class CoordinateHintTask extends BukkitRunnable {
    
    private final AnonymousHavocPlugin plugin;
    private final Random random = new Random();
    
    public CoordinateHintTask(AnonymousHavocPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void run() {
        broadcastRelicHint(Relic.SHARD_OF_ANONYMITY);
        broadcastRelicHint(Relic.SOUL_BINDER_DAGGER);
    }
    
    private void broadcastRelicHint(Relic relic) {
        RelicData relicData = plugin.getDatabaseManager().getRelicByType(relic);
        
        if (relicData == null || relicData.getLastLocationWorld() == null) {
            return;
        }
        
        int radius = plugin.getConfigManager().getCoordinateHintRadius();
        
        int actualX = (int) relicData.getLastLocationX();
        int actualZ = (int) relicData.getLastLocationZ();
        
        int hintX = actualX + random.nextInt(radius * 2 + 1) - radius;
        int hintZ = actualZ + random.nextInt(radius * 2 + 1) - radius;
        
        String message = String.format(
                "§6§l⚠ RELIC DETECTED §7- %s §6near §f[%d, %d] §7(±%dm)",
                relic.getDisplayName(),
                hintX,
                hintZ,
                radius
        );
        
        Bukkit.broadcastMessage(message);
    }
}