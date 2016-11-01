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

    public static ApplicationNumberType numberToNumberType(int i) {
        if (i == 1) {
            return CASE_NUMBER;
        } else if (i == 2) {
            return CHECK_NUMBER;
        }
        throw new IllegalArgumentException();
    }
}
