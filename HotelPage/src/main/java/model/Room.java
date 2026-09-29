/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Đặng Hoàng Vũ
 */
/**
 * Represents a hotel room with its current status and type information. This
 * class models the physical room entity in the hotel management system.
 */
public class Room {
    private int roomNumber;     // Unique identifier for the room (e.g., 101, 202)
    private String status;      // Current availability status (e.g., "Available", "Occupied")
    private RoomType roomType;  // Classification of room (Standard, Deluxe, Suite, etc.)

    /**
     * Default constructor creates an empty Room object. Used when initializing
     * a room before setting properties.
     */
    public Room() {
    }

    /**
     * Creates a fully initialized Room with all properties.
     *
     * @param roomNumber Unique numeric identifier for the room
     * @param status Current availability/occupancy status
     * @param roomType Classification and features of the room
     */
    public Room(int roomNumber, String status, RoomType roomType) {
        this.roomNumber = roomNumber;
        this.status = status;
        this.roomType = roomType;
    }

    /**
     * Gets the room's unique number identifier.
     *
     * @return int representing the room number
     */
    public int getRoomNumber() {
        return roomNumber;
    }

    /**
     * Gets the current operational status of the room.
     *
     * @return String describing room status (e.g., "Available", "Maintenance")
     */
    public String getStatus() {
        return status;
    }

    /**
     * Gets the type classification of the room.
     *
     * @return RoomType object containing category and amenities
     */
    public RoomType getRoomType() {
        return roomType;
    }

    /**
     * Sets the room's unique number identifier.
     *
     * @param roomNumber The numeric identifier to assign
     */
    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    /**
     * Updates the current operational status of the room.
     *
     * @param status The new status to set (e.g., "Occupied", "Cleaning")
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Changes the type classification of the room.
     *
     * @param roomType The new RoomType to assign
     */
    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }
}
