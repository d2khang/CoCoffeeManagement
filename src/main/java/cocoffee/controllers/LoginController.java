package cocoffee.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    @FXML
    protected void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Tạm thời hiển thị thông báo để test kết nối giao diện
        // Chúng ta sẽ viết hàm kiểm tra database ở bước tiếp theo
        if (username.equals("admin") && password.equals("123456")) {
            messageLabel.setText("Đăng nhập thành công!");
            messageLabel.setStyle("-fx-text-fill: green;");
        } else {
            messageLabel.setText("Sai tài khoản hoặc mật khẩu!");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }
}