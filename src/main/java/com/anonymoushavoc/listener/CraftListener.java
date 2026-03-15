package com.anonymoushavoc.listener;

import com.anonymoushavoc.AnonymousHavocPlugin;
import com.anonymoushavoc.relic.Relic;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class CraftListener implements Listener {
    
    private final AnonymousHavocPlugin plugin;
    
    public CraftListener(AnonymousHavocPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onCraft(CraftItemEvent event) {
        ItemStack result = event.getRecipe().getResult();
        
        if (!plugin.getRelicManager().isRelic(result)) {
            return;
        }
        
        Relic relic = plugin.getRelicManager().getRelicType(result);
        Player player = (Player) event.getWhoClicked();
        
        if (!plugin.getRelicManager().canCraftRelic(relic)) {
            event.setCancelled(true);
            
            if (!plugin.getRelicManager().isRelicReleased(relic)) {
                player.sendMessage("§c§lRESTRICTED: §7This relic has not been released yet.");
            } else {
                player.sendMessage("§c§lLIMIT REACHED: §7The global limit for this relic has been met.");
            }
            
            return;
        }
        
        plugin.getRelicManager().registerRelic(relic, player.getUniqueId(), player.getLocation());
        
        Location loc = player.getLocation();
        String announcement = String.format(
            "§6§l[CRAFTED] §f%s §7crafted §6%s §7at §f[%d, %d, %d] §c§l⚠ Happy Hunting!",
            player.getName(),
            relic.getDisplayName(),
            loc.getBlockX(),
            loc.getBlockY(),
            loc.getBlockZ()
        );
        
        Bukkit.broadcastMessage(announcement);
        
        XSound.matchXSound("UI_TOAST_CHALLENGE_COMPLETE").ifPresent(sound -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                sound.play(p, 1.0f, 1.0f);
            }
        });
        
        player.sendMessage("§6§l✦ §eYou have crafted a " + relic.getDisplayName() + "!");
    }
}