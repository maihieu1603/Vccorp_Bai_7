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
mvn clean package
```

### 3. Chạy server

```bash
java -jar target/ChatApp-jar-with-dependencies.jar
```

Server sẽ chạy mặc định tại cổng `8080`.

---

## 📡 API Endpoints

### 🟢 Đăng nhập

```http
POST /login
Body (x-www-form-urlencoded):
  username=alice
  password=123456
```

📌 Một số tài khoản mẫu: `alice`, `bob`, `charlie`  
🔑 Mật khẩu cho tất cả: `123456`  
📥 Sau khi đăng nhập, bạn sẽ nhận được `AccessToken` và `RefreshToken` trong response header.

---

### 📘 Danh sách bạn bè

```http
GET /friends
Headers:
  Authorization: {AccessToken}
```

---

### ✉️ Gửi tin nhắn (tài khoản: alice)

**Bước 1: Đăng nhập tài khoản alice**
```bash
curl -X POST http://localhost:8080/login \
  -d "username=alice" -d "password=123456" -i
```
→ Copy giá trị `AccessToken` trong response header.

**Bước 2: Gửi tin nhắn cho bob (không kèm file)**
```bash
curl -X POST http://localhost:8080/message/send \
  -H "Authorization: {AccessToken_Of_Alice}" \
  -F "username=bob" \
  -F "message=Hello Bob!"
```

**Bước 3: Gửi tin nhắn kèm file**
```bash
curl -X POST http://localhost:8080/message/send \
  -H "Authorization: {AccessToken_Of_Alice}" \
  -F "username=bob" \
  -F "message=Gửi file cho bạn nè" \
  -F "file=@path/to/ten_file.txt"
```

📎 `file` là tùy chọn, bạn có thể upload bất kỳ file nào như `.jpg`, `.pdf`, `.docx`, v.v.

---

### ⏳ Nhận tin nhắn mới (tài khoản: bob)

**Bước 1: Đăng nhập tài khoản bob**
```bash
curl -X POST http://localhost:8080/login \
  -d "username=bob" -d "password=123456" -i
```
→ Copy giá trị `AccessToken` trong response header.

**Bước 2: Lấy tin nhắn mới bằng long polling**
```bash
curl -X GET http://localhost:8080/message/new \
  -H "Authorization: {AccessToken_Of_Bob}"
```

📌 Long polling sẽ giữ kết nối tối đa 10 giây nếu chưa có tin nhắn.

---

### 📁 Tải file đính kèm

```http
GET /file/{filename}
Headers:
  Authorization: {AccessToken}
```

Ví dụ:
```bash
curl -O -J -L http://localhost:8080/file/example.pdf \
  -H "Authorization: {AccessToken}"
```

---

## 📦 Ghi chú

- Hệ thống kiểm tra token theo IP của thiết bị.
- Nếu `AccessToken` hết hạn, dùng `RefreshToken` để lấy token mới.
- Nếu cả hai token hết hạn → cần đăng nhập lại.
- Server mặc định lưu file vào thư mục `storage/` trong dự án.

