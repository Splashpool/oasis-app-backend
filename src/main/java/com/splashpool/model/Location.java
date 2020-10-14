package com.splashpool.model;

public class Location {

    private String locationName;
    private String locationId;

    public Location(String locationName, String locationId) {
        this.locationName = locationName;
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getLocationId() {
        return locationId;
    }
}
