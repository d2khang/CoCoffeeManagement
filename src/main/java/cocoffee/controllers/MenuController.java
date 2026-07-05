package cocoffee.controllers;

import cocoffee.models.Category;
import cocoffee.models.Product;
import cocoffee.repositories.CategoryRepository;
import cocoffee.repositories.ProductRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class MenuController {
    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Label messageLabel;
    @FXML private TableView<Product> productTable;

    @FXML private TableColumn<Product, Double> priceColumn;
    // Đã thêm: Khai báo cột trạng thái để phiên dịch
    @FXML private TableColumn<Product, String> statusColumn;

    private ProductRepository productRepository = new ProductRepository();
    private CategoryRepository categoryRepository = new CategoryRepository();

    private Product selectedProduct = null;

    @FXML
    public void initialize() {
        categoryComboBox.setItems(FXCollections.observableArrayList(categoryRepository.getAllCategories()));

        // ĐÃ SỬA: Hiển thị tiếng Việt trên nút sổ xuống
        statusComboBox.setItems(FXCollections.observableArrayList("Đang bán", "Hết hàng", "Ẩn"));

        loadProductTable();

        // 1. Định dạng cột Giá tiền
        priceColumn.setCellFactory(column -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f Đ", price));
                }
            }
        });

        // 2. ĐÃ THÊM: Phiên dịch cột Trạng thái sang Tiếng Việt trên Bảng
        statusColumn.setCellFactory(column -> new TableCell<Product, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                } else {
                    setText(toViStatus(status)); // Gọi hàm dịch sang tiếng Việt
                }
            }
        });

        // 3. Khi click vào 1 dòng trên bảng
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedProduct = newSelection;

                priceField.setText(String.format("%.0f", selectedProduct.getPrice()));
                nameField.setText(selectedProduct.getName());

                // Dịch từ DB (Anh) sang tiếng Việt để hiển thị lên ComboBox
                statusComboBox.setValue(toViStatus(selectedProduct.getStatus()));

                for (Category cat : categoryComboBox.getItems()) {
                    if (cat.getId() == selectedProduct.getCategoryId()) {
                        categoryComboBox.setValue(cat);
                        break;
                    }
                }
            }
        });
    }

    private void loadProductTable() {
        List<Product> products = productRepository.getAllProducts();
        productTable.setItems(FXCollections.observableArrayList(products));
    }

    private void clearFields() {
        nameField.clear();
        priceField.clear();
        categoryComboBox.getSelectionModel().clearSelection();
        statusComboBox.getSelectionModel().clearSelection();
        selectedProduct = null;
    }

    @FXML
    protected void handleAddProduct() {
        try {
            if (nameField.getText().isEmpty() || priceField.getText().isEmpty() ||
                    categoryComboBox.getValue() == null || statusComboBox.getValue() == null) {
                messageLabel.setText("Vui lòng nhập đầy đủ thông tin!");
                messageLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            Product newProduct = new Product();
            newProduct.setName(nameField.getText());
            newProduct.setPrice(Double.parseDouble(priceField.getText()));
            newProduct.setCategoryId(categoryComboBox.getValue().getId());

            // Dịch từ Tiếng Việt (ComboBox) sang Tiếng Anh để lưu vào DB
            newProduct.setStatus(toDbStatus(statusComboBox.getValue()));

            if (productRepository.addProduct(newProduct)) {
                messageLabel.setText("Thêm món thành công!");
                messageLabel.setStyle("-fx-text-fill: green;");
                loadProductTable();
                clearFields();
            } else {
                messageLabel.setText("Lỗi: Tên món có thể đã tồn tại!");
                messageLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("Lỗi: Giá tiền phải là một con số!");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    protected void handleUpdateProduct() {
        if (selectedProduct == null) {
            messageLabel.setText("Vui lòng click chọn một món trong bảng để sửa!");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            selectedProduct.setName(nameField.getText());
            selectedProduct.setPrice(Double.parseDouble(priceField.getText()));
            selectedProduct.setCategoryId(categoryComboBox.getValue().getId());

            // Dịch từ Tiếng Việt sang Tiếng Anh để cập nhật DB
            selectedProduct.setStatus(toDbStatus(statusComboBox.getValue()));

            if (productRepository.updateProduct(selectedProduct)) {
                messageLabel.setText("Cập nhật thành công!");
                messageLabel.setStyle("-fx-text-fill: green;");
                loadProductTable();
                clearFields();
            }
        } catch (Exception e) {
            messageLabel.setText("Lỗi kiểm tra lại dữ liệu nhập!");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    protected void handleDeleteProduct() {
        if (selectedProduct == null) {
            messageLabel.setText("Vui lòng click chọn một món trong bảng để xóa!");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (productRepository.deleteProduct(selectedProduct.getId())) {
            messageLabel.setText("Đã xóa món: " + selectedProduct.getName());
            messageLabel.setStyle("-fx-text-fill: green;");
            loadProductTable();
            clearFields();
        } else {
            messageLabel.setText("Không thể xóa món này!");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    // =========================================================
    // CÁC HÀM TIỆN ÍCH DỊCH THUẬT (ANH <-> VIỆT)
    // =========================================================

    // Dịch từ giao diện (Việt) xuống Database (Anh)
    private String toDbStatus(String viStatus) {
        if ("Hết hàng".equals(viStatus)) return "OUT_OF_STOCK";
        if ("Ẩn".equals(viStatus)) return "HIDDEN";
        return "AVAILABLE"; // Mặc định là Đang bán
    }

    // Dịch từ Database (Anh) lên giao diện (Việt)
    private String toViStatus(String dbStatus) {
        if ("OUT_OF_STOCK".equals(dbStatus)) return "Hết hàng";
        if ("HIDDEN".equals(dbStatus)) return "Ẩn";
        return "Đang bán"; // Mặc định là AVAILABLE
    }
    // ---> HÀM QUAY LẠI MÀN HÌNH CHÍNH <---
    @FXML
    protected void handleBackToMain() {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/main-view.fxml"));
            javafx.scene.Scene mainScene = new javafx.scene.Scene(fxmlLoader.load(), 800, 600);

            // Lấy khung cửa sổ hiện tại (dùng messageLabel làm mỏ neo để tìm)
            javafx.stage.Stage currentStage = (javafx.stage.Stage) messageLabel.getScene().getWindow();

            currentStage.setScene(mainScene);
            currentStage.setTitle("CỎ Coffee & Tea - Trang Chủ");
            currentStage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Lỗi: Không thể quay lại màn hình chính!");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }
}