-- Chọn cơ sở dữ liệu của ứng dụng (mặc định HotelDB) trước khi chạy.
IF COL_LENGTH(N'dbo.Amenity', N'Description') IS NOT NULL
    ALTER TABLE dbo.Amenity DROP COLUMN [Description];
