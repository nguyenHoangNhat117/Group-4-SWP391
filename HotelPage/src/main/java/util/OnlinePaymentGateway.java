package util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Map;
import java.util.TreeMap;

/** HTTP and signature utilities for payOS and MoMo. Contains no database logic. */
public final class OnlinePaymentGateway {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();
    private OnlinePaymentGateway() { }

    public static String createPayOSLink(long paymentId, long orderCode, long amount, String baseUrl) throws Exception {
        if (!OnlinePaymentConfig.payosConfigured()) throw new IllegalStateException("payOS credentials are not configured");
        String returnUrl = baseUrl + "/payment/return?gateway=payos&paymentId=" + paymentId;
        String cancelUrl = baseUrl + "/payment/return?gateway=payos&cancel=1&paymentId=" + paymentId;
        String description = ("HOTEL " + paymentId);
        String signatureData = "amount=" + amount + "&cancelUrl=" + cancelUrl + "&description=" + description
                + "&orderCode=" + orderCode + "&returnUrl=" + returnUrl;
        ObjectNode body = JSON.createObjectNode();
        body.put("orderCode", orderCode);
        body.put("amount", amount);
        body.put("description", description);
        body.put("cancelUrl", cancelUrl);
        body.put("returnUrl", returnUrl);
        body.put("signature", hmacSha256(OnlinePaymentConfig.payosChecksumKey(), signatureData));
        HttpRequest req = HttpRequest.newBuilder(URI.create(OnlinePaymentConfig.payosBaseUrl() + "/v2/payment-requests"))
                .timeout(Duration.ofSeconds(35)).header("Content-Type", "application/json")
                .header("x-client-id", OnlinePaymentConfig.payosClientId()).header("x-api-key", OnlinePaymentConfig.payosApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(body))).build();
        HttpResponse<String> response = HTTP.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonNode root = JSON.readTree(response.body());
        String code = root.path("code").asText();
        if (response.statusCode() < 200 || response.statusCode() >= 300 || !"00".equals(code)) {
            throw new IllegalStateException("payOS create link failed: HTTP " + response.statusCode()
                    + ", code " + code + " / " + root.path("desc").asText());
        }
        String checkout = root.path("data").path("checkoutUrl").asText();
        if (checkout.isEmpty()) throw new IllegalStateException("payOS did not return checkoutUrl");
        return checkout;
    }

    public static boolean verifyPayOSWebhook(JsonNode root) throws Exception {
        if (!OnlinePaymentConfig.payosConfigured()) return false;
        String received = root.path("signature").asText("");
        JsonNode data = root.path("data");
        if (received.isEmpty() || !data.isObject()) return false;
        TreeMap<String,String> sorted = new TreeMap<>();
        data.fields().forEachRemaining(e -> sorted.put(e.getKey(), e.getValue().isNull() ? "" : e.getValue().asText()));
        StringBuilder canonical = new StringBuilder();
        for (Map.Entry<String,String> e : sorted.entrySet()) {
            if (canonical.length() > 0) canonical.append('&');
            canonical.append(e.getKey()).append('=').append(e.getValue());
        }
        return constantTimeEquals(received, hmacSha256(OnlinePaymentConfig.payosChecksumKey(), canonical.toString()));
    }

    public static boolean isPayOSOrderPaid(long orderCode, long amount)
            throws java.io.IOException, InterruptedException {
        if (!OnlinePaymentConfig.payosConfigured() || orderCode <= 0 || amount <= 0) {
            return false;
        }
        HttpRequest request = HttpRequest.newBuilder(URI.create(
                OnlinePaymentConfig.payosBaseUrl() + "/v2/payment-requests/" + orderCode))
                .timeout(Duration.ofSeconds(5))
                .header("x-client-id", OnlinePaymentConfig.payosClientId())
                .header("x-api-key", OnlinePaymentConfig.payosApiKey())
                .GET().build();
        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) {
            throw new java.io.IOException("payOS status lookup failed: HTTP " + response.statusCode());
        }
        JsonNode root = JSON.readTree(response.body());
        JsonNode data = root.path("data");
        return "00".equals(root.path("code").asText())
                && data.path("orderCode").asLong(-1) == orderCode
                && "PAID".equals(data.path("status").asText())
                && data.path("amount").asLong(-1) == amount
                && data.path("amountPaid").asLong(-1) == amount
                && data.path("amountRemaining").asLong(-1) == 0;
    }

    public static JsonNode parse(String json) throws Exception { return JSON.readTree(json); }
    public static String hmacSha256(String key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder out = new StringBuilder();
        for (byte b : bytes) out.append(String.format("%02x", b & 0xff));
        return out.toString();
    }
    private static boolean constantTimeEquals(String a, String b) {
        return a != null && b != null && MessageDigest.isEqual(a.toLowerCase().getBytes(StandardCharsets.UTF_8), b.toLowerCase().getBytes(StandardCharsets.UTF_8));
    }
}
