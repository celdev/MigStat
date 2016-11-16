package com.celdev.migstat.controller;

import android.app.Activity;
import android.support.annotation.DrawableRes;
import android.util.Log;

import com.celdev.migstat.MainActivity;
import com.celdev.migstat.controller.parser.SimpleCaseStatusParser;
import com.celdev.migstat.model.Application;
import com.celdev.migstat.model.ApplicationNumberType;
import com.celdev.migstat.model.WaitingTime;
import com.celdev.migstat.view.ViewInterface;

public class Controller implements DataStorageInterface {


    private static Controller instance;
    private Activity activity;
    private ViewInterface viewInterface;

    private static Application application;
    private static WaitingTime waitingTime;

    public enum ApplicationState {
        NO_APPLICATION,
        NO_WAITING_TIME,
        HAVE_BOTH;
    }

    public ApplicationState getApplicationState() {
        try {
            if (application == null) {
                application = DataStorage.getInstance().loadApplication(activity);
            }
            if (waitingTime == null) {
                waitingTime = DataStorage.getInstance().loadWaitingTime(activity);
            }
        } catch (NoApplicationException e) {
            return ApplicationState.NO_APPLICATION;
        } catch (NoWaitingTimeException e) {
            return ApplicationState.NO_WAITING_TIME;
        }
        return ApplicationState.HAVE_BOTH;
    }


    private Controller(Activity activity, ViewInterface viewInterface) {
        this.activity = activity;
        this.viewInterface = viewInterface;
    }

    public static Controller getInstance(Activity activity, ViewInterface viewInterface) {
        if (instance != null) {
            instance = new Controller(activity, viewInterface);
        } else {
            instance = new Controller(activity, viewInterface);
        }
        return instance;
    }

    public void setApplication(Application application) {
        Controller.application = application;
    }

    public void setWaitingTime(WaitingTime waitingTime) {
        Controller.application.setWaitingTimeReturnBothIfNewer(waitingTime);
    }

    public void updateApplication() {
        if (application.isHasApplicationNumber()) {
            new ApplicationStatusChecker(applicationAsyncCallback).checkApplication(application);
        } else {
            applicationAsyncCallback.receiveAsyncResult(AsyncCallbackErrorObject.NO_APPLICATION_NUMBER);
        }
    }

    public void updateWaitingTime() {
        if (waitingTime != null && waitingTime.hasQuery()) {
            new WebViewResponseParser(waitingTime.getQuery(), waitingTimeAsyncCallback);
        } else {
            waitingTimeAsyncCallback.receiveAsyncResult(AsyncCallbackErrorObject.NO_WAITINGTIME_QUERY);
        }
    }

    public void updateApplicationAndWaitingTime() {
        updateApplication();
        updateWaitingTime();
    }


    private void updateView(ViewInterface.ModelChange modelChange) {
        Log.d(MainActivity.LOG_KEY, "Updating view " + viewInterface.getClass().getName() + " with model change " + modelChange.name());
        viewInterface.modelChange(modelChange);
    }

    @Override
    public Application getApplication() throws DataStorageLoadException {
        if (application == null) {
            application = DataStorage.getInstance().loadApplication(activity);
        }
        return application;
    }


    private AsyncCallback applicationAsyncCallback = new AsyncCallback() {
        @Override
        public void receiveAsyncResult(Object result) {
            if (result instanceof AsyncCallbackErrorObject) {
                AsyncCallbackErrorObject error = (AsyncCallbackErrorObject) result;
                Log.d(MainActivity.LOG_KEY,"application async callback: " + error.name());
                switch (error) {
                    case NO_APPLICATION_NUMBER:
                        updateView(ViewInterface.ModelChange.APPLICATION);
                        return;
                    case PARSER_EXCEPTION:
                        updateView(ViewInterface.ModelChange.ERROR_UPDATE);
                        return;
                }
            }
            if (result instanceof SimpleCaseStatusParser.StatusAndDate) {
                SimpleCaseStatusParser.StatusAndDate s = (SimpleCaseStatusParser.StatusAndDate) result;
                boolean finishedNow = application.newStatusTypeReturnTrueIfGetDecision(s.getStatusType());
                DataStorage.getInstance().saveApplication(activity, application);
                if (finishedNow) {
                    updateView(ViewInterface.ModelChange.FINISHED);
                } else {
                    updateView(ViewInterface.ModelChange.APPLICATION);
                }
                return;
            }
            updateView(ViewInterface.ModelChange.ERROR_UPDATE);
        }
    };

