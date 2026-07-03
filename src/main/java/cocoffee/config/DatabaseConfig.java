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

        String insertDefaultAdmin = """
            INSERT OR IGNORE INTO Employee (id, username, password_hash, full_name, role)
            VALUES (1, 'admin', '$2a$12$LQv3c1yqSN.R6E4.D3gC.OuJvM.hQ2y7M3U1x6X8Z4D2gC.O4z/q', 'Chủ Quán CỎ', 'ADMIN');
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createEmployeeTable);
            stmt.execute(insertDefaultAdmin);

            System.out.println("Kiểm tra và khởi tạo cơ sở dữ liệu thành công!");

        } catch (SQLException e) {
            System.out.println("Lỗi khởi tạo cơ sở dữ liệu: " + e.getMessage());
        }
    }
}