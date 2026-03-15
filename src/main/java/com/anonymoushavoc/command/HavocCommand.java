package com.anonymoushavoc.command;

import com.anonymoushavoc.AnonymousHavocPlugin;
import com.anonymoushavoc.relic.Relic;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HavocCommand implements CommandExecutor {
    
    private final AnonymousHavocPlugin plugin;
    
    public HavocCommand(AnonymousHavocPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("havoc.admin")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "give":
                return handleGive(sender, args);
            case "ban":
                return handleBan(sender, args);
            case "pardon":
            case "unban":
                return handlePardon(sender, args);
            default:
                sender.sendMessage("§cUnknown subcommand. Use /havoc for help.");
                return true;
        }
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§l=== AnonymousHavoc Commands ===");
        sender.sendMessage("§e/havoc give <id> §7- Give yourself a relic");
        sender.sendMessage("§7  IDs: 1=Compass, 2=Glass, 3=Bell, 4=Totem, 5=Shard, 6=Dagger");
        sender.sendMessage("§e/havoc ban <player> §7- Manually ban a player");
        sender.sendMessage("§e/havoc pardon <player> §7- Unban a player");
    }
    
    private boolean handleGive(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /havoc give <id>");
            sender.sendMessage("§7IDs: 1=Compass, 2=Glass, 3=Bell, 4=Totem, 5=Shard, 6=Dagger");
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        try {
            int relicId = Integer.parseInt(args[1]);
            
            if (relicId < 1 || relicId > 6) {
                player.sendMessage("§cInvalid relic ID. Must be between 1 and 6.");
                return true;
            }
            
            Relic relic = Relic.values()[relicId - 1];
            ItemStack relicItem = plugin.getRelicManager().createRelicItem(relic);
            
            if (relicItem == null) {
                player.sendMessage("§cFailed to create relic item.");
                return true;
            }
            
            player.getInventory().addItem(relicItem);
            player.sendMessage("§a§lSUCCESS: §7Given " + relic.getDisplayName() + " (bypassing all limits)");
            
            plugin.getLogger().info(player.getName() + " received " + relic.name() + " via admin command");
            
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid ID format. Use a number (1-6).");
        }
        
        return true;
    }
    
    private boolean handleBan(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /havoc ban <player>");
            return true;
        }
        
        String playerName = args[1];
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage("§cPlayer not found: " + playerName);
            return true;
        }
        
        if (plugin.getDeathManager().isPlayerBanned(target.getUniqueId())) {
            sender.sendMessage("§c" + playerName + " is already banned.");
            return true;
        }
        
        plugin.getDatabaseManager().saveBan(
            target.getUniqueId(), 
            "ADMIN", 
            System.currentTimeMillis() + (24 * 60 * 60 * 1000L)
        );
        
        Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(
            playerName,
            "§cBanned by administrator",
            new java.util.Date(System.currentTimeMillis() + (24 * 60 * 60 * 1000L)),
            sender.getName()
        );
        
        if (target.isOnline()) {
            ((Player) target).kickPlayer("§c§lBANNED\n\n§7You have been banned by an administrator.\n\n§7Return in 24 hours.");
        }
        
        sender.sendMessage("§a§lSUCCESS: §7Banned " + playerName + " for 24 hours.");
        plugin.getLogger().info(sender.getName() + " banned " + playerName);
        
        return true;
    }
    
    private boolean handlePardon(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /havoc pardon <player>");
            return true;
        }
        
        String playerName = args[1];
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage("§cPlayer not found: " + playerName);
            return true;
        }
        
        if (!plugin.getDeathManager().isPlayerBanned(target.getUniqueId())) {
            sender.sendMessage("§c" + playerName + " is not banned.");
            return true;
        }
        
        plugin.getDeathManager().unbanPlayer(target.getUniqueId(), playerName);
        
        sender.sendMessage("§a§lSUCCESS: §7Pardoned " + playerName);
        plugin.getLogger().info(sender.getName() + " pardoned " + playerName);
        
        return true;
    }
}