package com.unimagdalena.android.app.smtravelvoice;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.entire.sammalik.samlocationandgeocoding.SamLocationRequestService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mukesh.permissions.AppPermissions;

import org.fingerlinks.mobile.android.navigator.Navigator;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private ArrayList<Place> placeArrayList;
    private AppPermissions appPermissions;
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        appPermissions = new AppPermissions(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onBackPressed() {
        Navigator.with(this).utils().finishWithAnimation(android.R.anim.fade_in, android.R.anim.fade_out);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        /*List<Integer> permissionResults = new ArrayList<>();

        switch (requestCode) {
            case 100:
                for (int grantResult : grantResults) {
                    permissionResults.add(grantResult);
                }

                if (permissionResults.contains(PackageManager.PERMISSION_DENIED)) {
                    onBackPressed();
                } else {
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    mapFragment.getMapAsync(this);
                }

                break;
        }

        permissionResults.clear();*/
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
            try {
                new LoadMarkersWithJsonTask().execute(new URL("https://www.dropbox.com/s/8zgkyhp4jt9qsr0/places.json?dl=1"));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
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
            if (place.getPlaceId().equals(marker.getId())) {
                markerPlace = place;
                break;
            }
        }

        Bundle bundle = new Bundle();
        bundle.putSerializable("place", markerPlace);

        Navigator.with(this).build().goTo(DetailActivity.class, bundle).animation().commit();

        return false;
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
        protected void onPostExecute(ArrayList<Place> places) {
            super.onPostExecute(places);

            if (places == null || places.size() == 0) {
                Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
            } else {
                map.setOnMarkerClickListener(MapsActivity.this);

                placeArrayList = places;

                for (Place place : places) {
                    map.addMarker(new MarkerOptions().position(new LatLng(place.getCoordinates().getLatitude(), place.getCoordinates().getLongitude())).title(place.getName()));
                }
            }
        }
    }
}
