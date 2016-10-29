package com.celdev.migstat.model;

public enum ApplicationNumberType {

    CASE_NUMBER(1),
    CHECK_NUMBER(2);

    private int migrationsverketQueryNumber;

    ApplicationNumberType(int migrationsverketQueryNumber) {
        this.migrationsverketQueryNumber = migrationsverketQueryNumber;
    }

    public int getMigrationsverketQueryNumber() {
        return migrationsverketQueryNumber;
    }
}
