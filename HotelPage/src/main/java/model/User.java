/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author
 */
/**
 * Represents a system user with authentication credentials and access
 * privileges. This class models user accounts for the hotel management system.
 */
public class User {
    private int id;             // Unique user identifier
    private String username;   // Login username (unique)
    private String password;   // Hashed password credential
    private String role;       // Access role (e.g., "admin", "staff", "customer")
    private String displayName; // Friendly name for UI display (optional)

    /**
     * Default constructor creates an empty User object.
     */
    public User() {
    }

    /**
     * Creates a basic User with required authentication fields.
     *
     * @param id Unique user identifier
     * @param username Login username
     * @param password Hashed password
     */
    public User(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    /**
     * Creates a User with role specification.
     *
     * @param id Unique user identifier
     * @param username Login username
     * @param password Hashed password
     * @param role Access role/permission level
     */
    public User(int id, String username, String password, String role) {
        this(id, username, password);
        this.role = role;
    }

    /**
     * Gets the user's friendly display name.
     *
     * @return Display name string, or null if not set
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the unique user identifier.
     *
     * @return int ID value
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the login username.
     *
     * @return String username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the hashed password credential.
     *
     * @return String password hash
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the user's access role.
     *
     * @return String role (e.g., "admin", "staff")
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the user's friendly display name.
     *
     * @param displayName The name to show in UI
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Sets the unique user identifier.
     *
     * @param id The ID number to assign
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the login username.
     *
     * @param username Unique username credential
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Sets the password credential.
     *
     * @param password Hashed password string
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets the user's access role.
     *
     * @param role Authorization role (e.g., "manager")
     */
    public void setRole(String role) {
        this.role = role;
    }
}
