# Group 4 SWP391

## 1. Project Structure

```text
HotelPage/
│
├── nb-configuration.xml
├── pom.xml
│
└── src/
    ├── main/
    │   ├── java/
    │   │   ├── controller/
    │   │   ├── dao/
    │   │   ├── db/
    │   │   ├── filter/
    │   │   ├── model/
    │   │   └── util/
    │   │
    │   └── webapp/
    │       ├── assets/
    │       │   ├── css/
    │       │   ├── icons/
    │       │   └── img/
    │       ├── META-INF/
    │       ├── script/
    │       └── WEB-INF/
    │           ├── booking/
    │           ├── customer/
    │           ├── details/
    │           ├── discount/
    │           ├── error/
    │           ├── history/
    │           ├── include/
    │           ├── login/
    │           ├── pay/
    │           ├── profile/
    │           ├── register/
    │           ├── reports/
    │           ├── room/
    │           ├── roomType/
    │           └── web.xml
    │
    └── test/
        └── java/
```

## 2. Database

```text
NewDB.sql
```

Dữ liệu mẫu liên kết: [`HotelDB.seed.sql`](HotelDB.seed.sql). Xem tài khoản, tình huống và cách chạy tại [`DATABASE_SAMPLE.md`](DATABASE_SAMPLE.md).

Với HotelDB đã cài từ bản cũ, chạy [migration giới hạn phòng](HotelPage/src/main/resources/db/room_inventory_settings.sql) trước khi triển khai. Quản trị viên cập nhật giới hạn tại **Quản lý phòng → Chỉnh giới hạn**.

Với HotelDB chưa có trạng thái tiện nghi, chạy [migration tiện nghi](HotelPage/src/main/resources/db/amenity_status.sql) trước khi triển khai trang quản lý tiện nghi.

Với HotelDB đã cài, sau khi triển khai bản bỏ mô tả tiện nghi, chọn database của ứng dụng trong SSMS rồi chạy [migration xóa cột mô tả tiện nghi](HotelPage/src/main/resources/db/drop_amenity_description.sql).

Với HotelDB đang dùng, chạy [migration giữ phòng online](HotelPage/src/main/resources/db/online_room_reservations.sql) trước khi triển khai bản có giữ phòng theo số phòng và lưu người lớn, trẻ em theo từng phòng.

Với HotelDB đã cài, chọn database đó trong SSMS rồi chạy [migration xóa bảng thông số phòng](HotelPage/src/main/resources/db/drop_room_type_specifications.sql). Bản cài mới từ `NewDB.sql` đã bỏ bảng này.

## 3. Main Components

- Controller: Handle HTTP requests and application flow
- DAO: Handle database access
- Model: Represent application data
- Filter: Handle authentication
- JSP Views: Provide web pages
- Assets: CSS, JavaScript, icons and images
