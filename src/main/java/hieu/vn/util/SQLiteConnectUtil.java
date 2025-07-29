package hieu.vn.util;

import java.sql.*;

public class SQLiteConnectUtil {
    private static final String DB_URL = "jdbc:sqlite:bai_7.1_database.db";

    // Hàm tiện ích để lấy Connection
    public static Connection getConnection() {
        try {
            // Không bắt buộc gọi Class.forName với SQLite hiện đại, nhưng vẫn có thể dùng
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection(DB_URL);
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
            return null;
        }
    }

    // Hàm tiện ích đóng connection nếu không dùng try-with-resources
    public static void closeConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Failed to close connection: " + e.getMessage());
        }
    }
}
