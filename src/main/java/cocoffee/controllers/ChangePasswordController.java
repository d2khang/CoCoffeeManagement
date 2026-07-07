package cocoffee.controllers;

import cocoffee.models.Employee;
import cocoffee.repositories.EmployeeRepository;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.util.Duration;
import org.mindrot.jbcrypt.BCrypt;

public class ChangePasswordController {

    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button submitButton;
    @FXML private Label messageLabel;

    private final EmployeeRepository employeeRepository = new EmployeeRepository();

    // Nhân viên (chủ quán) đang thực hiện đổi mật khẩu.
    // Bắt buộc phải gọi setCurrentEmployee(...) ngay sau khi load FXML này.
    private Employee currentEmployee;

    // 🌟 Chống dò mật khẩu cũ: đếm số lần nhập sai liên tiếp
    private int wrongAttempts = 0;
    private static final int MAX_ATTEMPTS = 3;
    private static final int LOCK_SECONDS = 30;

    /**
     * Bắt buộc gọi hàm này ngay sau khi FXMLLoader.load() màn hình này,
     * để controller biết đang đổi mật khẩu cho ai.
     *
     * Ví dụ ở nơi mở màn hình:
     *   FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/change-password-view.fxml"));
     *   Parent root = loader.load();
     *   ChangePasswordController controller = loader.getController();
     *   controller.setCurrentEmployee(currentLoggedInEmployee);
     */
    public void setCurrentEmployee(Employee employee) {
        this.currentEmployee = employee;
    }

    @FXML
    protected void handleChangePassword() {
        if (currentEmployee == null) {
            showError("Lỗi hệ thống: không xác định được tài khoản đang đăng nhập.");
            return;
        }

        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // 1. Kiểm tra rỗng
        if (isBlank(oldPassword) || isBlank(newPassword) || isBlank(confirmPassword)) {
            showError("Vui lòng nhập đầy đủ cả 3 ô mật khẩu.");
            return;
        }

        // 2. Xác thực mật khẩu hiện tại — bắt buộc, tránh người khác đổi mật khẩu khi chủ quán quên đăng xuất
        if (!BCrypt.checkpw(oldPassword, currentEmployee.getPasswordHash())) {
            wrongAttempts++;
            if (wrongAttempts >= MAX_ATTEMPTS) {
                lockForm();
                showError("Bạn đã nhập sai mật khẩu hiện tại " + MAX_ATTEMPTS
                        + " lần. Vui lòng thử lại sau " + LOCK_SECONDS + " giây.");
            } else {
                showError("Mật khẩu hiện tại không đúng. (Còn " + (MAX_ATTEMPTS - wrongAttempts) + " lần thử)");
            }
            return;
        }

        // Nhập đúng mật khẩu cũ -> reset lại bộ đếm sai
        wrongAttempts = 0;

        // 3. Độ dài tối thiểu cho mật khẩu mới
        if (newPassword.length() < 6) {
            showError("Mật khẩu mới phải có ít nhất 6 ký tự.");
            return;
        }

        // 4. Không cho trùng mật khẩu cũ
        if (BCrypt.checkpw(newPassword, currentEmployee.getPasswordHash())) {
            showError("Mật khẩu mới không được trùng với mật khẩu hiện tại.");
            return;
        }

        // 5. Nhập lại phải khớp
        if (!newPassword.equals(confirmPassword)) {
            showError("Mật khẩu nhập lại không khớp với mật khẩu mới.");
            return;
        }

        // 6. Hash mật khẩu mới bằng BCrypt trước khi lưu — không bao giờ lưu plain text
        String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());

        try {
            boolean success = employeeRepository.updatePassword(currentEmployee.getId(), newHash);

            if (success) {
                // Cập nhật lại hash trong đối tượng đang giữ ở phiên hiện tại,
                // để các lần đổi mật khẩu tiếp theo trong cùng phiên vẫn đúng mà không cần load lại từ DB
                currentEmployee.setPasswordHash(newHash);

                System.out.println("[LOG] Đổi mật khẩu thành công cho tài khoản: "
                        + currentEmployee.getUsername() + " lúc " + java.time.LocalDateTime.now());

                oldPasswordField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();

                showSuccess("Đổi mật khẩu thành công!");
            } else {
                showError("Không thể cập nhật mật khẩu. Vui lòng thử lại.");
            }

        } catch (Exception e) {
            showError("Đã xảy ra lỗi khi đổi mật khẩu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 🌟 Tạm khóa form khi nhập sai quá số lần cho phép
    private void lockForm() {
        submitButton.setDisable(true);
        oldPasswordField.setDisable(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(LOCK_SECONDS));
        pause.setOnFinished(e -> {
            submitButton.setDisable(false);
            oldPasswordField.setDisable(false);
            wrongAttempts = 0;
            messageLabel.setText("");
        });
        pause.play();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void showError(String message) {
        messageLabel.setStyle("-fx-text-fill: #D32F2F;");
        messageLabel.setText(message);
    }

    private void showSuccess(String message) {
        messageLabel.setStyle("-fx-text-fill: #2E7D32;");
        messageLabel.setText(message);
    }
}