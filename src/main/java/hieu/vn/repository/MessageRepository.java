package hieu.vn.repository;

import hieu.vn.model.entity.Message;
import hieu.vn.util.SQLiteConnectUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Queue;
import java.sql.ResultSet;
import java.time.LocalDateTime;

public class MessageRepository {

    // Định dạng thời gian chuẩn ISO cho lưu xuống SQLite
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Lưu đối tượng Message vào cơ sở dữ liệu.
     *
     * @param message Đối tượng Message cần lưu
     */
    public static void saveMessage(Message message) {
        String sql = "INSERT INTO message (message, file_name, time, status, sender_id, receiver_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = SQLiteConnectUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, message.getMessage());
            stmt.setString(2, message.getFileName());
            stmt.setString(3, message.getTime().format(formatter)); // chuyển LocalDateTime -> String
            stmt.setString(4, message.getStatus());
            stmt.setInt(5, message.getSenderId());
            stmt.setInt(6, message.getReceiverId());

            stmt.executeUpdate(); // thực thi câu lệnh INSERT

        } catch (Exception e) {
            e.printStackTrace(); // Ghi log lỗi nếu có vấn đề khi ghi DB
        }
    }

    /**
     * Tìm tin nhắn có chứa file đính kèm với tên file tương ứng.
     *
     * @param fileName tên file đính kèm cần tìm
     * @return Đối tượng Message nếu tìm thấy, ngược lại trả về null
     */
    public static Message findByFileName(String fileName) {
        String sql = "SELECT * FROM message WHERE file_name = ?";

        try (Connection conn = SQLiteConnectUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fileName); // truyền tham số vào câu SQL
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Nếu có bản ghi, tạo đối tượng Message từ dữ liệu
                return new Message(
                        rs.getInt("id"),
                        rs.getString("message"),
                        rs.getString("file_name"),
                        LocalDateTime.parse(rs.getString("time")), // parse chuỗi thành LocalDateTime
                        rs.getString("status"),
                        rs.getInt("sender_id"),
                        rs.getInt("receiver_id")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null; // không tìm thấy tin nhắn chứa file
    }
}
