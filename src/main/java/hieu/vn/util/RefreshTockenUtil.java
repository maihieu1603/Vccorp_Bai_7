package hieu.vn.util;

import hieu.vn.repository.RefreshTockenRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class RefreshTockenUtil {

    // Bộ nhớ tạm trong RAM lưu trữ tất cả refresh token đang hợp lệ
    private Set<String> refreshTockenStore;

    /**
     * Constructor khởi tạo: load toàn bộ refresh token từ database vào bộ nhớ RAM (Set)
     */
    public RefreshTockenUtil() {
        // Lấy danh sách token từ database và đưa vào HashSet để tra cứu nhanh (O(1))
        this.refreshTockenStore = RefreshTockenRepository
                .getAllToken()
                .stream()
                .collect(Collectors.toSet());
    }

    /**
     * Xóa một refresh token khỏi bộ nhớ (thường khi token bị thay thế hoặc không còn hợp lệ).
     *
     * @param token chuỗi refresh token cần xóa
     */
    public void remove(String token) {
        this.refreshTockenStore.remove(token);
    }

    /**
     * Kiểm tra xem refresh token có tồn tại trong hệ thống không.
     *
     * @param token chuỗi refresh token cần kiểm tra
     * @return true nếu tồn tại, false nếu không hợp lệ hoặc đã bị xóa
     */
    public boolean containsRefreshTocken(String token) {
        return refreshTockenStore.contains(token);
    }

    /**
     * Sinh refresh token mới (UUID), thêm vào bộ nhớ và trả về chuỗi.
     *
     * @return chuỗi refresh token mới
     */
    public String generateToken() {
        String refreshToken = UUID.randomUUID().toString();
        this.refreshTockenStore.add(refreshToken); // lưu vào RAM, đồng thời được lưu DB tại LoginService
        return refreshToken;
    }
}
