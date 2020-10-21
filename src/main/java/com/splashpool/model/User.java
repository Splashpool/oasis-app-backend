package com.splashpool.model;

public class User {

    private String email;
    private String uuid;
    private String firstName;
    private String lastName;
    private String countryCode;
    private String mobileNumber;
    private boolean adminUser;
    private String organisation;
    private String orgAddress1;
    private String orgAddress2;
    private String orgCity;
    private String orgPostCode;
    private String orgCountry;

    public User(String email, String uuid, String firstName, String lastName,
                    String countryCode, String mobileNumber, boolean adminUser, String organisation,
                    String orgAddress1, String orgAddress2, String orgCity, String orgPostCode, String orgCountry) {
        this.email          = email;
        this.uuid           = uuid;
        this.firstName      = firstName;
        this.lastName       = lastName;
        this.countryCode    = countryCode;
        this.mobileNumber   = mobileNumber;
        this.adminUser      = adminUser;
        this.organisation   = organisation;
        this.orgAddress1    = orgAddress1;
        this.orgAddress2    = orgAddress2;
        this.orgCity        = orgCity;
        this.orgPostCode    = orgPostCode;
        this.orgCountry     = orgCountry;
    }

    public String getEmail() {
        return email;
    }
    public String getUuid() {
        return uuid;
    }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getCountryCode() { return countryCode; }
    public String getMobileNumber() { return mobileNumber; }
    public boolean getAdminUser() { return adminUser; }
    public String  getOrganisation() { return organisation; }
    public String  getOrgAddress1() { return orgAddress1; }
    public String  getOrgAddress2() { return orgAddress2; }
    public String  getOrgCity() { return orgCity; }
    public String  getOrgPostCode() { return orgPostCode; }
    public String getOrgCountry() { return orgCountry; }
}
