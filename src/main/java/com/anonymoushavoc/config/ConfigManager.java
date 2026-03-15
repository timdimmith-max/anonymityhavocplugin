package com.anonymoushavoc.config;

import com.anonymoushavoc.AnonymousHavocPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    
    private final AnonymousHavocPlugin plugin;
    private final FileConfiguration config;
    
    public ConfigManager(AnonymousHavocPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }
    
    public String getDatabasePath() {
        return config.getString("database.path", "plugins/AnonymousHavocSMP/data.db");
    }
    
    public int getStandardDeathHours() {
        return config.getInt("bans.standard-death-hours", 24);
    }
    
    public int getExecutionDeathHours() {
        return config.getInt("bans.execution-death-hours", 48);
    }
    
    public String getStandardKickMessage() {
        return config.getString("bans.standard-kick-message", "&cEliminated. You may return in 24 hours.");
    }
    
    public String getExecutionKickMessage() {
        return config.getString("bans.execution-kick-message", "&4Executed and erased. Return in 48 hours.");
    }
    
    public int getReleaseIntervalDays() {
        return config.getInt("relics.release-interval-days", 3);
    }
    
    public long getServerStartTimestamp() {
        long timestamp = config.getLong("relics.server-start-timestamp", 0);
        if (timestamp == 0) {
            timestamp = System.currentTimeMillis();
            config.set("relics.server-start-timestamp", timestamp);
            plugin.saveConfig();
        }
        return timestamp;
    }
    
    public int getOfflineThresholdHours() {
        return config.getInt("anti-hoarding.offline-threshold-hours", 48);
    }
    
    public int getAntiHoardingCheckMinutes() {
        return config.getInt("anti-hoarding.check-interval-minutes", 30);
    }
    
    public int getCoordinateHintIntervalHours() {
        return config.getInt("coordinate-hints.broadcast-interval-hours", 4);
    }
    
    public int getCoordinateHintRadius() {
        return config.getInt("coordinate-hints.radius", 150);
    }
    
    public String getMaskedName() {
        return config.getString("identity.masked-name", "Player");
    }
    
    public int getRevealDurationSeconds() {
        return config.getInt("identity.reveal-duration-seconds", 10);
    }
    
    public int getBellPulseRadius() {
        return config.getInt("identity.bell-pulse-radius", 8);
    }
    
    public boolean isDebug() {
        return config.getBoolean("debug", false);
    }
    
    public boolean isDisableVanillaTotems() {
        return config.getBoolean("totems.disable-vanilla", true);
    }
    
    public boolean isTotemPreventsBan() {
        return config.getBoolean("totems.prevents-death-ban", true);
    }
}