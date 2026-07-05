# CỎ Coffee & Tea - Quản lý Bán Hàng

Dự án phần mềm quản lý quán cà phê "CỎ Coffee & Tea" là một giải pháp quản lý bán hàng chuyên nghiệp, được thiết kế tối ưu cho các quán gia đình với quy mô dưới 20 bàn. Phần mềm tập trung vào sự đơn giản, dễ sử dụng, hiệu năng ổn định và khả năng bảo trì lâu dài.

## 🚀 Công nghệ sử dụng
- **Ngôn ngữ:** Java 21 (LTS)
- **Giao diện (GUI):** JavaFX
- **Build Tool:** Maven
- **Cơ sở dữ liệu:** SQLite
- **Kiến trúc:** MVC (Model-View-Controller) kết hợp Repository Pattern & Service Layer
- **IDE:** IntelliJ IDEA

## 🏗️ Kiến trúc thư mục
Dự án được tổ chức theo phân lớp chuẩn doanh nghiệp:
- `com.cocoffee.config`: Cấu hình kết nối cơ sở dữ liệu.
- `com.cocoffee.controllers`: Điều khiển logic giao diện (FXML).
- `com.cocoffee.models`: Định nghĩa các đối tượng dữ liệu (Entity).
- `com.cocoffee.repositories`: Tương tác trực tiếp với database.
- `com.cocoffee.services`: Xử lý logic nghiệp vụ.
- `src/main/resources/views`: Chứa file giao diện FXML.
- `src/main/resources/styles`: Chứa file CSS.

## 🛠️ Hướng dẫn cài đặt
1. **Yêu cầu hệ thống:**
   - Đã cài đặt JDK 21.
   - IntelliJ IDEA Community Edition.
   - DB Browser for SQLite (để xem dữ liệu).

2. **Cách chạy dự án:**
   - Clone dự án về máy.
   - Mở bằng IntelliJ IDEA.
   - Chờ Maven tải các thư viện trong `pom.xml`.
   - Chạy class `cocoffee.Launcher` để khởi động ứng dụng.

## 🗺️ Lộ trình phát triển (Roadmap)
- [x] **v0.1:** Khởi tạo project, cấu trúc thư mục, kết nối SQLite.
- [x] **v0.2:** Đăng nhập, quản lý tài khoản, phân quyền Admin/Staff.
- [ ] **v0.3:** Quản lý Menu (Thêm/Sửa/Xóa món, Danh mục, Size, Topping).
- [ ] **v0.4:** Màn hình POS (Bán hàng).
- [ ] **v0.5:** Quản lý Bàn.
- ... *(Đang cập nhật)*

## 📝 Nguyên tắc phát triển
- **Clean Code & SOLID:** Đảm bảo mã nguồn dễ hiểu và dễ mở rộng.
- **Dễ bảo trì:** Tách biệt hoàn toàn giao diện và logic nghiệp vụ.
- **Mở rộng:** Dễ dàng chuyển đổi từ SQLite sang MySQL trong tương lai.

---
*Dự án phát triển bởi Khang Dương - Giải pháp quản lý cho CỎ Coffee & Tea.*
