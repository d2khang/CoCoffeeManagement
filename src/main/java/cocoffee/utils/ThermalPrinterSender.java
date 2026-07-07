package cocoffee.utils;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

/**
 * Gửi dữ liệu ESC/POS thô tới máy in nhiệt thật, hỗ trợ 2 kiểu kết nối:
 *  - LAN : mở Socket TCP tới cổng RAW (thường là 9100) của máy in mạng.
 *  - USB : gửi qua Windows Print Spooler bằng javax.print (máy in phải đã cài driver
 *          và xuất hiện trong Windows > Settings > Printers & scanners).
 */
public class ThermalPrinterSender {

    /** Ném PrinterException nếu in thất bại, để nơi gọi tự quyết định xử lý (báo lỗi, ghi log...) */
    public static void send(PrinterConfig config, byte[] escPosData) throws PrinterException {
        if (config.connectionType == PrinterConfig.ConnectionType.LAN) {
            sendViaLan(config.lanIp, config.lanPort, escPosData);
        } else {
            sendViaUsb(config.usbPrinterName, escPosData);
        }
    }

    private static void sendViaLan(String ip, int port, byte[] data) throws PrinterException {
        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress(ip, port), 5000); // timeout 5s
            try (OutputStream out = socket.getOutputStream()) {
                out.write(data);
                out.flush();
            }
        } catch (IOException e) {
            throw new PrinterException("Không thể kết nối máy in qua LAN (" + ip + ":" + port + "): " + e.getMessage());
        }
    }

    private static void sendViaUsb(String printerName, byte[] data) throws PrinterException {
        DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
        PrintService[] services = PrintServiceLookup.lookupPrintServices(flavor, null);

        PrintService target = Arrays.stream(services)
                .filter(p -> p.getName().equalsIgnoreCase(printerName) || p.getName().contains(printerName))
                .findFirst()
                .orElse(null);

        if (target == null) {
            String available = Arrays.stream(services).map(PrintService::getName)
                    .reduce((a, b) -> a + ", " + b).orElse("(không có máy in nào được Windows nhận diện)");
            throw new PrinterException("Không tìm thấy máy in USB tên \"" + printerName + "\". "
                    + "Các máy in đang có: " + available);
        }

        DocPrintJob job = target.createPrintJob();
        Doc doc = new SimpleDoc(data, flavor, null);
        try {
            job.print(doc, new HashPrintRequestAttributeSet());
        } catch (javax.print.PrintException e) {
            throw new PrinterException("Lỗi khi gửi lệnh in tới máy in USB \"" + printerName + "\": " + e.getMessage());
        }
    }

    /** Đơn giản để in thử 1 dòng kiểm tra kết nối trước khi tích hợp thật */
    public static class PrinterException extends Exception {
        public PrinterException(String message) {
            super(message);
        }
    }
}