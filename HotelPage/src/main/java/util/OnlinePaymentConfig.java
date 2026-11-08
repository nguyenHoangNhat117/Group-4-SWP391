package util;

/** Reads online payment credentials from the process environment. */
public final class OnlinePaymentConfig {
    private OnlinePaymentConfig() { }
    private static String value(String key, String fallback) {
        String v = System.getenv(key);
        return v == null || v.trim().isEmpty() ? fallback : v.trim();
    }
    public static String payosClientId() { return value("PAYOS_CLIENT_ID", ""); }
    public static String payosApiKey() { return value("PAYOS_API_KEY", ""); }
    public static String payosChecksumKey() { return value("PAYOS_CHECKSUM_KEY", ""); }
    public static String payosBaseUrl() { return value("PAYOS_BASE_URL", "https://api-merchant.payos.vn"); }
    public static String publicBaseUrl() { return value("HOTEL_PUBLIC_BASE_URL", "").replaceAll("/$", ""); }
    public static boolean payosConfigured() { return !payosClientId().isEmpty() && !payosApiKey().isEmpty() && !payosChecksumKey().isEmpty(); }
    public static boolean callbacksConfigured() { return !publicBaseUrl().isEmpty() && publicBaseUrl().startsWith("https://"); }
}
