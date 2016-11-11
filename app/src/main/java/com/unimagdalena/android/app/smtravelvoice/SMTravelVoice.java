package com.unimagdalena.android.app.smtravelvoice;

import android.app.Application;
import android.content.Context;

import com.shawnlin.preferencesmanager.PreferencesManager;
import com.thefinestartist.Base;
import com.zhy.autolayout.config.AutoLayoutConifg;

import java.util.ArrayList;

/**
 * Created by Alberto on 10-Nov-16.
 */
public class SMTravelVoice extends Application {

    private static SMTravelVoice ourInstance;

    public static SMTravelVoice getInstance() {
        return ourInstance;
    }

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

    public static final int ACTION_RECOGNIZE_SPEECH_RC = 100;

    public static String GEOFENCE_TRANSITION_ENTER;

    public static String GEOFENCE_TRANSITION_EXIT;

    private ArrayList<Place> places;
    private PreferencesManager preferencesManager;

    @Override
    public void onCreate() {
        super.onCreate();

        ourInstance = this;

        AutoLayoutConifg.getInstance().useDeviceSize();
        Base.initialize(this);

        places = new ArrayList<>();
        preferencesManager = new PreferencesManager(this);

        PACKAGE_NAME = getPackageName().toUpperCase();

        SETTINGS_PREFERENCES = PACKAGE_NAME + ".SETTINGS";

        GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";

        GEOFENCE_TRANSITION_ENTER = PACKAGE_NAME + ".GEOFENCE_TRANSITION_ENTER";

        GEOFENCE_TRANSITION_EXIT = PACKAGE_NAME + ".GEOFENCE_TRANSITION_EXIT";

        setPreferencesManager(SETTINGS_PREFERENCES);
    }

    public void setPreferencesManager(String name) {
        preferencesManager.setName(name);
        preferencesManager.setMode(Context.MODE_PRIVATE);
        preferencesManager.init();
    }

    public ArrayList<Place> getPlaces() {
        return places;
    }

    public void setPlaces(ArrayList<Place> places) {
        this.places = places;
    }

    public void addPlace(Place place) {
        places.add(place);
    }

    public Place getPlaceById(String id) {
        Place place = null;

        for (Place search : places) {
            if (search.getName().equals(id)) {
                place = search;
                break;
            }
        }

        return place;
    }
}
