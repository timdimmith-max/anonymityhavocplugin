package com.anonymoushavoc;

import com.anonymoushavoc.command.HavocCommand;
import com.anonymoushavoc.config.ConfigManager;
import com.anonymoushavoc.database.DatabaseManager;
import com.anonymoushavoc.death.DeathManager;
import com.anonymoushavoc.identity.IdentityManager;
import com.anonymoushavoc.identity.RevealManager;
import com.anonymoushavoc.listener.*;
import com.anonymoushavoc.relic.RelicManager;
import com.anonymoushavoc.task.CoordinateHintTask;
import com.anonymoushavoc.task.ItemDropTask;
import com.anonymoushavoc.tracker.PulseTrackerManager;
import com.anonymoushavoc.util.CooldownManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class AnonymousHavocPlugin extends JavaPlugin {
    
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private RelicManager relicManager;
    private IdentityManager identityManager;
    private RevealManager revealManager;
    private DeathManager deathManager;
    private CooldownManager cooldownManager;
    private PulseTrackerManager pulseTrackerManager;
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        this.configManager = new ConfigManager(this);
        this.databaseManager = new DatabaseManager(this);
        this.databaseManager.initialize();
        
        this.relicManager = new RelicManager(this);
        this.identityManager = new IdentityManager(this);
        this.revealManager = new RevealManager(this);
        this.deathManager = new DeathManager(this);
        this.cooldownManager = new CooldownManager();
        this.pulseTrackerManager = new PulseTrackerManager(this);
        
        registerListeners();
        registerCommands();
        startTasks();
        
        getLogger().info("AnonymousHavocSMP enabled - Identity masking active");
    }
    
    @Override
    public void onDisable() {
        if (this.pulseTrackerManager != null) {
            this.pulseTrackerManager.cleanup();
        }
        if (this.databaseManager != null) {
            this.databaseManager.close();
        }
        getLogger().info("AnonymousHavocSMP disabled");
    }
    
    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new DeathListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CraftListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ItemUseListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BlockListener(this), this);
        Bukkit.getPluginManager().registerEvents(new TotemListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CombatListener(this), this);
    }
    
    private void registerCommands() {
        getCommand("havoc").setExecutor(new HavocCommand(this));
    }
    
    private void startTasks() {
        ItemDropTask itemDropTask = new ItemDropTask(this);
        long dropCheckTicks = configManager.getAntiHoardingCheckMinutes() * 60 * 20L;
        itemDropTask.runTaskTimer(this, dropCheckTicks, dropCheckTicks);
        
        CoordinateHintTask hintTask = new CoordinateHintTask(this);
        long hintTicks = configManager.getCoordinateHintIntervalHours() * 60 * 60 * 20L;
        hintTask.runTaskTimer(this, hintTicks, hintTicks);
    }
}