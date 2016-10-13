package com.unimagdalena.android.app.smtravelvoice;

import android.app.Application;
import android.content.Context;

import com.shawnlin.preferencesmanager.PreferencesManager;
import com.thefinestartist.Base;
import com.zhy.autolayout.config.AutoLayoutConifg;


public class SMTravelVoicApp extends Application {

    public static String PACKAGE_NAME;

    public static String SETTINGS_PREFERENCES;

    private PreferencesManager preferencesManager;

    @Override
    public void onCreate() {
        super.onCreate();

        AutoLayoutConifg.getInstance().useDeviceSize();
        Base.initialize(this);

        preferencesManager = new PreferencesManager(this);

        PACKAGE_NAME = getPackageName().toUpperCase();

        SETTINGS_PREFERENCES = PACKAGE_NAME + ".SETTINGS";

        setPreferencesManager(SETTINGS_PREFERENCES);
    }

    public void setPreferencesManager(String name) {
        preferencesManager.setName(name);
        preferencesManager.setMode(Context.MODE_PRIVATE);
        preferencesManager.init();
    }
}
