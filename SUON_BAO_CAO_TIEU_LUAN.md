# SƯỜN BÀI TIỂU LUẬN

**Đề tài: Hệ thống Quản lý đặt phòng khách sạn**
Học phần: Mẫu thiết kế cho phần mềm — HK1 2026-2027

> Sườn được xây dựng bám sát **Tiêu chí đánh giá chi tiết** của thông báo: Bố cục (1,5đ) — Hình thức (1,5đ) — Nội dung chính (7đ: Tổng quan 0,5 / Lý thuyết & Kiến trúc 1,5 / Thiết kế Patterns 2,5 / Cài đặt & Đánh giá 1,5 / Vấn đáp 1).

---

## PHẦN ĐẦU (đáp ứng tiêu chí "Bố cục" – 1,5đ)

- **Trang bìa**: tên trường/khoa, học phần, tên đề tài, GVHD, danh sách thành viên + MSSV, ngày nộp.
- **Mục lục** (tự động sinh).
- **Danh mục hình ảnh** (đánh số Hình 1, 2, ...).
- **Danh mục bảng biểu** (đánh số Bảng 1, 2, ...).
- **Danh mục từ viết tắt** (VD: SOLID, SRP, OCP, LSP, ISP, DIP, MVC, ERD, UML, JDBC, GUI, CSDL...).
- **Bảng phân công nhiệm vụ và tỷ lệ đóng góp %** của từng thành viên (bắt buộc – 0,5đ).
- **Lời mở đầu / Lời cảm ơn** (ngắn gọn).

---

## CHƯƠNG 1 — TỔNG QUAN VỀ ĐỀ TÀI VÀ BÀI TOÁN THỰC TẾ

> Tiêu chí "Tổng quan về đề tài và bài toán thực tế" – **0,5đ**

- **1.1. Bối cảnh thực tế**: nhu cầu số hóa quản lý phòng/đặt phòng của khách sạn nhỏ – vừa.
- **1.2. Mục tiêu của hệ thống**: quản lý phòng, khách hàng, đặt phòng, nhận/trả phòng, thanh toán, hóa đơn.
- **1.3. Phạm vi bài toán**: giới hạn chức năng trong/ngoài hệ thống.
- **1.4. Đối tượng người dùng**: Quản trị viên (ADMIN), Nhân viên (STAFF).
- **1.5. Yêu cầu chức năng** (liệt kê dạng bảng: mã FR-01...):
  - Quản lý phòng & loại phòng; Quản lý khách hàng; Đặt phòng; Nhận/Trả phòng; Thanh toán & hóa đơn; Đăng nhập; Thống kê doanh thu.
- **1.6. Yêu cầu phi chức năng**: hiệu năng, bảo mật (BCrypt), khả năng bảo trì/mở rộng, tính nhất quán dữ liệu (transaction).
- **1.7. Sơ đồ Use Case tổng quát** (Hình) + mô tả actor.
- **1.8. Đặc tả các ca sử dụng cốt lõi** (bảng mỗi UC: tiền điều kiện, luồng chính, luồng phụ, hậu điều kiện):
  - UC Đặt phòng, UC Nhận phòng, UC Trả phòng, UC Thanh toán, UC Hủy đặt phòng.

---

## CHƯƠNG 2 — CƠ SỞ LÝ THUYẾT VÀ KIẾN TRÚC HỆ THỐNG

> Tiêu chí "Cơ sở lý thuyết và Kiến trúc hệ thống" – **1,5đ**

### 2.1. Cơ sở lý thuyết về Design Patterns
- **2.1.1. Nhóm Creational**: khái niệm, vai trò, ví dụ (Singleton, Factory Method, Builder, Abstract Factory, Prototype).
- **2.1.2. Nhóm Structural**: (Adapter, Facade, Decorator, Composite, Proxy...).
- **2.1.3. Nhóm Behavioral**: (Strategy, Observer, Command, State...).
- Bảng so sánh mục đích – trường hợp sử dụng của từng nhóm.

### 2.2. Năm nguyên lý SOLID
- Trình bày định nghĩa + ví dụ vi phạm/tuân thủ cho **SRP, OCP, LSP, ISP, DIP**.

### 2.3. Thiết kế kiến trúc tổng thể phần mềm
- **Mô hình 3 tầng / MVC**: `ui (View)` → `service + patterns.facade (Controller)` → `dao (Data Access)` → MySQL.
- Sơ đồ kiến trúc (Hình) + giải thích luồng phụ thuộc một chiều.

### 2.4. Thiết kế mô hình thực thể dữ liệu
- **Sơ đồ ERD** (Hình) + **lược đồ quan hệ** và giải thích các bảng: `users`, `customers`, `room_types`, `rooms`, `bookings`, `booking_details`, `payments`, `invoices`.

---

## CHƯƠNG 3 — PHÂN TÍCH VÀ THIẾT KẾ CHI TIẾT CÁC MẪU THIẾT KẾ ÁP DỤNG

> Tiêu chí trọng tâm – **2,5đ**. Mỗi pattern trình bày theo cùng một khung để dễ chấm điểm.

