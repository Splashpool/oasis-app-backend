package com.splashpool.model;

public class Comment {

    private long   locationId;
    private String comment;
    private String pictureURL;
    private int    rating;

    public Comment(long locationId, String comment, String pictureURL, int rating) {
        this.locationId   = locationId;
        this.comment      = comment;
        this.pictureURL   = pictureURL;
        this.rating       = rating;
    }

    public long   getlocationId() {
        return locationId;
    }
    public String getComment() {
        return comment;
    }
    public String getPictureURL() {
        return pictureURL;
    }
    public int    getRating() {
        return rating;
    }
}