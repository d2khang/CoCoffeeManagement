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
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PosController {
    // --- KHU VỰC 1: MENU ---
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private TextField productSearchField;
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
    @FXML private ComboBox<String> tableSelectionComboBox; // Ô CHỌN BÀN
    @FXML private TextField invoiceSearchField;             // Ô TÌM HÓA ĐƠN

    // Các "Thợ lấy dữ liệu"
    private ProductRepository productRepository = new ProductRepository();
    private CategoryRepository categoryRepository = new CategoryRepository();
    private InvoiceRepository invoiceRepository = new InvoiceRepository();

    // Các danh sách dữ liệu động
    private ObservableList<Product> allProducts;
    private ObservableList<OrderDetail> cartItems = FXCollections.observableArrayList();
    private ObservableList<Invoice> openInvoicesList = FXCollections.observableArrayList();
    private FilteredList<Invoice> filteredInvoices; // Danh sách hóa đơn sau khi lọc

    private double currentTotal = 0;
    private Invoice currentInvoice = null;

    @FXML
    public void initialize() {
        productTable.setPlaceholder(new Label("Chưa có món đồ uống nào."));
        cartTable.setPlaceholder(new Label("Chưa có món nào, hãy chọn từ thực đơn bên trái."));

        setupMenu();
        setupCart();
        setupPaymentMethods();
        setupInvoiceList(); // Setup 1 LẦN DUY NHẤT: items, cellFactory, listener chọn, listener tìm kiếm

        // --- NẠP DANH SÁCH 19 BÀN VÀ MANG ĐI ---
        List<String> tables = new ArrayList<>();
        tables.add("Mang đi");
        for (int i = 1; i <= 19; i++) {
            tables.add("Bàn " + i);
        }
        tableSelectionComboBox.setItems(FXCollections.observableArrayList(tables));
        tableSelectionComboBox.setValue("Mang đi"); // Mặc định là mang đi

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

        categoryComboBox.setOnAction(e -> filterProducts());
        productSearchField.textProperty().addListener((observable, oldValue, newValue) -> filterProducts());
    }

    private void filterProducts() {
        Category selectedCat = categoryComboBox.getValue();
        String searchText = productSearchField.getText() == null ? "" : productSearchField.getText().toLowerCase();

        ObservableList<Product> filtered = FXCollections.observableArrayList();
        for (Product p : allProducts) {
            boolean matchCategory = (selectedCat == null || selectedCat.getId() == 0) || (p.getCategoryId() == selectedCat.getId());
            boolean matchName = p.getName().toLowerCase().contains(searchText);

            if (matchCategory && matchName) {
                filtered.add(p);
            }
        }
        productTable.setItems(filtered);
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
        paymentMethodComboBox.setItems(FXCollections.observableArrayList("Tiền mặt", "Chuyển khoản"));
        paymentMethodComboBox.setValue("Tiền mặt");
    }

    // Chạy 1 LẦN DUY NHẤT trong initialize(): gắn FilteredList, cellFactory,
    // listener chọn item, và listener tìm kiếm hóa đơn.
    private void setupInvoiceList() {
        filteredInvoices = new FilteredList<>(openInvoicesList, p -> true);
        openInvoicesListView.setItems(filteredInvoices);

        // HIỂN THỊ: Giờ | Bàn | Mã | Tiền
        openInvoicesListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Invoice item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String timeOnly = (item.getCreatedAt() != null && item.getCreatedAt().length() >= 16)
                            ? item.getCreatedAt().substring(11, 16) : "";
                    String code = item.getInvoiceCode();
                    String shortCode = "#" + code.substring(Math.max(0, code.length() - 4));

                    // Lấy số bàn
                    String tableInfo = (item.getTableNumber() != null && !item.getTableNumber().isEmpty())
                            ? item.getTableNumber() : "Mang đi";

                    Label lbl = new Label(timeOnly + " | " + tableInfo + " | " + shortCode + " | " + String.format("%,.0f Đ", item.getTotal()));
                    lbl.getStyleClass().add("badge-open");
                    setGraphic(lbl);
                    setText(null);
                }
            }
        });

        openInvoicesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadInvoiceDetails(newVal);
            }
        });

        // Lắng nghe gõ phím ở ô tìm kiếm hóa đơn
        invoiceSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredInvoices.setPredicate(invoice -> {
                if (newVal == null || newVal.isEmpty()) return true;

                String lowerCaseFilter = newVal.toLowerCase();
                String tableInfo = (invoice.getTableNumber() != null && !invoice.getTableNumber().isEmpty())
                        ? invoice.getTableNumber() : "Mang đi";

                return invoice.getInvoiceCode().toLowerCase().contains(lowerCaseFilter) ||
                        tableInfo.toLowerCase().contains(lowerCaseFilter);
            });
        });
    }

    // Chỉ NẠP LẠI dữ liệu gốc, không đụng vào items/cellFactory/listener nữa
    // (những cái đó đã setup 1 lần trong setupInvoiceList()).
    private void loadOpenInvoices() {
        openInvoicesList.setAll(invoiceRepository.getOpenInvoices());
    }

    // TẠO HÓA ĐƠN MỚI DỰA TRÊN BÀN ĐÃ CHỌN
    @FXML
    protected void handleCreateNewInvoice() {
        String selectedTable = tableSelectionComboBox.getValue();
        if (selectedTable == null) selectedTable = "Mang đi";

        Invoice newInv = invoiceRepository.createInvoice(1, selectedTable);

        if (newInv != null) {
            invoiceSearchField.clear(); // Xóa từ khóa tìm kiếm để hóa đơn mới không bị lọc ẩn
            loadOpenInvoices();
            for (Invoice inv : openInvoicesList) {
                if (inv.getId() == newInv.getId()) {
                    openInvoicesListView.getSelectionModel().select(inv);
                    loadInvoiceDetails(inv);
                    break;
                }
            }
            showMessage("Đã tạo hóa đơn cho: " + selectedTable, "blue");

            // Đặt lại mặc định là Mang đi cho khách tiếp theo
            tableSelectionComboBox.setValue("Mang đi");

            // Auto-focus vào ô tìm kiếm món
            productSearchField.requestFocus();
        } else {
            showMessage("Lỗi: Không thể tạo hóa đơn mới!", "red");
        }
    }

    private void loadInvoiceDetails(Invoice invoice) {
        currentInvoice = invoice;
        String shortCode = "#" + invoice.getInvoiceCode().substring(Math.max(0, invoice.getInvoiceCode().length() - 4));
        String tableInfo = (invoice.getTableNumber() != null && !invoice.getTableNumber().isEmpty())
                ? invoice.getTableNumber() : "Mang đi";

        currentInvoiceLabel.setText("HÓA ĐƠN: " + shortCode + " (" + tableInfo + ")");

        List<OrderDetail> details = invoiceRepository.getOrderDetailsByInvoiceId(invoice.getId());
        cartItems.setAll(details);
        calculateTotal();
        showMessage("Đã tải hóa đơn " + shortCode, "green");
    }

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

        productSearchField.requestFocus();
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
            openInvoicesListView.refresh();
        }
    }

    @FXML
    protected void handleSaveInvoice() {
        if (currentInvoice == null) {
            showMessage("Vui lòng chọn hoặc tạo hóa đơn trước khi lưu!", "red");
            return;
        }

        invoiceRepository.saveOrderDetails(currentInvoice.getId(), new ArrayList<>(cartItems));
        showMessage("Đã lưu Hóa đơn vào hàng chờ!", "green");
    }

    @FXML
    protected void handleVoidInvoice() {
        if (currentInvoice == null) {
            showMessage("Vui lòng chọn hóa đơn để hủy!", "red");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác Nhận Hủy");
        alert.setHeaderText("Bạn có chắc chắn muốn HỦY GIAO DỊCH này?");
        alert.setContentText("Hóa đơn này sẽ bị chuyển sang trạng thái ĐÃ HỦY và không thể khôi phục.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (invoiceRepository.voidInvoice(currentInvoice.getId())) {
                showMessage("ĐÃ HỦY GIAO DỊCH THÀNH CÔNG!", "red");
                currentInvoice = null;
                currentInvoiceLabel.setText("CHƯA CHỌN HÓA ĐƠN");
                cartItems.clear();
                calculateTotal();
                loadOpenInvoices();
            } else {
                showMessage("Lỗi: Không thể hủy hóa đơn!", "red");
            }
        }
    }

    @FXML
    protected void handleCheckout() {
        if (currentInvoice == null || cartItems.isEmpty()) {
            showMessage("Hóa đơn trống hoặc chưa chọn hóa đơn!", "red");
            return;
        }

        String displayMethod = paymentMethodComboBox.getValue();
        String dbMethod = displayMethod.equals("Tiền mặt") ? "CASH" : "BANK_TRANSFER";

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác Nhận Thanh Toán");
        alert.setHeaderText("HÓA ĐƠN " + currentInvoice.getInvoiceCode());
        alert.setContentText(String.format("Tổng tiền thu: %,.0f Đ\nPhương thức: %s\n\nBạn có muốn xuất hóa đơn?", currentTotal, displayMethod));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {

            invoiceRepository.saveOrderDetails(currentInvoice.getId(), new ArrayList<>(cartItems));

            if (invoiceRepository.payInvoice(currentInvoice.getId(), dbMethod, currentTotal, 0, currentTotal)) {
                showMessage("THANH TOÁN THÀNH CÔNG!", "green");
                currentInvoice = null;
                currentInvoiceLabel.setText("CHƯA CHỌN HÓA ĐƠN");
                cartItems.clear();
                calculateTotal();
                loadOpenInvoices();
            } else {
                showMessage("Lỗi thanh toán!", "red");
            }
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