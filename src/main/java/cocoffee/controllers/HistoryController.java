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
import java.util.ArrayList;
import java.util.List;

public class HistoryController {

    // --- KHU VỰC THỐNG KÊ (DASHBOARD) ---
    @FXML private Label totalRevenueLabel;
    @FXML private Label successCountLabel;
    @FXML private Label cancelCountLabel;

    // --- KHU VỰC BỘ LỌC (tìm kiếm / trạng thái) ---
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterComboBox;

    // --- 🌟 MỚI: KHU VỰC LỌC THEO NGÀY / THÁNG / NĂM ---
    @FXML private ToggleGroup filterModeGroup;
    @FXML private ToggleButton btnFilterDay;
    @FXML private ToggleButton btnFilterMonth;
    @FXML private ToggleButton btnFilterYear;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<Integer> monthComboBox;
    @FXML private ComboBox<Integer> yearComboBox;

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
    @FXML private Button reprintButton;

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

        // Format cột Trạng thái thành Badge bo tròn (kiểu GitHub) thay vì chỉ đổi màu chữ
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge;
                    switch (status) {
                        case "PAID":
                            badge = new Label("Đã thanh toán");
                            badge.getStyleClass().addAll("status-pill", "status-pill-success");
                            break;
                        case "CANCELLED":
                            badge = new Label("Đã hủy");
                            badge.getStyleClass().addAll("status-pill", "status-pill-danger");
                            break;
                        case "OPEN":
                            badge = new Label("Chờ thanh toán");
                            badge.getStyleClass().addAll("status-pill", "status-pill-warning");
                            break;
                        default:
                            badge = new Label(status);
                            badge.getStyleClass().add("status-pill");
                    }
                    setGraphic(badge);
                    setText(null);
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

        // 🌟 MỚI: Nạp danh sách Tháng (1-12) và Năm (5 năm gần nhất)
        ObservableList<Integer> months = FXCollections.observableArrayList();
        for (int m = 1; m <= 12; m++) months.add(m);
        monthComboBox.setItems(months);

        int currentYear = LocalDate.now().getYear();
        ObservableList<Integer> years = FXCollections.observableArrayList();
        for (int y = currentYear; y >= currentYear - 4; y--) years.add(y);
        yearComboBox.setItems(years);

        // Hiển thị "Tháng X" thay vì chỉ số nguyên
        monthComboBox.setButtonCell(createMonthCell());
        monthComboBox.setCellFactory(cb -> createMonthCell());

        // Giá trị mặc định: ngày/tháng/năm hiện tại
        datePicker.setValue(LocalDate.now());
        monthComboBox.setValue(LocalDate.now().getMonthValue());
        yearComboBox.setValue(currentYear);

        // Mặc định hiển thị chế độ "Ngày"
        updateFilterModeUI();

        filteredInvoices = new FilteredList<>(masterInvoiceList, p -> true);
        invoiceTable.setItems(filteredInvoices);

