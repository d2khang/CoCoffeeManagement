package cocoffee.models;

public class OrderDetail {
    private int id;
    private int invoiceId;      // Đã đổi từ orderId sang invoiceId
    private int productId;      // Món gì?
    private String productName; // Tên món (Lưu lại đề phòng sau này đổi tên)
    private int quantity;       // Số lượng ly
    private double price;       // Giá tiền của 1 ly ngay tại lúc bán

    // Constructor rỗng
    public OrderDetail() {
    }

    // Constructor đầy đủ (Đã cập nhật invoiceId)
    public OrderDetail(int id, int invoiceId, int productId, String productName, int quantity, double price) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    // --- TIỆN ÍCH TÍNH TOÁN ---
    // Hàm này giúp tính tổng tiền của 1 món (Ví dụ: 2 ly Cà phê x 25k = 50k)
    public double getTotal() {
        return this.quantity * this.price;
    }

    // --- CÁC HÀM GETTER VÀ SETTER ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    // Đã cập nhật Getter cho invoiceId
    public int getInvoiceId() { return invoiceId; }
    public void setInvoiceId(int invoiceId) { this.invoiceId = invoiceId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}