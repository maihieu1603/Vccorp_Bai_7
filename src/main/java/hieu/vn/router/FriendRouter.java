package hieu.vn.router;

import static spark.Spark.*;
import com.google.gson.Gson;
import hieu.vn.service.FriendService;

import java.util.List;

public class FriendRouter {

    // Gson để chuyển đổi danh sách bạn bè sang JSON
    private static final Gson gson = new Gson();

    // Service xử lý logic liên quan đến bạn bè
    private static final FriendService friendService = new FriendService();

    /**
     * Đăng ký các route liên quan đến danh sách bạn bè của người dùng.
     */
    public static void applyRoutes() {

        /**
         * GET /friends
         * Trả về danh sách bạn bè của người dùng.
         * Yêu cầu có header Authorization chứa access token hợp lệ.
         */
        get("/friends", (req, res) -> {
            // Lấy token từ header
            String token = req.headers("Authorization");

            // Lấy danh sách bạn bè từ service
            List<String> friendList = friendService.getFriends(token);

            // Nếu token không hợp lệ hoặc đã hết hạn
            if (friendList == null) {
                res.status(403);  // Forbidden
                return "Invalid or expired token";
            }

            // Trả về danh sách bạn bè ở dạng JSON
            res.type("application/json");
            return gson.toJson(friendList);
        });
    }
}
