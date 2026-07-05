package cocoffee.models;

public class Product {
    private int id;
    private String name;
    private double price;       // Giá tiền là số thập phân (REAL trong SQLite)
    private int categoryId;     // Lưu ID của danh mục mà món này thuộc về
    private String status;
    private String createdAt;

    // Constructor rỗng
    public Product() {
    }

    // Constructor đầy đủ
    public Product(int id, String name, double price, int categoryId, String status, String createdAt) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.categoryId = categoryId;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Các hàm Getter và Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}