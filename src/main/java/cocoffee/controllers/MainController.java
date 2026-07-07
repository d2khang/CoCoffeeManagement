package cocoffee.controllers;

import cocoffee.config.DatabaseConfig;
import cocoffee.models.Employee; // 🌟 NHỚ IMPORT MODEL EMPLOYEE
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.sql.Connection;

public class MainController {

    @FXML
    private Label statusLabel;

    // 🌟 THÊM BIẾN NÀY: Để lưu trữ thông tin người đang đăng nhập
    private Employee currentEmployee;

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

    // 🌟 THÊM HÀM NÀY: Hàm hứng dữ liệu từ LoginController truyền sang
    public void setCurrentEmployee(Employee employee) {
        this.currentEmployee = employee;
        // Đổi dòng trạng thái thành lời chào nhân viên
        statusLabel.setText("Xin chào, " + employee.getFullName() + " (" + employee.getRole() + ")");
        statusLabel.setStyle("-fx-text-fill: #1565C0; -fx-font-weight: bold;");
    }

    // ---> HÀM MỞ MÀN HÌNH BÁN HÀNG (POS) <---
    @FXML
    protected void onPosClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/pos-view.fxml"));
            Scene posScene = new Scene(fxmlLoader.load(), 1024, 700);

            Stage currentStage = (Stage) statusLabel.getScene().getWindow();
            currentStage.setScene(posScene);
            currentStage.setTitle("CỎ Coffee & Tea - BÁN HÀNG POS");
            currentStage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Lỗi: Không thể mở màn hình Bán Hàng!");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    // ---> HÀM XỬ LÝ CHUYỂN SANG MÀN HÌNH MENU <---
    @FXML
    protected void onMenuManagementClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/menu-view.fxml"));
            Scene menuScene = new Scene(fxmlLoader.load(), 900, 600);

            Stage currentStage = (Stage) statusLabel.getScene().getWindow();
            currentStage.setScene(menuScene);
            currentStage.setTitle("CỎ Coffee & Tea - Quản Lý Thực Đơn");
            currentStage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Không thể mở màn hình quản lý thực đơn: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    // ---> HÀM MỞ MÀN HÌNH LỊCH SỬ & THỐNG KÊ (VERSION 0.6) <---
    @FXML
    protected void onHistoryClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/history-view.fxml"));
            Scene historyScene = new Scene(fxmlLoader.load(), 1024, 700);

            Stage currentStage = (Stage) statusLabel.getScene().getWindow();
            currentStage.setScene(historyScene);
            currentStage.setTitle("CỎ Coffee & Tea - Lịch Sử & Doanh Thu");
            currentStage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Lỗi: Không thể mở màn hình Lịch Sử!");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
    // ---> HÀM BẬT POPUP ĐỔI MẬT KHẨU <---
    @FXML
    protected void onChangePasswordClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/change-password-view.fxml"));
            Scene scene = new Scene(loader.load());

            // Lấy Controller của popup và truyền nhân viên hiện tại sang
            cocoffee.controllers.ChangePasswordController popupController = loader.getController();
            popupController.setCurrentEmployee(currentEmployee);

            // Mở một cửa sổ mới (Popup) đè lên trên
            Stage popupStage = new Stage();
            popupStage.setScene(scene);
            popupStage.setTitle("Bảo mật - Đổi mật khẩu");
            popupStage.setResizable(false);
            // Ép người dùng phải thao tác xong popup này mới được quay lại cửa sổ chính
            popupStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            popupStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Lỗi: Không thể mở cửa sổ Đổi Mật Khẩu!");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
}