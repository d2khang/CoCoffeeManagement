package cocoffee.repositories;

import cocoffee.config.DatabaseConfig;
import cocoffee.models.Employee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmployeeRepository {

    public Employee findByUsername(String username) {
        String sql = "SELECT * FROM Employee WHERE username = ?";
        Employee employee = null;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

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

        return employee;
    }

    // 🌟 TÍNH NĂNG: Cập nhật thời gian đăng nhập cuối cùng
    public void updateLastLogin(int employeeId) {
        String sql = "UPDATE Employee SET last_login = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Lỗi cập nhật last_login: " + e.getMessage());
        }
    }

    // 🌟 MỚI: Cập nhật mật khẩu (đã hash bằng BCrypt trước khi truyền vào đây)
    public boolean updatePassword(int employeeId, String newPasswordHash) {
        String sql = "UPDATE Employee SET password_hash = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newPasswordHash);
            pstmt.setInt(2, employeeId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Lỗi cập nhật mật khẩu: " + e.getMessage());
            return false;
        }
    }
}