package hieu.vn.util;

import hieu.vn.Main;
import hieu.vn.model.entity.Token;
import hieu.vn.repository.TokenRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public class AccessTokenUtil {

    /**
     * Tạo access token mới cho user với thời hạn 10 phút kể từ thời điểm tạo.
     *
     * @param ipDevice địa chỉ IP của thiết bị đăng nhập
     * @param userId   ID người dùng
     * @return đối tượng Token chứa mã token và thông tin đi kèm
     */
    public static Token generate(String ipDevice, int userId) {
        UUID uuid = UUID.randomUUID(); // sinh chuỗi token ngẫu nhiên
        String token = uuid.toString();
        return new Token(
                token,
                LocalDateTime.now().plusMinutes(10), // hạn dùng: 10 phút
                ipDevice,
                "New",
                userId
        );
    }

    /**
     * Kiểm tra thời hạn token còn hợp lệ hay không.
     *
     * @param t token cần kiểm tra
     * @return true nếu token chưa hết hạn, false nếu đã hết hạn
     */
    public static boolean checkTime(Token t) {
        return LocalDateTime.now().isBefore(t.getTime());
    }

    /**
     * Trả về đối tượng Token từ mã token cho trước.
     * Ưu tiên lấy từ cache, nếu không có thì truy vấn database và lưu vào cache.
     *
     * @param tokenCode mã token (UUID)
     * @return Token nếu hợp lệ, null nếu không tồn tại
     */
    public static Token getToken(String tokenCode) {
        // Ưu tiên lấy token từ RAM cache
        Token token = Main.cachUtil.getTocken(tokenCode);
        if (token != null) {
            System.out.println("Token from cache: " + token);
            return token;
        }

        // Nếu không có trong cache → truy vấn DB
        token = TokenRepository.getByToken(tokenCode);
        if (token != null) {
            System.out.println("Token from DB: " + token);
            // Sau khi lấy từ DB → lưu lại cache để lần sau truy xuất nhanh hơn
            Main.cachUtil.putTocken(tokenCode, token);
        }

        return token; // có thể là null nếu tokenCode không hợp lệ
    }
}
