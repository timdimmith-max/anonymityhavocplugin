package com.anonymoushavoc.identity;

import com.anonymoushavoc.AnonymousHavocPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RevealManager {
    
    private final AnonymousHavocPlugin plugin;
    private final Map<UUID, Set<UUID>> revealedPlayers = new ConcurrentHashMap<>();
    private final Set<UUID> permanentImmunity = ConcurrentHashMap.newKeySet();
    
    public RevealManager(AnonymousHavocPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void revealPlayer(Player viewer, Player target, int durationSeconds) {
        UUID viewerUuid = viewer.getUniqueId();
        UUID targetUuid = target.getUniqueId();
        
        if (hasImmunity(targetUuid)) {
            return;
        }
        
        revealedPlayers.computeIfAbsent(viewerUuid, k -> ConcurrentHashMap.newKeySet())
                .add(targetUuid);
        
        if (durationSeconds > 0) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> 
                    maskPlayer(viewer, target), durationSeconds * 20L);
        }
    }
    
    public void maskPlayer(Player viewer, Player target) {
        UUID viewerUuid = viewer.getUniqueId();
        UUID targetUuid = target.getUniqueId();
        
        Set<UUID> revealed = revealedPlayers.get(viewerUuid);
        if (revealed != null) {
            revealed.remove(targetUuid);
            if (revealed.isEmpty()) {
                revealedPlayers.remove(viewerUuid);
            }
        }
    }
    
    public boolean isPlayerRevealed(Player viewer) {
        return revealedPlayers.containsKey(viewer.getUniqueId());
    }
    
    public boolean isTargetRevealedTo(Player viewer, Player target) {
        Set<UUID> revealed = revealedPlayers.get(viewer.getUniqueId());
        return revealed != null && revealed.contains(target.getUniqueId());
    }
    
    public void grantImmunity(UUID playerUuid) {
        permanentImmunity.add(playerUuid);
    }
    
    public void revokeImmunity(UUID playerUuid) {
        permanentImmunity.remove(playerUuid);
    }
    
    public boolean hasImmunity(UUID playerUuid) {
        return permanentImmunity.contains(playerUuid);
    }
    
    public void clearViewer(UUID viewerUuid) {
        revealedPlayers.remove(viewerUuid);
    }
}