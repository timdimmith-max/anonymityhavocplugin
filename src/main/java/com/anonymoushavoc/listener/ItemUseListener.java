package com.anonymoushavoc.listener;

import com.anonymoushavoc.AnonymousHavocPlugin;
import com.anonymoushavoc.relic.Relic;
import com.anonymoushavoc.util.EffectUtil;
import com.cryptomorin.xseries.XAttribute;
import com.cryptomorin.xseries.XEnchantment;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Bell;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.RayTraceResult;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemUseListener implements Listener {
    
    private final AnonymousHavocPlugin plugin;
    private final NamespacedKey bellKey;
    
    public ItemUseListener(AnonymousHavocPlugin plugin) {
        this.plugin = plugin;
        this.bellKey = new NamespacedKey(plugin, "bell_of_truth");
    }
    
    @EventHandler
    public void onItemUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (event.getAction() == Action.RIGHT_CLICK_AIR || 
            event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            
            if (item != null && plugin.getRelicManager().isRelic(item)) {
                Relic relic = plugin.getRelicManager().getRelicType(item);
                
                if (relic == Relic.HUNTERS_COMPASS) {
                    handlePulseTracker(player);
                    event.setCancelled(true);
                } else if (relic == Relic.SIGHT_GLASS) {
                    handleSightGlass(player);
                    event.setCancelled(true);
                }
            }
        }
        
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            
            if (block != null && block.getState() instanceof Bell) {
                Bell bell = (Bell) block.getState();
                PersistentDataContainer pdc = bell.getPersistentDataContainer();
                
                if (pdc.has(bellKey, PersistentDataType.BYTE)) {
                    handleBellOfTruth(player, block.getLocation());
                }
            }
        }
    }
    
    private void handlePulseTracker(Player user) {
        if (plugin.getCooldownManager().isOnCooldown(user, "pulse_tracker")) {
            long remaining = plugin.getCooldownManager().getRemainingCooldown(user, "pulse_tracker");
            user.sendActionBar("§c✖ Cooldown: " + (remaining / 1000) + "s");
            return;
        }
        
        plugin.getPulseTrackerManager().toggleTracking(user);
        plugin.getCooldownManager().setCooldown(user, "pulse_tracker", 2000);
    }
    
    private void handleSightGlass(Player user) {
        if (plugin.getCooldownManager().isOnCooldown(user, "sight_glass")) {
            long remaining = plugin.getCooldownManager().getRemainingCooldown(user, "sight_glass");
            user.sendActionBar("§c✖ Cooldown: " + (remaining / 1000) + "s");
            return;
        }
        
        RayTraceResult rayTrace = user.getWorld().rayTraceEntities(
            user.getEyeLocation(),
            user.getEyeLocation().getDirection(),
            50.0,
            1.0,
            entity -> entity instanceof Player && !entity.equals(user)
        );
        
        if (rayTrace == null || !(rayTrace.getHitEntity() instanceof Player)) {
            user.sendActionBar("§c✖ No player in sight");
            return;
        }
        
        Player target = (Player) rayTrace.getHitEntity();
        
        user.sendMessage("§8§m                                        ");
        user.sendMessage("§6§l⚔ Sight-Glass Analysis");
        user.sendMessage("");
        
        String targetName = plugin.getRevealManager().isTargetRevealedTo(user, target)
            ? target.getName() 
            : plugin.getConfigManager().getMaskedName();
        
        user.sendMessage("§7Target: §f" + targetName);
        
        double health = target.getHealth();
        double[] maxHealthHolder = {20.0};
        XAttribute.of("generic.max_health").ifPresent(attr -> {
            if (target.getAttribute(attr.get()) != null) {
                maxHealthHolder[0] = target.getAttribute(attr.get()).getValue();
            }
        });
        double maxHealth = maxHealthHolder[0];
        int hearts = (int) Math.ceil(health / 2.0);
        int maxHearts = (int) Math.ceil(maxHealth / 2.0);
        
        user.sendMessage("§7Health: §c" + hearts + "§8/§c" + maxHearts + " ❤");
        
        user.sendMessage("");
        user.sendMessage("§7Equipment Enchantments:");
        
        boolean hasEnchants = false;
        for (ItemStack armor : target.getInventory().getArmorContents()) {
            if (armor != null && !armor.getEnchantments().isEmpty()) {
                hasEnchants = true;
                String itemName = formatMaterialName(armor.getType().name());
                user.sendMessage("  §8▪ §f" + itemName);
                
                for (Map.Entry<Enchantment, Integer> entry : armor.getEnchantments().entrySet()) {
                    String enchantName = formatEnchantmentName(entry.getKey());
                    user.sendMessage("    §7- " + enchantName + " " + entry.getValue());
                }
            }
        }
        
        ItemStack mainHand = target.getInventory().getItemInMainHand();
        if (mainHand != null && !mainHand.getEnchantments().isEmpty()) {
            hasEnchants = true;
            String itemName = formatMaterialName(mainHand.getType().name());
            user.sendMessage("  §8▪ §f" + itemName);
            
            for (Map.Entry<Enchantment, Integer> entry : mainHand.getEnchantments().entrySet()) {
                String enchantName = formatEnchantmentName(entry.getKey());
                user.sendMessage("    §7- " + enchantName + " " + entry.getValue());
            }
        }
        
        if (!hasEnchants) {
            user.sendMessage("  §8No enchantments detected");
        }
        
        user.sendMessage("§8§m                                        ");
        
        EffectUtil.playRelicEffect(user, Relic.SIGHT_GLASS);
        
        plugin.getCooldownManager().setCooldown(user, "sight_glass", 10000);
    }
    
    private String formatMaterialName(String material) {
        String[] parts = material.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            result.append(Character.toUpperCase(part.charAt(0)))
                  .append(part.substring(1))
                  .append(" ");
        }
        return result.toString().trim();
    }
    
    private String formatEnchantmentName(Enchantment enchant) {
        String key = enchant.getKey().getKey();
        String[] parts = key.split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            result.append(Character.toUpperCase(part.charAt(0)))
                  .append(part.substring(1))
                  .append(" ");
        }
        return result.toString().trim();
    }
    
    private void handleBellOfTruth(Player user, Location bellLoc) {
        if (plugin.getCooldownManager().isOnCooldown(user, "bell_of_truth")) {
            long remaining = plugin.getCooldownManager().getRemainingCooldown(user, "bell_of_truth");
            user.sendActionBar("§c✖ Cooldown: " + (remaining / 1000) + "s");
            return;
        }
        
        int radius = plugin.getConfigManager().getBellPulseRadius();
        int duration = plugin.getConfigManager().getRevealDurationSeconds();
        
        List<Player> nearbyPlayers = user.getWorld().getPlayers().stream()
            .filter(p -> !p.equals(user))
            .filter(p -> p.getLocation().distance(bellLoc) <= radius)
            .collect(Collectors.toList());
        
        for (Player target : nearbyPlayers) {
            plugin.getIdentityManager().revealIdentity(user, target, duration);
        }
        
        EffectUtil.playRelicEffect(user, Relic.BELL_OF_TRUTH);
        
        plugin.getCooldownManager().setCooldown(user, "bell_of_truth", 30000);
        
        user.sendMessage("§6§l✦ §eThe Bell of Truth reveals " + nearbyPlayers.size() + " identities!");
    }
}