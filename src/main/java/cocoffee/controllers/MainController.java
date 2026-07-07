package cocoffee.controllers;

import cocoffee.models.Employee;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainController {

    @FXML private Label statusLabel;
    @FXML private StackPane contentArea; // Khung chứa nội dung ở giữa

    // 🌟 MỚI: 3 thanh chỉ báo (indicator) màu xanh bên trái mỗi mục sidebar
    @FXML private Region indicatorPos;
    @FXML private Region indicatorMenu;
    @FXML private Region indicatorHistory;

    private Employee currentEmployee;

    public void setCurrentEmployee(Employee employee) {
        this.currentEmployee = employee;
        statusLabel.setText("Xin chào, " + employee.getFullName());
    }

    // --- CÁC HÀM ĐIỀU HƯỚNG SIDEBAR ---
    @FXML
    protected void loadPosView() {
        loadViewIntoContentArea("/views/pos-view.fxml");
        setActiveIndicator(indicatorPos);
    }

    @FXML
    protected void loadMenuView() {
        loadViewIntoContentArea("/views/menu-view.fxml");
        setActiveIndicator(indicatorMenu);
    }

    @FXML
    protected void loadHistoryView() {
        loadViewIntoContentArea("/views/history-view.fxml");
        setActiveIndicator(indicatorHistory);
    }

    // 🌟 MỚI: Bật màu cho thanh chỉ báo của mục đang được chọn, tắt màu 2 mục còn lại
    private void setActiveIndicator(Region active) {
        indicatorPos.getStyleClass().remove("active");
        indicatorMenu.getStyleClass().remove("active");
        indicatorHistory.getStyleClass().remove("active");

        if (!active.getStyleClass().contains("active")) {
            active.getStyleClass().add("active");
        }
    }

    // Hàm lõi: Tải FXML và nhét vào giữa màn hình
    private void loadViewIntoContentArea(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();

            // Xóa nội dung cũ ở giữa, thay bằng màn hình mới
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi load màn hình: " + fxmlPath);
        }
    }

    // --- BẢO MẬT & ĐĂNG XUẤT ---
    @FXML
    protected void onChangePasswordClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/change-password-view.fxml"));
            Scene scene = new Scene(loader.load());

            ChangePasswordController popupController = loader.getController();
            popupController.setCurrentEmployee(currentEmployee);

            Stage popupStage = new Stage();
            popupStage.setScene(scene);
            popupStage.setTitle("Đổi mật khẩu");
            popupStage.setResizable(false);
            popupStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            popupStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login-view.fxml"));
            Scene loginScene = new Scene(loader.load());

            Stage currentStage = (Stage) statusLabel.getScene().getWindow();
            currentStage.setScene(loginScene);
            currentStage.setTitle("Đăng nhập - CỎ Coffee & Tea");
            currentStage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
