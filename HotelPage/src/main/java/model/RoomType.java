/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.math.BigDecimal;

/**
 *
 * @author Đặng Hoàng Vũ
 */
/**
 * Represents a category of hotel rooms with shared characteristics. Defines the
 * properties, amenities and pricing for a type of room.
 */
public class RoomType {
    private int Id;                 // Unique identifier for the room type
    private String name;            // Category name (e.g., "Deluxe", "Executive Suite")
    private String description;     // Detailed description of room features and amenities
    private BigDecimal pricePerNight; // Base nightly rate for this room type
    private int beds;               // Number of beds in the room
    private int capacity;           // Maximum guest capacity
    private String picture;         // URL or path to room type image (optional)

    /**
     * Default constructor creates an empty RoomType object.
     */
    public RoomType() {
    }

    /**
     * Creates a RoomType without picture reference.
     *
     * @param Id Unique identifier for the room type
     * @param name Category name (e.g., "Standard", "Family Suite")
     * @param description Detailed features and amenities
     * @param pricePerNight Base nightly rate
     * @param beds Number of beds in room
     * @param capacity Maximum number of guests allowed
     */
    public RoomType(int Id, String name, String description,
            BigDecimal pricePerNight, int beds, int capacity) {
        this.Id = Id;
        this.name = name;
        this.description = description;
        this.pricePerNight = pricePerNight;
        this.beds = beds;
        this.capacity = capacity;
    }

    /**
     * Creates a RoomType with all properties including picture reference.
     *
     * @param Id Unique identifier for the room type
     * @param name Category name
     * @param description Detailed features and amenities
     * @param pricePerNight Base nightly rate
     * @param beds Number of beds in room
     * @param capacity Maximum number of guests allowed
     * @param picture URL/path to room image
     */
    public RoomType(int Id, String name, String description,
            BigDecimal pricePerNight, int beds, int capacity,
            String picture) {
        this(Id, name, description, pricePerNight, beds, capacity);
        this.picture = picture;
    }

    /**
     * Gets the room type's unique identifier.
     *
     * @return int ID value
     */
    public int getId() {
        return Id;
    }

    /**
     * Gets the category name of the room type.
     *
     * @return String name (e.g., "Premium King")
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the detailed description of the room.
     *
     * @return String containing amenities and features
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the base nightly rate for this room type.
     *
     * @return BigDecimal representing price per night
     */
    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    /**
     * Gets the number of beds in the room.
     *
     * @return int bed count
     */
    public int getBeds() {
        return beds;
    }

    /**
     * Gets the maximum guest capacity.
     *
     * @return int maximum occupancy
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Gets the room type image reference.
     *
     * @return String URL or path to image, or null if not set
     */
    public String getPicture() {
        return picture;
    }

    /**
     * Sets the room type's unique identifier.
     *
     * @param Id The ID number to assign
     */
    public void setId(int Id) {
        this.Id = Id;
    }

    /**
     * Sets the category name of the room type.
     *
     * @param name The name to assign 
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the detailed room description.
     *
     * @param description The feature description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the base nightly rate.
     *
     * @param pricePerNight The price to set
     */
    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    /**
     * Sets the number of beds in the room.
     *
     * @param beds The bed count to set
     */
    public void setBeds(int beds) {
        this.beds = beds;
    }

    /**
     * Sets the maximum guest capacity.
     *
     * @param capacity The occupancy limit to set
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * Sets the room type image reference.
     *
     * @param picture URL or path to image
     */
    public void setPicture(String picture) {
        this.picture = picture;
    }
}
