package cocoffee.repositories;

import cocoffee.config.DatabaseConfig;
import cocoffee.models.Order;
import cocoffee.models.OrderDetail;

import java.sql.*;
import java.util.List;

public class OrderRepository {

    // Hàm này lưu cả Hóa Đơn và Chi Tiết Hóa Đơn cùng 1 lúc (Transaction)
    public boolean saveOrder(Order order, List<OrderDetail> details) {
        String insertOrderSql = "INSERT INTO Orders (employee_username, total_amount, status) VALUES (?, ?, ?)";
        String insertDetailSql = "INSERT INTO OrderDetail (order_id, product_id, product_name, quantity, price) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection()) {

            // 1. TẮT CHẾ ĐỘ LƯU TỰ ĐỘNG: Bắt đầu một Giao dịch (Transaction) an toàn
            conn.setAutoCommit(false);

            try (PreparedStatement orderStmt = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement detailStmt = conn.prepareStatement(insertDetailSql)) {

                // 2. Lưu thông tin Hóa đơn tổng
                orderStmt.setString(1, order.getEmployeeUsername());
                orderStmt.setDouble(2, order.getTotalAmount());
                orderStmt.setString(3, order.getStatus());
                orderStmt.executeUpdate();

                // 3. Lấy ra Mã Hóa Đơn (ID) vừa được SQLite tự động tạo ra
                ResultSet rs = orderStmt.getGeneratedKeys();
                int newOrderId = -1;
                if (rs.next()) {
                    newOrderId = rs.getInt(1);
                }

                // 4. Lưu từng ly nước vào bảng Chi Tiết Hóa Đơn
                for (OrderDetail item : details) {
                    detailStmt.setInt(1, newOrderId); // Gắn mã hóa đơn vào ly nước
                    detailStmt.setInt(2, item.getProductId());
                    detailStmt.setString(3, item.getProductName());
                    detailStmt.setInt(4, item.getQuantity());
                    detailStmt.setDouble(5, item.getPrice());

                    detailStmt.addBatch(); // Gom lại thành 1 mẻ (Batch) để lưu cho nhanh
                }
                detailStmt.executeBatch(); // Ra lệnh lưu cả mẻ xuống Database

                // 5. NẾU MỌI THỨ SUÔN SẺ -> XÁC NHẬN LƯU VĨNH VIỄN (COMMIT)
                conn.commit();
                return true;

            } catch (SQLException e) {
                // 6. NẾU CÓ LỖI (Ví dụ: đứt mạng, sai kiểu dữ liệu) -> HỦY BỎ TOÀN BỘ (ROLLBACK)
                conn.rollback();
                System.out.println("Lỗi khi lưu hóa đơn (Đã Rollback): " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Lỗi kết nối CSDL: " + e.getMessage());
            return false;
        }
    }
}