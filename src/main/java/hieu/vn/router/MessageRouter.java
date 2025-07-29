package hieu.vn.router;

import hieu.vn.model.entity.Token;
import hieu.vn.service.MessageService;
import hieu.vn.util.AccessTokenUtil;

import javax.servlet.http.Part;

import static spark.Spark.*;

public class MessageRouter {

    /**
     * Đăng ký các route liên quan đến gửi và nhận tin nhắn, cũng như tải file đính kèm.
     */
    public static void applyRoutes() {

        MessageService service = new MessageService();

        /**
         * POST /message/send
         * Gửi tin nhắn văn bản hoặc kèm file đến người dùng khác.
         * Yêu cầu header "Authorization" chứa access token.
         * Dữ liệu gửi gồm:
         * - username: tên người nhận
         * - message: nội dung tin nhắn
         * - file: (tuỳ chọn) file đính kèm
         */
        post("/message/send", (req, res) -> {
            // Lấy và xác thực token từ header
            String tokenCode = req.headers("Authorization");
            Token token = AccessTokenUtil.getToken(tokenCode);

            // Cấu hình để xử lý multipart/form-data
            req.raw().setAttribute("org.eclipse.jetty.multipartConfig",
                    new javax.servlet.MultipartConfigElement("/tmp"));

            // Lấy thông tin người nhận và nội dung tin nhắn
            String receiverUsername = req.raw().getParameter("username");
            String messageContent = req.raw().getParameter("message");

            // Lấy file đính kèm (nếu có)
            Part filePart = null;
            try {
                filePart = req.raw().getPart("file");
            } catch (Exception ignored) {}

            // Gửi tin nhắn qua service
            String result = service.sendMessage(token, receiverUsername, messageContent, filePart);

            // Xử lý mã lỗi phù hợp
            if(result.equals("3")) {
                res.status(403);
            }

            return result;
        });

        /**
         * GET /message/new
         * Lấy các tin nhắn mới của người dùng (sử dụng long polling).
         * Yêu cầu header "Authorization".
         * Trả về JSON các tin nhắn mới.
         */
        get("/message/new", (req, res) -> {
            String tokenCode = req.headers("Authorization");
            Token token = AccessTokenUtil.getToken(tokenCode);

            res.type("application/json");
            return service.getMessagesAsJson(token.getUserId());
        });

        /**
         * GET /file/:filename
         * Tải file đính kèm từ server theo tên file.
         * Kiểm tra quyền truy cập file theo userId.
         * Header yêu cầu: Authorization
         */
        get("/file/:filename", (req, res) -> {
            String tokenCode = req.headers("Authorization");
            Token token = AccessTokenUtil.getToken(tokenCode);

            String filename = req.params(":filename");

            // Truy xuất file và ghi vào output stream
            int statusCode = service.getFileForUser(token.getUserId(), filename, res.raw().getOutputStream());

            if (statusCode == 200) {
                // File tải thành công
                res.raw().setContentType("application/octet-stream");
                res.raw().setHeader("Content-Disposition", "attachment; filename=" + filename);
                res.status(200);
                return res.raw();
            } else if (statusCode == 403) {
                // Không có quyền truy cập file
                res.status(403);
                return "Forbidden";
            } else {
                // File không tồn tại
                res.status(404);
                return "File not found";
            }
        });
    }
}
