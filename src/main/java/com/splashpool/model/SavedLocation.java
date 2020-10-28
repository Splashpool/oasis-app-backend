package com.splashpool.model;

public class SavedLocation {

    private long locationId;
    private String uuid;

    public SavedLocation(long locationId, String uuid) {
        this.locationId     = locationId;
        this.uuid           = uuid;
    }

    public long getLocationId() {
        return locationId;
    }
    public String getUuid() {
        return uuid;
    }
}
