package com.unimagdalena.android.app.smtravelvoice;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.mukesh.permissions.AppPermissions;

import org.fingerlinks.mobile.android.navigator.Navigator;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        AppPermissions appPermissions = new AppPermissions(this);

        if (appPermissions.hasPermission(new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION})) {
            try {
                new LoadMarkersWithJsonTask().execute(new URL("https://www.dropbox.com/s/8zgkyhp4jt9qsr0/places.json?dl=1"));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else {
            appPermissions.requestPermission(new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, 100);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 100: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    try {
                        new LoadMarkersWithJsonTask().execute(new URL("https://www.dropbox.com/s/8zgkyhp4jt9qsr0/places.json?dl=1"));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    onBackPressed();
                }
                break;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    class LoadMarkersWithJsonTask extends AsyncTask<URL, String, ArrayList<Place>> {

        @Override
        protected ArrayList<Place> doInBackground(URL... params) {
            ArrayList<Place> places = null;
            HttpURLConnection httpURLConnection = null;

            try {
                httpURLConnection = (HttpURLConnection) params[0].openConnection();
                httpURLConnection.setConnectTimeout(20 * 1000);
                httpURLConnection.setReadTimeout(10 * 1000);

                if (httpURLConnection.getResponseCode() == 200) {
                    InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());

                    GSonPlaceParser gSonPlaceParser = new GSonPlaceParser();
                    places = gSonPlaceParser.getPlaces(inputStream);
                }
            } catch (IOException e) {
                Log.e(getClass().getSimpleName(), e.getMessage());
            } finally {
                assert httpURLConnection != null;
                httpURLConnection.disconnect();
            }

            return places;
        }

        @Override
        protected void onPostExecute(final ArrayList<Place> places) {
            super.onPostExecute(places);

            if (places == null || places.size() == 0) {
                Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        SMTravelVoice.getInstance().setPlaces(places);

                        Navigator.with(SplashScreenActivity.this).build().goTo(MapsActivity.class).animation(android.R.anim.fade_in, android.R.anim.fade_out).commit();
                        finish();
                    }
                }, 2500);
            }
        }
    }
}
