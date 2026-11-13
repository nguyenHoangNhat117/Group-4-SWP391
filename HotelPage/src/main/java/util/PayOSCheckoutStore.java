package util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/** Stores display data for active links outside the deployed application. */
public final class PayOSCheckoutStore {
    private static final ObjectMapper JSON = new ObjectMapper();
    private final Path directory;

    public PayOSCheckoutStore(Path directory) {
        this.directory = directory;
    }

    public void save(long orderCode, JsonNode data, String returnUrl, Instant expiresAt) throws IOException {
        Files.createDirectories(directory);
        ObjectNode entry = JSON.createObjectNode();
        ObjectNode display = entry.putObject("data");
        for (String field : List.of("paymentLinkId", "checkoutUrl", "qrCode", "bin",
                "accountNumber", "accountName", "description", "amount")) {
            if (data.hasNonNull(field)) {
                display.set(field, data.get(field));
            }
        }
        entry.put("returnUrl", returnUrl);
        entry.put("expiresAt", expiresAt.getEpochSecond());
        Path temp = Files.createTempFile(directory, "checkout-", ".tmp");
        try {
            JSON.writeValue(temp.toFile(), entry);
            Files.move(temp, path(orderCode), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    public JsonNode load(long orderCode, Instant now) throws IOException {
        Path file = path(orderCode);
        JsonNode entry;
        try {
            entry = read(file);
        } catch (NoSuchFileException ex) {
            return null;
        }
        if (entry == null || entry.path("expiresAt").asLong() <= now.getEpochSecond()) {
            Files.deleteIfExists(file);
            return null;
        }
        return entry;
    }

    public void removeExpired(Instant now) throws IOException {
        try (Stream<Path> files = Files.list(directory)) {
            Iterator<Path> entries = files.filter(file -> file.getFileName().toString().matches("[0-9]+\\.json")).iterator();
            while (entries.hasNext()) {
                Path file = entries.next();
                try {
                    JsonNode entry = read(file);
                    if (entry == null || entry.path("expiresAt").asLong() <= now.getEpochSecond()) {
                        Files.deleteIfExists(file);
                    }
                } catch (NoSuchFileException ignored) {
                    // A concurrent request already removed the expired file.
                }
            }
        }
    }

    private static JsonNode read(Path file) throws IOException {
        try (InputStream input = Files.newInputStream(file)) {
            return JSON.readTree(input);
        }
    }

    private Path path(long orderCode) {
        return directory.resolve(orderCode + ".json");
    }
}
