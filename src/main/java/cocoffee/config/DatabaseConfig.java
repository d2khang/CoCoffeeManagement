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
        // 1. Lệnh tạo bảng Nhân viên (Đã có)
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

        // 2. Lệnh tạo bảng Danh mục (Mới)
        String createCategoryTable = """
            CREATE TABLE IF NOT EXISTS Category (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE,
                description TEXT
            );
        """;

        // 3. Lệnh tạo bảng Sản phẩm (Mới)
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

        String insertDefaultAdmin = """
            INSERT OR IGNORE INTO Employee (id, username, password_hash, full_name, role)
            VALUES (1, 'admin', '$2a$12$LQv3c1yqSN.R6E4.D3gC.OuJvM.hQ2y7M3U1x6X8Z4D2gC.O4z/q', 'Chủ Quán CỎ', 'ADMIN');
        """;

        // Chèn sẵn 3 danh mục mặc định để test
        String insertDefaultCategories = """
            INSERT OR IGNORE INTO Category (id, name, description) VALUES
            (1, 'Cà Phê', 'Các loại cà phê pha phin, pha máy'),
            (2, 'Trà Trái Cây', 'Trà kết hợp trái cây tươi'),
            (3, 'Đá Xay', 'Thức uống đá xay giải nhiệt');
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Bật tính năng kiểm tra khóa ngoại (Foreign Key) của SQLite
            stmt.execute("PRAGMA foreign_keys = ON;");

            // Thực thi tạo các bảng
            stmt.execute(createEmployeeTable);
            stmt.execute(createCategoryTable);
            stmt.execute(createProductTable);

            // Chèn dữ liệu mẫu
            stmt.execute(insertDefaultAdmin);
            stmt.execute(insertDefaultCategories);

            System.out.println("Kiểm tra và khởi tạo cơ sở dữ liệu thành công!");

        } catch (SQLException e) {
            System.out.println("Lỗi khởi tạo cơ sở dữ liệu: " + e.getMessage());
        }
    }
}