package cocoffee.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {
    private static final String URL = "jdbc:sqlite:co_coffee.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        // 1. Các bảng cũ (Nhân viên, Danh mục, Sản phẩm)
        String createEmployeeTable = """
            CREATE TABLE IF NOT EXISTS Employee (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL,
                full_name TEXT NOT NULL,
                role TEXT NOT NULL DEFAULT 'ADMIN' CHECK(role IN ('ADMIN','STAFF')),
                status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE','LOCKED')),
                created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                last_login TEXT NULL
            );
        """;

        String createCategoryTable = """
            CREATE TABLE IF NOT EXISTS Category (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE,
                description TEXT
            );
        """;

        String createProductTable = """
            CREATE TABLE IF NOT EXISTS Product (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE,
                price REAL NOT NULL CHECK(price >= 0),
                category_id INTEGER,
                status TEXT NOT NULL DEFAULT 'AVAILABLE' CHECK(status IN ('AVAILABLE','OUT_OF_STOCK','HIDDEN')),
                created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (category_id) REFERENCES Category(id)
            );
        """;

        // ======================= PHẦN MỚI THÊM VÀO =======================
        // 2. Bảng Hóa đơn tổng
        String createOrderTable = """
            CREATE TABLE IF NOT EXISTS Orders (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                employee_username TEXT NOT NULL,
                total_amount REAL NOT NULL DEFAULT 0,
                status TEXT NOT NULL DEFAULT 'PAID' CHECK(status IN ('PAID','CANCELLED')),
                created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            );
        """;

        // 3. Bảng Chi tiết hóa đơn (Các món trong 1 hóa đơn)
        String createOrderDetailTable = """
            CREATE TABLE IF NOT EXISTS OrderDetail (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                order_id INTEGER NOT NULL,
                product_id INTEGER NOT NULL,
                product_name TEXT NOT NULL,
                quantity INTEGER NOT NULL CHECK(quantity > 0),
                price REAL NOT NULL,
                FOREIGN KEY (order_id) REFERENCES Orders(id),
                FOREIGN KEY (product_id) REFERENCES Product(id)
            );
        """;
        // =================================================================

        String insertDefaultAdmin = """
            INSERT OR IGNORE INTO Employee (id, username, password_hash, full_name, role)
            VALUES (1, 'admin', '$2a$12$LQv3c1yqSN.R6E4.D3gC.OuJvM.hQ2y7M3U1x6X8Z4D2gC.O4z/q', 'Chủ Quán CỎ', 'ADMIN');
        """;

        String insertDefaultCategories = """
            INSERT OR IGNORE INTO Category (id, name, description) VALUES
            (1, 'Cà Phê', 'Các loại cà phê pha phin, pha máy'),
            (2, 'Trà Trái Cây', 'Trà kết hợp trái cây tươi'),
            (3, 'Đá Xay', 'Thức uống đá xay giải nhiệt');
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA foreign_keys = ON;"); // Bật kiểm tra khóa ngoại

            // Chạy lệnh tạo bảng
            stmt.execute(createEmployeeTable);
            stmt.execute(createCategoryTable);
            stmt.execute(createProductTable);
            stmt.execute(createOrderTable);         // Tạo bảng Orders
            stmt.execute(createOrderDetailTable);   // Tạo bảng OrderDetail

            // Chèn dữ liệu mẫu
            stmt.execute(insertDefaultAdmin);
            stmt.execute(insertDefaultCategories);

            System.out.println("Kiểm tra và khởi tạo cơ sở dữ liệu thành công!");

        } catch (SQLException e) {
            System.out.println("Lỗi khởi tạo cơ sở dữ liệu: " + e.getMessage());
        }
    }
}