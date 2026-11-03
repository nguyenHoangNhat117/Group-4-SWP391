package model;

/**
 * UC18 view model. Never contains password hashes or plaintext passwords.
 */
public class AdminAccount {

    private int id;
    private String username;
    private String displayName;
    private String role;
    private String status;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private String country;
    private String city;
    private String street;
    private String version;
    private Integer customerId;
    private int bookingCount;
    private java.time.LocalDateTime createdAt;

    public int getId() {
        return id;
    }

    public void setId(int value) {
        id = value;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String value) {
        username = value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String value) {
        displayName = value;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String value) {
        role = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        status = value;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String value) {
        email = value;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String value) {
        phone = value;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String value) {
        firstName = value;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String value) {
        lastName = value;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String value) {
        country = value;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String value) {
        city = value;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String value) {
        street = value;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String value) {
        version = value;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer value) {
        customerId = value;
    }

    public int getBookingCount() {
        return bookingCount;
    }

    public void setBookingCount(int value) {
        bookingCount = value;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime value) {
        createdAt = value;
    }
}
