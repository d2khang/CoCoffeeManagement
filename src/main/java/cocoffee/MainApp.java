package cocoffee;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        cocoffee.config.DatabaseConfig.initializeDatabase();
        // Nạp file giao diện
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/views/login-view.fxml"));

        // Tạo một Scene (khung cảnh) kích thước 600x400
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);

        // Cấu hình cửa sổ
        stage.setTitle("CỎ Coffee & Tea - Quản lý Bán Hàng");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(); // Lệnh khởi chạy ứng dụng JavaFX
    }
}