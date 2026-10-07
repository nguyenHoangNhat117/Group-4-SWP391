package model;

import java.time.LocalDateTime;

/**
 * Represents a system user account.
 *
 * Matches the current HotelDB [User] table.
 */
public class User {

    private int id;
    private String username;
    private String password;
    private String role;
    private String displayName;

    private String accountStatus;
    private LocalDateTime createdAt;

    public User() {
    }

    /*
     * Constructor kept for compatibility
     * with existing code.
     */
    public User(
            int id,
            String username,
            String password) {

        this.id = id;
        this.username = username;
        this.password = password;
    }

    /*
     * Constructor kept for compatibility
     * with existing DAO code.
     */
    public User(
            int id,
            String username,
            String password,
            String role) {

        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    /*
     * Full constructor matching new database.
     */
    public User(
            int id,
            String username,
            String password,
            String role,
            String displayName,
            String accountStatus,
            LocalDateTime createdAt) {

        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.displayName = displayName;
        this.accountStatus = accountStatus;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(
            String username) {

        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(
            String password) {

        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(
            String role) {

        this.role = role;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(
            String displayName) {

        this.displayName = displayName;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(
            String accountStatus) {

        this.accountStatus = accountStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {

        this.createdAt = createdAt;
    }

    /*
     * Convenience helpers.
     */

    public boolean isActive() {

        return "active".equalsIgnoreCase(
                accountStatus
        );
    }

    public boolean isCustomer() {

        return "customer".equalsIgnoreCase(
                role
        );
    }

    public boolean isStaff() {

        return "staff".equalsIgnoreCase(
                role
        );
    }

    public boolean isAdmin() {

        return "admin".equalsIgnoreCase(
                role
        );
    }

    public boolean isManager() {

        return "manager".equalsIgnoreCase(
                role
        );
    }

    public boolean isITSupport() {

        return "it_support".equalsIgnoreCase(
                role
        );
    }
}