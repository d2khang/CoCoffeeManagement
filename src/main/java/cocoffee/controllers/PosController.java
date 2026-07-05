package cocoffee.controllers;

import cocoffee.models.Category;
import cocoffee.models.Invoice;
import cocoffee.models.OrderDetail;
import cocoffee.models.Product;
import cocoffee.repositories.CategoryRepository;
import cocoffee.repositories.InvoiceRepository;
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
    // --- KHU VỰC 1: MENU ---
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Double> menuPriceColumn;
    @FXML private TextField qtyField;

    // --- KHU VỰC 2: CHI TIẾT HÓA ĐƠN ---
    @FXML private Label currentInvoiceLabel;
    @FXML private TableView<OrderDetail> cartTable;
    @FXML private TableColumn<OrderDetail, Double> cartTotalColumn;
    @FXML private Label totalAmountLabel;
    @FXML private ComboBox<String> paymentMethodComboBox;
    @FXML private Label messageLabel;

    // --- KHU VỰC 3: HÓA ĐƠN CHỜ ---
    @FXML private ListView<Invoice> openInvoicesListView;

    // Các "Thợ lấy dữ liệu"
    private ProductRepository productRepository = new ProductRepository();
    private CategoryRepository categoryRepository = new CategoryRepository();
    private InvoiceRepository invoiceRepository = new InvoiceRepository();

    // Các danh sách dữ liệu động
    private ObservableList<Product> allProducts;
    private ObservableList<OrderDetail> cartItems = FXCollections.observableArrayList();
    private ObservableList<Invoice> openInvoicesList = FXCollections.observableArrayList();

    private double currentTotal = 0;
    private Invoice currentInvoice = null; // Lưu hóa đơn đang được thao tác

    @FXML
    public void initialize() {
        setupMenu();
        setupCart();
        setupPaymentMethods();
        loadOpenInvoices();
    }

    private void setupMenu() {
        List<Category> categories = new ArrayList<>();
        Category allCategory = new Category(0, "Tất cả đồ uống", "");
        categories.add(allCategory);
        categories.addAll(categoryRepository.getAllCategories());
        categoryComboBox.setItems(FXCollections.observableArrayList(categories));
        categoryComboBox.setValue(allCategory);

        List<Product> availableProducts = productRepository.getAllProducts().stream()
                .filter(p -> "AVAILABLE".equals(p.getStatus()))
                .collect(Collectors.toList());
        allProducts = FXCollections.observableArrayList(availableProducts);
        productTable.setItems(allProducts);

        menuPriceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) setText(null);
                else setText(String.format("%,.0f Đ", price));
            }
        });

        categoryComboBox.setOnAction(e -> filterProductsByCategory());
    }

    private void setupCart() {
        cartTotalColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTotal()));
        cartTotalColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) setText(null);
                else setText(String.format("%,.0f Đ", total));
            }
        });
        cartTable.setItems(cartItems);
    }

    private void setupPaymentMethods() {
        paymentMethodComboBox.setItems(FXCollections.observableArrayList("CASH", "BANK_TRANSFER", "CARD"));
        paymentMethodComboBox.setValue("CASH");
    }

    private void loadOpenInvoices() {
        openInvoicesList.setAll(invoiceRepository.getOpenInvoices());
        openInvoicesListView.setItems(openInvoicesList);

        // Cấu hình hiển thị cho ListView (Hiển thị Mã hóa đơn)
        openInvoicesListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Invoice item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getInvoiceCode() + " - " + String.format("%,.0f Đ", item.getTotal()));
                }
            }
        });

        // Bắt sự kiện khi click vào một hóa đơn chờ
        openInvoicesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadInvoiceDetails(newVal);
            }
        });
    }

    private void filterProductsByCategory() {
        Category selectedCat = categoryComboBox.getValue();
        if (selectedCat == null || selectedCat.getId() == 0) {
            productTable.setItems(allProducts);
        } else {
            ObservableList<Product> filtered = FXCollections.observableArrayList();
            for (Product p : allProducts) {
                if (p.getCategoryId() == selectedCat.getId()) filtered.add(p);
            }
            productTable.setItems(filtered);
        }
    }

    // TẠO HÓA ĐƠN MỚI
    @FXML
    protected void handleCreateNewInvoice() {
        Invoice newInv = invoiceRepository.createInvoice(1); // Mặc định employee_id = 1
        if (newInv != null) {
            loadOpenInvoices();

            // Tìm đúng hóa đơn vừa tạo trong danh sách mới load (so sánh theo ID,
            // vì object trả về từ createInvoice() khác instance với object trong openInvoicesList)
            for (Invoice inv : openInvoicesList) {
                if (inv.getId() == newInv.getId()) {
                    openInvoicesListView.getSelectionModel().select(inv);
                    loadInvoiceDetails(inv);
                    break;
                }
            }
            showMessage("Đã tạo hóa đơn mới: " + newInv.getInvoiceCode(), "blue");
        } else {
            showMessage("Lỗi: Không thể tạo hóa đơn mới!", "red");
        }
    }

    // LOAD CHI TIẾT HÓA ĐƠN (nạp lại món đã lưu từ Database)
    private void loadInvoiceDetails(Invoice invoice) {
        currentInvoice = invoice;
        currentInvoiceLabel.setText("HÓA ĐƠN: " + invoice.getInvoiceCode());

        List<OrderDetail> details = invoiceRepository.getOrderDetailsByInvoiceId(invoice.getId());
        cartItems.setAll(details); // Nạp danh sách món từ DB vào giỏ
        calculateTotal();
        showMessage("Đã tải hóa đơn " + invoice.getInvoiceCode(), "green");
    }

    // THÊM MÓN
    @FXML
    protected void handleAddToCart() {
        if (currentInvoice == null) {
            showMessage("Vui lòng CHỌN hoặc TẠO HÓA ĐƠN MỚI trước khi gọi món!", "red");
            return;
        }

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

        boolean found = false;
        for (OrderDetail item : cartItems) {
            if (item.getProductId() == selectedProduct.getId()) {
                item.setQuantity(item.getQuantity() + qty);
                found = true;
                break;
            }
        }

        if (!found) {
            OrderDetail newItem = new OrderDetail(0, currentInvoice.getId(), selectedProduct.getId(), selectedProduct.getName(), qty, selectedProduct.getPrice());
            cartItems.add(newItem);
        }

        cartTable.refresh();
        calculateTotal();
        showMessage("Đã thêm " + qty + " " + selectedProduct.getName(), "blue");
        qtyField.setText("1");
    }

    @FXML
    protected void handleRemoveFromCart() {
        OrderDetail selectedItem = cartTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            cartItems.remove(selectedItem);
            calculateTotal();
        }
    }

    private void calculateTotal() {
        currentTotal = 0;
        for (OrderDetail item : cartItems) {
            currentTotal += item.getTotal();
        }
        totalAmountLabel.setText(String.format("%,.0f Đ", currentTotal));
        if (currentInvoice != null) {
            currentInvoice.setTotal(currentTotal);
            openInvoicesListView.refresh(); // Cập nhật lại giá tiền trên danh sách
        }
    }

    // LƯU TẠM HÓA ĐƠN (ghi giỏ hàng hiện tại xuống Database)
    @FXML
    protected void handleSaveInvoice() {
        if (currentInvoice == null) {
            showMessage("Vui lòng chọn hoặc tạo hóa đơn trước khi lưu!", "red");
            return;
        }

        invoiceRepository.saveOrderDetails(currentInvoice.getId(), new ArrayList<>(cartItems));
        showMessage("Đã lưu tạm Hóa đơn " + currentInvoice.getInvoiceCode(), "green");
    }

    // THANH TOÁN
    @FXML
    protected void handleCheckout() {
        if (currentInvoice == null || cartItems.isEmpty()) {
            showMessage("Hóa đơn trống hoặc chưa chọn hóa đơn!", "red");
            return;
        }

        String paymentMethod = paymentMethodComboBox.getValue();

        // Lưu lại lần cuối trước khi chốt, đảm bảo dữ liệu mới nhất được ghi xuống DB
        invoiceRepository.saveOrderDetails(currentInvoice.getId(), new ArrayList<>(cartItems));

        if (invoiceRepository.payInvoice(currentInvoice.getId(), paymentMethod, currentTotal, 0, currentTotal)) {
            showMessage("THANH TOÁN THÀNH CÔNG: " + currentInvoice.getInvoiceCode(), "green");
            currentInvoice = null;
            currentInvoiceLabel.setText("CHƯA CHỌN HÓA ĐƠN");
            cartItems.clear();
            calculateTotal();
            loadOpenInvoices(); // Làm mới danh sách chờ
        } else {
            showMessage("Lỗi thanh toán!", "red");
        }
    }

    private void showMessage(String msg, String color) {
        messageLabel.setText(msg);
        messageLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-padding: 10;");
    }

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
        }
    }
}