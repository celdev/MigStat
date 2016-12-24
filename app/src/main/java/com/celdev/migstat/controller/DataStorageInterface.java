package com.celdev.migstat.controller;

import android.support.annotation.DrawableRes;

import com.celdev.migstat.model.Application;
import com.celdev.migstat.model.WaitingTime;

/*  Provides the methods the data storage layer will need to provide
* */
public interface DataStorageInterface {

    Application getApplication() throws IncorrectStateException;

    WaitingTime getWaitingTime() throws IncorrectStateException;

    void loadAll() throws DataStorageLoadException;

    boolean saveAll();

    void saveBackground(@DrawableRes int background);

    int loadBackground();

    void deleteWaitingTime();

    void deleteAll();

}
