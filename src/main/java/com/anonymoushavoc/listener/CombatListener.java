package com.anonymoushavoc.listener;

import com.anonymoushavoc.AnonymousHavocPlugin;
import com.anonymoushavoc.relic.Relic;
import com.anonymoushavoc.util.EffectUtil;
import com.cryptomorin.xseries.XAttribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class CombatListener implements Listener {
    
    private final AnonymousHavocPlugin plugin;
    
    public CombatListener(AnonymousHavocPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player attacker = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();
        
        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        
        if (!plugin.getRelicManager().isRelic(weapon)) {
            return;
        }
        
        Relic relic = plugin.getRelicManager().getRelicType(weapon);
        
        if (relic == Relic.SOUL_BINDER_DAGGER) {
            handleSoulBinderDagger(attacker, victim, event);
        } else if (relic == Relic.SHARD_OF_ANONYMITY) {
            handleShardLifesteal(attacker, victim, event);
        }
    }
    
    private void handleSoulBinderDagger(Player attacker, Player victim, EntityDamageByEntityEvent event) {
        if (plugin.getCooldownManager().isOnCooldown(attacker, "soul_binder")) {
            long remaining = plugin.getCooldownManager().getRemainingCooldown(attacker, "soul_binder");
            attacker.sendActionBar("§c✖ Cooldown: " + (remaining / 1000) + "s");
            return;
        }
        
        event.setDamage(1.0);
        
        plugin.getCooldownManager().setCooldown(attacker, "soul_binder", 5000);
        
        EffectUtil.playRelicEffect(victim, Relic.SOUL_BINDER_DAGGER);
        
        attacker.sendActionBar("§d§l⚔ §5Soul Binder hit!");
    }
    
    private void handleShardLifesteal(Player attacker, Player victim, EntityDamageByEntityEvent event) {
        double damage = event.getFinalDamage();
        double lifesteal = damage * 0.15;
        
        XAttribute.of("max_health").ifPresent(attr -> {
            double currentHealth = attacker.getHealth();
            double maxHealth = attacker.getAttribute(attr.get()).getValue();
            double newHealth = Math.min(currentHealth + lifesteal, maxHealth);
            attacker.setHealth(newHealth);
        });
        
        attacker.sendActionBar("§c♥ §a+§f" + String.format("%.1f", lifesteal) + " §7HP");
    }
}