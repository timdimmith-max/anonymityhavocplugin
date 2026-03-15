package com.anonymoushavoc.listener;

import com.anonymoushavoc.AnonymousHavocPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    
    private final AnonymousHavocPlugin plugin;
    
    public PlayerJoinListener(AnonymousHavocPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.getDeathManager().isPlayerBanned(event.getPlayer().getUniqueId())) {
            event.getPlayer().kickPlayer("You are still banned!");
        }
    }
}