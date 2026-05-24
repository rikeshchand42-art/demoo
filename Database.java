import java.sql.*;
import java.util.*;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:users.db";
    private Connection connection;

    public Database() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            createTables();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables() {
        try (Statement stmt = connection.createStatement()) {
            // Users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT UNIQUE NOT NULL," +
                    "email TEXT UNIQUE NOT NULL," +
                    "password TEXT NOT NULL," +
                    "full_name TEXT," +
                    "phone TEXT," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "is_admin BOOLEAN DEFAULT 0," +
                    "is_locked BOOLEAN DEFAULT 0," +
                    "locked_until TIMESTAMP" +
                    ")");

            // Login history table
            stmt.execute("CREATE TABLE IF NOT EXISTS login_history (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER," +
                    "login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "status TEXT," +
                    "ip_address TEXT," +
                    "FOREIGN KEY(user_id) REFERENCES users(id)" +
                    ")");

            // 2FA table
            stmt.execute("CREATE TABLE IF NOT EXISTS two_factor_auth (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER UNIQUE," +
                    "secret_code TEXT," +
                    "is_enabled BOOLEAN DEFAULT 0," +
                    "FOREIGN KEY(user_id) REFERENCES users(id)" +
                    ")");

            // Password reset table
            stmt.execute("CREATE TABLE IF NOT EXISTS password_reset (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER," +
                    "reset_token TEXT," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "expires_at TIMESTAMP," +
                    "is_used BOOLEAN DEFAULT 0," +
                    "FOREIGN KEY(user_id) REFERENCES users(id)" +
                    ")");

            // Insert default admin if not exists
            try (PreparedStatement pstmt = connection.prepareStatement(
                    "INSERT OR IGNORE INTO users (username, email, password, full_name, is_admin) VALUES (?, ?, ?, ?, ?)")) {
                pstmt.setString(1, "admin");
                pstmt.setString(2, "admin@example.com");
                pstmt.setString(3, hashPassword("password"));
                pstmt.setString(4, "Administrator");
                pstmt.setInt(5, 1);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] messageDigest = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            return password;
        }
    }

    // User registration
    public boolean registerUser(String username, String email, String password, String fullName, String phone) {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO users (username, email, password, full_name, phone) VALUES (?, ?, ?, ?, ?)")) {
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, hashPassword(password));
            pstmt.setString(4, fullName);
            pstmt.setString(5, phone);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    // User login validation
    public int validateUser(String username, String password) {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT id, is_locked, locked_until FROM users WHERE username = ? AND password = ?")) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashPassword(password));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("id");
                boolean isLocked = rs.getBoolean("is_locked");
                String lockedUntil = rs.getString("locked_until");

                if (isLocked) {
                    if (lockedUntil != null) {
                        long lockedTime = Timestamp.valueOf(lockedUntil).getTime();
                        long currentTime = System.currentTimeMillis();
                        if (currentTime < lockedTime) {
                            return -2; // Account locked
                        } else {
                            unlockUser(userId);
                        }
                    }
                }
                return userId;
            }
            return -1; // Invalid credentials
        } catch (SQLException e) {
            return -1;
        }
    }

    // Get user by ID
    public Map<String, String> getUserInfo(int userId) {
        Map<String, String> userInfo = new HashMap<>();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT id, username, email, full_name, phone, created_at, is_admin FROM users WHERE id = ?")) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                userInfo.put("id", String.valueOf(rs.getInt("id")));
                userInfo.put("username", rs.getString("username"));
                userInfo.put("email", rs.getString("email"));
                userInfo.put("full_name", rs.getString("full_name"));
                userInfo.put("phone", rs.getString("phone"));
                userInfo.put("created_at", rs.getString("created_at"));
                userInfo.put("is_admin", String.valueOf(rs.getInt("is_admin")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userInfo;
    }

    // Add login history
    public void addLoginHistory(int userId, String status) {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO login_history (user_id, status, ip_address) VALUES (?, ?, ?)")) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, status);
            pstmt.setString(3, "127.0.0.1");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get login history for user
    public List<Map<String, String>> getLoginHistory(int userId) {
        List<Map<String, String>> history = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT login_time, status FROM login_history WHERE user_id = ? ORDER BY login_time DESC LIMIT 10")) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, String> entry = new HashMap<>();
                entry.put("login_time", rs.getString("login_time"));
                entry.put("status", rs.getString("status"));
                history.add(entry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    // Lock user account after failed attempts
    public void lockUser(int userId, long minutes) {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "UPDATE users SET is_locked = 1, locked_until = datetime('now', '+' || ? || ' minutes') WHERE id = ?")) {
            pstmt.setLong(1, minutes);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Unlock user account
    public void unlockUser(int userId) {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "UPDATE users SET is_locked = 0, locked_until = NULL WHERE id = ?")) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Generate password reset token
    public String generatePasswordResetToken(int userId) {
        String token = UUID.randomUUID().toString();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO password_reset (user_id, reset_token, expires_at) VALUES (?, ?, datetime('now', '+24 hours'))")) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, token);
            pstmt.executeUpdate();
            return token;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Validate reset token
    public int validateResetToken(String token) {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT user_id FROM password_reset WHERE reset_token = ? AND is_used = 0 AND expires_at > datetime('now')")) {
            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Reset password
    public boolean resetPassword(String token, String newPassword) {
        int userId = validateResetToken(token);
        if (userId == -1) return false;

        try (PreparedStatement pstmt1 = connection.prepareStatement(
                "UPDATE users SET password = ? WHERE id = ?");
             PreparedStatement pstmt2 = connection.prepareStatement(
                     "UPDATE password_reset SET is_used = 1 WHERE reset_token = ?")) {
            pstmt1.setString(1, hashPassword(newPassword));
            pstmt1.setInt(2, userId);
            pstmt1.executeUpdate();

            pstmt2.setString(1, token);
            pstmt2.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Get all users (for admin)
    public List<Map<String, String>> getAllUsers() {
        List<Map<String, String>> users = new ArrayList<>();
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT id, username, email, full_name, is_admin, is_locked, created_at FROM users");
            while (rs.next()) {
                Map<String, String> user = new HashMap<>();
                user.put("id", String.valueOf(rs.getInt("id")));
                user.put("username", rs.getString("username"));
                user.put("email", rs.getString("email"));
                user.put("full_name", rs.getString("full_name"));
                user.put("is_admin", String.valueOf(rs.getInt("is_admin")));
                user.put("is_locked", String.valueOf(rs.getInt("is_locked")));
                user.put("created_at", rs.getString("created_at"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // Delete user (admin only)
    public boolean deleteUser(int userId) {
        try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM users WHERE id = ?")) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    // Update user info
    public boolean updateUserInfo(int userId, String fullName, String phone, String email) {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "UPDATE users SET full_name = ?, phone = ?, email = ? WHERE id = ?")) {
            pstmt.setString(1, fullName);
            pstmt.setString(2, phone);
            pstmt.setString(3, email);
            pstmt.setInt(4, userId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    // Enable 2FA
    public String enable2FA(int userId) {
        String secretCode = generateSecretCode();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT OR REPLACE INTO two_factor_auth (user_id, secret_code, is_enabled) VALUES (?, ?, 1)")) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, secretCode);
            pstmt.executeUpdate();
            return secretCode;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String generateSecretCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
