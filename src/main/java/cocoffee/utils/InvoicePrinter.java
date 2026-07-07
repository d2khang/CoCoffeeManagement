package cocoffee.utils;

import cocoffee.models.Invoice;
import cocoffee.models.OrderDetail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class InvoicePrinter {

    /**
     * Hàm tự động xuất hóa đơn ra file text định dạng chuẩn K80 (UTF-8),
     * ĐỒNG THỜI gửi lệnh in thật tới máy in nhiệt nếu đã bật trong printer.properties.
     *
     * File .txt vẫn luôn được lưu như cũ để đối chiếu/dự phòng, kể cả khi máy in vật lý
     * bị lỗi hoặc chưa cấu hình — đảm bảo không phá vỡ logic đã có.
     */
    public static boolean exportReceiptK80(Invoice invoice, List<OrderDetail> details) {
        // Tạo thư mục "receipts" trong dự án nếu chưa tồn tại để lưu trữ hóa đơn cũ
        File directory = new File("receipts");
        if (!directory.exists()) {
            directory.mkdir();
        }

        String fileName = "receipts/" + invoice.getInvoiceCode() + ".txt";
        boolean fileOk;

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8))) {

            // Cấu hình độ rộng ký tự chuẩn cho máy in nhiệt K80 (Khoảng 40 ký tự một dòng)
            String separator = "----------------------------------------\n";

            // 1. TRUNG TÂM HEADER
            writer.write(centerText("CỎ COFFEE & TEA") + "\n");
            writer.write(centerText("Đ/C: Đường số 5, Quán Cà Phê Gia Đình") + "\n");
            writer.write(centerText("Hotline: 09xx.xxx.xxx") + "\n");
            writer.write("\n");
            writer.write(centerText("HÓA ĐƠN THANH TOÁN") + "\n");
            writer.write("\n");

            // 2. THÔNG TIN THỜI GIAN / VỊ TRÍ
            writer.write("Mã HD : " + invoice.getInvoiceCode() + "\n");
            String tableInfo = (invoice.getTableNumber() != null && !invoice.getTableNumber().isEmpty())
                    ? invoice.getTableNumber() : "Mang đi";
            writer.write("Vị trí: " + tableInfo + "\n");
            writer.write("Ngày  : " + invoice.getCreatedAt() + "\n");
            writer.write("Thu ngân: Chủ Quán CỎ\n");
            writer.write(separator);

            // 3. TIÊU ĐỀ BẢNG MÓN AN
            writer.write(String.format("%-20s %4s %14s\n", "Tên Món", "SL", "T.Tiền"));
            writer.write(separator);

            // 4. DANH SÁCH MÓN ĂN / ĐỒ UỐNG
            for (OrderDetail item : details) {
                String productName = item.getProductName();
                if (productName.length() > 18) {
                    productName = productName.substring(0, 16) + "..";
                }
                String qty = String.valueOf(item.getQuantity());
                String totalStr = String.format("%,.0f", item.getTotal());

                writer.write(String.format("%-20s %4s %14s\n", productName, qty, totalStr));
            }

            writer.write(separator);

            // 5. PHẦN TỔNG TIỀN VÀ PHƯƠNG THỨC THANH TOÁN
            writer.write(String.format("%-20s %20s\n", "TỔNG TIỀN THU:", String.format("%,.0f Đ", invoice.getTotal())));

            String displayMethod = "CASH".equals(invoice.getPaymentMethod()) ? "Tiền mặt" : "Chuyển khoản";
            writer.write(String.format("%-20s %20s\n", "Hình thức PTTT:", displayMethod));
            writer.write("\n");

            // 6. LỜI CẢM ƠN FOOTER
            writer.write(centerText("Cám ơn Quý khách!") + "\n");
            writer.write(centerText("Hẹn gặp lại Quý khách lần sau!") + "\n");
            writer.write("\n\n\n\n");

            writer.flush();
            System.out.println("Đã xuất hóa đơn thành công file: " + fileName);
            fileOk = true;

        } catch (Exception e) {
            System.out.println("Lỗi xuất hóa đơn K80: " + e.getMessage());
            e.printStackTrace();
            fileOk = false;
        }

        // 🌟 MỚI: Sau khi lưu file xong, thử in thật ra máy in nhiệt nếu đã bật cấu hình.
        // Lỗi in vật lý (chưa cắm máy, sai IP...) sẽ KHÔNG làm hàm này trả về false,
        // vì file .txt vẫn đã lưu thành công — tránh chặn luồng thanh toán của thu ngân.
        tryPrintPhysical(invoice, details);

        return fileOk;
    }

    /**
     * Gửi lệnh in thật tới máy in nhiệt (USB hoặc LAN) dựa theo cấu hình trong printer.properties.
     * Nếu printer.enabled=false hoặc gửi lỗi, chỉ ghi log ra console, không ném exception ra ngoài.
     */
    private static void tryPrintPhysical(Invoice invoice, List<OrderDetail> details) {
        PrinterConfig config = PrinterConfig.load();

        if (!config.enabled) {
            return; // Người dùng chưa bật in nhiệt thật -> bỏ qua, chỉ có file .txt
        }

        try {
            byte[] escPosData = buildEscPosReceipt(invoice, details);
            ThermalPrinterSender.send(config, escPosData);
            System.out.println("[ThermalPrinter] Đã gửi lệnh in hóa đơn " + invoice.getInvoiceCode()
                    + " tới máy in (" + config.connectionType + ").");
        } catch (Exception e) {
            System.out.println("[ThermalPrinter] In hóa đơn thất bại (đã có file .txt dự phòng): " + e.getMessage());
        }
    }

    /**
     * Build nội dung hóa đơn dạng bytes ESC/POS, bố cục giống hệt bản .txt ở trên
     * nhưng có thêm chữ đậm/chữ to cho phần tổng tiền để dễ nhìn trên giấy in nhiệt thật.
     */
    private static byte[] buildEscPosReceipt(Invoice invoice, List<OrderDetail> details) {
        EscPosBuilder p = new EscPosBuilder();

        String tableInfo = (invoice.getTableNumber() != null && !invoice.getTableNumber().isEmpty())
                ? invoice.getTableNumber() : "Mang đi";
        String displayMethod = "CASH".equals(invoice.getPaymentMethod()) ? "Tiền mặt" : "Chuyển khoản";

        p.centerAlign();
        p.boldOn().line("CỎ COFFEE & TEA").boldOff();
        p.line("Đ/C: Đường số 5, Quán Cà Phê Gia Đình");
        p.line("Hotline: 09xx.xxx.xxx");
        p.newLine();
        p.boldOn().line("HÓA ĐƠN THANH TOÁN").boldOff();
        p.newLine();

        p.leftAlign();
        p.line("Mã HD : " + invoice.getInvoiceCode());
        p.line("Vị trí: " + tableInfo);
        p.line("Ngày  : " + invoice.getCreatedAt());
        p.line("Thu ngân: Chủ Quán CỎ");
        p.divider();

        p.line(String.format("%-20s %4s %14s", "Tên Món", "SL", "T.Tiền"));
        p.divider();

        for (OrderDetail item : details) {
            String productName = item.getProductName();
            if (productName.length() > 18) {
                productName = productName.substring(0, 16) + "..";
            }
            String qty = String.valueOf(item.getQuantity());
            String totalStr = String.format("%,.0f", item.getTotal());
            p.line(String.format("%-20s %4s %14s", productName, qty, totalStr));
        }

        p.divider();

        p.boldOn();
        p.line(String.format("%-20s %20s", "TỔNG TIỀN THU:", String.format("%,.0f Đ", invoice.getTotal())));
        p.boldOff();
        p.line(String.format("%-20s %20s", "Hình thức PTTT:", displayMethod));
        p.newLine();

        p.centerAlign();
        p.line("Cám ơn Quý khách!");
        p.line("Hẹn gặp lại Quý khách lần sau!");

        p.cut();

        return p.build();
    }

    /**
     * 🌟 Xuất báo cáo doanh thu tổng hợp theo khoảng thời gian (Ngày/Tháng/Năm)
     * ra file .txt, định dạng khổ giấy K80 (~40 ký tự/dòng).
     * (Báo cáo chỉ xuất file, không in ra máy in nhiệt do nội dung thường dài hơn 1 cuộn giấy)
     *
     * @param periodLabel Nhãn mô tả khoảng thời gian, ví dụ: "Ngày 07/07/2026", "Tháng 7/2026", "Năm 2026"
     * @param invoices    Danh sách hóa đơn đã được lọc theo đúng khoảng thời gian cần báo cáo
     */
    public static boolean exportRevenueReport(String periodLabel, List<Invoice> invoices) {
        File directory = new File("reports");
        if (!directory.exists()) {
            directory.mkdir();
        }

        String safeLabel = periodLabel.replaceAll("[^a-zA-Z0-9]", "_");
        String fileName = "reports/BaoCaoDoanhThu_" + safeLabel + "_" + System.currentTimeMillis() + ".txt";

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8))) {

            String separator = "----------------------------------------\n";

            writer.write(centerText("CỎ COFFEE & TEA") + "\n");
            writer.write(centerText("BÁO CÁO DOANH THU") + "\n");
            writer.write(centerText(periodLabel) + "\n");
            writer.write("\n");

            double totalRevenue = 0;
            int paidCount = 0;
            int cancelledCount = 0;
            double cashTotal = 0;
            double transferTotal = 0;
            double cardTotal = 0;

            writer.write(String.format("%-14s %-12s %12s\n", "Mã HD", "PTTT", "Tiền"));
            writer.write(separator);

            for (Invoice inv : invoices) {
                if ("PAID".equals(inv.getStatus())) {
                    paidCount++;
                    totalRevenue += inv.getTotal();

                    String method = inv.getPaymentMethod();
                    if ("CASH".equals(method)) cashTotal += inv.getTotal();
                    else if ("BANK_TRANSFER".equals(method)) transferTotal += inv.getTotal();
                    else if ("CARD".equals(method)) cardTotal += inv.getTotal();

                    writer.write(String.format("%-14s %-12s %12s\n",
                            inv.getInvoiceCode(),
                            displayMethod(method),
                            String.format("%,.0f", inv.getTotal())));

                } else if ("CANCELLED".equals(inv.getStatus())) {
                    cancelledCount++;
                }
            }

            writer.write(separator);

            writer.write(String.format("%-22s %16s\n", "TỔNG DOANH THU:", String.format("%,.0f Đ", totalRevenue)));
            writer.write(String.format("%-22s %16d\n", "Số HD thành công:", paidCount));
            writer.write(String.format("%-22s %16d\n", "Số HD đã hủy:", cancelledCount));
            writer.write("\n");

            writer.write(centerText("-- Chi tiết theo PTTT --") + "\n");
            writer.write(String.format("%-22s %16s\n", "Tiền mặt:", String.format("%,.0f Đ", cashTotal)));
            writer.write(String.format("%-22s %16s\n", "Chuyển khoản:", String.format("%,.0f Đ", transferTotal)));
            writer.write(String.format("%-22s %16s\n", "Quẹt thẻ:", String.format("%,.0f Đ", cardTotal)));
            writer.write("\n");

            writer.write(centerText("Người in: Chủ Quán CỎ") + "\n");
            writer.write(centerText("Thời gian in: " + java.time.LocalDateTime.now()) + "\n");
            writer.write("\n\n\n");

            writer.flush();
            System.out.println("Đã xuất báo cáo doanh thu thành công file: " + fileName);
            return true;

        } catch (Exception e) {
            System.out.println("Lỗi xuất báo cáo doanh thu: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static String centerText(String text) {
        int width = 40;
        if (text.length() >= width) {
            return text;
        }
        int padding = (width - text.length()) / 2;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < padding; i++) {
            sb.append(" ");
        }
        sb.append(text);
        return sb.toString();
    }

    private static String displayMethod(String method) {
        if (method == null) return "N/A";
        switch (method) {
            case "CASH": return "Tiền mặt";
            case "BANK_TRANSFER": return "Chuyển khoản";
            case "CARD": return "Quẹt thẻ";
            default: return method;
        }
    }
}