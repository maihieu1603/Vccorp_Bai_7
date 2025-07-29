package hieu.vn.middleware;

import hieu.vn.Main;
import hieu.vn.model.entity.Token;
import hieu.vn.repository.RefreshTockenRepository;
import hieu.vn.repository.TokenRepository;
import hieu.vn.util.AccessTokenUtil;
import spark.Request;
import spark.Response;

import static spark.Spark.halt;

public class AuthMiddleware {

    /**
     * Middleware dùng để xác thực access token trong các API yêu cầu đăng nhập.
     * Nếu token hợp lệ và chưa hết hạn, cho phép tiếp tục xử lý request.
     * Nếu token hết hạn và có refresh token hợp lệ, phát hành token mới.
     * Nếu không, từ chối truy cập với mã lỗi 401.
     *
     * @param req Spark Request
     * @param res Spark Response
     */
    public static void apply(Request req, Response res) {
        // Lấy access token từ header
        String accessToken = req.headers("Authorization");
        if (accessToken == null) {
            halt(401, "Vui lòng gửi kèm token");
        }

        System.out.println("accessToken: " + accessToken);
        String ipDevice = req.ip();

        // Kiểm tra token trong cache, nếu không có thì truy vấn DB
        Token tokenData = Main.cachUtil.containsTocken(accessToken)
                ? Main.cachUtil.getTocken(accessToken)
                : TokenRepository.getByToken(accessToken);

        System.out.println("tokenData: " + tokenData);
        if (tokenData == null) {
            halt(401, "Token không hợp lệ. Vui lòng đăng nhập lại");
        }

        // Kiểm tra IP thiết bị có trùng với khi đăng nhập không
        if (!tokenData.getIpDevice().equals(ipDevice)) {
            halt(401, "Không đúng thiết bị đã đăng nhập");
        }

        // Kiểm tra access token có còn thời gian sử dụng không
        if (!AccessTokenUtil.checkTime(tokenData)) {
            // Nếu token đã hết hạn, kiểm tra refresh token
            String refreshToken = req.headers("RefreshToken");
            if (refreshToken == null || !Main.refreshTockenUtil.containsRefreshTocken(refreshToken)) {
                halt(401, "Token đã hết hạn và không có refresh token hợp lệ");
            }

            // Tạo access token mới
            Token newToken = AccessTokenUtil.generate(ipDevice, tokenData.getUserId());

            // Lưu token mới vào DB, đánh dấu token cũ là "old"
            TokenRepository.saveToken(newToken, tokenData.getUserId());
            TokenRepository.updateTokenStatus(tokenData.getToken(), "old");

            // Gửi access token mới qua header về cho client
            res.header("NewToken", newToken.getToken());

            // Xóa refresh token cũ khỏi cache và tạo refresh token mới lưu và cache và csdl
            Main.refreshTockenUtil.remove(refreshToken);
            String newRefreshToken = Main.refreshTockenUtil.generateToken();
            RefreshTockenRepository.save(newRefreshToken);
            res.header("NewRefreshToken", newRefreshToken);
        }
    }
}
