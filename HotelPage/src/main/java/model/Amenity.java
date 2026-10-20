package model;

public class Amenity {
    private int amenityID;
    private String name;
    private String description;

    public Amenity() {
    }

    public Amenity(int amenityID, String name, String description) {
        this.amenityID = amenityID;
        this.name = name;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}