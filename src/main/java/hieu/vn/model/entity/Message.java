package hieu.vn.model.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {
    private int id;
    private String message;        // Nội dung văn bản
    private String fileName;       // Tên file đính kèm (nếu có)
    private LocalDateTime time;
    private String status;
    private int senderId;
    private int receiverId;

    public Message() {
        this.time = LocalDateTime.now();
        this.status = "pending";
    }

    // Constructor khi gửi từ client
    public Message(String message, String fileName, int senderId, int receiverId) {
        this.message = message;
        this.fileName = fileName;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.time = LocalDateTime.now();
        this.status = "pending";
    }

    // Constructor đầy đủ (thường dùng khi lấy từ DB)
    public Message(int id, String message, String fileName, LocalDateTime time, String status, int senderId, int receiverId) {
        this.id = id;
        this.message = message;
        this.fileName = fileName;
        this.time = time;
        this.status = status;
        this.senderId = senderId;
        this.receiverId = receiverId;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public LocalDateTime getTime() { return time; }
    public void setTime(LocalDateTime time) { this.time = time; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }

    public int getReceiverId() { return receiverId; }
    public void setReceiverId(int receiverId) { this.receiverId = receiverId; }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", fileName='" + fileName + '\'' +
                ", time=" + time +
                ", status='" + status + '\'' +
                ", senderId=" + senderId +
                ", receiverId=" + receiverId +
                '}';
    }
}
