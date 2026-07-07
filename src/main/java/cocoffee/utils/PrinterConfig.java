package cocoffee.utils;

import java.io.*;
import java.util.Properties;

/**
 * Đọc cấu hình máy in nhiệt từ file "printer.properties" nằm cùng thư mục chạy chương trình
 * (ngang hàng với thư mục receipts/, reports/).
 *
 * Nếu file chưa tồn tại, hàm sẽ tự tạo 1 file mẫu với printer.enabled=false,
 * để không làm crash app khi chưa cắm/chưa cấu hình máy in.
 *
 * Cách dùng:
 *   1. Mở file printer.properties (tự sinh ra sau lần chạy đầu tiên) trong thư mục gốc dự án
 *      (ngang hàng với thư mục receipts/, reports/, hoặc cùng chỗ với file .jar khi đóng gói).
 *   2. Sửa printer.enabled=true
 *   3. Chọn printer.connection=LAN hoặc USB
 *   4. Điền đúng IP (nếu LAN) hoặc tên máy in (nếu USB, xem trong Windows > Settings > Printers)
 */
public class PrinterConfig {

    public enum ConnectionType { USB, LAN }

    private static final String CONFIG_FILE = "printer.properties";

    public boolean enabled = false;
    public ConnectionType connectionType = ConnectionType.LAN;

    // Cấu hình cho kết nối LAN (máy in nối mạng, thường mở sẵn cổng 9100)
    public String lanIp = "192.168.1.100";
    public int lanPort = 9100;

    // Cấu hình cho kết nối USB (máy in cắm dây, Windows đã cài driver và đặt tên máy in)
    public String usbPrinterName = "POS-80";

    public static PrinterConfig load() {
        PrinterConfig config = new PrinterConfig();
        File file = new File(CONFIG_FILE);

        if (!file.exists()) {
            createDefaultConfigFile(file);
            System.out.println("[PrinterConfig] Chưa có file cấu hình máy in. Đã tạo file mẫu: "
                    + file.getAbsolutePath() + " (mặc định đang TẮT in nhiệt, chỉ xuất file .txt như cũ)");
            return config; // enabled = false mặc định, an toàn
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            Properties props = new Properties();
            props.load(fis);

            config.enabled = Boolean.parseBoolean(props.getProperty("printer.enabled", "false"));

            String connStr = props.getProperty("printer.connection", "LAN").trim().toUpperCase();
            config.connectionType = "USB".equals(connStr) ? ConnectionType.USB : ConnectionType.LAN;

            config.lanIp = props.getProperty("printer.lan.ip", config.lanIp);
            config.lanPort = Integer.parseInt(props.getProperty("printer.lan.port", "9100").trim());

            config.usbPrinterName = props.getProperty("printer.usb.name", config.usbPrinterName);

        } catch (Exception e) {
            System.out.println("[PrinterConfig] Lỗi đọc file cấu hình, dùng cấu hình mặc định (TẮT in nhiệt): " + e.getMessage());
        }

        return config;
    }

    private static void createDefaultConfigFile(File file) {
        String content =
                "# ===================================================\n" +
                        "# CẤU HÌNH MÁY IN NHIỆT - CỎ COFFEE & TEA\n" +
                        "# ===================================================\n" +
                        "# Đổi thành true để bật in hóa đơn ra máy in nhiệt thật.\n" +
                        "# Khi false, app chỉ xuất file .txt vào thư mục receipts/ như trước (an toàn, không lỗi).\n" +
                        "printer.enabled=false\n" +
                        "\n" +
                        "# Kiểu kết nối: LAN hoặc USB\n" +
                        "printer.connection=LAN\n" +
                        "\n" +
                        "# --- Cấu hình khi dùng LAN (máy in nối mạng, cổng RAW 9100) ---\n" +
                        "# Xem IP bằng cách bấm nút Feed/Test trên máy in để in ra tờ cấu hình.\n" +
                        "printer.lan.ip=192.168.1.100\n" +
                        "printer.lan.port=9100\n" +
                        "\n" +
                        "# --- Cấu hình khi dùng USB ---\n" +
                        "# Điền ĐÚNG tên máy in như hiển thị trong Windows > Settings > Printers & scanners.\n" +
                        "printer.usb.name=POS-80\n";

        try (FileWriter fw = new FileWriter(file)) {
            fw.write(content);
        } catch (IOException e) {
            System.out.println("[PrinterConfig] Không thể tạo file cấu hình mẫu: " + e.getMessage());
        }
    }
}