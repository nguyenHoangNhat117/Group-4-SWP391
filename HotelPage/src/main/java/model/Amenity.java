package model;

public class Amenity {
    private int amenityID;
    private String name;
    private boolean active = true;

    public Amenity() {
    }

    public Amenity(int amenityID, String name) {
        this.amenityID = amenityID;
        this.name = name;
    }

    public int getAmenityID() {
        return amenityID;
    }

    public void setAmenityID(int amenityID) {
        this.amenityID = amenityID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
