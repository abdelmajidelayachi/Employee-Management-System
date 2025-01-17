package io.hahnsoftware.emp.dto;



import io.hahnsoftware.emp.model.User;
import io.hahnsoftware.emp.model.UserRole;
import io.hahnsoftware.emp.util.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private final Connection connection;
    private final DepartmentDAO departmentDAO;
    
    public UserDAO() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
        this.departmentDAO = new DepartmentDAO();
    }
    
    public User validateCredentials(String username, String password) throws SQLException {
        String sql = "SELECT id, username, password_hash, role, department_id FROM users WHERE username = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    
                    // Verify password using BCrypt
                    if (BCrypt.checkpw(password, storedHash)) {
                        User user = new User();
                        user.setId(rs.getLong("id"));
                        user.setUsername(rs.getString("username"));
                        user.setRole(UserRole.valueOf(rs.getString("role")));
                        
                        // Load department if exists
                        Long departmentId = rs.getLong("department_id");
                        if (!rs.wasNull()) {
                            user.setDepartment(departmentDAO.findById(departmentId));
                        }
                        
                        return user;
                    }
                }
            }
        }
        
        return null;
    }
    
    public User findById(Long id) throws SQLException {
        String sql = "SELECT id, username, role, department_id FROM users WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getLong("id"));
                    user.setUsername(rs.getString("username"));
                    user.setRole(UserRole.valueOf(rs.getString("role")));
                    
                    // Load department if exists
                    Long departmentId = rs.getLong("department_id");
                    if (!rs.wasNull()) {
                        user.setDepartment(departmentDAO.findById(departmentId));
                    }
                    
                    return user;
                }
            }
        }
        
        return null;
    }

    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, role, department_id FROM users";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setRole(UserRole.valueOf(rs.getString("role")));

                // Load department if exists
                Long departmentId = rs.getLong("department_id");
                if (!rs.wasNull()) {
                    user.setDepartment(departmentDAO.findById(departmentId));
                }

                users.add(user);
            }
        }

        return users;
    }


    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, role, department_id FROM users WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getLong("id"));
                    user.setUsername(rs.getString("username"));
                    user.setRole(UserRole.valueOf(rs.getString("role")));

                    // Load department if exists
                    Long departmentId = rs.getLong("department_id");
                    if (!rs.wasNull()) {
                        user.setDepartment(departmentDAO.findById(departmentId));
                    }

                    return user;
                }
            }
        }

        return null;
    }


    public void updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET role = ?, department_id = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getRole().name());

            if (user.getDepartment() != null) {
                stmt.setLong(2, user.getDepartment().getId());
            } else {
                stmt.setNull(2, Types.BIGINT);
            }

            stmt.setLong(3, user.getId());

            stmt.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public void updateUserWithPassword(User user, String newPassword) throws SQLException {
        String sql = "UPDATE users SET role = ?, department_id = ?, password_hash = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getRole().name());

            if (user.getDepartment() != null) {
                stmt.setLong(2, user.getDepartment().getId());
            } else {
                stmt.setNull(2, Types.BIGINT);
            }

            stmt.setString(3, BCrypt.hashpw(newPassword, BCrypt.gensalt()));
            stmt.setLong(4, user.getId());

            stmt.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }
    public void deleteByUsername(String username) throws SQLException {
        String sql = "DELETE FROM users WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);

            stmt.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public User createUser(User user, String password) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, role, department_id) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, new String[] {"id"})) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, BCrypt.hashpw(password, BCrypt.gensalt()));
            stmt.setString(3, user.getRole().name());

            if (user.getDepartment() != null) {
                stmt.setLong(4, user.getDepartment().getId());
            } else {
                stmt.setNull(4, Types.BIGINT);
            }

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getLong(1));
                }
            }

            connection.commit();
            return user;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }
    
    public void updatePassword(Long userId, String newPassword) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, BCrypt.hashpw(newPassword, BCrypt.gensalt()));
            stmt.setLong(2, userId);
            
            stmt.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }


}