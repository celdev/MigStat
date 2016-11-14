package com.celdev.migstat.controller;

import android.support.annotation.DrawableRes;

import com.celdev.migstat.model.Application;
import com.celdev.migstat.model.WaitingTime;

public interface DataStorageInterface {

    Application getApplication() throws DataStorageLoadException;

    WaitingTime getWaitingTime() throws DataStorageLoadException;

    void loadAll() throws DataStorageLoadException;

    boolean saveAll();

    void saveBackground(@DrawableRes int background);

    int loadBackground();

    void deleteWaitingTime();

    void deleteAll();

}
