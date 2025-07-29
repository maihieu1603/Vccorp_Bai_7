package hieu.vn.repository;

import hieu.vn.model.entity.Token;
import hieu.vn.util.SQLiteConnectUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TokenRepository {

    // Định dạng thời gian lưu và đọc từ CSDL (theo ISO format)
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Truy vấn lấy đối tượng Token từ database dựa trên mã token (tokenCode),
     * chỉ trả về nếu token còn hiệu lực (status = "New").
     *
     * @param tokenCode mã token
     * @return đối tượng Token nếu hợp lệ, ngược lại trả về null
     */
    public static Token getByToken(String tokenCode) {
        String sql = "SELECT * FROM token WHERE token = ? AND status = ?";

        try (Connection conn = SQLiteConnectUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tokenCode);
            stmt.setString(2, "New"); // chỉ lấy token chưa bị vô hiệu hóa
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Parse dữ liệu từ ResultSet
                String token = rs.getString("token");
                String timeStr = rs.getString("time");
                String ip = rs.getString("ip_device");
                String status = rs.getString("status");
                int userId = rs.getInt("user_id");

                LocalDateTime time = LocalDateTime.parse(timeStr, formatter);

                return new Token(token, time, ip, status, userId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // không tìm thấy token hợp lệ
    }

    /**
     * Lưu một access token mới vào bảng `token`.
     *
     * @param token  đối tượng Token
     * @param userId ID người dùng liên kết với token
     * @return true nếu lưu thành công
     */
    public static boolean saveToken(Token token, int userId) {
        String sql = "INSERT INTO token (token, time, ip_device, status, user_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = SQLiteConnectUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, token.getToken());
            stmt.setString(2, token.getTime().format(formatter));
            stmt.setString(3, token.getIpDevice());
            stmt.setString(4, token.getStatus());
            stmt.setInt(5, userId);

            int affected = stmt.executeUpdate();
            return affected > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật trạng thái (status) của một token, ví dụ từ "New" thành "Old".
     *
     * @param tokenCode  mã token cần cập nhật
     * @param newStatus  trạng thái mới
     * @return true nếu cập nhật thành công
     */
    public static boolean updateTokenStatus(String tokenCode, String newStatus) {
        String sql = "UPDATE token SET status = ? WHERE token = ?";
        try (Connection conn = SQLiteConnectUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setString(2, tokenCode);

            int affected = stmt.executeUpdate();
            return affected > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Lấy userId tương ứng với token nếu token còn hiệu lực.
     *
     * @param tokenCode mã token
     * @return ID người dùng nếu token hợp lệ, null nếu không hợp lệ hoặc không tồn tại
     */
    public static Integer getUserIdByValidToken(String tokenCode) {
        String sql = "SELECT user_id FROM token WHERE token = ? AND status = 'New'";

        try (Connection conn = SQLiteConnectUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tokenCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("user_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // token không hợp lệ
    }
}
