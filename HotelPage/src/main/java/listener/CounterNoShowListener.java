package listener;

import db.DBContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebListener
public final class CounterNoShowListener implements ServletContextListener {

    private static final ZoneId HOTEL_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final int BATCH_SIZE = 50;
    private static final int INTERVAL_MINUTES = 10;

    // Cờ OnlineDepositPolicy=0 tách đơn quầy khỏi luồng hoàn tiền muộn hiện có.
    private static final String ELIGIBLE
            = "b.CreatedByUserID IS NOT NULL AND b.OnlineDepositPolicy=0 "
            + "AND b.Status='confirmed' AND b.ActualCheckInAt IS NULL "
            + "AND (SELECT MIN(d.CheckInDate) FROM dbo.BookingDetail d WHERE d.BookingID=b.BookingID)<=? "
            + "AND NOT EXISTS (SELECT 1 FROM dbo.CounterQrCheckout q%s WHERE q.BookingID=b.BookingID "
            + "AND (q.Active=1 OR q.State='review'))";

    private static final String FIND_DUE
            = "SELECT TOP (?) b.BookingID FROM dbo.Booking b WHERE "
            + String.format(ELIGIBLE, "") + " ORDER BY b.BookingID";
    private static final String LOCK_BOOKING
            = "SELECT BookingID FROM dbo.Booking WITH (UPDLOCK,HOLDLOCK) WHERE BookingID=?";
    private static final String CANCEL_DUE
            = "UPDATE b SET Status='cancelled', CancellationReason='no_show', CancellationDate=SYSDATETIME() "
            + "FROM dbo.Booking b WHERE b.BookingID=? AND " + String.format(ELIGIBLE, " WITH (HOLDLOCK)");

    private ScheduledExecutorService worker;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        LocalTime cutoff;
        try {
            cutoff = LocalTime.parse(System.getProperty("hotel.counterNoShow.cutoff", "00:00"));
        } catch (IllegalArgumentException | java.time.format.DateTimeParseException ex) {
            context.log("Job no-show tại quầy chưa chạy vì cấu hình giờ không hợp lệ.", ex);
            return;
        }

        worker = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "counter-no-show");
            thread.setDaemon(true);
            return thread;
        });
        worker.scheduleWithFixedDelay(() -> sweep(context, cutoff), 0, INTERVAL_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (worker != null) {
            worker.shutdownNow();
        }
    }

    // 00:00 là đầu ngày hôm sau. Mốc khác áp dụng vào ngày nhận phòng.
    static LocalDate latestDueArrival(ZonedDateTime instant, LocalTime cutoff) {
        ZonedDateTime local = instant.withZoneSameInstant(HOTEL_ZONE);
        LocalDate today = local.toLocalDate();
        return cutoff.equals(LocalTime.MIDNIGHT) || local.toLocalTime().isBefore(cutoff)
                ? today.minusDays(1) : today;
    }

    private static void sweep(ServletContext context, LocalTime cutoff) {
        LocalDate lastDue = latestDueArrival(ZonedDateTime.now(HOTEL_ZONE), cutoff);
        DBContext database = new DBContext();

        try {
            List<Integer> ids = findDue(database, lastDue);
            int cancelled = 0;
            for (int id : ids) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                try {
                    if (cancelOne(database, id, lastDue)) {
                        cancelled++;
                    }
                } catch (SQLException ex) {
                    context.log("Không thể xử lý no-show cho đơn quầy #" + id, ex);
                }
            }
            if (!ids.isEmpty()) {
                context.log("No-show tại quầy: đã hủy " + cancelled + "/" + ids.size() + " đơn đã chọn.");
            }
        } catch (SQLException | RuntimeException ex) {
            context.log("Lỗi chạy job no-show tại quầy.", ex);
        }
    }

    private static List<Integer> findDue(DBContext database, LocalDate lastDue) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        try (Connection connection = database.getConnection(); PreparedStatement statement = connection.prepareStatement(FIND_DUE)) {
            statement.setInt(1, BATCH_SIZE);
            statement.setDate(2, java.sql.Date.valueOf(lastDue));
            statement.setQueryTimeout(15);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    ids.add(rows.getInt(1));
                }
            }
        }
        return ids;
    }

    private static boolean cancelOne(DBContext database, int id, LocalDate lastDue) throws SQLException {
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                // Cùng thứ tự với xử lý check-in và QR: khóa Booking trước khi đọc lại QR.
                try (PreparedStatement lock = connection.prepareStatement(LOCK_BOOKING)) {
                    lock.setInt(1, id);
                    lock.setQueryTimeout(15);
                    try (ResultSet rows = lock.executeQuery()) {
                        if (!rows.next()) {
                            connection.commit();
                            return false;
                        }
                    }
                }
                int changed;
                try (PreparedStatement update = connection.prepareStatement(CANCEL_DUE)) {
                    update.setInt(1, id);
                    update.setDate(2, java.sql.Date.valueOf(lastDue));
                    update.setQueryTimeout(15);
                    changed = update.executeUpdate();
                }
                connection.commit();
                return changed == 1;
            } catch (SQLException | RuntimeException ex) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    ex.addSuppressed(rollbackError);
                }
                throw ex;
            }
        }
    }
}
