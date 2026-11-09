-- Run against the existing hotel database before deploying the updated application.
-- Existing amounts must already be in whole VND. Fractional balances are reported
-- so they can be reconciled before the schema is changed.
SET XACT_ABORT ON;
BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID(N'dbo.Booking', N'U') IS NULL OR OBJECT_ID(N'dbo.Payment', N'U') IS NULL
        THROW 50000, 'Select the hotel database before running this migration.', 1;

    DECLARE @currency TABLE (TableName sysname, ColumnName sysname);
    INSERT @currency VALUES
        (N'RoomType', N'PricePerNight'), (N'Discount', N'MinimumAmount'),
        (N'Discount', N'MaximumDiscount'), (N'Booking', N'TotalPrice'),
        (N'BookingDetail', N'PricePerNight'), (N'Payment', N'Amount'),
        (N'Payment', N'DiscountAmount'), (N'Wallet', N'Balance'),
        (N'WalletTransaction', N'Amount');

    DECLARE @table sysname, @column sysname, @nullable bit, @objectId int, @columnId int;
    DECLARE @sql nvarchar(max), @target nvarchar(512), @quotedColumn nvarchar(258), @error nvarchar(2048);
    DECLARE currency_columns CURSOR LOCAL FAST_FORWARD FOR
        SELECT t.name, c.name, c.is_nullable, c.object_id, c.column_id
        FROM @currency AS currency
        JOIN sys.tables AS t ON t.name = currency.TableName AND t.schema_id = SCHEMA_ID(N'dbo')
        JOIN sys.columns AS c ON c.object_id = t.object_id AND c.name = currency.ColumnName
        WHERE c.scale <> 0;

    OPEN currency_columns;
    FETCH NEXT FROM currency_columns INTO @table, @column, @nullable, @objectId, @columnId;
    WHILE @@FETCH_STATUS = 0
    BEGIN
        SET @target = N'dbo.' + QUOTENAME(@table);
        SET @quotedColumn = QUOTENAME(@column);
        SET @error = @table + N'.' + @column + N' contains fractional amounts. Reconcile these amounts before migrating.';
        SET @sql = N'IF EXISTS (SELECT 1 FROM ' + @target + N' WITH (TABLOCKX,HOLDLOCK) WHERE '
            + @quotedColumn + N' <> ROUND(' + @quotedColumn + N', 0, 1)) THROW 50001, @message, 1;';
        EXEC sys.sp_executesql @sql, N'@message nvarchar(2048)', @message = @error;

        DECLARE @drop nvarchar(max), @restore nvarchar(max);
        SET @drop = N'';
        SET @restore = N'';
        SELECT @drop = @drop + N'ALTER TABLE ' + @target + N' DROP CONSTRAINT ' + QUOTENAME(cc.name) + N';',
            @restore = @restore + N'ALTER TABLE ' + @target
                + CASE WHEN cc.is_not_trusted = 1 THEN N' WITH NOCHECK' ELSE N' WITH CHECK' END
                + N' ADD CONSTRAINT ' + QUOTENAME(cc.name) + N' CHECK '
                + CASE WHEN cc.is_not_for_replication = 1 THEN N'NOT FOR REPLICATION ' ELSE N'' END
                + cc.definition + N';'
                + CASE WHEN cc.is_disabled = 1 THEN N'ALTER TABLE ' + @target
                    + N' NOCHECK CONSTRAINT ' + QUOTENAME(cc.name) + N';' ELSE N'' END
        FROM sys.check_constraints AS cc
        WHERE cc.parent_object_id = @objectId
            AND (cc.parent_column_id = @columnId
                OR EXISTS (
                    SELECT 1 FROM sys.sql_expression_dependencies AS d
                    WHERE d.referencing_id = cc.object_id AND d.referenced_id = @objectId
                        AND d.referenced_minor_id = @columnId
                ));

        SELECT @drop = @drop + N'ALTER TABLE ' + @target + N' DROP CONSTRAINT ' + QUOTENAME(dc.name) + N';',
            @restore = @restore + N'ALTER TABLE ' + @target + N' ADD CONSTRAINT ' + QUOTENAME(dc.name)
                + N' DEFAULT ' + dc.definition + N' FOR ' + @quotedColumn + N';'
        FROM sys.default_constraints AS dc
        WHERE dc.parent_object_id = @objectId AND dc.parent_column_id = @columnId;

        SET @sql = @drop + N'ALTER TABLE ' + @target + N' ALTER COLUMN ' + @quotedColumn
            + N' DECIMAL(18,0)' + CASE WHEN @nullable = 1 THEN N' NULL;' ELSE N' NOT NULL;' END + @restore;
        EXEC sys.sp_executesql @sql;

        FETCH NEXT FROM currency_columns INTO @table, @column, @nullable, @objectId, @columnId;
    END;
    CLOSE currency_columns;
    DEALLOCATE currency_columns;

    IF COL_LENGTH(N'dbo.Booking', N'ContactName') IS NULL
        ALTER TABLE dbo.Booking ADD ContactName NVARCHAR(120) NULL;
    IF COL_LENGTH(N'dbo.Booking', N'ContactPhone') IS NULL
        ALTER TABLE dbo.Booking ADD ContactPhone NVARCHAR(32) NULL;
    IF COL_LENGTH(N'dbo.Booking', N'ContactEmail') IS NULL
        ALTER TABLE dbo.Booking ADD ContactEmail NVARCHAR(254) NULL;
    IF COL_LENGTH(N'dbo.Booking', N'ContactCountry') IS NULL
        ALTER TABLE dbo.Booking ADD ContactCountry NVARCHAR(80) NULL;
    IF COL_LENGTH(N'dbo.Payment', N'DiscountAmount') IS NULL
        ALTER TABLE dbo.Payment ADD DiscountAmount DECIMAL(18,0) NULL;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
