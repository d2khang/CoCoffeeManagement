package cocoffee.controllers;

import cocoffee.models.Invoice;
import cocoffee.models.OrderDetail;
import cocoffee.repositories.InvoiceRepository;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HistoryController {

    // --- KHU VỰC THỐNG KÊ (DASHBOARD) ---
    @FXML private Label totalRevenueLabel;
    @FXML private Label successCountLabel;
    @FXML private Label cancelCountLabel;

    // --- KHU VỰC BỘ LỌC ---
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private DatePicker datePicker;

    // --- BẢNG DANH SÁCH HÓA ĐƠN ---
    @FXML private TableView<Invoice> invoiceTable;
    @FXML private TableColumn<Invoice, Double> colTotal;
    @FXML private TableColumn<Invoice, String> colStatus;
    @FXML private TableColumn<Invoice, String> colPaymentMethod;

    // --- BẢNG CHI TIẾT HÓA ĐƠN ---
    @FXML private Label detailsTitleLabel;
    @FXML private Label invoiceTimeLabel;
    @FXML private TableView<OrderDetail> detailTable;
    @FXML private TableColumn<OrderDetail, Double> colDetailTotal;

    private InvoiceRepository invoiceRepository = new InvoiceRepository();
    private ObservableList<Invoice> masterInvoiceList = FXCollections.observableArrayList();
    private FilteredList<Invoice> filteredInvoices;

    @FXML
    public void initialize() {
        setupTables();
        setupFilters();
        loadData();
    }

    private void setupTables() {
        // Format cột Tổng tiền Bảng Hóa đơn
        colTotal.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) setText(null);
                else setText(String.format("%,.0f Đ", total));
            }
        });

        // 🌟 ĐÃ THÊM: Format cột Phương thức thanh toán thành Tiếng Việt
        colPaymentMethod.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String method, boolean empty) {
                super.updateItem(method, empty);
                if (empty || method == null) {
                    setText(null);
                } else {
                    switch (method) {
                        case "CASH":
                            setText("Tiền mặt");
                            break;
                        case "BANK_TRANSFER":
                            setText("Chuyển khoản");
                            break;
                        case "CARD":
                            setText("Quẹt thẻ");
                            break;
                        default:
                            setText(method); // Phòng trường hợp có mã khác
                    }
                }
            }
        });

        // Format cột Trạng thái thành Tiếng Việt
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    switch (status) {
                        case "PAID":
                            setText("🟢 Đã thanh toán");
                            setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                            break;
                        case "CANCELLED":
                            setText("🔴 Đã hủy");
                            setStyle("-fx-text-fill: #C62828; -fx-font-weight: bold;");
                            break;
                        case "OPEN":
                            setText("🟡 Chờ thanh toán");
                            setStyle("-fx-text-fill: #F57F17; -fx-font-weight: bold;");
                            break;
                        default:
                            setText(status);
                    }
                }
            }
        });

        // Lắng nghe sự kiện Click vào 1 hóa đơn để xem chi tiết
        invoiceTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadInvoiceDetails(newVal);
            }
        });

        // Format cột Thành tiền bảng Chi tiết
        colDetailTotal.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTotal()));
        colDetailTotal.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) setText(null);
                else setText(String.format("%,.0f Đ", total));
            }
        });
    }

    private void setupFilters() {
        // Nạp tùy chọn Trạng thái
        statusFilterComboBox.setItems(FXCollections.observableArrayList(
                "Tất cả trạng thái", "Đã thanh toán (PAID)", "Đã hủy (CANCELLED)", "Chờ thanh toán (OPEN)"
        ));
        statusFilterComboBox.setValue("Tất cả trạng thái");

        filteredInvoices = new FilteredList<>(masterInvoiceList, p -> true);
        invoiceTable.setItems(filteredInvoices);

        // Lắng nghe thay đổi từ Bộ lọc
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        statusFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Cập nhật thống kê mỗi khi danh sách lọc thay đổi
        filteredInvoices.predicateProperty().addListener((obs, oldVal, newVal) -> updateDashboard());
    }

    private void loadData() {
        masterInvoiceList.setAll(invoiceRepository.getAllInvoices());
        applyFilters(); // Áp dụng bộ lọc ngay từ đầu
    }

    // --- LOGIC LỌC TỔNG HỢP ---
    private void applyFilters() {
        String searchText = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        String statusFilter = statusFilterComboBox.getValue();
        LocalDate selectedDate = datePicker.getValue();

        filteredInvoices.setPredicate(invoice -> {
            // 1. Lọc theo chữ (Mã hoặc Bàn)
            boolean matchText = invoice.getInvoiceCode().toLowerCase().contains(searchText) ||
                    (invoice.getTableNumber() != null && invoice.getTableNumber().toLowerCase().contains(searchText));

            // 2. Lọc theo trạng thái
            boolean matchStatus = true;
            if (statusFilter.contains("PAID")) matchStatus = invoice.getStatus().equals("PAID");
            else if (statusFilter.contains("CANCELLED")) matchStatus = invoice.getStatus().equals("CANCELLED");
            else if (statusFilter.contains("OPEN")) matchStatus = invoice.getStatus().equals("OPEN");

            // 3. Lọc theo Ngày (So sánh chuỗi yyyy-MM-dd)
            boolean matchDate = true;
            if (selectedDate != null) {
                String dateString = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                matchDate = invoice.getCreatedAt().startsWith(dateString);
            }

            return matchText && matchStatus && matchDate;
        });

        // Nếu không chọn dòng nào, làm rỗng bảng chi tiết
        detailTable.getItems().clear();
        detailsTitleLabel.setText("CHI TIẾT HÓA ĐƠN");
        invoiceTimeLabel.setText("Thời gian: --/--/----");
    }

    // --- LOGIC TÍNH DOANH THU (DASHBOARD) ---
    private void updateDashboard() {
        double revenue = 0;
        int success = 0;
        int cancel = 0;

        for (Invoice inv : filteredInvoices) {
            if ("PAID".equals(inv.getStatus())) {
                revenue += inv.getTotal();
                success++;
            } else if ("CANCELLED".equals(inv.getStatus())) {
                cancel++;
            }
        }

        totalRevenueLabel.setText(String.format("%,.0f Đ", revenue));
        successCountLabel.setText(String.valueOf(success));
        cancelCountLabel.setText(String.valueOf(cancel));
    }

    // --- XEM CHI TIẾT 1 HÓA ĐƠN ---
    private void loadInvoiceDetails(Invoice invoice) {
        detailsTitleLabel.setText("CHI TIẾT: " + invoice.getInvoiceCode());
        invoiceTimeLabel.setText("Thời gian tạo: " + invoice.getCreatedAt());

        List<OrderDetail> details = invoiceRepository.getOrderDetailsByInvoiceId(invoice.getId());
        detailTable.setItems(FXCollections.observableArrayList(details));
    }

    @FXML
    protected void handleResetFilters() {
        searchField.clear();
        statusFilterComboBox.setValue("Tất cả trạng thái");
        datePicker.setValue(null);
    }

    @FXML
    protected void handleBackToMain() {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/main-view.fxml"));
            javafx.scene.Scene mainScene = new javafx.scene.Scene(fxmlLoader.load(), 800, 600);
            javafx.stage.Stage currentStage = (javafx.stage.Stage) totalRevenueLabel.getScene().getWindow();
            currentStage.setScene(mainScene);
            currentStage.setTitle("CỎ Coffee & Tea - Trang Chủ");
            currentStage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}