    private AsyncCallback waitingTimeAsyncCallback = new AsyncCallback() {
        @Override
        public void receiveAsyncResult(Object result) {
            if (result instanceof AsyncCallbackErrorObject) {
                AsyncCallbackErrorObject error = (AsyncCallbackErrorObject) result;
                switch (error) {
                    case NO_WAITINGTIME_QUERY:
                        updateView(ViewInterface.ModelChange.WAITING_TIME);
                        return;
                    case PARSER_EXCEPTION:
                        updateView(ViewInterface.ModelChange.ERROR_UPDATE);
                        return;
                }
            }
            if (result instanceof WaitingTime) {
                WaitingTime waitingTime = (WaitingTime) result;
                Application.OldAndNewWaitingTimeWrapper newWaitingTime = application.setWaitingTimeReturnBothIfNewer(waitingTime);
                saveWaitingTime(waitingTime);
                Controller.waitingTime = waitingTime;
                if (newWaitingTime != null) {
                    updateView(ViewInterface.ModelChange.NEW_WAITING_TIME);
                } else {
                    updateView(ViewInterface.ModelChange.WAITING_TIME);
                }
                return;
            }
            updateView(ViewInterface.ModelChange.ERROR_UPDATE);
        }
    };

    private AsyncCallback checkIfApplicationIsValidCallback = new AsyncCallback() {
        @Override
        public void receiveAsyncResult(Object result) {
            if (result instanceof AsyncCallbackErrorObject) {
                updateView(ViewInterface.ModelChange.INVALID_APPLICATION);
            }
            if (result instanceof SimpleCaseStatusParser.StatusAndDate) {
                SimpleCaseStatusParser.StatusAndDate s = (SimpleCaseStatusParser.StatusAndDate) result;
                application = new Application(s.getStatusType(),
                        s.getApplicationDate().getApplicationDate(),
                        s.getApplicationNumber().getApplicationNumber(), s.getApplicationNumber().getApplicationNumberType());
                updateView(ViewInterface.ModelChange.APPLICATION_OK);
                saveApplication(application);
            }
            updateView(ViewInterface.ModelChange.INVALID_APPLICATION);
        }
    };

    private AsyncCallback checkWaitingTimeQueryCallback = new AsyncCallback() {
        @Override
        public void receiveAsyncResult(Object result) {
            if (result instanceof WaitingTime) {
                WaitingTime waitingTime = (WaitingTime) result;
                saveWaitingTime(waitingTime);
                Controller.waitingTime = waitingTime;
                updateView(ViewInterface.ModelChange.WAITING_TIME_OK);
                return;
            }
            updateView(ViewInterface.ModelChange.ERROR_UPDATE);
        }
    };

    public void checkApplicationNumberReturnApplication(int applicationNumber, ApplicationNumberType applicationNumberType) {
        new ApplicationStatusChecker(checkIfApplicationIsValidCallback).checkApplication(applicationNumber, applicationNumberType);
    }

    public void handleWaitingTimeQuery(String query) {
        new WebViewResponseParser(query, checkWaitingTimeQueryCallback);
    }

    @Override
    public boolean saveAll() {
        DataStorage dataStorage = DataStorage.getInstance();
        return !(application == null || waitingTime == null) && dataStorage.saveApplication(activity, application) && dataStorage.saveWaitingTime(activity, waitingTime);
    }

    public boolean saveApplication(Application application) {
        return DataStorage.getInstance().saveApplication(activity, application);
    }

    public boolean saveWaitingTime(WaitingTime waitingTime) {
        return DataStorage.getInstance().saveWaitingTime(activity, waitingTime);
    }

    @Override
    public WaitingTime getWaitingTime() throws DataStorageLoadException {
        if (waitingTime == null) {
            waitingTime = DataStorage.getInstance().loadWaitingTime(activity);
            application.setWaitingTimeReturnBothIfNewer(waitingTime);
        }
        return waitingTime;
    }

    @Override
    public void loadAll() throws DataStorageLoadException {
        DataStorage dataStorage = DataStorage.getInstance();
        application = dataStorage.loadApplication(activity);
        waitingTime = dataStorage.loadWaitingTime(activity);
    }

    @Override
    public void saveBackground(@DrawableRes int background) {
        DataStorage.getInstance().saveBackgroundIndex(activity, background);
    }

    @Override
    public int loadBackground() {
        return DataStorage.getInstance().getBackgroundIndex(activity);
    }

    @Override
    public void deleteWaitingTime() {
        DataStorage.getInstance().deleteWaitingTime(activity);
        waitingTime = null;
    }

    @Override
    public void deleteAll() {
        DataStorage.getInstance().deleteAllData(activity);
        application = null;
        waitingTime = null;
    }

    public boolean resetBecauseNewVersion() {
        return DataStorage.getInstance().checkVersionResetIfNeeded(activity);
    }


    public void saveEnableThemes() {
        DataStorage.getInstance().saveEnabledThemes(activity);
    }

    public boolean themesIsEnabled() {
        return DataStorage.getInstance().isThemeEnabled(activity);
    }

}
