package cocoffee.controllers;

import cocoffee.models.Category;
import cocoffee.models.Order;
import cocoffee.models.OrderDetail;
import cocoffee.models.Product;
import cocoffee.repositories.CategoryRepository;
import cocoffee.repositories.OrderRepository;
import cocoffee.repositories.ProductRepository;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PosController {
    // --- KHU VỰC BÊN TRÁI: MENU ---
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Double> menuPriceColumn;
    @FXML private TextField qtyField;

    // --- KHU VỰC BÊN PHẢI: GIỎ HÀNG ---
    @FXML private TableView<OrderDetail> cartTable;
    @FXML private TableColumn<OrderDetail, Double> cartTotalColumn;
    @FXML private Label totalAmountLabel;
    @FXML private Label messageLabel;

    // Các "Thợ lấy dữ liệu"
    private ProductRepository productRepository = new ProductRepository();
    private CategoryRepository categoryRepository = new CategoryRepository();
    private OrderRepository orderRepository = new OrderRepository();

    // Các danh sách dữ liệu động
    private ObservableList<Product> allProducts;
    private ObservableList<OrderDetail> cartItems = FXCollections.observableArrayList();
    private double currentTotal = 0;

    @FXML
    public void initialize() {
        // 1. Tải danh mục và thêm tùy chọn "Tất cả đồ uống" lên đầu tiên
        List<Category> categories = new ArrayList<>();
        Category allCategory = new Category(0, "Tất cả đồ uống", "");
        categories.add(allCategory);
        categories.addAll(categoryRepository.getAllCategories());
        categoryComboBox.setItems(FXCollections.observableArrayList(categories));
        categoryComboBox.setValue(allCategory); // Chọn mặc định

        // 2. Chỉ tải các món đang có trạng thái 'AVAILABLE' (Đang bán)
        List<Product> availableProducts = productRepository.getAllProducts().stream()
                .filter(p -> "AVAILABLE".equals(p.getStatus()))
                .collect(Collectors.toList());
        allProducts = FXCollections.observableArrayList(availableProducts);
        productTable.setItems(allProducts);

        // 3. Định dạng cột Giá Tiền bên Menu (Thêm dấu phẩy và chữ Đ)
        menuPriceColumn.setCellFactory(column -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) setText(null);
                else setText(String.format("%,.0f Đ", price));
            }
        });

        // 4. Cấu hình cột Thành Tiền trong Giỏ hàng (Tính = Số lượng x Giá)
        cartTotalColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTotal()));
        cartTotalColumn.setCellFactory(column -> new TableCell<OrderDetail, Double>() {
            @Override
            protected void updateItem(Double total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) setText(null);
                else setText(String.format("%,.0f Đ", total));
            }
        });

        // 5. Gắn danh sách giỏ hàng vào Bảng bên phải
        cartTable.setItems(cartItems);

        // 6. BẮT SỰ KIỆN: Lọc đồ uống mỗi khi chọn danh mục khác
        categoryComboBox.setOnAction(e -> filterProductsByCategory());
    }

    // Hàm lọc món ăn theo danh mục
    private void filterProductsByCategory() {
        Category selectedCat = categoryComboBox.getValue();
        if (selectedCat == null || selectedCat.getId() == 0) {
            productTable.setItems(allProducts); // Hiện tất cả
        } else {
            ObservableList<Product> filtered = FXCollections.observableArrayList();
            for (Product p : allProducts) {
                if (p.getCategoryId() == selectedCat.getId()) {
                    filtered.add(p);
                }
            }
            productTable.setItems(filtered);
        }
    }

    // NÚT: THÊM VÀO HÓA ĐƠN
    @FXML
    protected void handleAddToCart() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showMessage("Vui lòng chọn một món từ thực đơn bên trái!", "red");
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(qtyField.getText());
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showMessage("Số lượng phải là số nguyên dương!", "red");
            return;
        }

        // Kiểm tra xem món này đã có trong giỏ hàng chưa, nếu có thì cộng dồn số lượng
        boolean found = false;
        for (OrderDetail item : cartItems) {
            if (item.getProductId() == selectedProduct.getId()) {
                item.setQuantity(item.getQuantity() + qty);
                found = true;
                break;
            }
        }

        // Nếu chưa có thì tạo mới và ném vào giỏ
        if (!found) {
            OrderDetail newItem = new OrderDetail(0, 0, selectedProduct.getId(), selectedProduct.getName(), qty, selectedProduct.getPrice());
            cartItems.add(newItem);
        }

        cartTable.refresh(); // Làm mới giao diện bảng giỏ hàng
        calculateTotal();    // Tính lại tiền
        showMessage("Đã thêm " + qty + " " + selectedProduct.getName() + " vào hóa đơn.", "blue");
        qtyField.setText("1"); // Reset lại số lượng về 1
    }

    // NÚT: XÓA MÓN
    @FXML
    protected void handleRemoveFromCart() {
        OrderDetail selectedItem = cartTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            cartItems.remove(selectedItem);
            calculateTotal();
            showMessage("Đã xóa món khỏi hóa đơn.", "#D32F2F");
        }
    }

    // NÚT: XÓA SẠCH TOÀN BỘ
    @FXML
    protected void handleClearCart() {
        cartItems.clear();
        calculateTotal();
        showMessage("Đã làm sạch hóa đơn.", "#757575");
    }

    // Hàm tự động tính toán lại Tổng tiền
    private void calculateTotal() {
        currentTotal = 0;
        for (OrderDetail item : cartItems) {
            currentTotal += item.getTotal();
        }
        totalAmountLabel.setText(String.format("%,.0f Đ", currentTotal));
    }

    // NÚT CHỐT: THANH TOÁN
    @FXML
    protected void handleCheckout() {
        if (cartItems.isEmpty()) {
            showMessage("Giỏ hàng trống! Vui lòng chọn món trước khi thanh toán.", "red");
            return;
        }

        // 1. Tạo Hóa đơn tổng
        Order newOrder = new Order();
        newOrder.setEmployeeUsername("admin"); // (Tạm thời gán cứng, các bài sau sẽ lấy tài khoản đang đăng nhập)
        newOrder.setTotalAmount(currentTotal);
        newOrder.setStatus("PAID");

        // 2. Chuyển giỏ hàng sang dạng Danh sách (List) chuẩn
        List<OrderDetail> detailsList = new ArrayList<>(cartItems);

        // 3. Ra lệnh cho Repository lưu vào CSDL (bằng Transaction an toàn)
        if (orderRepository.saveOrder(newOrder, detailsList)) {
            showMessage("THANH TOÁN THÀNH CÔNG! Đã lưu hóa đơn.", "green");
            cartItems.clear();
            calculateTotal();
        } else {
            showMessage("LỖI: Không thể lưu hóa đơn. Vui lòng thử lại!", "red");
        }
    }

    // Tiện ích hiển thị thông báo
    private void showMessage(String msg, String color) {
        messageLabel.setText(msg);
        messageLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-padding: 10;");
    }
    // ---> HÀM QUAY LẠI MÀN HÌNH CHÍNH <---
    @FXML
    protected void handleBackToMain() {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/main-view.fxml"));
            javafx.scene.Scene mainScene = new javafx.scene.Scene(fxmlLoader.load(), 800, 600);

            javafx.stage.Stage currentStage = (javafx.stage.Stage) messageLabel.getScene().getWindow();

            currentStage.setScene(mainScene);
            currentStage.setTitle("CỎ Coffee & Tea - Trang Chủ");
            currentStage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Lỗi: Không thể quay lại màn hình chính!", "red");
        }
    }
}