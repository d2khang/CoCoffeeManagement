package cocoffee.models;

public class Order {
    private int id;
    private String employeeUsername; // Lưu tên nhân viên tạo hóa đơn
    private double totalAmount;      // Tổng tiền
    private String status;           // PAID (Đã thanh toán) hoặc CANCELLED (Đã hủy)
    private String createdAt;        // Thời gian tạo

    // Constructor rỗng
    public Order() {
    }

    // Constructor đầy đủ
    public Order(int id, String employeeUsername, double totalAmount, String status, String createdAt) {
        this.id = id;
        this.employeeUsername = employeeUsername;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    // --- CÁC HÀM GETTER VÀ SETTER ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmployeeUsername() { return employeeUsername; }
    public void setEmployeeUsername(String employeeUsername) { this.employeeUsername = employeeUsername; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}