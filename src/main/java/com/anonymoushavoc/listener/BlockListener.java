package com.anonymoushavoc.listener;

import com.anonymoushavoc.AnonymousHavocPlugin;
import com.anonymoushavoc.relic.Relic;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Bell;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class BlockListener implements Listener {
    
    private final AnonymousHavocPlugin plugin;
    private final NamespacedKey bellKey;
    
    public BlockListener(AnonymousHavocPlugin plugin) {
        this.plugin = plugin;
        this.bellKey = new NamespacedKey(plugin, "bell_of_truth");
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        
        if (!plugin.getRelicManager().isRelic(item)) {
            return;
        }
        
        Relic relic = plugin.getRelicManager().getRelicType(item);
        
        if (relic == Relic.BELL_OF_TRUTH) {
            Block block = event.getBlockPlaced();
            if (block.getState() instanceof Bell) {
                Bell bell = (Bell) block.getState();
                PersistentDataContainer pdc = bell.getPersistentDataContainer();
                pdc.set(bellKey, PersistentDataType.BYTE, (byte) 1);
                bell.update();
                
                event.getPlayer().sendMessage("§6§l✦ §eBell of Truth placed!");
            }
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        
        if (XMaterial.matchXMaterial(block.getType()) != XMaterial.BELL) {
            return;
        }
        
        if (!(block.getState() instanceof Bell)) {
            return;
        }
        
        Bell bell = (Bell) block.getState();
        PersistentDataContainer pdc = bell.getPersistentDataContainer();
        
        if (pdc.has(bellKey, PersistentDataType.BYTE)) {
            event.setDropItems(false);
            
            ItemStack bellItem = plugin.getRelicManager().createRelicItem(Relic.BELL_OF_TRUTH);
            block.getWorld().dropItemNaturally(block.getLocation(), bellItem);
            
            event.getPlayer().sendMessage("§6§l✦ §eBell of Truth retrieved!");
        }
    }
}