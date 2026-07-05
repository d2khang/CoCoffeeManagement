package cocoffee;

import org.mindrot.jbcrypt.BCrypt;

public class GenerateHash {
    public static void main(String[] args) {
        String password = "admin123"; // Đổi thành mật khẩu bạn muốn dùng
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        System.out.println("Hash: " + hash);
    }
}