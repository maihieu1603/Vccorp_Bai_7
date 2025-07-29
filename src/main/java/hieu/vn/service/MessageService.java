package hieu.vn.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hieu.vn.Main;
import hieu.vn.model.entity.Message;
import hieu.vn.model.entity.Token;
import hieu.vn.repository.FriendRepository;
import hieu.vn.repository.MessageRepository;
import hieu.vn.repository.UserRepository;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.Part;
import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Queue;

public class MessageService {

    /**
     * Gửi tin nhắn từ người gửi (dựa trên token) đến người nhận (theo username).
     * Có thể kèm nội dung văn bản và file đính kèm.
     *
     * @param token Token xác thực người gửi
     * @param receiverUsername Tên người nhận
     * @param messageContent Nội dung tin nhắn
     * @param filePart File đính kèm (nếu có)
     * @return "1" nếu người nhận đang online, "2" nếu offline, "3" nếu không phải bạn bè
     */
    public String sendMessage(Token token, String receiverUsername, String messageContent, Part filePart) {
        int senderId = token.getUserId();
        String fileName = null;

        try {
            // Nếu có file đính kèm
            if (filePart != null && filePart.getSize() > 0) {
                // Lấy tên file cuối cùng (tránh đường dẫn độc hại từ client)
                fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();

                // Tạo thư mục lưu file nếu chưa có
                File uploadDir = new File("storage");
                if (!uploadDir.exists()) uploadDir.mkdir();

                // Lưu file vào thư mục storage
                File uploadedFile = new File(uploadDir, fileName);
                try (FileOutputStream out = new FileOutputStream(uploadedFile)) {
                    IOUtils.copy(filePart.getInputStream(), out);
                }

                // Ghi chú thông tin file trong nội dung tin nhắn
                messageContent += " [FILE: " + fileName + "]";
            }
        } catch (Exception ignored) {
            // Nếu lỗi file (như định dạng, ghi lỗi...), vẫn cho phép gửi tin nhắn văn bản
        }

        // Tìm ID của người nhận dựa vào username
        int receiverId = UserRepository.findByUsername(receiverUsername).getId();

        // Nếu không phải bạn bè thì không cho gửi
        if (!FriendRepository.isFriend(senderId, receiverId)) return "3";

        // Tạo tin nhắn và lưu vào DB
        Message message = new Message(messageContent, fileName, senderId, receiverId);
        MessageRepository.saveMessage(message);

        // Nếu người nhận đang đợi tin nhắn qua long polling
        if (Main.cachUtil.hasWaiting(receiverId)) {
            // Gửi trực tiếp cho client đang đợi
            Main.cachUtil.sendMessageToUser(receiverId, message);
            return "1"; // trạng thái: gửi trực tiếp
        }

        // Nếu người nhận offline → đưa tin nhắn vào hàng đợi
        Main.cachUtil.queueMessage(receiverId, message);
        return "2"; // trạng thái: chờ gửi sau
    }

    /**
     * Lấy các tin nhắn mới cho user. Nếu không có, chờ tối đa 10 giây (long polling).
     *
     * @param userId ID người nhận
     * @return Hàng đợi tin nhắn, có thể rỗng nếu timeout
     * @throws InterruptedException nếu chờ quá lâu
     */
    public Queue<Message> getNewMessages(int userId) throws InterruptedException {
        // Kiểm tra nếu có sẵn tin nhắn trong hàng đợi
        Queue<Message> queue = Main.cachUtil.getMessages(userId);
        if (queue != null && !queue.isEmpty()) {
            return queue;
        }

        // Nếu chưa có tin → đăng ký user này vào danh sách đang chờ
        Object lock = Main.cachUtil.registerWaiting(userId);

        // Chờ tối đa 10 giây để nhận tin nhắn mới
        synchronized (lock) {
            lock.wait(10000); // nếu có tin, nơi khác sẽ notify()
        }

        //Sau khi chờ xong, xóa userId khỏi hàng chờ (dù có tin hay không)
        Main.cachUtil.clearWaiting(userId);
        // Sau thời gian chờ → kiểm tra lại hàng đợi
        queue = Main.cachUtil.getMessages(userId);
        return queue != null ? queue : new java.util.LinkedList<>();
    }

    /**
     * Lấy các tin nhắn mới dưới dạng JSON để gửi về client.
     *
     * @param userId ID người nhận
     * @return JSON chuỗi các tin nhắn
     * @throws InterruptedException nếu chờ quá lâu (long polling)
     */
    public String getMessagesAsJson(int userId) throws InterruptedException {
        // Gọi long polling lấy tin nhắn mới
        Queue<Message> queue = getNewMessages(userId);

        // Sau khi lấy tin xong → xóa khỏi hàng đợi (tránh lặp)
        Main.cachUtil.clearMessages(userId);

        // Cấu hình Gson để serialize LocalDateTime đúng chuẩn ISO
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, (com.google.gson.JsonSerializer<LocalDateTime>)
                        (src, typeOfSrc, context) -> new com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .create();

        // Trả về JSON danh sách tin nhắn
        return gson.toJson(new ArrayList<>(queue));
    }

    /**
     * Trả file đính kèm từ tin nhắn nếu user có quyền tải (người gửi hoặc người nhận).
     *
     * @param userId ID của người yêu cầu tải file
     * @param filename Tên file cần tải
     * @param outStream OutputStream để ghi nội dung file trả về client
     * @return 200 nếu thành công, 403 nếu không có quyền, 404 nếu không tồn tại
     * @throws IOException lỗi đọc/ghi file
     */
    public int getFileForUser(int userId, String filename, OutputStream outStream) throws IOException {
        // Tìm tin nhắn có file đó
        Message message = MessageRepository.findByFileName(filename);

        // Nếu không có tin nhắn chứa file
        if (message == null) return 404;

        // Chỉ cho phép tải nếu là người gửi hoặc người nhận
        if (message.getReceiverId() != userId && message.getSenderId() != userId) return 403;

        // Kiểm tra xem file còn tồn tại vật lý không
        File file = new File("storage/" + filename);
        if (!file.exists()) return 404;

        // Ghi file vào output stream để gửi về client
        try (FileInputStream in = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = in.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
            }
            outStream.flush();
        }

        return 200; // OK
    }
}
