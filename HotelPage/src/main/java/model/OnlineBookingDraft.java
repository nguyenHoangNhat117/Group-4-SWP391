package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class OnlineBookingDraft {
    public static final String SESSION_ATTRIBUTE = "onlineBookingDraft";
    public static final String PATH = "/booking-confirmation";

    private final String token = UUID.randomUUID().toString();
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final int adults;
    private final int children;
    private final List<RoomLine> rooms;
    private int customerId;
    private Map<String, String> contactFields = Map.of();
    private BookingContact contact;

    public OnlineBookingDraft(LocalDate checkIn, LocalDate checkOut, List<RoomLine> rooms) {
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.rooms = List.copyOf(rooms);
        this.adults = this.rooms.stream().mapToInt(RoomLine::getAdults).sum();
        this.children = this.rooms.stream().mapToInt(RoomLine::getChildren).sum();
    }

    public String getToken() { return token; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public int getAdults() { return adults; }
    public int getChildren() { return children; }
    public List<RoomLine> getRooms() { return rooms; }
    public long getNights() { return ChronoUnit.DAYS.between(checkIn, checkOut); }
    public String getPath() { return PATH + "?draft=" + token; }
    public Map<String, String> getContactFields() { return contactFields; }
    public BookingContact getContact() { return contact; }

    public void saveContact(Map<String, String> fields, BookingContact validated) {
        contactFields = Map.copyOf(fields);
        contact = validated;
    }

    public void copyContactFrom(OnlineBookingDraft previous) {
        customerId = previous.customerId;
        saveContact(previous.contactFields, previous.contact);
    }

    public BigDecimal getTotal() {
        return rooms.stream().map(room -> room.nightly.multiply(BigDecimal.valueOf(getNights() * room.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean claim(int id) {
        if (customerId == 0) {
            customerId = id;
        }
        return customerId == id;
    }

    public static final class RoomParty {
        private final int adults;
        private final int children;

        public RoomParty(int adults, int children) {
            this.adults = adults;
            this.children = children;
        }

        public int getAdults() { return adults; }
        public int getChildren() { return children; }
    }

    public static final class RoomLine {
        private final int typeId;
        private final String name;
        private final BigDecimal nightly;
        private final List<RoomParty> parties;

        public RoomLine(RoomType type, List<RoomParty> parties) {
            this.typeId = type.getId();
            this.name = type.getName();
            this.nightly = type.getPricePerNight();
            this.parties = List.copyOf(parties);
        }

        public int getTypeId() { return typeId; }
        public String getName() { return name; }
        public int getQuantity() { return parties.size(); }
        public List<RoomParty> getParties() { return parties; }
        public int getAdults() { return parties.stream().mapToInt(party -> party.adults).sum(); }
        public int getChildren() { return parties.stream().mapToInt(party -> party.children).sum(); }
        public int getMaxAdultsPerRoom() { return parties.stream().mapToInt(party -> party.adults).max().orElse(0); }
        public int getMaxChildrenPerRoom() { return parties.stream().mapToInt(party -> party.children).max().orElse(0); }
        public BigDecimal getNightly() { return nightly; }
    }
}
