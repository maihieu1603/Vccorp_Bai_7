package hieu.vn.service;

import hieu.vn.Main;
import hieu.vn.repository.TokenRepository;
import hieu.vn.repository.FriendRepository;

import java.util.List;

public class FriendService {

    // Repository xử lý truy vấn token từ database
    private final TokenRepository tokenRepo = new TokenRepository();

    // Repository truy vấn danh sách bạn bè
    private final FriendRepository friendRepo = new FriendRepository();

    /**
     * Trả về danh sách tên bạn bè của người dùng dựa trên access token.
     * Nếu token có sẵn trong cache thì dùng cache, nếu không thì lấy từ database.
     *
     * @param accessToken mã token được gửi từ client
     * @return danh sách username bạn bè nếu token hợp lệ, null nếu token không hợp lệ
     */
    public List<String> getFriends(String accessToken) {
        int userId;

        // Ưu tiên lấy từ cache để giảm truy vấn DB
        if (Main.cachUtil.containsTocken(accessToken)) {
            userId = Main.cachUtil.getUser(accessToken).getId();
        } else {
            // Nếu không có trong cache, tìm từ database
            Integer dbUserId = tokenRepo.getUserIdByValidToken(accessToken);
            if (dbUserId == null) return null; // Token không hợp lệ
            userId = dbUserId;
        }

        // Truy vấn danh sách bạn bè từ FriendRepository
        return friendRepo.getFriendUsernames(userId);
    }
}
