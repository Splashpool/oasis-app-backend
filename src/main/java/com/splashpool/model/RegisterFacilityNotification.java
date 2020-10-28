package com.splashpool.model;

public class RegisterFacilityNotification {

    private long problemId;
    private String uuid;

    public RegisterFacilityNotification(long problemId, String uuid) {
        this.problemId     = problemId;
        this.uuid           = uuid;
    }

    public long getProblemId() { return problemId; }
    public String getUuid() {
        return uuid;
    }
}