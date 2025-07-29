package hieu.vn;

import hieu.vn.middleware.AuthMiddleware;
import hieu.vn.router.FriendRouter;
import hieu.vn.router.MessageRouter;
import hieu.vn.router.UserRouter;
import hieu.vn.util.CachUtil;
import hieu.vn.util.RefreshTockenUtil;

import static spark.Spark.*;

/**
 * Lớp khởi động chính cho ứng dụng chat sử dụng SparkJava.
 *
 * - Thiết lập cổng chạy server (port 8080)
 * - Khởi tạo các tiện ích toàn cục như bộ nhớ đệm tin nhắn (cache) và quản lý refresh token
 * - Áp dụng middleware để kiểm tra đăng nhập trước khi truy cập các API cần xác thực
 * - Đăng ký các router xử lý nghiệp vụ: đăng nhập, bạn bè, tin nhắn
 */
public class Main {

    // Bộ nhớ đệm để lưu hàng đợi tin nhắn và người dùng đang chờ nhận tin nhắn (sử dụng cho long polling)
    public static CachUtil cachUtil = new CachUtil();

    // Tiện ích quản lý refresh token cho cơ chế cấp lại access token
    public static RefreshTockenUtil refreshTockenUtil = new RefreshTockenUtil();

    public static void main(String[] args) {

        // Thiết lập server chạy trên cổng 8080
        port(8080);

        // ================= Middleware bảo vệ API =================
        // Middleware kiểm tra token hợp lệ trước khi truy cập các route bắt đầu bằng "/friends"
        before("/friends", AuthMiddleware::apply);
        // Middleware kiểm tra token hợp lệ trước khi truy cập các route bắt đầu bằng "/message"
        before("/message", AuthMiddleware::apply);

        // ================= Đăng ký các tuyến đường (route) =================

        // Router xử lý các chức năng liên quan đến người dùng: đăng ký, đăng nhập, refresh token
        UserRouter.applyRoutes();

        // Router xử lý các chức năng liên quan đến bạn bè: lấy danh sách, thêm bạn bè
        FriendRouter.applyRoutes();

        // Router xử lý các chức năng liên quan đến tin nhắn: gửi, nhận tin nhắn và tải file
        MessageRouter.applyRoutes();

        // Sau khi chạy, API có thể truy cập tại http://localhost:8080
    }
}
