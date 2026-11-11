package model;

public final class BookingContact {
    private final String name;
    private final String phone;
    private final String email;
    private final String country;

    public BookingContact(String name, String phone, String email, String country) {
        this.name = required(name, 120);
        this.phone = required(phone, 32);
        this.email = required(email, 254);
        this.country = required(country, 80);
        if (!this.phone.matches("[+0-9(). -]+") || this.phone.replaceAll("[^0-9]", "").length() < 7
                || !this.email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new IllegalArgumentException("Invalid contact information");
        }
    }

    private static String required(String value, int length) {
        if (value == null || value.isBlank() || value.length() > length
                || value.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Invalid contact information");
        }
        return value.trim();
    }

    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getCountry() { return country; }
}
