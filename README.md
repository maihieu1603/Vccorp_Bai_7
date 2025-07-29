# Chat API - Java Spark + SQLite

## ✨ Mô tả dự án

Đây là một **RESTful API** đơn giản được xây dựng bằng **Java (Spark framework)** để mô phỏng hệ thống chat:

* Đăng nhập bằng `username/password` → nhận `AccessToken` và `RefreshToken`
* Gửi tin nhắn văn bản hoặc file
* Nhận tin nhắn mới bằng **long polling**
* Tải file đính kèm nếu có quyền
* Tất cả dữ liệu được lưu trong **SQLite**

---

## ⚙️ Công nghệ sử dụng

* 💻 Java 17+
* ⚡ SparkJava (micro framework)
* 💃 SQLite (CSDL nhúng)
* 🧠 Guava Cache (cache RAM)
* 🔐 Token-based Authentication (Access + Refresh Token)
* 📌 File upload via multipart
* ⏳ Long Polling để lấy tin nhắn mới

---

## 🚀 Cách chạy dự án

### 1. Clone dự án

```bash
git clone https://github.com/your-username/chat-api-spark.git
cd chat-api-spark
```

### 2. Build dự án (sử dụng Maven)

```bash
mvn clean install
```

### 3. Chạy server

```bash
java -jar target/chat-api-spark.jar
```

Server sẽ chạy mặc định tại cổng `8080`.

---

## 🔐 Authentication Flow

* `POST /login?username=...&password=...`

 * Trả về: `AccessToken` và `RefreshToken` trong response header

* Tất cả API phía sau đều bắt buộc gửi header:

  ```http
  Authorization: {AccessToken}
  ```

---

## 📡 API Endpoints

### 🟢 Đăng nhập

```http
POST /login
Params: username, password
Response Headers: AccessToken, RefreshToken
```

### 📘 Danh sách bạn bè

```http
GET /friends
Headers: Authorization: {AccessToken}
```

### ✉️ Gửi tin nhắn

```http
POST /message/send
Headers:
  Authorization: {AccessToken}
Body (multipart/form-data):
  username: tên người nhận
  message: nội dung
  file: (tùy chọn)
```

### ⏳ Nhận tin nhắn mới (long polling)

```http
GET /message/new
Headers: Authorization: {AccessToken}
Response: JSON danh sách tin nhắn
```

### 📁 Tải file

```http
GET /file/{filename}
Headers: Authorization: {AccessToken}
```

---

## ✅ TODO (phát triển thêm)

* [ ] API đăng ký tài khoản
* [ ] API logout
* [ ] API đổi mật khẩu
* [ ] WebSocket (thay thế long polling)
* [ ] Phân quyền theo vai trò (Admin/User)

---

## 🧪 Chạy Unit Test

```bash
mvn test
```

---

## 👤 Tác giả

**Your Name** – [@yourgithub](https://github.com/yourgithub)

---

## 📄 License

MIT License
