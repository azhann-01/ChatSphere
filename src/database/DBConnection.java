package database;

import config.AppConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public final class DBConnection {

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        String url = AppConfig.getRequiredString("db.url", "CHATAPP_DB_URL");
        String user = AppConfig.getRequiredString("db.user", "CHATAPP_DB_USER");
        String password = AppConfig.getString("db.password", "CHATAPP_DB_PASSWORD", "");

        return DriverManager.getConnection(url, user, password);
    }

    public static boolean checkLogin(String username, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }

                String storedHash = getColumnValue(rs, "password_hash");
                if (storedHash != null && !storedHash.trim().isEmpty()) {
                    return PasswordUtil.matches(password, storedHash);
                }

                String storedPassword = getColumnValue(rs, "password");
                return storedPassword != null && storedPassword.equals(password);
            }
        }
    }

    private static String getColumnValue(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();

        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            if (columnName.equalsIgnoreCase(metaData.getColumnLabel(i))) {
                return rs.getString(columnName);
            }
        }

        return null;
    }
}
