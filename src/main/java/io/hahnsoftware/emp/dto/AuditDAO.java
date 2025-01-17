package io.hahnsoftware.emp.dto;


import io.hahnsoftware.emp.model.AuditLog;
import io.hahnsoftware.emp.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditDAO {
    private final Connection connection;
    private final UserDAO userDAO;
    
    public AuditDAO() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
        this.userDAO = new UserDAO();
    }
    
    public void logAction(String action, String entityType, Long entityId, Long userId) throws SQLException {
        String sql = "INSERT INTO audit_log (action, entity_type, entity_id, user_id, timestamp) " +
                    "VALUES (?, ?, ?, ?, ?)";
                    
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, action);
            stmt.setString(2, entityType);
            stmt.setLong(3, entityId);
            stmt.setLong(4, userId);
            stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            
            stmt.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }
    
    public List<AuditLog> getAuditLogs(String entityType, Long entityId,
                                       LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        List<AuditLog> auditLogs = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM audit_log WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (entityType != null) {
            sql.append(" AND entity_type = ?");
            params.add(entityType);
        }
        
        if (entityId != null) {
            sql.append(" AND entity_id = ?");
            params.add(entityId);
        }
        
        if (startDate != null) {
            sql.append(" AND timestamp >= ?");
            params.add(Timestamp.valueOf(startDate));
        }
        
        if (endDate != null) {
            sql.append(" AND timestamp <= ?");
            params.add(Timestamp.valueOf(endDate));
        }
        
        sql.append(" ORDER BY timestamp DESC");
        
        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    auditLogs.add(mapResultSetToAuditLog(rs));
                }
            }
        }
        
        return auditLogs;
    }
    
    private AuditLog mapResultSetToAuditLog(ResultSet rs) throws SQLException {
        AuditLog auditLog = new AuditLog();
        auditLog.setId(rs.getLong("id"));
        auditLog.setAction(rs.getString("action"));
        auditLog.setEntityType(rs.getString("entity_type"));
        auditLog.setEntityId(rs.getLong("entity_id"));
        auditLog.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        
        // Load user
        Long userId = rs.getLong("user_id");
        if (!rs.wasNull()) {
            auditLog.setUser(userDAO.findById(userId));
        }
        
        return auditLog;
    }
}