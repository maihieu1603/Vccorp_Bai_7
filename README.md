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
git clone https://github.com/maihieu1603/Vccorp_Bai_7.git

cd Vccorp_Bai_7
```

### 2. Build dự án (sử dụng Maven)

```bash
mvn clean install
```

### 3. Chạy server

```bash
java -jar target/ChatApp-jar-with-dependencies.jar
```

Server sẽ chạy mặc định tại cổng `8080`.

---

## 📡 API Endpoints

### 🟢 Đăng nhập

```http:
POST /login
Params: username, password
Response Headers: AccessToken, RefreshToken
```
1 số tài khoản: alice, bob, charlie
mật khẩu đều là 123456
### 📘 Danh sách bạn bè

```http
GET /friends
Headers: Authorization: {AccessToken}
```

### ✉️ Gửi tin nhắn
Login với tài khoản alice
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
