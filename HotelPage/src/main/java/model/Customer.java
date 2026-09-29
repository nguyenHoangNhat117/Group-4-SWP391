/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Trần Nguyễn
 */
/**
 * Represents a customer entity with personal information, contact details,
 * address, and associated user account. This class models customer data for
 * hotel management systems.
 */
public class Customer {
    private int customerID;      // Unique identifier for the customer
    private String firstName;   // Customer's first name
    private String lastName;    // Customer's last name
    private String email;       // Customer's email address
    private String phone;       // Customer's phone number
    private String country;     // Customer's country of residence
    private String city;        // Customer's city of residence
    private String street;      // Customer's street address
    private String avatar;      // URL or path to customer's profile image
    private User user;          // Associated user account details

    /**
     * Gets the customer's avatar/image URL.
     *
     * @return String representing the avatar path/URL
     */
    public String getAvatar() {
        return avatar;
    }

    /**
     * Sets the customer's avatar/image URL.
     *
     * @param avatar The path/URL to set as the customer's avatar
     */
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    /**
     * Default constructor creates an empty Customer object.
     */
    public Customer() {
    }

    /**
     * Creates a Customer with basic information (without avatar).
     *
     * @param customerID Unique identifier for the customer
     * @param firstName Customer's first name
     * @param lastName Customer's last name
     * @param email Customer's email address
     * @param phone Customer's phone number
     * @param country Customer's country
     * @param city Customer's city
     * @param street Customer's street address
     * @param user Associated User account object
     */
    public Customer(int customerID, String firstName, String lastName, String email,
            String phone, String country, String city, String street, User user) {
        this.customerID = customerID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.country = country;
        this.city = city;
        this.street = street;
        this.user = user;
    }

    /**
     * Creates a Customer with complete information (including avatar).
     *
     * @param customerID Unique identifier for the customer
     * @param firstName Customer's first name
     * @param lastName Customer's last name
     * @param email Customer's email address
     * @param phone Customer's phone number
     * @param country Customer's country
     * @param city Customer's city
     * @param street Customer's street address
     * @param avatar Path/URL to customer's profile image
     * @param user Associated User account object
     */
    public Customer(int customerID, String firstName, String lastName, String email,
            String phone, String country, String city, String street,
            String avatar, User user) {
        this.customerID = customerID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.country = country;
        this.city = city;
        this.street = street;
        this.avatar = avatar;
        this.user = user;
    }

    /**
     * Gets the customer's unique ID.
     *
     * @return Customer ID number
     */
    public int getCustomerID() {
        return customerID;
    }

    /**
     * Gets the customer's first name.
     *
     * @return First name as String
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Gets the customer's last name.
     *
     * @return Last name as String
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Gets the customer's email address.
     *
     * @return Email address as String
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gets the customer's phone number.
     *
     * @return Phone number as String
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Gets the customer's country.
     *
     * @return Country name as String
     */
    public String getCountry() {
        return country;
    }

    /**
     * Gets the customer's city.
     *
     * @return City name as String
     */
    public String getCity() {
        return city;
    }

    /**
     * Gets the customer's street address.
     *
     * @return Street address as String
     */
    public String getStreet() {
        return street;
    }

    /**
     * Gets the associated User account.
     *
     * @return User object containing login credentials
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the customer's unique ID.
     *
     * @param customerID The ID number to set
     */
    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    /**
     * Sets the customer's first name.
     *
     * @param firstName The first name to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Sets the customer's last name.
     *
     * @param lastName The last name to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Sets the customer's email address.
     *
     * @param email The email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sets the customer's phone number.
     *
     * @param phone The phone number to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Sets the customer's country.
     *
     * @param country The country to set
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Sets the customer's city.
     *
     * @param city The city to set
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Sets the customer's street address.
     *
     * @param street The street address to set
     */
    public void setStreet(String street) {
        this.street = street;
    }

    /**
     * Sets the associated User account.
     *
     * @param user The User object to associate
     */
    public void setUser(User user) {
        this.user = user;
    }
}
