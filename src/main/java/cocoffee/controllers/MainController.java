package cocoffee.controllers;

import cocoffee.config.DatabaseConfig;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.sql.Connection;

public class MainController {

    // Khớp với fx:id="statusLabel" ở file FXML để điều khiển dòng chữ này
    @FXML
    private Label statusLabel;

    // Hàm này tự động chạy ngay khi cửa sổ vừa mở lên
    @FXML
    public void initialize() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            if (conn != null) {
                statusLabel.setText("Cơ sở dữ liệu SQLite: ĐÃ KẾT NỐI THÀNH CÔNG!");
                statusLabel.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
            }
        } catch (Exception e) {
            statusLabel.setText("Lỗi kết nối: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold;"); // Màu đỏ báo lỗi
        }
    }

    // Khớp với onAction="#onStartButtonClick" ở nút bấm trong FXML
    @FXML
    protected void onStartButtonClick() {
        statusLabel.setText("Đang chuẩn bị vào màn hình Đăng Nhập...");
        // (Phiên bản 0.2 chúng ta sẽ viết code chuyển màn hình ở đây)
    }
}