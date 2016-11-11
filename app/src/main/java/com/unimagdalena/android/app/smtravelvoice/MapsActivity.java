package com.unimagdalena.android.app.smtravelvoice;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.entire.sammalik.samlocationandgeocoding.SamLocationRequestService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mukesh.permissions.AppPermissions;
import com.shawnlin.preferencesmanager.PreferencesManager;

import org.fingerlinks.mobile.android.navigator.Navigator;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.unimagdalena.android.app.smtravelvoice.SMTravelVoice.ACTION_RECOGNIZE_SPEECH_RC;
import static com.unimagdalena.android.app.smtravelvoice.SMTravelVoice.GEOFENCES_ADDED_KEY;
import static com.unimagdalena.android.app.smtravelvoice.SMTravelVoice.GEOFENCE_EXPIRATION_IN_MILLISECONDS;
import static com.unimagdalena.android.app.smtravelvoice.SMTravelVoice.GEOFENCE_TRANSITION_ENTER;
import static com.unimagdalena.android.app.smtravelvoice.SMTravelVoice.GEOFENCE_TRANSITION_EXIT;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status>, LocationListener, TextToSpeech.OnInitListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    protected static final String tag = "LifeCycleEventsMA";
    protected static final String TAG = "c-and-m-geofences";

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    protected Boolean mGeofencesAdded;
    protected Boolean mRequestingLocationUpdates;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    protected Location mCurrentLocation;
    protected PendingIntent mGeofencePendingIntent;
    protected SharedPreferences mSharedPreferences;
    protected String mLastUpdateTime;

    protected ArrayList<CircleOptions> mCircleOptions;
    protected ArrayList<Geofence> mGeofenceList;

    private ArrayList<Place> placeArrayList;
    private AppPermissions appPermissions;

    private Boolean mainDescriptionReadied = false;
    private Boolean descriptionsReadied = false;

    private GeofenceTransitionReceiver geofenceTransitionReceiver;
    private GoogleMap map;
    private Integer position = 0;
    private Place place;
    private TextToSpeech mainVoice;
    private TextToSpeech messageVoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        appPermissions = new AppPermissions(this);

        gpsManager();
        settingsManager();
        updateValuesFromBundle(savedInstanceState);

        placeArrayList = SMTravelVoice.getInstance().getPlaces();

        try {
            for (Place place : placeArrayList) {
                PreferencesManager.putObject(place.getName(), place);

                mGeofenceList.add(geofencesBuilder(place));
            }

            buildGoogleApiClient();
        } catch (NullPointerException e) {
            Log.e(getClass().getSimpleName(), e.getMessage());
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onDestroy() {
        mainVoice.stop();
        mainVoice.shutdown();

        messageVoice.stop();
        messageVoice.shutdown();

        unregisterReceiver(geofenceTransitionReceiver);

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Navigator.with(this).utils().finishWithAnimation(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ACTION_RECOGNIZE_SPEECH_RC:
                    if (data != null) {
                        ArrayList<String> extra = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        String text = extra.get(0);

                        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();

                        messageVoice.speak("Ok", TextToSpeech.QUEUE_FLUSH, null, null);

                        if (text.toLowerCase().contains("sí".toLowerCase()) || text.toLowerCase().equalsIgnoreCase("sí".toLowerCase())) {
                            if (!mainDescriptionReadied) {
                                mainDescriptionReadied = true;

                                speak(mainVoice, place.getMainDescription() + ", ¿quieres saber mas al respecto?");
                            } else {
                                if (position != place.getDescriptions().size() - 1) {
                                    speak(mainVoice, place.getDescriptions().get(position).getMessage() + ", ¿quieres saber mas al respecto?");
                                } else {
                                    speak(mainVoice, place.getDescriptions().get(position).getMessage());
                                }
                            }
                        } else if (text.toLowerCase().contains("no".toLowerCase()) || text.toLowerCase().equalsIgnoreCase("no".toLowerCase())) {
                            mainDescriptionReadied = false;
                            descriptionsReadied = false;

                            position = 0;
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        outState.putParcelable(LOCATION_KEY, mCurrentLocation);
        outState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_maps, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.app_bar_about:
                new AlertDialog.Builder(this).setTitle(R.string.created_by).setMessage(R.string.developers).create().show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        map.setMyLocationEnabled(true);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            for (Place place : placeArrayList) {
                map.addCircle(new CircleOptions()
                        .center(new LatLng(place.getCoordinates().getLatitude(), place.getCoordinates().getLongitude()))
                        .radius(place.getRatio())
                        .strokeColor(Color.TRANSPARENT)
                        .strokeWidth(2.5f)
                        .fillColor(ContextCompat.getColor(MapsActivity.this, R.color.transparent_black_percent_25)));
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(place.getCoordinates().getLatitude(), place.getCoordinates().getLongitude()))
                        .title(place.getName()));
            }

            map.setOnMarkerClickListener(MapsActivity.this);
        } else {
            Toast.makeText(this, R.string.connection_error_message, Toast.LENGTH_SHORT).show();
        }

        new SamLocationRequestService(MapsActivity.this).executeService(new SamLocationRequestService.SamLocationListener() {
            @Override
            public void onLocationUpdate(Location location, Address address) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(this, marker.getTitle(), Toast.LENGTH_SHORT).show();

        Place markerPlace = null;

        for (Place place : placeArrayList) {
            if (place.getName().equals(marker.getTitle())) {
                markerPlace = place;
                break;
            }
        }

        Bundle bundle = new Bundle();
        bundle.putSerializable("place", markerPlace);

        Navigator.with(this).build().goTo(DetailActivity.class, bundle).animation().commit();

        return false;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mCurrentLocation == null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

            updateUI();
        }

        startLocationUpdates();
        addGeofences();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            PreferencesManager.putBoolean(GEOFENCES_ADDED_KEY, mGeofencesAdded);
            Toast.makeText(this, getString(mGeofencesAdded ? R.string.geofences_added : R.string.geofences_removed), Toast.LENGTH_SHORT).show();
        } else {
            String errorMessage = GeofenceErrorMessages.getErrorString(this, status.getStatusCode());
            Log.e(TAG, errorMessage);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

        updateUI();
    }

    public void settingsManager() {
        //hashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "Done");

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        mCircleOptions = new ArrayList<>();
        mGeofenceList = new ArrayList<>();

        mGeofencePendingIntent = null;
        mGeofencesAdded = PreferencesManager.getBoolean(GEOFENCES_ADDED_KEY, false);

        mainVoice = new TextToSpeech(this, this);
        mainVoice.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {
                //Toast.makeText(getApplicationContext(), "onStart " + s, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDone(String s) {
                if (mainDescriptionReadied) {
                    position++;
                }

                if (position < place.getDescriptions().size()) {
                    listen();
                }

                if (position == place.getDescriptions().size()) {
                    descriptionsReadied = false;

                    messageVoice.speak("Esta es toda la información que tengo con respecto a " + place.getName(), TextToSpeech.QUEUE_FLUSH, null, null);
                }

                //Toast.makeText(getApplicationContext(), "onDone " + s, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String s) {
                //Toast.makeText(getApplicationContext(), "onError " + s, Toast.LENGTH_SHORT).show();
            }
        });

        messageVoice = new TextToSpeech(this, this);

        geofenceTransitionReceiver = new GeofenceTransitionReceiver();

        IntentFilter transitionFilter = new IntentFilter();
        transitionFilter.addAction(GEOFENCE_TRANSITION_ENTER);
        transitionFilter.addAction(GEOFENCE_TRANSITION_EXIT);

        registerReceiver(geofenceTransitionReceiver, transitionFilter);
    }

    public void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");

        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
            }

            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }

            updateUI();
        }
    }

    public synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        createLocationRequest();
    }

    public void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void gpsManager() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        boolean enabledGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!enabledGPS) {
            Toast.makeText(this, getString(R.string.gps_signal_error), Toast.LENGTH_LONG).show();

            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    public Geofence geofencesBuilder(Place place) {
        return new Geofence.Builder()
                .setRequestId((place.getName()))
                .setCircularRegion(place.getCoordinates().getLatitude(), place.getCoordinates().getLongitude(), place.getRatio())
                .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    public void addGeofences() {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            mGeofencesAdded = true;
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, getGeofencingRequest(), getGeofencePendingIntent()).setResultCallback(this);
        } catch (SecurityException securityException) {
            logSecurityException(securityException);
        }
    }

    public GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    public PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    public void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    public void updateUI() {
        if (PreferencesManager.getBoolean(getString(R.string.update_ui), false)) {
            if (mCurrentLocation != null) {
                double latitude = mCurrentLocation.getLatitude();
                double longitude = mCurrentLocation.getLongitude();

                LatLng latLng = new LatLng(latitude, longitude);
                map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                map.animateCamera(CameraUpdateFactory.zoomTo(16));
            }
        }
    }

    public void logSecurityException(SecurityException securityException) {
        Log.e(TAG, "Invalid location permission. " + "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }

    public void listen() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, new Locale("es", "US"));
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");

        startActivityForResult(intent, ACTION_RECOGNIZE_SPEECH_RC);
    }

    public void speak(TextToSpeech voice, String text) {
        voice.speak(text, TextToSpeech.QUEUE_FLUSH, null, place.getPlaceId());
    }

    @Override
    public void onInit(int i) {
        if (i == TextToSpeech.SUCCESS) {
            int mainResult = mainVoice.setLanguage(new Locale("es", "US"));
            int descriptionResult = mainVoice.setLanguage(new Locale("es", "US"));

            if (mainResult == TextToSpeech.LANG_MISSING_DATA || mainResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            }
        } else {
            Log.e("TTS", "Initialization Failed!");
        }
    }

    class GeofenceTransitionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();

            place = (Place) bundle.getSerializable("place");

            if (intent.getAction().equals(GEOFENCE_TRANSITION_ENTER)) {
                speak(mainVoice, "Has entrado ha " + place.getName() + ", ¿quieres saber mas al respecto?");
            } else {
                speak(mainVoice, "Has salido de " + place.getName() + ", espero que vuelvas pronto.");
                mainDescriptionReadied = false;
                descriptionsReadied = false;
                position = 0;
            }
        }
    }
}
