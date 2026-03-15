package com.anonymoushavoc.util;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {
    
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    
    public boolean isOnCooldown(Player player, String key) {
        UUID uuid = player.getUniqueId();
        if (!cooldowns.containsKey(uuid)) {
            return false;
        }
        
        Map<String, Long> playerCooldowns = cooldowns.get(uuid);
        if (!playerCooldowns.containsKey(key)) {
            return false;
        }
        
        long expiry = playerCooldowns.get(key);
        if (System.currentTimeMillis() >= expiry) {
            playerCooldowns.remove(key);
            return false;
        }
        
        return true;
    }
    
    public void setCooldown(Player player, String key, long durationMillis) {
        UUID uuid = player.getUniqueId();
        cooldowns.computeIfAbsent(uuid, k -> new HashMap<>())
                .put(key, System.currentTimeMillis() + durationMillis);
    }
    
    public long getRemainingCooldown(Player player, String key) {
        UUID uuid = player.getUniqueId();
        if (!cooldowns.containsKey(uuid)) {
            return 0;
        }
        
        Map<String, Long> playerCooldowns = cooldowns.get(uuid);
        if (!playerCooldowns.containsKey(key)) {
            return 0;
        }
        
        long expiry = playerCooldowns.get(key);
        long remaining = expiry - System.currentTimeMillis();
        return Math.max(0, remaining);
    }
    
    public void clearCooldowns(UUID uuid) {
        cooldowns.remove(uuid);
    }
}