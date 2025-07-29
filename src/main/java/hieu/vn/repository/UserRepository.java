package hieu.vn.repository;

import hieu.vn.model.entity.User;
import hieu.vn.util.SQLiteConnectUtil;

import java.sql.*;

public class UserRepository {

    /**
     * Truy vấn người dùng theo username từ bảng `user`.
     *
     * @param username tên đăng nhập cần tìm
     * @return Đối tượng User nếu tồn tại, null nếu không tìm thấy hoặc có lỗi
     */
    public static User findByUsername(String username) {
        String sql = "SELECT id, username, password FROM user WHERE username = ?";

        try (Connection conn = SQLiteConnectUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Gán giá trị cho tham số SQL
            stmt.setString(1, username);

            // Thực hiện truy vấn
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Nếu có kết quả, tạo đối tượng User và gán dữ liệu
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                return user;
            }

        } catch (SQLException e) {
            // Log lỗi truy vấn nếu xảy ra lỗi kết nối hoặc SQL
            System.err.println("❌ findByUsername failed: " + e.getMessage());
        }

        // Trả về null nếu không tìm thấy hoặc có lỗi
        return null;
    }
}
