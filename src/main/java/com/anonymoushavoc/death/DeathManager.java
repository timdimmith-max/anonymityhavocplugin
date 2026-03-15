package com.anonymoushavoc.death;

import com.anonymoushavoc.AnonymousHavocPlugin;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DeathManager {
    
    private final AnonymousHavocPlugin plugin;
    private final Set<UUID> pendingBans = new HashSet<>();
    
    public DeathManager(AnonymousHavocPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void handleSoulBinderDeath(Player player, String executorName) {
        int banHours = 6;
        String kickMessage = ChatColor.translateAlternateColorCodes('&',
            "&4&l⚔ SOUL BOUND &r&c\n\nYou were killed by the Soul-Binder Dagger!\n\n&7Return in 6 hours.");
        
        banPlayer(player, banHours, "SOUL_BINDER", kickMessage);
        
        String broadcastMessage = "§4§l⚔ §c" + player.getName() + 
            " §4was soul-bound by the dagger! §c[6 hour ban]";
        Bukkit.broadcastMessage(broadcastMessage);
    }
    
    public void handleNamedWeaponDeath(Player player, String executorName) {
        int banHours = 3;
        String kickMessage = ChatColor.translateAlternateColorCodes('&',
            "&c&lEXECUTED &r&7\n\nKilled by a weapon bearing your name!\n\n&7Return in 3 hours.");
        
        banPlayer(player, banHours, "NAMED_WEAPON", kickMessage);
        
        String broadcastMessage = "§c§l⚔ §7" + player.getName() + 
            " §cwas executed! §7[3 hour ban]";
        Bukkit.broadcastMessage(broadcastMessage);
    }
    
    private void banPlayer(Player player, int hours, String banType, String kickMessage) {
        UUID uuid = player.getUniqueId();
        
        if (!pendingBans.contains(uuid)) {
            pendingBans.add(uuid);
        }
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (pendingBans.contains(uuid)) {
                executeBan(player, hours, banType, kickMessage);
                pendingBans.remove(uuid);
            }
        }, 5L);
    }
    
    private void executeBan(Player player, int hours, String banType, String kickMessage) {
        UUID uuid = player.getUniqueId();
        long banDuration = hours * 60 * 60 * 1000L;
        long unbanTime = System.currentTimeMillis() + banDuration;
        
        plugin.getDatabaseManager().saveBan(uuid, banType, unbanTime);
        
        Date expirationDate = new Date(unbanTime);
        Bukkit.getBanList(BanList.Type.NAME).addBan(
            player.getName(),
            kickMessage,
            expirationDate,
            "AnonymousHavoc"
        );
        
        player.kickPlayer(kickMessage);
    }
    
    public void clearPendingBan(UUID playerUuid) {
        pendingBans.remove(playerUuid);
    }
    
    public boolean isPlayerBanned(UUID playerUuid) {
        Long expiry = plugin.getDatabaseManager().getBanExpiry(playerUuid);
        if (expiry == null) {
            return false;
        }
        
        if (System.currentTimeMillis() >= expiry) {
            plugin.getDatabaseManager().removeBan(playerUuid);
            return false;
        }
        
        return true;
    }
    
    public void unbanPlayer(UUID playerUuid, String playerName) {
        plugin.getDatabaseManager().removeBan(playerUuid);
        Bukkit.getBanList(BanList.Type.NAME).pardon(playerName);
    }
}