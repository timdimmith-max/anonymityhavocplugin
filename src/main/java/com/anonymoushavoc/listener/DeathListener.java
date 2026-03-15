package com.anonymoushavoc.listener;

import com.anonymoushavoc.AnonymousHavocPlugin;
import com.anonymoushavoc.relic.Relic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class DeathListener implements Listener {
    
    private final AnonymousHavocPlugin plugin;
    
    public DeathListener(AnonymousHavocPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        
        if (killer == null || !isPlayerKill(victim)) {
            return;
        }
        
        ItemStack weapon = killer.getInventory().getItemInMainHand();
        
        boolean isSoulBinderKill = false;
        boolean isNamedWeaponKill = false;
        
        if (plugin.getRelicManager().isRelic(weapon)) {
            Relic relic = plugin.getRelicManager().getRelicType(weapon);
            if (relic == Relic.SOUL_BINDER_DAGGER) {
                isSoulBinderKill = true;
            }
        }
        
        if (weapon.hasItemMeta() && weapon.getItemMeta().hasDisplayName()) {
            String weaponName = weapon.getItemMeta().getDisplayName();
            if (weaponName.equals(victim.getName())) {
                isNamedWeaponKill = true;
            }
        }
        
        if (isSoulBinderKill) {
            plugin.getDeathManager().handleSoulBinderDeath(victim, killer.getName());
        } else if (isNamedWeaponKill) {
            plugin.getDeathManager().handleNamedWeaponDeath(victim, killer.getName());
        }
    }
    
    private boolean isPlayerKill(Player victim) {
        EntityDamageEvent lastDamage = victim.getLastDamageCause();
        if (lastDamage == null) {
            return false;
        }
        
        EntityDamageEvent.DamageCause cause = lastDamage.getCause();
        
        return cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK ||
               cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK ||
               cause == EntityDamageEvent.DamageCause.PROJECTILE;
    }
}