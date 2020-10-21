package com.splashpool.model;

public class Location {

    private long locationId;
    private String locationName;
    private String address1;
    private String address2;
    private String city;
    private String postCode;
    private String country;
    private Float  longitude;
    private Float  latitude;
    private String adminOrg;

    public Location(Long locationId, String locationName,
                    String address1, String address2, String city, String postCode, String country,
                    Float longitude, Float latitude, String adminOrg) {
        this.locationId   = locationId;
        this.locationName = locationName;
        this.address1     = address1;
        this.address2     = address2;
        this.city         = city;
        this.postCode     = postCode;
        this.country      = country;
        this.longitude    = longitude;
        this.latitude     = latitude;
        this.adminOrg     = adminOrg;
    }

    public Long getLocationId() {
        return locationId;
    }
    public String getLocationName() {
        return locationName;
    }
    public String getAddress1() { return address1; }
    public String getAddress2() { return address2; }
    public String getCity() { return city; }
    public String getPostCode() { return postCode; }
    public String getCountry() { return country; }
    public Float  getLongitude() { return longitude; }
    public Float  getLatitude() { return latitude; }
    public String getAdminOrg() { return adminOrg; }
}
