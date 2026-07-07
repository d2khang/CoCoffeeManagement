package cocoffee.controllers;

import cocoffee.models.Employee;
import cocoffee.repositories.EmployeeRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.util.prefs.Preferences;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private CheckBox rememberMeCheckBox; // 🌟 Tiêm CheckBox vào

    private EmployeeRepository employeeRepository = new EmployeeRepository();
    private Preferences prefs; // 🌟 Bộ lưu trữ an toàn của Java

    @FXML
    public void initialize() {
        // Khởi tạo vùng nhớ an toàn cho màn hình đăng nhập
        prefs = Preferences.userNodeForPackage(LoginController.class);

        // Đọc tên đăng nhập cũ (nếu có)
        String savedUsername = prefs.get("saved_username", "");
        if (!savedUsername.isEmpty()) {
            usernameField.setText(savedUsername);
            rememberMeCheckBox.setSelected(true);

            // Chuyên nghiệp: Đã nhớ tài khoản rồi thì auto-focus vào ô Mật khẩu
            Platform.runLater(() -> passwordField.requestFocus());
        } else {
            // 🌟 MỚI: Chưa có ai được nhớ -> focus sẵn vào ô Tên đăng nhập để gõ luôn
            Platform.runLater(() -> usernameField.requestFocus());
        }

        // 🌟 MỚI: Xóa thông báo lỗi cũ ngay khi người dùng bắt đầu sửa lại thông tin
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> clearMessage());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> clearMessage());
    }

    @FXML
    protected void handleLogin() {
        // 🌟 ĐÃ SỬA: trim() username để tránh lỗi khoảng trắng thừa khi gõ nhanh
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password == null || password.trim().isEmpty()) {
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

            // 🌟 1. XỬ LÝ NHỚ TÀI KHOẢN
            if (rememberMeCheckBox.isSelected()) {
                prefs.put("saved_username", username); // Ghi đè vào Registry
            } else {
                prefs.remove("saved_username"); // Xóa đi nếu không muốn nhớ
            }

            // 🌟 2. LƯU VẾT THỜI GIAN ĐĂNG NHẬP
            employeeRepository.updateLastLogin(employee.getId());

            // 3. CHUYỂN MÀN HÌNH
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/main-view.fxml"));
                Scene mainScene = new Scene(fxmlLoader.load(), 800, 600);

                // 🌟 MỚI: Truyền nhân viên vừa đăng nhập sang MainController
                // để dùng cho tính năng Đổi mật khẩu và các tính năng cần biết "ai đang đăng nhập" sau này
                MainController mainController = fxmlLoader.getController();
                mainController.setCurrentEmployee(employee);

                Stage currentStage = (Stage) usernameField.getScene().getWindow();
                currentStage.setScene(mainScene);
                currentStage.setTitle("CỎ Coffee & Tea - Chào " + employee.getFullName());
                currentStage.centerOnScreen();

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

    // 🌟 MỚI: Hàm tiện ích xóa thông báo lỗi trên màn hình
    private void clearMessage() {
        if (messageLabel.getText() != null && !messageLabel.getText().isEmpty()) {
            messageLabel.setText("");
        }
    }
}