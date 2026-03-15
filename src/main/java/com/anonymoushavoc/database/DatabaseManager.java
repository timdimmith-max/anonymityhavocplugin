package com.anonymoushavoc.database;

import com.anonymoushavoc.AnonymousHavocPlugin;
import com.anonymoushavoc.relic.Relic;
import com.anonymoushavoc.relic.RelicData;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {
    
    private final AnonymousHavocPlugin plugin;
    private Connection connection;
    
    public DatabaseManager(AnonymousHavocPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void initialize() {
        try {
            String dbPath = plugin.getConfigManager().getDatabasePath();
            File dbFile = new File(dbPath);
            dbFile.getParentFile().mkdirs();
            
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            
            createTables();
            plugin.getLogger().info("Database initialized successfully");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createTables() throws SQLException {
        String relicsTable = "CREATE TABLE IF NOT EXISTS relics (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "type TEXT NOT NULL, " +
                "owner_uuid TEXT NOT NULL, " +
                "crafted_date BIGINT NOT NULL, " +
                "last_location_world TEXT, " +
                "last_location_x DOUBLE, " +
                "last_location_y DOUBLE, " +
                "last_location_z DOUBLE, " +
                "locked_mob_uuid TEXT" +
                ")";
        
        String bansTable = "CREATE TABLE IF NOT EXISTS bans (" +
                "uuid TEXT PRIMARY KEY, " +
                "ban_type TEXT NOT NULL, " +
                "unban_time BIGINT NOT NULL" +
                ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(relicsTable);
            stmt.execute(bansTable);
        }
    }
    
    public void saveRelic(Relic relic, UUID ownerUuid, Location location) {
        String sql = "INSERT INTO relics (type, owner_uuid, crafted_date, last_location_world, " +
                "last_location_x, last_location_y, last_location_z) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, relic.name());
            pstmt.setString(2, ownerUuid.toString());
            pstmt.setLong(3, System.currentTimeMillis());
            pstmt.setString(4, location.getWorld().getName());
            pstmt.setDouble(5, location.getX());
            pstmt.setDouble(6, location.getY());
            pstmt.setDouble(7, location.getZ());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save relic: " + e.getMessage());
        }
    }
    
    public void updateRelicOwner(Relic relic, UUID newOwnerUuid) {
        String sql = "UPDATE relics SET owner_uuid = ? WHERE type = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newOwnerUuid.toString());
            pstmt.setString(2, relic.name());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to update relic owner: " + e.getMessage());
        }
    }
    
    public void updateRelicLocation(Relic relic, Location location) {
        String sql = "UPDATE relics SET last_location_world = ?, last_location_x = ?, " +
                "last_location_y = ?, last_location_z = ? WHERE type = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, location.getWorld().getName());
            pstmt.setDouble(2, location.getX());
            pstmt.setDouble(3, location.getY());
            pstmt.setDouble(4, location.getZ());
            pstmt.setString(5, relic.name());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to update relic location: " + e.getMessage());
        }
    }
    
    public void deleteRelic(Relic relic) {
        String sql = "DELETE FROM relics WHERE type = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, relic.name());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete relic: " + e.getMessage());
        }
    }
    
    public int getRelicCount(Relic relic) {
        String sql = "SELECT COUNT(*) FROM relics WHERE type = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, relic.name());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get relic count: " + e.getMessage());
        }
        return 0;
    }
    
    public List<RelicData> getAllRelics() {
        List<RelicData> relics = new ArrayList<>();
        String sql = "SELECT * FROM relics";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                RelicData data = new RelicData();
                data.setType(Relic.valueOf(rs.getString("type")));
                data.setOwnerUuid(UUID.fromString(rs.getString("owner_uuid")));
                data.setCraftedDate(rs.getLong("crafted_date"));
                
                String world = rs.getString("last_location_world");
                if (world != null) {
                    data.setLastLocationWorld(world);
                    data.setLastLocationX(rs.getDouble("last_location_x"));
                    data.setLastLocationY(rs.getDouble("last_location_y"));
                    data.setLastLocationZ(rs.getDouble("last_location_z"));
                }
                
                String mobUuid = rs.getString("locked_mob_uuid");
                if (mobUuid != null) {
                    data.setLockedMobUuid(UUID.fromString(mobUuid));
                }
                
                relics.add(data);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get all relics: " + e.getMessage());
        }
        return relics;
    }
    
    public RelicData getRelicByType(Relic relic) {
        String sql = "SELECT * FROM relics WHERE type = ? LIMIT 1";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, relic.name());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                RelicData data = new RelicData();
                data.setType(Relic.valueOf(rs.getString("type")));
                data.setOwnerUuid(UUID.fromString(rs.getString("owner_uuid")));
                data.setCraftedDate(rs.getLong("crafted_date"));
                
                String world = rs.getString("last_location_world");
                if (world != null) {
                    data.setLastLocationWorld(world);
                    data.setLastLocationX(rs.getDouble("last_location_x"));
                    data.setLastLocationY(rs.getDouble("last_location_y"));
                    data.setLastLocationZ(rs.getDouble("last_location_z"));
                }
                
                String mobUuid = rs.getString("locked_mob_uuid");
                if (mobUuid != null) {
                    data.setLockedMobUuid(UUID.fromString(mobUuid));
                }
                
                return data;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get relic by type: " + e.getMessage());
        }
        return null;
    }
    
    public UUID getLockedMobUuid(Relic relic) {
        String sql = "SELECT locked_mob_uuid FROM relics WHERE type = ? LIMIT 1";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, relic.name());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String uuidStr = rs.getString("locked_mob_uuid");
                if (uuidStr != null) {
                    return UUID.fromString(uuidStr);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get locked mob uuid: " + e.getMessage());
        }
        return null;
    }
    
    public String getRealName(UUID playerUuid) {
        return Bukkit.getOfflinePlayer(playerUuid).getName();
    }
    
    public void saveLockedMob(Relic relic, UUID mobUuid) {
        String sql = "UPDATE relics SET locked_mob_uuid = ? WHERE type = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, mobUuid.toString());
            pstmt.setString(2, relic.name());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save locked mob: " + e.getMessage());
        }
    }
    
    public void saveBan(UUID playerUuid, String banType, long unbanTime) {
        String sql = "INSERT OR REPLACE INTO bans (uuid, ban_type, unban_time) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid.toString());
            pstmt.setString(2, banType);
            pstmt.setLong(3, unbanTime);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save ban: " + e.getMessage());
        }
    }
    
    public Long getBanExpiry(UUID playerUuid) {
        String sql = "SELECT unban_time FROM bans WHERE uuid = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid.toString());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getLong("unban_time");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get ban expiry: " + e.getMessage());
        }
        return null;
    }
    
    public void removeBan(UUID playerUuid) {
        String sql = "DELETE FROM bans WHERE uuid = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to remove ban: " + e.getMessage());
        }
    }
    
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Database connection closed");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to close database: " + e.getMessage());
        }
    }
}