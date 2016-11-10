package com.unimagdalena.android.app.smtravelvoice;

import android.app.Application;
import android.content.Context;

import com.shawnlin.preferencesmanager.PreferencesManager;
import com.thefinestartist.Base;
import com.zhy.autolayout.config.AutoLayoutConifg;


public class SMTravelVoicApp extends Application {

    public static String PACKAGE_NAME;

    public static String SETTINGS_PREFERENCES;

    public static String GEOFENCES_ADDED_KEY;

    /**
     * Used to set an expiration time for a geofence. After this amount of time Location Services
     * stops tracking the geofence.
     */
    public static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;

    /**
     * For this sample, geofences expire after twelve hours.
     */
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;

    public static final float GEOFENCE_RADIUS_IN_METERS = 500; // 1 mile, 1.6 km

    private PreferencesManager preferencesManager;

    @Override
    public void onCreate() {
        super.onCreate();

        AutoLayoutConifg.getInstance().useDeviceSize();
        Base.initialize(this);

        preferencesManager = new PreferencesManager(this);

        PACKAGE_NAME = getPackageName().toUpperCase();

        SETTINGS_PREFERENCES = PACKAGE_NAME + ".SETTINGS";

        GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";

        setPreferencesManager(SETTINGS_PREFERENCES);
    }

    public void setPreferencesManager(String name) {
        preferencesManager.setName(name);
        preferencesManager.setMode(Context.MODE_PRIVATE);
        preferencesManager.init();
    }
}
