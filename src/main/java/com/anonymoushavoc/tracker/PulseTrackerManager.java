package com.anonymoushavoc.tracker;

import com.anonymoushavoc.AnonymousHavocPlugin;
import com.anonymoushavoc.relic.Relic;
import com.anonymoushavoc.util.EffectUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PulseTrackerManager {
    
    private final AnonymousHavocPlugin plugin;
    private final Map<UUID, BossBar> activeBars = new HashMap<>();
    private final Map<UUID, UUID> trackingTargets = new HashMap<>();
    
    public PulseTrackerManager(AnonymousHavocPlugin plugin) {
        this.plugin = plugin;
        startUpdateTask();
    }
    
    public void toggleTracking(Player tracker) {
        UUID trackerUuid = tracker.getUniqueId();
        
        if (activeBars.containsKey(trackerUuid)) {
            stopTracking(tracker);
            tracker.sendMessage("§c✖ Pulse Tracker disabled");
        } else {
            Player target = findNearestPlayer(tracker);
            if (target == null) {
                tracker.sendMessage("§c✖ No players detected in range");
                return;
            }
            
            startTracking(tracker, target);
            tracker.sendMessage("§a✔ Pulse Tracker activated");
            EffectUtil.playRelicEffect(tracker, Relic.HUNTERS_COMPASS);
        }
    }
    
    private void startTracking(Player tracker, Player target) {
        UUID trackerUuid = tracker.getUniqueId();
        
        BossBar bar = Bukkit.createBossBar(
            "§6Tracking...",
            BarColor.YELLOW,
            BarStyle.SEGMENTED_10
        );
        
        bar.addPlayer(tracker);
        bar.setVisible(true);
        
        activeBars.put(trackerUuid, bar);
        trackingTargets.put(trackerUuid, target.getUniqueId());
    }
    
    public void stopTracking(Player tracker) {
        UUID trackerUuid = tracker.getUniqueId();
        
        BossBar bar = activeBars.remove(trackerUuid);
        if (bar != null) {
            bar.removeAll();
        }
        
        trackingTargets.remove(trackerUuid);
    }
    
    private void startUpdateTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Map.Entry<UUID, BossBar> entry : new HashMap<>(activeBars).entrySet()) {
                UUID trackerUuid = entry.getKey();
                BossBar bar = entry.getValue();
                
                Player tracker = Bukkit.getPlayer(trackerUuid);
                if (tracker == null || !tracker.isOnline()) {
                    stopTracking(tracker);
                    continue;
                }
                
                ItemStack mainHand = tracker.getInventory().getItemInMainHand();
                if (!plugin.getRelicManager().isRelic(mainHand) ||
                    plugin.getRelicManager().getRelicType(mainHand) != Relic.HUNTERS_COMPASS) {
                    stopTracking(tracker);
                    continue;
                }
                
                UUID targetUuid = trackingTargets.get(trackerUuid);
                Player target = Bukkit.getPlayer(targetUuid);
                
                if (target == null || !target.isOnline() || target.isDead()) {
                    target = findNearestPlayer(tracker);
                    if (target == null) {
                        stopTracking(tracker);
                        tracker.sendMessage("§c✖ Target lost - No players in range");
                        continue;
                    }
                    trackingTargets.put(trackerUuid, target.getUniqueId());
                }
                
                updateBossBar(tracker, target, bar);
            }
        }, 10L, 10L);
    }
    
    private void updateBossBar(Player tracker, Player target, BossBar bar) {
        Location targetLoc = getEffectiveLocation(target);
        Location trackerLoc = tracker.getLocation();
        
        double distance = trackerLoc.distance(targetLoc);
        
        String realName = plugin.getRevealManager().isTargetRevealedTo(tracker, target) 
            ? target.getName() 
            : plugin.getDatabaseManager().getRealName(target.getUniqueId());
        
        double yawDiff = calculateYawDifference(trackerLoc, targetLoc, tracker.getLocation().getYaw());
        double progress = Math.abs(yawDiff) / 180.0;
        progress = 1.0 - Math.min(1.0, progress);
        
        String direction = getDirectionIndicator(yawDiff);
        
        bar.setTitle(String.format("§6⚡ §f%s §7[%s] §e%.0fm", realName, direction, distance));
        bar.setProgress(progress);
        
        if (distance < 50) {
            bar.setColor(BarColor.RED);
        } else if (distance < 150) {
            bar.setColor(BarColor.YELLOW);
        } else {
            bar.setColor(BarColor.GREEN);
        }
        
        if (distance < 100 && System.currentTimeMillis() % 1000 < 500) {
            EffectUtil.playTrackingPulse(tracker, distance);
        }
    }
    
    private double calculateYawDifference(Location from, Location to, float trackerYaw) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        
        double targetYaw = Math.toDegrees(Math.atan2(-dx, dz));
        
        double diff = targetYaw - trackerYaw;
        
        while (diff > 180) diff -= 360;
        while (diff < -180) diff += 360;
        
        return diff;
    }
    
    private String getDirectionIndicator(double yawDiff) {
        if (Math.abs(yawDiff) < 15) {
            return "▲▲▲";
        } else if (Math.abs(yawDiff) < 45) {
            return yawDiff > 0 ? "▲▲▶" : "◀▲▲";
        } else if (Math.abs(yawDiff) < 90) {
            return yawDiff > 0 ? "▲▶▶" : "◀◀▲";
        } else if (Math.abs(yawDiff) < 135) {
            return yawDiff > 0 ? "▶▶▼" : "◀◀▼";
        } else {
            return "▼▼▼";
        }
    }
    
    private Player findNearestPlayer(Player tracker) {
        return tracker.getWorld().getPlayers().stream()
            .filter(p -> !p.equals(tracker))
            .filter(p -> !p.isDead())
            .filter(p -> p.getLocation().distance(tracker.getLocation()) <= 500)
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
        UUID mobUuid = plugin.getDatabaseManager().getLockedMobUuid(Relic.TOTEM_OF_SCRAMBLING);
        
        if (mobUuid != null) {
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
    
    public void cleanup() {
        for (BossBar bar : activeBars.values()) {
            bar.removeAll();
        }
        activeBars.clear();
        trackingTargets.clear();
    }
}