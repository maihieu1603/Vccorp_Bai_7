package hieu.vn.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import hieu.vn.model.entity.Message;
import hieu.vn.model.entity.Token;
import hieu.vn.model.entity.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class CachUtil {

    // Cache lưu thông tin người dùng theo access token
    private final Cache<String, User> cacheUser;

    // Cache lưu access token (Token entity)
    private final Cache<String, Token> cacheTocken;

    // Hàng đợi tin nhắn: mỗi userId có một hàng đợi các tin nhắn đang chờ xử lý
    private final Map<Integer, Queue<Message>> messageQueues = new ConcurrentHashMap<>();

    // Danh sách user đang long-polling để chờ tin nhắn mới (userId → lock object)
    private final Map<Integer, Object> waitingUsers = new ConcurrentHashMap<>();

    // Constructor khởi tạo cache với thời gian sống khác nhau
    public CachUtil() {
        // Cache user sống 20s sau khi ghi, và tối đa 10 phút kể từ lần truy cập cuối
        cacheUser = CacheBuilder.newBuilder()
                .expireAfterWrite(20, TimeUnit.SECONDS)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build();

        // Cache token sống 10 phút cả sau khi ghi và truy cập
        cacheTocken = CacheBuilder.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build();
    }

    // ---------------------- USER CACHE ----------------------

    /** Lưu User vào cache theo token */
    public void putUser(String key, User user) {
        cacheUser.put(key, user);
    }

    /** Truy xuất User từ cache */
    public User getUser(String key) {
        return cacheUser.getIfPresent(key);
    }

    // ---------------------- TOKEN CACHE ----------------------

    /** Lưu Token vào cache theo token string */
    public void putTocken(String key, Token tocken) {
        cacheTocken.put(key, tocken);
    }

    /** Truy xuất Token từ cache */
    public Token getTocken(String key) {
        return cacheTocken.getIfPresent(key);
    }

    /** Kiểm tra cache có chứa token theo key */
    public boolean containsTocken(String key) {
        return cacheTocken.getIfPresent(key) != null;
    }

    // ---------------------- MESSAGE QUEUE ----------------------

    /** Thêm tin nhắn vào hàng đợi của người nhận */
    public void queueMessage(int receiverId, Message message) {
        messageQueues
                .computeIfAbsent(receiverId, k -> new LinkedList<>())
                .add(message);
    }

    /** Lấy hàng đợi tin nhắn của user */
    public Queue<Message> getMessages(int userId) {
        return messageQueues.getOrDefault(userId, new LinkedList<>());
    }

    /** Xoá toàn bộ tin nhắn đã xử lý của user */
    public void clearMessages(int userId) {
        messageQueues.remove(userId);
    }

    // ---------------------- LONG POLLING ----------------------

    /** Kiểm tra user có đang đợi long-polling không */
    public boolean hasWaiting(int userId) {
        return waitingUsers.containsKey(userId);
    }

    /**
     * Đăng ký user đang đợi long-polling.
     * Trả về lock object để dùng cho synchronized-wait.
     */
    public Object registerWaiting(int userId) {
        return waitingUsers.computeIfAbsent(userId, k -> new Object());
    }

    /**
     * Gửi tin nhắn đến user đang chờ long-polling và notify để đánh thức request.
     */
    public void sendMessageToUser(int userId, Message message) {
        queueMessage(userId, message); // thêm vào hàng đợi
        Object lock = waitingUsers.remove(userId); // xoá khỏi danh sách đang chờ
        if (lock != null) {
            synchronized (lock) {
                lock.notify(); // đánh thức thread đang chờ
            }
        }
    }

    /** Xoá trạng thái chờ của user khỏi long-polling */
    public void clearWaiting(int userId) {
        waitingUsers.remove(userId);
    }
}