> **Khung chung cho mỗi mục 3.x:**
> 1. Mục đích & lý do chọn pattern.
> 2. **Sơ đồ lớp UML (Class Diagram)**.
> 3. **Sơ đồ tuần tự (Sequence Diagram)**.
> 4. Ánh xạ pattern vào mã nguồn (đoạn code minh họa).
> 5. **So sánh ưu/nhược** so với thiết kế thông thường (không dùng pattern).
> 6. **Đánh giá mức tuân thủ SOLID** (map tới nguyên lý nào).

- **3.1. Creational**
  - 3.1.1. **Singleton** — `DatabaseConnection` (một điểm kết nối dùng chung).
  - 3.1.2. **Factory Method** — `PaymentFactory` → `CashPayment`, `CardPayment`, `BankPaymentAdapter`.
  - 3.1.3. **Builder** — `BookingRequest.Builder` (dựng yêu cầu đặt phòng + kiểm tra hợp lệ).
- **3.2. Structural**
  - 3.2.1. **Facade** — `BookingFacade` (thống nhất chuỗi nghiệp vụ đặt/check-in/check-out/hủy/thanh toán).
  - 3.2.2. **Adapter** — `BankPaymentAdapter` + `LegacyBankGateway` (chuyển cổng ngân hàng cũ về interface `Payment`).
- **3.3. Behavioral**
  - 3.3.1. **Strategy** — `PricingStrategy` (`StandardPricing`, `WeekendPricing`) + `PricingSelector`.
  - 3.3.2. **Observer** — `BookingNotifier` + `BookingObserver` (thông báo khi trạng thái đặt phòng đổi).

- **3.4. Tổng hợp & đối chiếu**: bảng ánh xạ `Pattern ↔ Lớp ↔ Nhóm ↔ Nguyên lý SOLID được minh họa`.

---

## CHƯƠNG 4 — CÀI ĐẶT, THỬ NGHIỆM VÀ ĐÁNH GIÁ KẾT QUẢ

> Tiêu chí "Cài đặt, Thử nghiệm và Đánh giá kết quả" – **1,5đ**

- **4.1. Môi trường & công nghệ**: Java 17, Swing + FlatLaf, JDBC, MySQL 8, Maven, BCrypt.
- **4.2. Cấu trúc mã nguồn**: cây thư mục (`model`, `dao`, `service`, `patterns`, `config`, `ui`) + mô tả.
- **4.3. Kịch bản demo / Test Cases** (bảng: mã TC, mô tả, dữ liệu vào, kết quả mong đợi, pattern liên quan):
  - TC01 Đăng nhập đúng/sai;
  - TC02 Đặt phòng (kiểm tra trùng thời gian); TC03 Nhận phòng → đồng bộ trạng thái `OCCUPIED`;
  - TC04 Trả phòng → `AVAILABLE`; TC05 Thanh toán 3 phương thức (Factory + Adapter);
  - TC06 Giá cuối tuần +10% (Strategy); TC07 Hủy đơn đã thanh toán (bị chặn);
  - TC08 Nhận phòng ngày quá khứ (bị chặn); TC09 Thông báo lỗi ràng buộc dữ liệu.
- **4.4. Kết quả demo**: ảnh chụp màn hình từng chức năng + minh chứng pattern hoạt động.
- **4.5. Đánh giá hiệu năng**: thời gian phản hồi, xử lý transaction `FOR UPDATE` chống trùng đặt phòng.
- **4.6. Đánh giá khả năng bảo trì**: mức tách rời giữa các tầng, khả năng thêm mới không sửa code cũ (OCP).
- **4.7. Khó khăn & hạn chế**.

---

## CHƯƠNG 5 — KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN

- **5.1. Kết quả đạt được**: bám mục tiêu Chương 1.
- **5.2. Tự đánh giá theo tiêu chí**: pattern đã áp dụng đủ 3 nhóm; SOLID đã tuân thủ.
- **5.3. Hướng phát triển**: tích hợp cổng thanh toán thật, báo cáo thống kê nâng cao, xuất PDF hóa đơn...

---

## PHẦN CUỐI

- **Tài liệu tham khảo** — trình bày theo chuẩn **APA hoặc IEEE** (0,5đ).
- **Phụ lục**: mã nguồn tiêu biểu, biểu đồ UML bổ sung.

---

## BẢNG ĐỐI CHIẾU SƯỜN ↔ TIÊU CHÍ CHẤM ĐIỂM

| Tiêu chí | Điểm | Sườn tương ứng |
|----------|------|----------------|
| Bố cục (trang bìa, mục lục, danh mục, phân công) | 1,5 | Phần đầu |
| Hình thức (trình bày, sơ đồ/bảng, trích dẫn) | 1,5 | Toàn bài + Phần cuối |
| Tổng quan đề tài & bài toán | 0,5 | Chương 1 |
| Cơ sở lý thuyết & Kiến trúc hệ thống | 1,5 | Chương 2 |
| Phân tích & Thiết kế chi tiết các Pattern | 2,5 | Chương 3 |
| Cài đặt, Thử nghiệm & Đánh giá kết quả | 1,5 | Chương 4 |
| Vấn đáp và hiểu biết | 1,0 | Chương 3, 4, 5 |
| **Tổng** | **10** | |
