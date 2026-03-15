package com.anonymoushavoc.listener;

import com.anonymoushavoc.AnonymousHavocPlugin;
import com.anonymoushavoc.relic.Relic;
import com.anonymoushavoc.util.EffectUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.ItemStack;

public class TotemListener implements Listener {
    
    private final AnonymousHavocPlugin plugin;
    
    public TotemListener(AnonymousHavocPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTotemUse(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        ItemStack hand = event.getHand() != null 
            ? player.getInventory().getItem(event.getHand()) 
            : null;
        
        if (hand == null || !plugin.getRelicManager().isRelic(hand)) {
            if (plugin.getConfigManager().isDisableVanillaTotems()) {
                event.setCancelled(true);
                player.sendMessage("§c✖ Only the Totem of Scrambling can save you!");
            }
            return;
        }
        
        Relic relic = plugin.getRelicManager().getRelicType(hand);
        
        if (relic != Relic.TOTEM_OF_SCRAMBLING) {
            if (plugin.getConfigManager().isDisableVanillaTotems()) {
                event.setCancelled(true);
            }
            return;
        }
        
        EffectUtil.playRelicEffect(player, Relic.TOTEM_OF_SCRAMBLING);
        
        player.sendMessage("§d§l⚡ §5The Totem of Scrambling saved you from death!");
        
        if (plugin.getConfigManager().isTotemPreventsBan()) {
            plugin.getDeathManager().clearPendingBan(player.getUniqueId());
        }
    }
}