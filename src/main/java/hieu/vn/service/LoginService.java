package hieu.vn.service;

import hieu.vn.model.entity.Token;
import hieu.vn.model.entity.User;
import hieu.vn.repository.TokenRepository;
import hieu.vn.repository.UserRepository;
import hieu.vn.repository.RefreshTockenRepository;
import hieu.vn.Main;
import hieu.vn.util.AccessTokenUtil;
import spark.Request;
import spark.Response;

import static spark.Spark.halt;

public class LoginService {

    /**
     * Xử lý đăng nhập người dùng.
     * Nếu username/password hợp lệ → tạo access token + refresh token,
     * lưu vào DB và cache, trả về token trong header.
     *
     * @param req yêu cầu HTTP từ client (chứa username và password)
     * @param res phản hồi HTTP để gắn header
     * @return thông báo thành công nếu đăng nhập đúng
     */
    public static Object login(Request req, Response res) {
        // Lấy thông tin từ query parameters
        String username = req.queryParams("username");
        String password = req.queryParams("password");
        String ipDevice = req.ip(); // IP thiết bị dùng để xác thực

        // Kiểm tra thiếu đầu vào
        if (username == null || password == null) {
            halt(400, "Thiếu username hoặc password");
        }

        // Kiểm tra người dùng tồn tại và mật khẩu đúng
        User user = UserRepository.findByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            halt(401, "Sai tài khoản hoặc mật khẩu");
        }

        // Tạo access token (thời gian sống ngắn)
        Token accessToken = AccessTokenUtil.generate(ipDevice, user.getId());

        // Lưu access token vào DB
        TokenRepository.saveToken(accessToken, user.getId());

        // Lưu access token và user vào RAM cache để truy xuất nhanh
        Main.cachUtil.putTocken(accessToken.getToken(), accessToken); // lưu token
        Main.cachUtil.putUser(accessToken.getToken(), user);          // lưu user

        // Tạo refresh token (sống lâu hơn, chỉ dùng để lấy access token mới)
        String refreshToken = Main.refreshTockenUtil.generateToken();
        RefreshTockenRepository.save(refreshToken);

        // Trả token cho client trong header HTTP
        res.header("AccessToken", accessToken.getToken());
        res.header("RefreshToken", refreshToken);

        return "Đăng nhập thành công";
    }
}
