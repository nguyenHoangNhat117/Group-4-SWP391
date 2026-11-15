package util;

public final class AuthUcPolicy {

    // Giới hạn số lượng lock, không tạo một lock mới cho mỗi request.
    private static final Object[] LOGIN_LOCKS = new Object[256];

    static {
        for (int i = 0; i < LOGIN_LOCKS.length; i++) {
            LOGIN_LOCKS[i] = new Object();
        }
    }

    private AuthUcPolicy() {
    }

    public static Object loginLock(String key) {
        int index = Math.floorMod(key.hashCode(), LOGIN_LOCKS.length);
        return LOGIN_LOCKS[index];
    }

    /**
     * Chính sách cho email thông thường trên các form xác thực. Không hỗ trợ
     * email quoted hoặc tên miền Unicode trực tiếp. Không kiểm tra hộp thư có
     * tồn tại hay không.
     */
    public static boolean validEmail(String email) {
        if (email == null || email.length() > 100) {
            return false;
        }

        int at = email.indexOf('@');

        if (at <= 0 || at != email.lastIndexOf('@')) {
            return false;
        }

        String local = email.substring(0, at);
        String domain = email.substring(at + 1);

        if (local.length() > 64
                || local.startsWith(".")
                || local.endsWith(".")
                || local.contains("..")
                || !local.matches("[A-Za-z0-9+_.-]+")) {
            return false;
        }

        String[] labels = domain.split("\\.", -1);

        if (labels.length < 2) {
            return false;
        }

        for (String label : labels) {
            if (label.isEmpty()
                    || label.length() > 63
                    || label.startsWith("-")
                    || label.endsWith("-")
                    || !label.matches("[A-Za-z0-9-]+")) {
                return false;
            }
        }

        return true;
    }
}
