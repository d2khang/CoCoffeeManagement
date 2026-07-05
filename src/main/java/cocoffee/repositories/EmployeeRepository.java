package cocoffee.repositories;

import cocoffee.config.DatabaseConfig;
import cocoffee.models.Employee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmployeeRepository {

    // Hàm này sẽ tìm kiếm nhân viên trong DB dựa trên tên đăng nhập (username)
    public Employee findByUsername(String username) {
        // Câu lệnh SQL với dấu ? để chống lỗi bảo mật SQL Injection
        String sql = "SELECT * FROM Employee WHERE username = ?";
        Employee employee = null;

        // Tự động mở và đóng kết nối Database an toàn bằng try-with-resources
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Thay thế dấu ? bằng username người dùng nhập vào
            pstmt.setString(1, username);

            // Thực thi tìm kiếm
            ResultSet rs = pstmt.executeQuery();

            // Nếu tìm thấy dòng dữ liệu khớp trong bảng SQLite
            if (rs.next()) {
                employee = new Employee(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("full_name"),
                        rs.getString("role"),
                        rs.getString("status"),
                        rs.getString("created_at"),
                        rs.getString("last_login")
                );
            }
        } catch (SQLException e) {
            System.out.println("Lỗi khi tìm tài khoản: " + e.getMessage());
        }

        // Trả về tài khoản tìm được (hoặc null nếu không thấy)
        return employee;
    }
}