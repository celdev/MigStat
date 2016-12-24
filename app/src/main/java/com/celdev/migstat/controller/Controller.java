package com.celdev.migstat.controller;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.util.Log;

import com.celdev.migstat.MainActivity;
import com.celdev.migstat.controller.parser.SimpleCaseStatusParser;
import com.celdev.migstat.model.Application;
import com.celdev.migstat.model.ApplicationNumberType;
import com.celdev.migstat.model.WaitingTime;
import com.celdev.migstat.view.ViewInterface;


/*  This class contains the functionality for communication between
*   the Activity and the data storage,
*   the Activity and the Model and
*   the Activity and the AsyncTask (parsers)
* */
public class Controller implements DataStorageInterface {


    private Context context;
    private ViewInterface viewInterface;

    private static Application application;
    private static WaitingTime waitingTime;

    public boolean waitingTimeIsLoaded() {
        return waitingTime != null;
    }

    public enum ApplicationState {
        NO_APPLICATION,
        NO_WAITING_TIME,
        HAVE_BOTH;
    }

    /*  returns the application state
    *   if the application or waiting time is null it tries to load those from the
    *   DataStorage
    *   if the application can't be loaded NO_APPLICATION is returned
    *   if the waiting time can't be loaded NO_WAITING_TIME is returned
    *   else
    *   HAVE_BOTH is returned
    * */
    public ApplicationState getApplicationState() {
        try {
            if (application == null) {
                application = DataStorage.getInstance().loadApplication(context);
            }
            if (waitingTime == null) {
                waitingTime = DataStorage.getInstance().loadWaitingTime(context);
                application.setWaitingTime(waitingTime);
            }
        } catch (NoApplicationException e) {
            return ApplicationState.NO_APPLICATION;
        } catch (NoWaitingTimeException e) {
            return ApplicationState.NO_WAITING_TIME;
        }
        return ApplicationState.HAVE_BOTH;
    }

    //sets which ViewInterface should received updates when the AsyncTask finishes
    public Controller(Context context, ViewInterface viewInterface) {
        this.context = context;
        this.viewInterface = viewInterface;
    }

    public void setApplication(Application application) {
        Controller.application = application;
    }

    //if the application has an application number, the status of the application is updated
    public void updateApplication() {
        if (application.isHasApplicationNumber()) {
            new ApplicationStatusChecker(applicationAsyncCallback).checkApplication(application);
        } else {
            applicationAsyncCallback.receiveAsyncResult(AsyncCallbackErrorObject.NO_APPLICATION_NUMBER);
        }
    }

    //returns true if the application is using custom months
    public boolean isUsingCustomWaitingTime() {
        return waitingTime.isUseCustomMonths();
    }

    //returns true if the waiting time query (the migrationsverket url) is set
    public boolean hasWaitingTimeQuery() {
        return application.getWaitingTime().hasQuery();
    }

    //returns true if a custom months value has been set
    public boolean hasCustomWaitingTime() {
        return waitingTime.hasCustomWaitingTime();
    }

    //if the waiting time isn't null and has a waiting time query the waiting time will be updated
    public void updateWaitingTime() {
        if (waitingTime != null && waitingTime.hasQuery()) {
            new WebViewResponseParser(waitingTime.getQuery(), waitingTimeAsyncCallback);
        } else {
            waitingTimeAsyncCallback.receiveAsyncResult(AsyncCallbackErrorObject.NO_WAITINGTIME_QUERY);
        }
    }

    //updates both the waiting time and application
    public void updateApplicationAndWaitingTime() {
        updateApplication();
        updateWaitingTime();
    }

    //passes the ModelChange-object to the ViewInterface
    private void updateView(ViewInterface.ModelChange modelChange) {
        Log.d(MainActivity.LOG_KEY, "Updating view " + viewInterface.getClass().getName() + " with model change " + modelChange.name());
        viewInterface.modelChange(modelChange);
    }

    //returns the application is it isn't null, throws an exception if it is null
    @Override
    public Application getApplication() throws IncorrectStateException {
        if (application == null) {
            throw new IncorrectStateException();
        }
        return application;
    }

    /*  This implementation of the AsyncCallback will handle the result from the
    *   check application Async Tasks
    *
    *   Depending on the result different ModelChange-object will be passed to the view
    * */
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
                DataStorage.getInstance().saveApplication(context, application);
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


    /*  This implementation of the AsyncCallback will handle the result from the
    *   waiting time (parsers) Async Tasks
    *
    *   Depending on the result different ModelChange-object will be passed to the view
    * */
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

