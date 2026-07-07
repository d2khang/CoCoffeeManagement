package cocoffee.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Lớp tiện ích dựng chuỗi lệnh ESC/POS (chuẩn phổ biến của hầu hết máy in nhiệt K80/K58:
 * Xprinter, Gprinter, Antech, Hoin...).
 *
 * LƯU Ý VỀ TIẾNG VIỆT CÓ DẤU:
 * Máy in nhiệt không hỗ trợ Unicode/UTF-8 trực tiếp như màn hình máy tính.
 * Phải chọn đúng bảng mã (code page) máy in hỗ trợ, ở đây dùng Windows-1258 (phổ biến nhất
 * cho tiếng Việt trên máy in nhiệt Trung Quốc). Nếu bill in ra bị lỗi font (mất dấu, ra ký tự lạ),
 * thử đổi giá trị codePageId trong hàm selectVietnameseCodePage() — mỗi hãng máy in có thể
 * đánh số bảng mã khác nhau (thường thử các giá trị: 25, 30, 52, 253, 255 tùy dòng máy).
 */
public class EscPosBuilder {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final Charset vietnameseCharset = Charset.forName("windows-1258");

    // ===== CÁC LỆNH ESC/POS CƠ BẢN =====
    private static final byte[] INIT             = {0x1B, 0x40};                 // Reset máy in
    private static final byte[] ALIGN_LEFT        = {0x1B, 0x61, 0x00};
    private static final byte[] ALIGN_CENTER       = {0x1B, 0x61, 0x01};
    private static final byte[] BOLD_ON           = {0x1B, 0x45, 0x01};
    private static final byte[] BOLD_OFF          = {0x1B, 0x45, 0x00};
    private static final byte[] DOUBLE_SIZE_ON     = {0x1D, 0x21, 0x11};          // Chữ to gấp đôi (cao + rộng)
    private static final byte[] NORMAL_SIZE        = {0x1D, 0x21, 0x00};
    private static final byte[] CUT_PAPER          = {0x1D, 0x56, 0x01};          // Cắt giấy (partial cut)
    private static final byte[] LINE_FEED_3        = {0x0A, 0x0A, 0x0A};

    public EscPosBuilder() {
        write(INIT);
        selectVietnameseCodePage();
    }

    /**
     * Chọn bảng mã tiếng Việt trên máy in. 25 = Windows-1258 trên phần lớn máy in
     * dùng chipset phổ biến (Xprinter, Gprinter...). Nếu in ra sai dấu, thử đổi số này.
     */
    private void selectVietnameseCodePage() {
        write(new byte[]{0x1B, 0x74, 25});
    }

    private void write(byte[] bytes) {
        try {
            buffer.write(bytes);
        } catch (IOException ignored) {
        }
    }

    public EscPosBuilder text(String s) {
        write(s.getBytes(vietnameseCharset));
        return this;
    }

    public EscPosBuilder newLine() {
        write(new byte[]{0x0A});
        return this;
    }

    public EscPosBuilder line(String s) {
        text(s);
        return newLine();
    }

    public EscPosBuilder centerAlign() {
        write(ALIGN_CENTER);
        return this;
    }

    public EscPosBuilder leftAlign() {
        write(ALIGN_LEFT);
        return this;
    }

    public EscPosBuilder boldOn() {
        write(BOLD_ON);
        return this;
    }

    public EscPosBuilder boldOff() {
        write(BOLD_OFF);
        return this;
    }

    public EscPosBuilder doubleSizeOn() {
        write(DOUBLE_SIZE_ON);
        return this;
    }

    public EscPosBuilder normalSize() {
        write(NORMAL_SIZE);
        return this;
    }

    /** Dòng kẻ ngang đầy đủ chiều ngang khổ giấy K80 (~40 ký tự) */
    public EscPosBuilder divider() {
        return line("----------------------------------------");
    }

    /** Đẩy vài dòng trắng + lệnh cắt giấy, đặt cuối cùng khi build xong bill */
    public EscPosBuilder cut() {
        write(LINE_FEED_3);
        write(CUT_PAPER);
        return this;
    }

    public byte[] build() {
        return buffer.toByteArray();
    }
}