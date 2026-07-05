package cocoffee.repositories;

import cocoffee.config.DatabaseConfig;
import cocoffee.models.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    // 1. Hàm ĐỌC dữ liệu (Đã có từ trước)
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM Product";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                products.add(new Product(
                        rs.getInt("id"), rs.getString("name"),
                        rs.getDouble("price"), rs.getInt("category_id"),
                        rs.getString("status"), rs.getString("created_at")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Lỗi lấy danh sách Sản phẩm: " + e.getMessage());
        }
        return products;
    }

    // 2. Hàm THÊM món mới
    public boolean addProduct(Product product) {
        String sql = "INSERT INTO Product (name, price, category_id, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPrice());
            pstmt.setInt(3, product.getCategoryId());
            pstmt.setString(4, product.getStatus());

            return pstmt.executeUpdate() > 0; // Trả về true nếu thêm thành công
        } catch (SQLException e) {
            System.out.println("Lỗi thêm món: " + e.getMessage());
            return false;
        }
    }

    // 3. Hàm SỬA món ăn
    public boolean updateProduct(Product product) {
        String sql = "UPDATE Product SET name = ?, price = ?, category_id = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPrice());
            pstmt.setInt(3, product.getCategoryId());
            pstmt.setString(4, product.getStatus());
            pstmt.setInt(5, product.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Lỗi sửa món: " + e.getMessage());
            return false;
        }
    }

    // 4. Hàm XÓA món ăn
    public boolean deleteProduct(int productId) {
        String sql = "DELETE FROM Product WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Lỗi xóa món: " + e.getMessage());
            return false;
        }
    }
}