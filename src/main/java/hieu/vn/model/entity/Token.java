package hieu.vn.model.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class Token implements Serializable {
    private int id;
    private String token;
    private LocalDateTime time;
    private String ipDevice;
    private String status;
    private int userId;

    // Constructor đầy đủ


    public Token(String token, LocalDateTime time, String ipDevice, String status, int userId) {
        this.token = token;
        this.time = time;
        this.ipDevice = ipDevice;
        this.status = status;
        this.userId = userId;
    }



    // Getter & Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public String getIpDevice() {
        return ipDevice;
    }

    public void setIpDevice(String ipDevice) {
        this.ipDevice = ipDevice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // equals & hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token tocken = (Token) o;
        return Objects.equals(token, tocken.token) &&
                Objects.equals(time, tocken.time) &&
                Objects.equals(ipDevice, tocken.ipDevice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token, time, ipDevice);
    }

    @Override
    public String toString() {
        return "Token{" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", time=" + time +
                ", ipDevice='" + ipDevice + '\'' +
                ", status='" + status + '\'' +
                ", userId=" + userId +
                '}';
    }
}
