package hieu.vn.router;

import hieu.vn.service.LoginService;

import static spark.Spark.post;

public class UserRouter {

    /**
     * Đăng ký các route (endpoint) liên quan đến người dùng.
     * Hiện tại chỉ có một route là /login dùng để xử lý đăng nhập.
     */
    public static void applyRoutes() {
        // Đăng ký route POST /login để gọi hàm login trong LoginService
        post("/login", LoginService::login);
    }
}
