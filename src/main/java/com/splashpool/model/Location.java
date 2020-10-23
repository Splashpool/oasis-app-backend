package com.splashpool.model;

public class Location {

    private long locationId;
    private String locationName;
    private String address1;
    private String address2;
    private String city;
    private String postCode;
    private String country;
    private double longitude;
    private double latitude;
    private String adminOrg;
    private boolean water;
    private boolean drinkable;
    private boolean treatment;
    private boolean unknown;
    private boolean largeWaterFacility;
    private boolean maleToilets;
    private boolean femaleToilets;
    private boolean largeToiletFacility;
    private boolean disabledAccess;
    private boolean chargeForUse;
    private String  openingHours;
    private boolean hasIssue;


    public Location(Long locationId, String locationName,
                    String address1, String address2, String city, String postCode, String country,
                    double longitude, double latitude, String adminOrg, boolean water, boolean drinkable,
                    boolean treatment, boolean unknown, boolean largeWaterFacility, boolean maleToilets,
                    boolean femaleToilets, boolean largeToiletFacility, boolean disabledAccess,
                    boolean chargeForUse, String openingHours, boolean hasIssue) {
        this.locationId          = locationId;
        this.locationName        = locationName;
        this.address1            = address1;
        this.address2            = address2;
        this.city                = city;
        this.postCode            = postCode;
        this.country             = country;
        this.longitude           = longitude;
        this.latitude            = latitude;
        this.adminOrg            = adminOrg;
        this.water               = water;
        this.drinkable           = drinkable;
        this.treatment           = treatment;
        this.unknown             = unknown;
        this.largeWaterFacility  = largeWaterFacility;
        this.maleToilets         = maleToilets;
        this.femaleToilets       = femaleToilets;
        this.largeToiletFacility = largeToiletFacility;
        this.disabledAccess      = disabledAccess;
        this.chargeForUse        = chargeForUse;
        this.openingHours        = openingHours;
        this.hasIssue            = hasIssue;
    }

    public Long    getLocationId() {
        return locationId;
    }
    public String  getLocationName() {
        return locationName;
    }
    public String  getAddress1() { return address1; }
    public String  getAddress2() { return address2; }
    public String  getCity() { return city; }
    public String  getPostCode() { return postCode; }
    public String  getCountry() { return country; }
    public double  getLongitude() { return longitude; }
    public double  getLatitude() { return latitude; }
    public String  getAdminOrg() { return adminOrg; }
    public boolean getWater() { return water; }
    public boolean getDrinkable() { return drinkable; }
    public boolean getTreatment() { return treatment; }
    public boolean getUnknown() { return unknown; }
    public boolean getLargeWaterFacility() { return largeWaterFacility; }
    public boolean getMaleToilets() { return maleToilets; }
    public boolean getFemaleToilets() { return femaleToilets; }
    public boolean getLargeToiletFacility() { return largeToiletFacility; }
    public boolean getDisabledAccess() { return disabledAccess; }
    public boolean getChargeForUse() { return chargeForUse; }
    public String  getOpeningHours() { return openingHours; }
    public boolean getHasIssue() { return hasIssue; }

}
