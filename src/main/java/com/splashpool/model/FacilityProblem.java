package com.splashpool.model;

import java.sql.Timestamp;

public class FacilityProblem {

    private long problemId;
    private long locationId;
    private int  version;
    private Timestamp auditDateTime;
    private String description;

    public FacilityProblem(long problemId, long locationId, int version,
                           Timestamp auditDateTime, String description) {
        this.problemId      = problemId;
        this.locationId     = locationId;
        this.version        = version;
        this.auditDateTime  = auditDateTime;
        this.description    = description;
    }

    public long getProblemId() { return problemId; }
    public long getLocationId() { return locationId; }
    public int  getVersion() { return version; }
    public Timestamp getAuditDateTime() { return auditDateTime; }
    public String getDescription() { return description; }
}
