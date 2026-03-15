package com.anonymoushavoc.listener;

import com.anonymoushavoc.AnonymousHavocPlugin;
import com.anonymoushavoc.relic.Relic;
import com.anonymoushavoc.relic.RelicData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

public class CompassListener implements Listener {
    
    private final AnonymousHavocPlugin plugin;
    
    public CompassListener(AnonymousHavocPlugin plugin) {
        this.plugin = plugin;
        startCompassUpdateTask();
    }
    
    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        
        if (plugin.getRelicManager().isRelic(newItem)) {
            Relic relic = plugin.getRelicManager().getRelicType(newItem);
            if (relic == Relic.HUNTERS_COMPASS) {
                updateCompassTarget(player);
            }
        }
    }
    
    private void startCompassUpdateTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                ItemStack mainHand = player.getInventory().getItemInMainHand();
                
                if (plugin.getRelicManager().isRelic(mainHand)) {
                    Relic relic = plugin.getRelicManager().getRelicType(mainHand);
                    if (relic == Relic.HUNTERS_COMPASS) {
                        updateCompassTarget(player);
                    }
                }
            }
        }, 20L, 20L);
    }
    
    private void updateCompassTarget(Player tracker) {
        Player nearestPlayer = findNearestPlayer(tracker);
        
        if (nearestPlayer == null) {
            tracker.sendActionBar("§7No players detected nearby");
            return;
        }
        
        Location targetLocation = getEffectiveLocation(nearestPlayer);
        tracker.setCompassTarget(targetLocation);
        
        double distance = tracker.getLocation().distance(targetLocation);
        tracker.sendActionBar("§6Tracking: §f" + nearestPlayer.getName() + 
                " §7(" + String.format("%.1f", distance) + "m)");
    }
    
    private Player findNearestPlayer(Player tracker) {
        return tracker.getWorld().getPlayers().stream()
                .filter(p -> !p.equals(tracker))
                .filter(p -> !p.isDead())
                .min(Comparator.comparingDouble(p -> 
                        p.getLocation().distance(tracker.getLocation())))
                .orElse(null);
    }
    
    private Location getEffectiveLocation(Player target) {
        ItemStack offhand = target.getInventory().getItemInOffHand();
        
        if (plugin.getRelicManager().isRelic(offhand)) {
            Relic relic = plugin.getRelicManager().getRelicType(offhand);
            
            if (relic == Relic.TOTEM_OF_SCRAMBLING) {
                return getScrambledLocation(target);
            }
        }
        
        return target.getLocation();
    }
    
    private Location getScrambledLocation(Player target) {
        RelicData totemData = plugin.getDatabaseManager().getRelicByType(Relic.TOTEM_OF_SCRAMBLING);
        
        if (totemData != null && totemData.getLockedMobUuid() != null) {
            UUID mobUuid = totemData.getLockedMobUuid();
            
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (entity.getUniqueId().equals(mobUuid) && entity instanceof LivingEntity) {
                        return entity.getLocation();
                    }
                }
            }
        }
        
        Optional<LivingEntity> distantMob = findDistantMob(target.getWorld(), target.getLocation());
        
        if (distantMob.isPresent()) {
            LivingEntity mob = distantMob.get();
            plugin.getDatabaseManager().saveLockedMob(Relic.TOTEM_OF_SCRAMBLING, mob.getUniqueId());
            return mob.getLocation();
        }
        
        return target.getLocation();
    }
    
    private Optional<LivingEntity> findDistantMob(World world, Location playerLoc) {
        return world.getLivingEntities().stream()
                .filter(e -> !(e instanceof Player))
                .filter(e -> {
                    Location mobLoc = e.getLocation();
                    return world.getPlayers().stream()
                            .allMatch(p -> p.getLocation().distance(mobLoc) > 100);
                })
                .findFirst();
    }
}