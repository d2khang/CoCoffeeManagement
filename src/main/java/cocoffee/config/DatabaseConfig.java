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
            VALUES (1, 'admin', '$2a$10$BtxNhiu5leEE9hszi0KYoem.WOD53EvPb8L6j6RKLk3v8Bpo7KAHy', 'Chủ Quán CỎ', 'ADMIN');
        """;

        // 🌟 ĐÃ SỬA: Danh mục chính xác 100% theo yêu cầu mới nhất của bạn
        String insertDefaultCategories = """
            INSERT OR IGNORE INTO Category (id, name, description) VALUES
            (1, 'Cà Phê', 'Pha phin, pha máy truyền thống'),
            (2, 'Trà trái cây', 'Thức uống thanh mát từ trái cây tươi'),
            (3, 'Trà sữa', 'Các dòng trà sữa đậm vị, béo ngậy'),
            (4, 'Sinh tố', 'Sinh tố xay từ hoa quả tươi nguyên chất'),
            (5, 'Latte', 'Cà phê sữa tươi nghệ thuật kiểu Ý'),
            (6, 'Topping', 'Các loại thạch, trân châu, pudding ăn kèm'),
            (7, 'Nước ép', 'Nước ép trái cây tươi giàu vitamin'),
            (8, 'Yogurt', 'Sữa chua uống, sữa chua trái cây dẻo'),
            (9, 'Soda', 'Nước giải khát có ga mix hương vị trái cây'),
            (10, 'Danh mục đồ ăn', 'Các món ăn chính phục vụ tại quán'),
            (11, 'Đồ ăn vặt', 'Món ăn chơi kèm theo cho khách nhâm nhi');
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