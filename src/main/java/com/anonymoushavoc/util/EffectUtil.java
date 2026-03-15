package com.anonymoushavoc.util;

import com.anonymoushavoc.relic.Relic;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class EffectUtil {
    
    public static void playRelicEffect(Player player, Relic relic) {
        Location loc = player.getLocation();
        
        switch (relic) {
            case HUNTERS_COMPASS:
                XSound.matchXSound("BLOCK_BEACON_ACTIVATE").ifPresent(s -> s.play(player, 0.5f, 1.2f));
                XParticle.of("FLAME").ifPresent(p -> 
                    player.getWorld().spawnParticle(p.get(), loc.clone().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.02)
                );
                break;
                
            case SIGHT_GLASS:
                XSound.matchXSound("BLOCK_GLASS_BREAK").ifPresent(s -> s.play(player, 0.3f, 1.8f));
                XSound.matchXSound("ENTITY_EXPERIENCE_ORB_PICKUP").ifPresent(s -> s.play(player, 0.5f, 1.0f));
                XParticle.of("END_ROD").ifPresent(p -> 
                    player.getWorld().spawnParticle(p.get(), loc.clone().add(0, 1.5, 0), 15, 0.5, 0.5, 0.5, 0.05)
                );
                break;
                
            case BELL_OF_TRUTH:
                XSound.matchXSound("BLOCK_BELL_RESONATE").ifPresent(s -> s.play(player, 1.0f, 1.0f));
                XSound.matchXSound("ENTITY_EXPERIENCE_ORB_PICKUP").ifPresent(s -> s.play(player, 0.8f, 0.8f));
                XParticle.of("CLOUD").ifPresent(p -> 
                    player.getWorld().spawnParticle(p.get(), loc, 50, 5.0, 1.0, 5.0, 0.1)
                );
                XParticle.of("FLASH").ifPresent(p -> 
                    player.getWorld().spawnParticle(p.get(), loc, 3, 0, 0, 0, 0)
                );
                break;
                
            case TOTEM_OF_SCRAMBLING:
                XSound.matchXSound("ENTITY_ENDERMAN_TELEPORT").ifPresent(s -> s.play(player, 1.0f, 0.8f));
                XSound.matchXSound("BLOCK_ENCHANTMENT_TABLE_USE").ifPresent(s -> s.play(player, 0.6f, 1.5f));
                XParticle.of("PORTAL").ifPresent(p -> 
                    player.getWorld().spawnParticle(p.get(), loc, 30, 0.5, 1.0, 0.5, 0.5)
                );
                XParticle.of("WITCH").ifPresent(p -> 
                    player.getWorld().spawnParticle(p.get(), loc, 15, 0.3, 0.5, 0.3, 0.1)
                );
                break;
                
            case SHARD_OF_ANONYMITY:
                XSound.matchXSound("ENTITY_GENERIC_EXTINGUISH_FIRE").ifPresent(s -> s.play(player, 0.5f, 0.7f));
                XParticle.of("SQUID_INK").ifPresent(p -> 
                    player.getWorld().spawnParticle(p.get(), loc.clone().add(0, 0.2, 0), 20, 0.3, 0.1, 0.3, 0.05)
                );
                XParticle.of("SMOKE").ifPresent(p -> 
                    player.getWorld().spawnParticle(p.get(), loc.clone().add(0, 0.5, 0), 10, 0.2, 0.2, 0.2, 0.02)
                );
                break;
                
            case SOUL_BINDER_DAGGER:
                XSound.matchXSound("PARTICLE_SOUL_ESCAPE").ifPresent(s -> s.play(player, 1.0f, 0.9f));
                XSound.matchXSound("ENTITY_WITHER_BREAK_BLOCK").ifPresent(s -> s.play(player, 0.3f, 1.2f));
                XParticle.of("SOUL").ifPresent(p -> 
                    player.getWorld().spawnParticle(p.get(), loc.clone().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.05)
                );
                XParticle.of("SOUL_FIRE_FLAME").ifPresent(p -> 
                    player.getWorld().spawnParticle(p.get(), loc.clone().add(0, 1, 0), 8, 0.2, 0.3, 0.2, 0.02)
                );
                break;
        }
    }
    
    public static void playTrackingPulse(Player player, double distance) {
        XSound.matchXSound("BLOCK_NOTE_BLOCK_HAT").ifPresent(s -> {
            float pitch = distance < 50 ? 1.5f : distance < 100 ? 1.2f : 1.0f;
            s.play(player, 0.3f, pitch);
        });
        
        Location loc = player.getEyeLocation();
        XParticle.of("ELECTRIC_SPARK").ifPresent(p -> 
            player.getWorld().spawnParticle(p.get(), loc, 3, 0.1, 0.1, 0.1, 0.01)
        );
    }
}