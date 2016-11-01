package com.celdev.migstat.controller;

public class WaitingTimeDataStoragePacket {

    private String query;
    private boolean customMode;
    private int customMonths;

    public WaitingTimeDataStoragePacket(String query, boolean customMode, int customMonths) {
        this.query = query;
        this.customMode = customMode;
        this.customMonths = customMonths;
    }

    public String getQuery() {
        return query;
    }

    public boolean isCustomMode() {
        return customMode;
    }

    public int getCustomMonths() {
        return customMonths;
    }
}