        // Lắng nghe thay đổi từ Bộ lọc
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        statusFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        monthComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        yearComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // 🌟 MỚI: Lắng nghe đổi chế độ Ngày/Tháng/Năm
        filterModeGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                // Không cho phép bỏ chọn hết -> giữ nguyên lựa chọn trước đó
                if (oldToggle != null) oldToggle.setSelected(true);
                return;
            }
            updateFilterModeUI();
            applyFilters();
        });

        // Cập nhật thống kê mỗi khi danh sách lọc thay đổi
        filteredInvoices.predicateProperty().addListener((obs, oldVal, newVal) -> updateDashboard());
    }

    // Tạo cell hiển thị "Tháng 1", "Tháng 2"... cho ComboBox tháng
    private ListCell<Integer> createMonthCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Integer month, boolean empty) {
                super.updateItem(month, empty);
                setText(empty || month == null ? null : "Tháng " + month);
            }
        };
    }

    // 🌟 MỚI: Bật/tắt hiển thị DatePicker / ComboBox tháng / ComboBox năm theo chế độ đang chọn
    private void updateFilterModeUI() {
        boolean isDay = btnFilterDay.isSelected();
        boolean isMonth = btnFilterMonth.isSelected();
        boolean isYear = btnFilterYear.isSelected();

        datePicker.setVisible(isDay);
        datePicker.setManaged(isDay);

        monthComboBox.setVisible(isMonth);
        monthComboBox.setManaged(isMonth);

        yearComboBox.setVisible(isMonth || isYear);
        yearComboBox.setManaged(isMonth || isYear);
    }

    private void loadData() {
        masterInvoiceList.setAll(invoiceRepository.getAllInvoices());
        applyFilters(); // Áp dụng bộ lọc ngay từ đầu
    }

    // --- LOGIC LỌC TỔNG HỢP ---
    private void applyFilters() {
        String searchText = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        String statusFilter = statusFilterComboBox.getValue();

        filteredInvoices.setPredicate(invoice -> {
            // 1. Lọc theo chữ (Mã hoặc Bàn)
            boolean matchText = invoice.getInvoiceCode().toLowerCase().contains(searchText) ||
                    (invoice.getTableNumber() != null && invoice.getTableNumber().toLowerCase().contains(searchText));

            // 2. Lọc theo trạng thái
            boolean matchStatus = true;
            if (statusFilter.contains("PAID")) matchStatus = invoice.getStatus().equals("PAID");
            else if (statusFilter.contains("CANCELLED")) matchStatus = invoice.getStatus().equals("CANCELLED");
            else if (statusFilter.contains("OPEN")) matchStatus = invoice.getStatus().equals("OPEN");

            // 3. 🌟 MỚI: Lọc theo khoảng thời gian Ngày / Tháng / Năm
            boolean matchDate = true;
            if (invoice.getCreatedAt() != null) {
                if (btnFilterDay.isSelected()) {
                    LocalDate selectedDate = datePicker.getValue();
                    if (selectedDate != null) {
                        String dateString = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        matchDate = invoice.getCreatedAt().startsWith(dateString);
                    }
                } else if (btnFilterMonth.isSelected()) {
                    Integer month = monthComboBox.getValue();
                    Integer year = yearComboBox.getValue();
                    if (month != null && year != null) {
                        String monthString = String.format("%04d-%02d", year, month);
                        matchDate = invoice.getCreatedAt().startsWith(monthString);
                    }
                } else if (btnFilterYear.isSelected()) {
                    Integer year = yearComboBox.getValue();
                    if (year != null) {
                        String yearString = String.format("%04d", year);
                        matchDate = invoice.getCreatedAt().startsWith(yearString);
                    }
                }
            }

            return matchText && matchStatus && matchDate;
        });

        // Nếu không chọn dòng nào, làm rỗng bảng chi tiết
        detailTable.getItems().clear();
        detailsTitleLabel.setText("CHI TIẾT HÓA ĐƠN");
        invoiceTimeLabel.setText("Thời gian: --/--/----");
        reprintButton.setDisable(true); // Chưa chọn hóa đơn -> khóa nút in lại
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

        reprintButton.setDisable(false); // Có hóa đơn được chọn -> cho phép in lại
    }

    // --- IN LẠI HÓA ĐƠN (REPRINT) ---
    @FXML
    protected void handleReprintInvoice() {
        Invoice selected = invoiceTable.getSelectionModel().getSelectedItem();

        // 1. Kiểm tra đã chọn hóa đơn chưa
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn hóa đơn",
                    "Vui lòng chọn một hóa đơn trong danh sách trước khi in lại.");
            return;
        }

        // 2. Không cho in lại hóa đơn đã hủy (tránh gây nhầm lẫn/gian lận)
        if ("CANCELLED".equals(selected.getStatus())) {
            showAlert(Alert.AlertType.WARNING, "Không thể in",
                    "Hóa đơn " + selected.getInvoiceCode() + " đã bị hủy, không thể in lại.");
            return;
        }

        // 3. Thực hiện in lại, có bắt lỗi đầy đủ
        try {
            List<OrderDetail> details = invoiceRepository.getOrderDetailsByInvoiceId(selected.getId());

            if (details == null || details.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Không có dữ liệu",
                        "Không tìm thấy chi tiết món ăn cho hóa đơn này.");
                return;
            }

            boolean success = cocoffee.utils.InvoicePrinter.exportReceiptK80(selected, details);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công",
                        "Đã in lại hóa đơn " + selected.getInvoiceCode() + " thành công.");
                System.out.println("[LOG] In lại hóa đơn: " + selected.getInvoiceCode()
                        + " lúc " + java.time.LocalDateTime.now());
            } else {
                showAlert(Alert.AlertType.ERROR, "In thất bại",
                        "Không thể in hóa đơn " + selected.getInvoiceCode()
                                + ". Vui lòng kiểm tra lại máy in hoặc thử lại.");
            }

        } catch (Exception e) {
            // Không để crash UI nếu DB lỗi hoặc file bị khóa
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống",
                    "Đã xảy ra lỗi khi in lại hóa đơn: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- 🌟 MỚI: IN BÁO CÁO DOANH THU THEO NGÀY/THÁNG/NĂM ĐANG CHỌN ---
    @FXML
    protected void handlePrintRevenueReport() {
        // Lấy đúng danh sách hóa đơn đang hiển thị sau khi lọc (Ngày/Tháng/Năm + tìm kiếm + trạng thái)
        List<Invoice> currentList = new ArrayList<>(filteredInvoices);

        if (currentList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Không có dữ liệu",
                    "Không có hóa đơn nào trong khoảng thời gian đã chọn để in báo cáo.");
            return;
        }

        String periodLabel = buildPeriodLabel();

        try {
            boolean success = cocoffee.utils.InvoicePrinter.exportRevenueReport(periodLabel, currentList);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công",
                        "Đã in báo cáo doanh thu (" + periodLabel + ") thành công.");
                System.out.println("[LOG] In báo cáo doanh thu: " + periodLabel
                        + " lúc " + java.time.LocalDateTime.now());
            } else {
                showAlert(Alert.AlertType.ERROR, "In thất bại",
                        "Không thể in báo cáo doanh thu. Vui lòng thử lại.");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống",
                    "Đã xảy ra lỗi khi in báo cáo doanh thu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Tạo nhãn mô tả khoảng thời gian đang chọn, dùng để in tiêu đề báo cáo
    private String buildPeriodLabel() {
        if (btnFilterDay.isSelected()) {
            LocalDate d = datePicker.getValue();
            return d != null ? "Ngày " + d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Tất cả các ngày";
        } else if (btnFilterMonth.isSelected()) {
            Integer m = monthComboBox.getValue();
            Integer y = yearComboBox.getValue();
            return (m != null && y != null) ? "Tháng " + m + "/" + y : "Tất cả các tháng";
        } else {
            Integer y = yearComboBox.getValue();
            return y != null ? "Năm " + y : "Tất cả các năm";
        }
    }

    // --- HÀM TIỆN ÍCH HIỂN THỊ THÔNG BÁO ---
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    protected void handleResetFilters() {
        searchField.clear();
        statusFilterComboBox.setValue("Tất cả trạng thái");
        btnFilterDay.setSelected(true);
        datePicker.setValue(LocalDate.now());
        monthComboBox.setValue(LocalDate.now().getMonthValue());
        yearComboBox.setValue(LocalDate.now().getYear());
        updateFilterModeUI();
        applyFilters();
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