    /*  this implementation of the AsyncCallback interface will handle the result
    *   from the "check if the application is valid"-part of the application
    * */
    private AsyncCallback checkIfApplicationIsValidCallback = new AsyncCallback() {
        @Override
        public void receiveAsyncResult(Object result) {
            if (result instanceof AsyncCallbackErrorObject) {
                Log.d(MainActivity.LOG_KEY, "check if application is valid object class = AsyncCallbackErrorObject");
                updateView(ViewInterface.ModelChange.INVALID_APPLICATION);
            }
            if (result instanceof SimpleCaseStatusParser.StatusAndDate) {
                Log.d(MainActivity.LOG_KEY, "check if application is valid object class = StatusAndDate");
                SimpleCaseStatusParser.StatusAndDate s = (SimpleCaseStatusParser.StatusAndDate) result;
                application = new Application(s.getStatusType(),
                        s.getApplicationDate().getApplicationDate(),
                        s.getApplicationNumber().getApplicationNumber(), s.getApplicationNumber().getApplicationNumberType());
                updateView(ViewInterface.ModelChange.APPLICATION_OK);
                saveApplication(application);
                return;
            }
            updateView(ViewInterface.ModelChange.INVALID_APPLICATION);
        }
    };


    /*  This implementation of the AsyncCallback will handle the result from the
    *   first check of the waiting time (in the webview)
    *
    *   Depending on the result different ModelChange-object will be passed to the view
    * */
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

    //checks the application number and returns an Application object if it is valid (returns to the
    //checkIfApplicationIsValidCallback-object)
    public void checkApplicationNumberReturnApplication(int applicationNumber, ApplicationNumberType applicationNumberType) {
        new ApplicationStatusChecker(checkIfApplicationIsValidCallback).checkApplication(applicationNumber, applicationNumberType);
    }

    //checks the waiting time query (used in the web view)
    public void handleWaitingTimeQuery(String query) {
        new WebViewResponseParser(query, checkWaitingTimeQueryCallback);
    }

    //save all data (application and waiting time)
    @Override
    public boolean saveAll() {
        DataStorage dataStorage = DataStorage.getInstance();
        return !(application == null || waitingTime == null) && dataStorage.saveApplication(context, application) && dataStorage.saveWaitingTime(context, waitingTime);
    }

    //saves the application
    public boolean saveApplication(Application application) {
        return DataStorage.getInstance().saveApplication(context, application);
    }

    //sets the waiting time mode and saves the mode in the data storage
    public void setWaitingTimeMode(boolean useCustom) {
        application.getWaitingTime().setWaitingTimeMode(useCustom ? WaitingTime.WaitingTimeMode.CUSTOM : WaitingTime.WaitingTimeMode.MIGRATIONSVERKET);
        saveWaitingTime(waitingTime);
    }

    //sets the mode to custom months (and it's value) and saves the waiting time in the data storage
    public void setAndUseCustomWaitingTime(int months) {
        application.getWaitingTime().setUseCustomMonthsMode(months);
        saveWaitingTime(waitingTime);
    }

    //saves the waiting time
    public boolean saveWaitingTime(WaitingTime waitingTime) {
        return DataStorage.getInstance().saveWaitingTime(context, waitingTime);
    }

    //returns the waiting time if it isn't null, otherwise throws an exception
    @Override
    public WaitingTime getWaitingTime() throws IncorrectStateException {
        if (waitingTime == null) {
            throw new IncorrectStateException();
        }
        return waitingTime;
    }

    //loads the application and waiting time from the data storage
    @Override
    public void loadAll() throws DataStorageLoadException {
        DataStorage dataStorage = DataStorage.getInstance();
        application = dataStorage.loadApplication(context);
        waitingTime = dataStorage.loadWaitingTime(context);
        application.setWaitingTime(waitingTime);
    }

    //saves the background index
    @Override
    public void saveBackground(@DrawableRes int background) {
        DataStorage.getInstance().saveBackgroundIndex(context, background);
    }

    //loads the background index
    @Override
    public int loadBackground() {
        return DataStorage.getInstance().getBackgroundIndex(context);
    }

    //deletes the waiting time
    @Override
    public void deleteWaitingTime() {
        DataStorage.getInstance().deleteWaitingTime(context);
        waitingTime = null;
    }

    //deletes all data
    @Override
    public void deleteAll() {
        DataStorage.getInstance().deleteAllData(context);
        application = null;
        waitingTime = null;
    }

    //resets the data and returns true if
    //this is the first time the application is run on a new version
    public boolean resetBecauseNewVersion() {
        return DataStorage.getInstance().checkVersionResetIfNeeded(context);
    }

    //save the change background enable state
    public void saveEnableThemes() {
        DataStorage.getInstance().saveEnabledThemes(context);
    }

    //returns true if the change background state is enabled
    public boolean themesIsEnabled() {
        return DataStorage.getInstance().isThemeEnabled(context);
    }

}
