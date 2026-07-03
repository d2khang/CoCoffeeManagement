package cocoffee.models;

public class Employee {
    private int id;
    private String username;
    private String passwordHash;
    private String fullName;
    private String role;
    private String status;
    private String createdAt;
    private String lastLogin;

    // Khởi tạo Constructor rỗng
    public Employee() {
    }

    // Khởi tạo Constructor đầy đủ
    public Employee(int id, String username, String passwordHash, String fullName, String role, String status, String createdAt, String lastLogin) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
    }

    // --- CÁC HÀM GETTER VÀ SETTER ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getLastLogin() { return lastLogin; }
    public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }
}