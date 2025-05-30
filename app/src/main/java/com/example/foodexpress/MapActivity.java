package com.example.foodexpress;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MapActivity extends AppCompatActivity implements LocationListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private MapView map;
    private IMapController mapController;
    private MyLocationNewOverlay locationOverlay;
    private LocationManager locationManager;
    private Button buttonSetLocation;
    private GeoPoint currentLocation;
    private Marker startMarker;

    // Initial coordinates for Tunis
    private static final double INITIAL_LATITUDE = 33.8818;
    private static final double INITIAL_LONGITUDE = 10.0982;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize OSMDroid configuration
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_map);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Select Delivery Location");

        // Initialize map
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        // Initialize map controller
        mapController = map.getController();
        mapController.setZoom(15.0);

        // Set initial location to Tunis
        currentLocation = new GeoPoint(INITIAL_LATITUDE, INITIAL_LONGITUDE);
        mapController.setCenter(currentLocation);

        // Add marker at initial position
        startMarker = new Marker(map);
        startMarker.setPosition(currentLocation);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setTitle("Tunis - Position initiale");
        map.getOverlays().add(startMarker);

        // Make marker draggable
        startMarker.setDraggable(true);
        startMarker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(Marker marker) {
                // Do nothing while dragging
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                GeoPoint newPosition = marker.getPosition();
                currentLocation = newPosition;
                Toast.makeText(MapActivity.this,
                        "Nouveau point : " + newPosition.getLatitude() + ", " + newPosition.getLongitude(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMarkerDragStart(Marker marker) {
                // Do nothing when drag starts
            }
        });

        // Initialize location overlay
        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        locationOverlay.enableMyLocation();
        map.getOverlays().add(locationOverlay);

        // Initialize button
        buttonSetLocation = findViewById(R.id.buttonSetLocation);
        buttonSetLocation.setOnClickListener(v -> {
            if (currentLocation != null) {
                // Return location to previous activity
                getIntent().putExtra("latitude", currentLocation.getLatitude());
                getIntent().putExtra("longitude", currentLocation.getLongitude());
                setResult(RESULT_OK, getIntent());
                finish();
            } else {
                Toast.makeText(this, "Please wait for location to be determined", Toast.LENGTH_SHORT).show();
            }
        });

        // Check for location permission
        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // Only update if we don't have a location yet
        if (currentLocation == null ||
                (currentLocation.getLatitude() == INITIAL_LATITUDE &&
                        currentLocation.getLongitude() == INITIAL_LONGITUDE)) {

            currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
            mapController.setCenter(currentLocation);

            // Update marker position
            startMarker.setPosition(currentLocation);
            map.invalidate();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
