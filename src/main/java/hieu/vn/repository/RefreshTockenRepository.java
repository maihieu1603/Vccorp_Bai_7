package hieu.vn.repository;

import hieu.vn.util.SQLiteConnectUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class RefreshTockenRepository {

    /**
     * Lưu một refresh token mới vào bảng `refresh_tocken`.
     *
     * @param token chuỗi refresh token
     * @return true nếu lưu thành công, false nếu có lỗi
     */
    public static boolean save(String token) {
        String sql = "INSERT INTO refresh_tocken (token) VALUES (?)";

        try (Connection conn = SQLiteConnectUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, token); // set giá trị token vào câu lệnh SQL
            int affected = stmt.executeUpdate(); // thực thi câu lệnh
            return affected > 0; // thành công nếu có dòng bị ảnh hưởng

        } catch (Exception e) {
            e.printStackTrace(); // log lỗi nếu có
        }

        return false; // trả về false nếu xảy ra exception
    }

    /**
     * Lấy toàn bộ danh sách refresh token trong hệ thống.
     *
     * @return List các token nếu truy vấn thành công, null nếu xảy ra lỗi
     */
    public static List<String> getAllToken() {
        String sql = "SELECT * FROM refresh_tocken";
        List<String> result = new ArrayList<>();

        try (Connection conn = SQLiteConnectUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // Lặp qua các dòng kết quả và lấy token
            while (rs.next()) {
                result.add(rs.getString("token"));
            }
            return result;

        } catch (Exception e) {
            e.printStackTrace(); // log lỗi nếu có
        }

        return null; // trả về null nếu có lỗi
    }
}
