package model;

import java.math.BigDecimal;

public enum RoomPriceBand {
    ALL(null, null),
    UNDER_1M(null, 1_000_000),
    FROM_1M(1_000_000, 5_000_000),
    FROM_5M(5_000_000, 15_000_001),
    ABOVE_15M(15_000_001, null);

    private final Integer minimum;
    private final Integer upperExclusive;

    RoomPriceBand(Integer minimum, Integer upperExclusive) {
        this.minimum = minimum;
        this.upperExclusive = upperExclusive;
    }

    public BigDecimal getMinimum() {
        return minimum == null ? null : BigDecimal.valueOf(minimum);
    }

    public BigDecimal getUpperExclusive() {
        return upperExclusive == null ? null : BigDecimal.valueOf(upperExclusive);
    }

    public static RoomPriceBand parse(String value) {
        return value == null || value.isEmpty() ? ALL : valueOf(value);
    }
}
