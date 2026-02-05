package parkinglotmanagementsystem.dao;

import parkinglotmanagementsystem.model.FineScheme;

import java.sql.*;

public class SystemConfigDAO {
    
    private Connection connection;
    
    public SystemConfigDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }
    
    public boolean setConfig(String key, String value) {
        String sql = """
            INSERT INTO system_config (config_key, config_value) 
            VALUES (?, ?)
            ON CONFLICT(config_key) DO UPDATE SET config_value = excluded.config_value;
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, key);
            pstmt.setString(2, value);
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to set config: " + key);
            e.printStackTrace();
            return false;
        }
    }
    
    public String getConfig(String key) {
        String sql = "SELECT config_value FROM system_config WHERE config_key = ?;";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, key);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("config_value");
            }
        } catch (SQLException e) {
            System.err.println("Failed to get config: " + key);
            e.printStackTrace();
        }
        
        return null;
    }
    
    public FineScheme getCurrentFineScheme() {
        String schemeStr = getConfig("FINE_SCHEME");
        
        if (schemeStr == null) {
            return FineScheme.FIXED; // Default
        }
        
        try {
            return FineScheme.valueOf(schemeStr);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid fine scheme in config: " + schemeStr);
            return FineScheme.FIXED; // Default fallback
        }
    }
    
    public boolean setFineScheme(FineScheme scheme) {
        return setConfig("FINE_SCHEME", scheme.name());
    }
}
