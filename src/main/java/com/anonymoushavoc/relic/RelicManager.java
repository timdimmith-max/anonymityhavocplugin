package com.anonymoushavoc.relic;

import com.anonymoushavoc.AnonymousHavocPlugin;
import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RelicManager {
    
    private final AnonymousHavocPlugin plugin;
    
    public RelicManager(AnonymousHavocPlugin plugin) {
        this.plugin = plugin;
    }
    
    public boolean isRelicReleased(Relic relic) {
        long serverStart = plugin.getConfigManager().getServerStartTimestamp();
        int releaseInterval = plugin.getConfigManager().getReleaseIntervalDays();
        
        long currentTime = System.currentTimeMillis();
        long daysPassed = (currentTime - serverStart) / (1000L * 60 * 60 * 24);
        
        return daysPassed >= (relic.getReleaseDay() - 1);
    }
    
    public boolean canCraftRelic(Relic relic) {
        if (!isRelicReleased(relic)) {
            return false;
        }
        
        int currentCount = plugin.getDatabaseManager().getRelicCount(relic);
        return currentCount < relic.getGlobalLimit();
    }
    
    public ItemStack createRelicItem(Relic relic) {
        ItemStack item = null;
        
        switch (relic) {
            case HUNTERS_COMPASS:
                item = XMaterial.PAPER.parseItem();
                break;
            case SIGHT_GLASS:
                item = XMaterial.SPYGLASS.parseItem();
                break;
            case BELL_OF_TRUTH:
                item = XMaterial.BELL.parseItem();
                break;
            case TOTEM_OF_SCRAMBLING:
                item = XMaterial.TOTEM_OF_UNDYING.parseItem();
                break;
            case SHARD_OF_ANONYMITY:
                item = XMaterial.ECHO_SHARD.parseItem();
                break;
            case SOUL_BINDER_DAGGER:
                item = XMaterial.NETHERITE_SWORD.parseItem();
                break;
        }
        
        if (item == null) {
            return null;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§l" + relic.getDisplayName());
            meta.setCustomModelData(relic.getCustomModelData());
            
            List<String> lore = new ArrayList<>();
            lore.add("§7" + getRelicDescription(relic));
            lore.add("");
            lore.add("§8Relic Item");
            meta.setLore(lore);
            
            XEnchantment.matchXEnchantment("UNBREAKING").ifPresent(e -> 
                meta.addEnchant(e.getEnchant(), 1, true)
            );
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    private String getRelicDescription(Relic relic) {
        switch (relic) {
            case HUNTERS_COMPASS:
                return "Points to the nearest player";
            case SIGHT_GLASS:
                return "Reveals target health and enchantments";
            case BELL_OF_TRUTH:
                return "Reveals identities in 8-block radius";
            case TOTEM_OF_SCRAMBLING:
                return "Redirects tracking compasses";
            case SHARD_OF_ANONYMITY:
                return "15% Lifesteal | Immune to reveals";
            case SOUL_BINDER_DAGGER:
                return "Execute targets for 48-hour bans";
            default:
                return "Unknown Relic";
        }
    }
    
    public boolean isRelic(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasCustomModelData()) {
            return false;
        }
        
        return Relic.fromCustomModelData(meta.getCustomModelData()) != null;
    }
    
    public Relic getRelicType(ItemStack item) {
        if (!isRelic(item)) {
            return null;
        }
        
        return Relic.fromCustomModelData(item.getItemMeta().getCustomModelData());
    }
    
    public void registerRelic(Relic relic, UUID ownerUuid, Location location) {
        plugin.getDatabaseManager().saveRelic(relic, ownerUuid, location);
    }
    
    public void transferRelic(Relic relic, UUID newOwnerUuid) {
        plugin.getDatabaseManager().updateRelicOwner(relic, newOwnerUuid);
    }
    
    public void updateRelicLocation(Relic relic, Location location) {
        plugin.getDatabaseManager().updateRelicLocation(relic, location);
    }
}