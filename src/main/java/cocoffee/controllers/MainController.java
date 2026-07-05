package cocoffee.controllers;

import cocoffee.config.DatabaseConfig;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.sql.Connection;

public class MainController {

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            if (conn != null) {
                statusLabel.setText("Cơ sở dữ liệu SQLite: ĐÃ KẾT NỐI THÀNH CÔNG!");
                statusLabel.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
            }
        } catch (Exception e) {
            statusLabel.setText("Lỗi kết nối: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold;");
        }
    }

    @FXML
    protected void onStartButtonClick() {
        statusLabel.setText("Đang chuẩn bị vào màn hình Đăng Nhập...");
    }

    // ---> HÀM XỬ LÝ CHUYỂN SANG MÀN HÌNH MENU <---
    @FXML
    protected void onMenuManagementClick() {
        try {
            // 1. Tải bản thiết kế menu-view.fxml
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/menu-view.fxml"));
            // Tạo cảnh mới với kích thước rộng rãi 900x600 để xem bảng dữ liệu
            Scene menuScene = new Scene(fxmlLoader.load(), 900, 600);

            // 2. Lấy cửa sổ hiện tại
            Stage currentStage = (Stage) statusLabel.getScene().getWindow();

            // 3. Đổi ruột sang màn hình quản lý thực đơn
            currentStage.setScene(menuScene);
            currentStage.setTitle("CỎ Coffee & Tea - Quản Lý Thực Đơn");
            currentStage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Không thể mở màn hình quản lý thực đơn: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
}