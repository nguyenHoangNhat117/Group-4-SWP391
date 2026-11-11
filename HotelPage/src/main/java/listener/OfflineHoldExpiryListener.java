package listener;

import dao.OfflineBookingDAO;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.concurrent.*;

@WebListener
public class OfflineHoldExpiryListener
        implements ServletContextListener {

    private ScheduledExecutorService executor;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        executor = Executors.newSingleThreadScheduledExecutor(
                task -> {
                    Thread thread = new Thread(
                            task, "uc09-expiry"
                    );
                    thread.setDaemon(true);
                    return thread;
                }
        );

        executor.scheduleWithFixedDelay(
                () -> {
                    try {
                        new OfflineBookingDAO().expireHolds();
                    } catch (Exception e) {
                        event.getServletContext().log(
                                "UC09 expiry failed; check migration",
                                e
                        );
                    }
                },
                0,
                15,
                TimeUnit.SECONDS
        );
        executor.scheduleWithFixedDelay(() -> {
            try {
                dao.BookingDAO bookings = new dao.BookingDAO();
                bookings.expireOnlineNoShows();
                bookings.refundLateOnlinePayments();
            } catch (Exception e) {
                event.getServletContext().log("Online cancellation reconciliation failed", e);
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}
