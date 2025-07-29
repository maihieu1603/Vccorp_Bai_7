package hieu.vn.repository;

import hieu.vn.util.SQLiteConnectUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FriendRepository {

    /**
     * Trả về danh sách tên người dùng (username) là bạn bè của user có ID cho trước.
     *
     * @param userId ID của người dùng cần truy vấn
     * @return Danh sách username bạn bè
     */
    public List<String> getFriendUsernames(int userId) {
        List<String> friends = new ArrayList<>();

        // Truy vấn các username bạn bè bằng JOIN từ bảng friends sang bảng user
        String sql = "SELECT u.username FROM friends f " +
                "JOIN user u ON f.friend_id = u.id " +
                "WHERE f.user_id = ?";

        try (Connection conn = SQLiteConnectUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            // Thêm từng username vào danh sách kết quả
            while (rs.next()) {
                friends.add(rs.getString("username"));
            }
        } catch (Exception e) {
            e.printStackTrace(); // Ghi log lỗi nếu có
        }

        return friends;
    }

    /**
     * Kiểm tra xem userId có phải là bạn với friendId không.
     *
     * @param userId    ID người dùng
     * @param friendId  ID bạn bè cần kiểm tra
     * @return true nếu là bạn bè, false nếu không
     */
    public static boolean isFriend(int userId, int friendId) {
        String sql = "SELECT 1 FROM friends WHERE user_id = ? AND friend_id = ?";

        try (Connection conn = SQLiteConnectUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, friendId);

            try (ResultSet rs = stmt.executeQuery()) {
                // Nếu tồn tại bản ghi thì là bạn bè
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false; // Không tìm thấy bản ghi
    }
}
