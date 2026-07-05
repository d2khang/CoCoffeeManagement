package cocoffee.repositories;

import cocoffee.config.DatabaseConfig;
import cocoffee.models.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryRepository {

    // Hàm này sẽ vào SQLite, lấy toàn bộ danh mục và đóng gói thành một Danh sách (List)
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM Category";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            // Vòng lặp while: Cứ đọc được 1 dòng trong DB thì đúc thành 1 đối tượng Category
            while (rs.next()) {
                Category category = new Category(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description")
                );
                // Thêm đối tượng vừa đúc vào danh sách
                categories.add(category);
            }
        } catch (SQLException e) {
            System.out.println("Lỗi khi lấy danh sách Danh mục: " + e.getMessage());
        }

        return categories; // Gửi danh sách này lên cho Giao diện
    }
}