package listener;

import dao.CounterQrDAO;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.concurrent.*;
import util.OnlinePaymentConfig;

/** Đối soát cả khi staff đóng tab hoặc webhook chưa đến. */
@WebListener
public class CounterQrReconciler implements ServletContextListener {
    private ScheduledExecutorService worker;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        if (!OnlinePaymentConfig.payosConfigured()) return;
        worker = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "counter-qr-reconciliation");
            thread.setDaemon(true);
            return thread;
        });
        worker.scheduleWithFixedDelay(() -> {
            try {
                CounterQrDAO dao = new CounterQrDAO();
                for (long code : dao.pendingCodes()) {
                    if (Thread.currentThread().isInterrupted()) return;
                    try { dao.reconcile(code); }
                    catch (InterruptedException ex) { Thread.currentThread().interrupt(); return; }
                    catch (Exception ex) {
                        event.getServletContext().log("Counter QR awaiting reconciliation: " + code, ex);
                    }
                }
            } catch (Exception ex) {
                event.getServletContext().log("Counter QR reconciliation unavailable", ex);
            }
        }, 10, 30, TimeUnit.SECONDS);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (worker != null) worker.shutdownNow();
    }
}
