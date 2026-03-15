package com.anonymoushavoc.identity;

import com.anonymoushavoc.AnonymousHavocPlugin;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.*;
import org.bukkit.entity.Player;

import java.util.UUID;

public class IdentityManager {
    
    private final AnonymousHavocPlugin plugin;
    private static final String STEVE_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTYwNzc2MjY3MzAwMCwKICAicHJvZmlsZUlkIiA6ICJjNmNkZGQ3YmNjZjQ0ZTQ3YjI5ODFjNTdmNzRjZWQ3YSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTdGV2ZSIsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS82MGE1YmQ2OGQ1OTg2YzY4YjI2YTMyYTMyNjA3NjI4NGVlZTBiYTc1NzdkODY1MWI5YWY0OWU0NTM3NTQ2NDgyIgogICAgfQogIH0KfQ==";
    private static final String STEVE_SIGNATURE = "Signature";
    
    public IdentityManager(AnonymousHavocPlugin plugin) {
        this.plugin = plugin;
        registerPacketListeners();
    }
    
    private void registerPacketListeners() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(
                plugin,
                ListenerPriority.HIGHEST,
                PacketType.Play.Server.PLAYER_INFO
        ) {
            @Override
            public void onPacketSending(PacketEvent event) {
                handlePlayerInfoPacket(event);
            }
        });
        
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(
                plugin,
                ListenerPriority.HIGHEST,
                PacketType.Play.Server.NAMED_ENTITY_SPAWN
        ) {
            @Override
            public void onPacketSending(PacketEvent event) {
                handleEntitySpawnPacket(event);
            }
        });
    }
    
    private void handlePlayerInfoPacket(PacketEvent event) {
        Player receiver = event.getPlayer();
        
        if (plugin.getRevealManager().isPlayerRevealed(receiver)) {
            return;
        }
        
        event.getPacket().getPlayerInfoDataLists().read(0).forEach(data -> {
            if (data == null || data.getProfile() == null) {
                return;
            }
            
            WrappedGameProfile originalProfile = data.getProfile();
            UUID originalUuid = originalProfile.getUUID();
            Player targetPlayer = org.bukkit.Bukkit.getPlayer(originalUuid);
            if (targetPlayer == null) {
                return;
            }
            
            WrappedGameProfile maskedProfile = new WrappedGameProfile(
                    originalUuid,
                    plugin.getConfigManager().getMaskedName()
            );
            
            maskedProfile.getProperties().clear();
            maskedProfile.getProperties().put("textures",
                    new WrappedSignedProperty("textures", STEVE_TEXTURE, STEVE_SIGNATURE));
            
            PlayerInfoData maskedData = new PlayerInfoData(
                    maskedProfile,
                    data.getLatency(),
                    data.getGameMode(),
                    data.getDisplayName()
            );
            
            event.getPacket().getPlayerInfoDataLists().write(0,
                    java.util.Collections.singletonList(maskedData));
        });
    }
    
    private void handleEntitySpawnPacket(PacketEvent event) {
        Player receiver = event.getPlayer();
        
        if (plugin.getRevealManager().isPlayerRevealed(receiver)) {
            return;
        }
    }
    
    public void revealIdentity(Player viewer, Player target, int durationSeconds) {
        plugin.getRevealManager().revealPlayer(viewer, target, durationSeconds);
        
        sendRealPlayerInfo(viewer, target);
    }
    
    private void sendRealPlayerInfo(Player viewer, Player target) {
        UUID targetUuid = target.getUniqueId();
        WrappedGameProfile wrappedProfile = new WrappedGameProfile(targetUuid, target.getName());

        try {
            WrappedGameProfile existingProfile = WrappedGameProfile.fromPlayer(target);
            if (existingProfile != null) {
                wrappedProfile.getProperties().putAll(existingProfile.getProperties());
            }
        } catch (Exception ignored) {
        }

        WrappedChatComponent displayName = WrappedChatComponent.fromText(target.getName());

        PlayerInfoData realData = new PlayerInfoData(
                wrappedProfile,
                target.getPing(),
                EnumWrappers.NativeGameMode.fromBukkit(target.getGameMode()),
                displayName
        );

        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
        packet.getPlayerInfoActions().write(0, java.util.EnumSet.of(EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME));
        packet.getPlayerInfoDataLists().write(1, java.util.Collections.singletonList(realData));

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(viewer, packet);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send reveal packet: " + e.getMessage());
        }

        viewer.sendMessage("§e§l⚠ §6Identity Revealed: §f" + target.getName());
    }
    
    public void maskIdentity(Player viewer, Player target) {
        plugin.getRevealManager().maskPlayer(viewer, target);
    }
}