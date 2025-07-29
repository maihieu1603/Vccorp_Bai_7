package hieu.vn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class SQLiteSetup {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:bai_7.1_database.db"; // Tên file SQLite

        // Bảng người dùng
        String createUserTable =
                "CREATE TABLE IF NOT EXISTS user (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username TEXT NOT NULL UNIQUE, " +
                        "password TEXT NOT NULL" +
                        ");";

        // Bảng token xác thực
        String createTokenTable =
                "CREATE TABLE IF NOT EXISTS token (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "token TEXT NOT NULL, " +
                        "time TEXT NOT NULL, " +
                        "ip_device TEXT, " +
                        "status TEXT DEFAULT 'active', " +
                        "user_id INTEGER, " +
                        "FOREIGN KEY(user_id) REFERENCES user(id) ON DELETE CASCADE" +
                        ");";

        // Bảng tin nhắn
        String createMessageTable =
                "CREATE TABLE IF NOT EXISTS message (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "message TEXT NOT NULL, " +
                        "file_name TEXT, " +
                        "time TEXT NOT NULL, " +
                        "status TEXT DEFAULT 'pending', " +
                        "sender_id INTEGER, " +
                        "receiver_id INTEGER, " +
                        "FOREIGN KEY(sender_id) REFERENCES user(id) ON DELETE CASCADE, " +
                        "FOREIGN KEY(receiver_id) REFERENCES user(id) ON DELETE CASCADE" +
                        ");";


        // Bảng quan hệ bạn bè
        String createFriendsTable =
                "CREATE TABLE IF NOT EXISTS friends (" +
                        "user_id INTEGER NOT NULL, " +
                        "friend_id INTEGER NOT NULL, " +
                        "PRIMARY KEY (user_id, friend_id), " +
                        "FOREIGN KEY(user_id) REFERENCES user(id) ON DELETE CASCADE, " +
                        "FOREIGN KEY(friend_id) REFERENCES user(id) ON DELETE CASCADE" +
                        ");";
        // Bảng refresh token (bổ sung)
        String createRefreshTokenTable =
                "CREATE TABLE IF NOT EXISTS refresh_tocken (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "token TEXT NOT NULL" +
                        ");";


        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA foreign_keys = ON;"); // Bật kiểm tra khóa ngoại

            stmt.execute(createUserTable);
            stmt.execute(createTokenTable);
            stmt.execute(createMessageTable);
            stmt.execute(createFriendsTable);
            stmt.execute(createRefreshTokenTable);
            // Thêm người dùng mẫu
            stmt.execute("INSERT OR IGNORE INTO user(username, password) VALUES ('alice', '123456');");
            stmt.execute("INSERT OR IGNORE INTO user(username, password) VALUES ('bob', '123456');");
            stmt.execute("INSERT OR IGNORE INTO user(username, password) VALUES ('charlie', '123456');");

            // Tạo quan hệ bạn bè (sử dụng ID trực tiếp)
            stmt.execute("INSERT OR IGNORE INTO friends(user_id, friend_id) VALUES (1, 2);");
            stmt.execute("INSERT OR IGNORE INTO friends(user_id, friend_id) VALUES (2, 3);");
            stmt.execute("INSERT OR IGNORE INTO friends(user_id, friend_id) VALUES (3, 1);");



            System.out.println("All tables created successfully.");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
