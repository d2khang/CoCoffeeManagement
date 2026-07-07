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
        // ==========================================
        // 1. KHAI BÁO CÁC CÂU LỆNH TẠO BẢNG
        // ==========================================

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

        // 🌟 ĐÃ SỬA: Bảng Invoice - Bổ sung thêm table_number
        String createInvoiceTable = """
            CREATE TABLE IF NOT EXISTS Invoice (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                invoice_code TEXT NOT NULL UNIQUE,
                table_number TEXT NULL, 
                employee_id INTEGER NOT NULL,
                subtotal REAL NOT NULL DEFAULT 0,
                discount REAL NOT NULL DEFAULT 0,
                total REAL NOT NULL DEFAULT 0,
                payment_method TEXT CHECK(payment_method IN ('CASH', 'BANK_TRANSFER', 'CARD')),
                status TEXT NOT NULL DEFAULT 'OPEN' CHECK(status IN ('OPEN', 'PAID', 'CANCELLED')),
                created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                paid_at TEXT,
                FOREIGN KEY (employee_id) REFERENCES Employee(id)
            );
        """;

        // Version 0.5: Chi tiết Hóa đơn
        String createOrderDetailTable = """
            CREATE TABLE IF NOT EXISTS OrderDetail (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                invoice_id INTEGER NOT NULL,
                product_id INTEGER NOT NULL,
                product_name TEXT NOT NULL,
                quantity INTEGER NOT NULL CHECK(quantity > 0),
                price REAL NOT NULL,
                FOREIGN KEY (invoice_id) REFERENCES Invoice(id),
                FOREIGN KEY (product_id) REFERENCES Product(id)
            );
        """;

        String insertDefaultAdmin = """
            INSERT OR IGNORE INTO Employee (id, username, password_hash, full_name, role)
            VALUES (1, 'admin', '$2a$10$iVCGiFV/dlSXuCXBPTuEn.nw8shJOKkVcrBsn1VtLp6WV5nvwdE/a', 'Chủ Quán CỎ', 'ADMIN');
        """;

        // 🌟 ĐÃ SỬA: Danh mục mới cực kỳ chi tiết theo yêu cầu của bạn
        String insertDefaultCategories = """
            INSERT OR IGNORE INTO Category (id, name, description) VALUES
            (1, 'Cà Phê', 'Pha phin, pha máy'),
            (2, 'Trà', 'Trà trái cây, trà hoa'),
            (3, 'Trà sữa', 'Các loại trà sữa truyền thống'),
            (4, 'Sinh tố', 'Sinh tố trái cây tươi'),
            (5, 'Latte', 'Latte nóng và lạnh'),
            (6, 'Topping', 'Trân châu, thạch, pudding'),
            (7, 'Nước ép', 'Ép nguyên chất'),
            (8, 'Yogurt', 'Yogurt trái cây'),
            (9, 'Soda', 'Soda mix'),
            (10, 'Đồ ăn vặt', 'Snacks'),
            (11, 'Bánh tráng trộn', 'Ăn vặt'),
            (12, 'Mì trộn Hàn', 'Mì trộn cay'),
            (13, 'Mì trộn Việt', 'Mì trộn truyền thống'),
            (14, 'Combo cá viên chiên', 'Đồ ăn nhanh');
        """;

        // ==========================================
        // 2. MỞ KẾT NỐI VÀ THỰC THI SQL
        // ==========================================
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA foreign_keys = ON;"); // Bật kiểm tra khóa ngoại

            stmt.execute(createEmployeeTable);
            stmt.execute(createCategoryTable);
            stmt.execute(createProductTable);
            stmt.execute(createInvoiceTable);
            stmt.execute(createOrderDetailTable);

            stmt.execute(insertDefaultAdmin);
            stmt.execute(insertDefaultCategories);

            System.out.println("Kiểm tra và khởi tạo cơ sở dữ liệu thành công!");

        } catch (SQLException e) {
            System.out.println("Lỗi khởi tạo cơ sở dữ liệu: " + e.getMessage());
        }
    }
}