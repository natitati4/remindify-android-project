package com.example.yearprojectfinal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;

import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

// This class represents the activity for choosing a location
public class ChooseLocationActivity extends AppCompatActivity implements
        OnMapReadyCallback, GoogleMap.OnCameraIdleListener, GoogleMap.OnCameraMoveListener {
    private final String activityName = "ChooseLocationActivity";
    private static final int REQUEST_LOCATION_PERMISSION = 5;


    TextView tvLatLng;
    TextView tvAddressAddressInfo;

    GoogleMap mMap;

    ImageButton btnBack;
    Button btnSubmit;

    String addressInfo;
    LatLng currentLatLng;

    LocationRequest locationRequest;

    Geocoder geocoder;

    Marker marker; // a good ol' friendly marker, which will follow our camera.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_choose_location);

        btnBack = findViewById(R.id.btnChooseLocationBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        LocationClass currentLocationObjectClass =
                (LocationClass) getIntent().getSerializableExtra("entire location object");

        if (currentLocationObjectClass != null)
        {
            Double latitude = currentLocationObjectClass.getLatitude();
            Double longitude = currentLocationObjectClass.getLongitude();
            currentLatLng = new LatLng(latitude, longitude); // Create a new LatLng object with the parsed values

            addressInfo = currentLocationObjectClass.getAddressName();
        }

        // create geocoder for activity
        geocoder = new Geocoder(ChooseLocationActivity.this);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        tvLatLng = findViewById(R.id.tvChooseLocationCoordinates);
        tvAddressAddressInfo = findViewById(R.id.ChooseLocationFeatureName);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
               .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);



         // Initialize Places. Without that we can't use the Places SDK.
        if (!Places.isInitialized())
        {
            Places.initialize(getApplicationContext(), getString(R.string.api_key));
        }

        AutocompleteSupportFragment autocompleteFragment =
                (AutocompleteSupportFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.getView().setBackgroundColor(Color.WHITE);

        autocompleteFragment.setPlaceFields(Arrays.asList
                (Place.Field.LAT_LNG)); // only need latLng
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener()
        {
            // When a place is selected from the auto complete, this function is called to update data and UI
            @Override
            public void onPlaceSelected(@NonNull Place place)
            {
                // Get LatLng of selected place.
                currentLatLng = place.getLatLng();

                // Get the address from the latitude and longitude.
                try
                {
                    List<Address> addresses =
                            geocoder.getFromLocation(currentLatLng.latitude, currentLatLng.longitude, 1);
                    if (!addresses.isEmpty()) {
                        Address address = addresses.get(0);

                        // Get the feature name (e.g., a street name or place name)
                        StringBuilder currentAddressInfo = new StringBuilder();
                        int maxIndex = address.getMaxAddressLineIndex();
                        for (int i = 0; i <= maxIndex; i++) {
                            String line = address.getAddressLine(i);
                            currentAddressInfo.append(line);
                        }
                        addressInfo = currentAddressInfo.toString(); // save address info
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                tvLatLng.setText(currentLatLng.toString());
                tvAddressAddressInfo.setText("Info about the location: " + addressInfo);

                mMap.clear(); // clear existing markers

                // on below line we are adding marker to that position.
                marker = mMap.addMarker(new MarkerOptions().position(currentLatLng).title(addressInfo));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));

            }

            @Override
            public void onError(@NonNull Status status) {
                // Handle the error.
                Toast.makeText(ChooseLocationActivity.this,
                        "An error occurred.", Toast.LENGTH_SHORT).show();
                Log.e(activityName, status.toString());
            }
        });

        btnSubmit = findViewById(R.id.ChooseLocationBtnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle submit button press
                Intent previousIntent = getIntent();

                if (addressInfo == null || currentLatLng == null)
                {
                    Toast.makeText(getApplicationContext(), "Please choose a location", Toast.LENGTH_SHORT).show();
                    return;
                }

                previousIntent.putExtra("location address name", addressInfo);
                previousIntent.putExtra("location lat", currentLatLng.latitude);
                previousIntent.putExtra("location long", currentLatLng.longitude);

                setResult(RESULT_OK, previousIntent);
                finish();
            }
        });
    }


    // This function is called when the map is ready to be used. It sets up the map and gets the
    // user's current location. It also sets up a search view that allows the user to search for
    // a location on the map.
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Save the GoogleMap object
        mMap = googleMap;

        enableUserLocation();

        // set listener to camera moves.
       mMap.setOnCameraIdleListener(this);
       mMap.setOnCameraMoveListener(this);


        // Check if the activity was started from the EditLocationActivity. Meaning we have
        // a location to display already.
        if (currentLatLng != null && addressInfo != null) {
            // Update the TextViews with the latitude and longitude of the location, and the feature name
            tvLatLng.setText("Latitude: " + currentLatLng.latitude + ", longitude: " + currentLatLng.longitude);
            tvAddressAddressInfo.setText("Info about the location: " + addressInfo);

            mMap.clear(); // clear existing markers

            // on below line we are adding marker to that position.
            marker = mMap.addMarker(new MarkerOptions().position(currentLatLng).title(addressInfo));

            // below line is to animate camera to that position.
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));

            // Return early to skip the rest of the method
            return;
        }
    }

    // Request the user to enable GPS if not enabled
    private void enableUserLocation()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            mMap.setMyLocationEnabled(true);
        }

        else
        {
            // Ask for permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
            {
                // We need to show user a dialog for displaying why the permission is needed and then ask for the permission...
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            }
            else
            {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            }
        }
    }

    // Update the coords and details when the camera is idle - finished moving.
    // saves resources and makes it less laggy.
    @Override
    public void onCameraIdle()
    {
        // Get the center of the map
        LatLng center = mMap.getCameraPosition().target;
        currentLatLng = new LatLng(center.latitude, center.longitude);

        // Get the address from the latitude and longitude
        try
        {
            List<Address> addresses = geocoder.getFromLocation(center.latitude, center.longitude, 1);
            if (!addresses.isEmpty())
            {
                Address address = addresses.get(0);

                // Get the feature name (e.g., a street name or place name)
                StringBuilder currentAddressInfo = new StringBuilder();
                int maxIndex = address.getMaxAddressLineIndex();
                for (int i = 0; i <= maxIndex; i++) {
                    String line = address.getAddressLine(i);
                    currentAddressInfo.append(line);
                }
                addressInfo = currentAddressInfo.toString();
            }
            // if addresses is empty, the location isn't valid (a known one). Set address info to null.
            else
                addressInfo = null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        tvLatLng.setText(currentLatLng.toString());
        tvAddressAddressInfo.setText("Info about the location: " + addressInfo);

        mMap.clear(); // clear existing markers

        // on below line we are adding marker to that position.
        marker = mMap.addMarker(new MarkerOptions().position(center).title(addressInfo));
    }

    // Move the marker with the camera moving, so the user can keep track on where he is moving it.
    @Override
    public void onCameraMove()
    {
        LatLng center = mMap.getCameraPosition().target;

        mMap.clear(); // clear existing markers
        marker = mMap.addMarker(new MarkerOptions().position(center));
    }
}


