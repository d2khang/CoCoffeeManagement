package cocoffee.repositories;

import cocoffee.config.DatabaseConfig;
import cocoffee.models.Invoice;
import cocoffee.models.OrderDetail;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class InvoiceRepository {

    // 1. TẠO HÓA ĐƠN MỚI (Lưu tạm - Trạng thái OPEN)
    // 🌟 ĐÃ NÂNG CẤP: Nhận thêm tham số tableNumber (Số bàn)
    public Invoice createInvoice(int employeeId, String tableNumber) {
        // Sinh mã hóa đơn tự động theo thời gian thực (Ví dụ: INV-20260705-153022)
        String invoiceCode = "INV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

        // 🌟 ĐÃ NÂNG CẤP: Câu lệnh SQL thêm cột table_number
        String sql = "INSERT INTO Invoice (invoice_code, table_number, employee_id, status) VALUES (?, ?, ?, 'OPEN')";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, invoiceCode);
            pstmt.setString(2, tableNumber); // Lưu số bàn vào Database
            pstmt.setInt(3, employeeId);
            pstmt.executeUpdate();

            // Lấy ID của hóa đơn vừa được tạo trong database
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int id = rs.getInt(1);
                // 🌟 ĐÃ NÂNG CẤP: Truyền tableNumber vào constructor của Invoice
                return new Invoice(id, invoiceCode, tableNumber, employeeId, 0, 0, 0, null, "OPEN", null, null);
            }
        } catch (SQLException e) {
            System.out.println("Lỗi tạo hóa đơn mới: " + e.getMessage());
        }
        return null;
    }

    // 2. LẤY DANH SÁCH HÓA ĐƠN ĐANG CHỜ THANH TOÁN
    public List<Invoice> getOpenInvoices() {
        List<Invoice> openInvoices = new ArrayList<>();
        String sql = "SELECT * FROM Invoice WHERE status = 'OPEN' ORDER BY created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Invoice invoice = new Invoice(
                        rs.getInt("id"),
                        rs.getString("invoice_code"),
                        rs.getString("table_number"), // 🌟 ĐÃ NÂNG CẤP: Lấy số bàn từ DB lên
                        rs.getInt("employee_id"),
                        rs.getDouble("subtotal"),
                        rs.getDouble("discount"),
                        rs.getDouble("total"),
                        rs.getString("payment_method"),
                        rs.getString("status"),
                        rs.getString("created_at"),
                        rs.getString("paid_at")
                );
                openInvoices.add(invoice);
            }
        } catch (SQLException e) {
            System.out.println("Lỗi lấy danh sách hóa đơn đang mở: " + e.getMessage());
        }
        return openInvoices;
    }

    // 3. THANH TOÁN HÓA ĐƠN (Phiên bản Bảo mật - Có khóa chốt)
    public boolean payInvoice(int invoiceId, String paymentMethod, double subtotal, double discount, double total) {
        // CHỐT CHẶN: Chỉ update những hóa đơn đang ở trạng thái 'OPEN'
        String sql = "UPDATE Invoice SET status = 'PAID', payment_method = ?, subtotal = ?, discount = ?, total = ?, paid_at = CURRENT_TIMESTAMP WHERE id = ? AND status = 'OPEN'";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, paymentMethod);
            pstmt.setDouble(2, subtotal);
            pstmt.setDouble(3, discount);
            pstmt.setDouble(4, total);
            pstmt.setInt(5, invoiceId);

            int rowsAffected = pstmt.executeUpdate();

            // Nếu rowsAffected = 0 nghĩa là hóa đơn không tồn tại hoặc đã bị thanh toán/hủy trước đó rồi
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Lỗi thanh toán hóa đơn: " + e.getMessage());
            return false;
        }
    }

    // 4. LƯU DANH SÁCH MÓN CỦA HÓA ĐƠN
    public void saveOrderDetails(int invoiceId, List<OrderDetail> details) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Xóa chi tiết cũ trước khi lưu mới (để cập nhật lại)
            conn.createStatement().execute("DELETE FROM OrderDetail WHERE invoice_id = " + invoiceId);

            String sql = "INSERT INTO OrderDetail (invoice_id, product_id, product_name, quantity, price) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (OrderDetail item : details) {
                    pstmt.setInt(1, invoiceId);
                    pstmt.setInt(2, item.getProductId());
                    pstmt.setString(3, item.getProductName());
                    pstmt.setInt(4, item.getQuantity());
                    pstmt.setDouble(5, item.getPrice());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 5. LẤY CHI TIẾT MÓN LÊN KHI CLICK VÀO HÓA ĐƠN CHỜ
    public List<OrderDetail> getOrderDetailsByInvoiceId(int invoiceId) {
        List<OrderDetail> list = new ArrayList<>();
        String sql = "SELECT * FROM OrderDetail WHERE invoice_id = " + invoiceId;
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new OrderDetail(rs.getInt("id"), rs.getInt("invoice_id"),
                        rs.getInt("product_id"), rs.getString("product_name"),
                        rs.getInt("quantity"), rs.getDouble("price")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 6. HỦY GIAO DỊCH (Void Invoice)
    public boolean voidInvoice(int invoiceId) {
        // Chỉ cho phép hủy những hóa đơn đang chờ thanh toán (OPEN)
        String sql = "UPDATE Invoice SET status = 'CANCELLED' WHERE id = ? AND status = 'OPEN'";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, invoiceId);
            int rowsAffected = pstmt.executeUpdate();

            // Trả về true nếu hủy thành công (tức là hóa đơn đó tồn tại và đang OPEN)
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Lỗi hủy hóa đơn: " + e.getMessage());
            return false;
        }
    }
}