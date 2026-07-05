package cocoffee.controllers;

import cocoffee.models.Employee;
import cocoffee.repositories.EmployeeRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private EmployeeRepository employeeRepository = new EmployeeRepository();

    @FXML
    protected void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            messageLabel.setText("Vui lòng nhập đầy đủ tài khoản và mật khẩu!");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        Employee employee = employeeRepository.findByUsername(username);

        if (employee == null) {
            messageLabel.setText("Tài khoản không tồn tại!");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if ("LOCKED".equals(employee.getStatus())) {
            messageLabel.setText("Tài khoản này đã bị khóa!");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (BCrypt.checkpw(password, employee.getPasswordHash())) {
            // ---> ĐÃ ĐĂNG NHẬP THÀNH CÔNG, BẮT ĐẦU CHUYỂN MÀN HÌNH <---
            try {
                // 1. Nạp file thiết kế của màn hình chính
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/main-view.fxml"));
                Scene mainScene = new Scene(fxmlLoader.load(), 800, 600); // Kích thước cửa sổ to hơn để bán hàng

                // 2. Lấy được cái "Khung cửa sổ" (Stage) hiện tại đang chứa màn hình Login
                Stage currentStage = (Stage) usernameField.getScene().getWindow();

                // 3. Tráo đổi ruột (Scene) của cửa sổ sang màn hình chính
                currentStage.setScene(mainScene);
                currentStage.setTitle("CỎ Coffee & Tea - Chào " + employee.getFullName()); // Hiện tên nhân viên lên tiêu đề
                currentStage.centerOnScreen(); // Canh lại cửa sổ ra giữa màn hình

            } catch (Exception e) {
                e.printStackTrace();
                messageLabel.setText("Lỗi tải màn hình chính!");
                messageLabel.setStyle("-fx-text-fill: red;");
            }
        } else {
            messageLabel.setText("Mật khẩu không chính xác!");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }
}