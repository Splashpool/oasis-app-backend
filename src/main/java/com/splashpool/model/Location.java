package com.splashpool.model;

public class Location {

    private String locationName;
    private long locationId;

    public Location(String locationName, long locationId) {
        this.locationName = locationName;
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public long getLocationId() {
        return locationId;
    }
}
