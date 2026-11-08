# Hotel Management System

Ứng dụng quản lý đặt phòng khách sạn bằng Java 17, Swing, JDBC và MySQL 8.

## 1. Yêu cầu
- JDK 17
- Maven 3.8+
- MySQL 8+
- IntelliJ IDEA / Eclipse / NetBeans

## 2. Cài đặt database
1. Mở MySQL Workbench.
2. Chạy toàn bộ file `database/hotel_management.sql`.
3. Mặc định kết nối: host `localhost`, port `3306`, database `hotel_management`, user `root`, password rỗng.

Có thể cấu hình bằng biến môi trường:
- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`

## 3. Chạy ứng dụng
Mở terminal tại thư mục dự án:
```bash
mvn clean compile
mvn exec:java
```
Hoặc mở `pom.xml` bằng IntelliJ và chạy `com.hotel.Main`.

## 4. Tài khoản demo
- admin / password
- staff / password

Không sử dụng tài khoản mẫu trong môi trường thật.

## 5. Chức năng hiện có
- Đăng nhập theo tài khoản và vai trò
- Quản lý phòng: xem danh sách; quản trị thêm phòng, sửa phòng (số phòng / loại / tầng) và đổi trạng thái (Bảo trì / Mở lại)
- Quản lý loại phòng: xem đầy đủ (kể cả đã ngưng), thêm, sửa, cập nhật giá/đêm và ngưng dùng / kích hoạt lại
- Xem và thêm/cập nhật khách hàng
- Tìm kiếm và lọc bảng tức thời cho Phòng, Khách hàng, Đặt phòng, Hóa đơn (theo từ khóa và trạng thái)
- Tạo đặt phòng một phòng / một booking, có xem trước số đêm và giá dự kiến theo chính sách giá
- Đặt nhiều phòng trong một booking (đoàn khách): chọn nhiều phòng cùng lúc, xem giá dự kiến và tổng tiền của cả đoàn
- Tách / ghép đặt phòng: tách một phòng của đơn đoàn sang đơn riêng; ghép các đơn cùng khách và cùng khoảng ngày (chỉ với đơn chưa thanh toán)
- Xem chi tiết phòng của một đơn (số phòng, loại, giá/đêm, số đêm, thành tiền từng dòng)
- Khuyến mãi / mã giảm giá (`WELCOME5`, `SUMMER10`, `GROUP15`) áp dụng khi tạo đặt phòng
- Giá theo mùa cao điểm (Tết, 30/4–1/5, hè) tự động theo ngày nhận/trả phòng
- Đặt phòng linh hoạt: cho phép chọn phòng đang có khách (`OCCUPIED`) cho khoảng ngày tương lai
- Kiểm tra trùng thời gian khi đặt phòng trong transaction (`FOR UPDATE`)
- Nhận phòng / trả phòng, tự động đồng bộ trạng thái phòng (`OCCUPIED` / `AVAILABLE`)
- Thanh toán từng phần: thu nhiều lần theo phương thức (tiền mặt / thẻ / chuyển khoản), hiển thị số tiền còn lại; hóa đơn chỉ phát hành khi đã thu đủ
- Xem danh sách hóa đơn và tổng doanh thu đã thu
- Báo cáo doanh thu dạng biểu đồ cột (vẽ bằng Java2D, không cần thư viện ngoài): doanh thu theo ngày, theo tháng, theo loại phòng; kèm tổng doanh thu, số đơn và công suất phòng
- Hủy đặt phòng (chặn hủy nếu đơn đã phát sinh thanh toán)
- Ràng buộc nghiệp vụ: không nhận phòng trong quá khứ, không thanh toán đơn đã hủy
- Thông báo lỗi thân thiện tiếng Việt khi vi phạm ràng buộc dữ liệu

## 6. Design Patterns được hiện thực
- Creational: Singleton (`DatabaseConnection`), Factory Method (`PaymentFactory`), Builder (`BookingRequest.Builder`)
- Structural: Facade (`BookingFacade`), Adapter (`BankPaymentAdapter` + `LegacyBankGateway`), Decorator (`PromoPricing` bọc một `PricingStrategy` để áp mã giảm giá)
- Behavioral: Strategy (`PricingStrategy`, `StandardPricing`, `WeekendPricing`, `SeasonPricing`), Observer (`BookingNotifier`, `BookingObserver`)

Lưu ý: nhờ OCP, việc thêm chính sách giá theo mùa (`SeasonPricing`) hay mã khuyến mãi (`PromoPricing`) chỉ cần thêm lớp mới — không sửa các lớp tính giá đang có. Các lớp Payment/Strategy/Adapter là nền tảng pattern để mở rộng nghiệp vụ thanh toán và tính giá; chức năng thanh toán trực tuyến chưa tích hợp cổng thanh toán thật.

## 7. Cấu trúc
- `model`: mô hình dữ liệu
- `repository`: hợp đồng (`*Repository`) — abstraction của tầng truy cập dữ liệu (DIP)
- `dao`: hiện thực truy cập MySQL bằng JDBC
- `service`: nghiệp vụ và kiểm tra đầu vào, chỉ phụ thuộc abstraction `*Repository` (DIP)
- `patterns`: 8 Design Patterns thuộc đủ 3 nhóm Creational/Structural/Behavioral
- `ui`: giao diện Swing (kèm `ui.components.BarChartPanel` vẽ biểu đồ)
- `config`: cấu hình kết nối

## 8. Nguyên lý SOLID
- SRP: mỗi lớp một trách nhiệm — DAO chỉ truy cập dữ liệu, Service/Facade xử lý nghiệp vụ (ví dụ `AuthService` không còn chứa SQL).
- OCP: thêm phương thức thanh toán qua `PaymentFactory`, thêm chính sách giá (mùa, khuyến mãi) qua `PricingStrategy` mà không sửa mã gọi.
- LSP: mọi hiện thực `Payment`/`PricingStrategy`/`BookingObserver` thay thế được cho abstraction mà không phá vỡ hợp đồng.
- ISP: các interface nhỏ, chuyên biệt (`Payment`, `BookingObserver`, `*Repository` — ví dụ `ReportRepository` tách riêng cho thống kê).
- DIP: Service/Facade phụ thuộc abstraction (`BookingRepository`, `PaymentRepository`, `ReportRepository`, ...) và các `Payment`/`PricingStrategy`; không phụ thuộc lớp DAO cụ thể.

