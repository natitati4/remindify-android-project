package com.example.yearprojectfinal;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// This class represents the main locations activity, where the user can see all the locations
// they added
public class MainLocationsActivity extends AppCompatActivity implements OnMapReadyCallback {

    int PRESSED_LOCATION_ACTIVITY_REQUEST_CODE = 1;
    int ADD_LOCATION_ACTIVITY_REQUEST_CODE = 2;
    int ACCOUNT_ACTIVITY_REQUEST_CODE = 3;

    ListView locationListView;
    ArrayList<LocationClass> locationsList;
    LocationAdapter locationAdapter;

    LocationClass currentLocationClass;
    LocationClass locationClassToDelete;

    String connectedUsername;
    private final String activityName = this.getClass().getSimpleName();

    GoogleMap mMap;

    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;

    Map<String, Geofence> currentGeofences = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_locations_list);

        Intent previousIntent = getIntent();
        connectedUsername = previousIntent.getExtras().getString("connected username");

        locationsList = UtilityClass.getLocationListForUser
                (activityName, connectedUsername);

        if (locationsList == null) {
            Toast.makeText(this, "Server did not respond", Toast.LENGTH_SHORT).show();
            return;
        }

        // if came from notification, start the tasks activity.
        if (previousIntent.getExtras().getBoolean("From location notification")) {


            Intent mainTasksByLocationIntent =
                    new Intent(MainLocationsActivity.this, MainTasksByLocationActivity.class);

            LocationClass notifiedLocation =
                    (LocationClass) previousIntent.getSerializableExtra("entire location object");

            if (UtilityClass.searchLocationInListById(locationsList, notifiedLocation)) {

                mainTasksByLocationIntent.putExtra("entire location object", notifiedLocation);

                mainTasksByLocationIntent.putExtra("location name",
                        previousIntent.getExtras().getString("location name"));

                mainTasksByLocationIntent.putExtra("location tasks list",
                        previousIntent.getSerializableExtra("location tasks list"));

                mainTasksByLocationIntent.putExtra("current location json string",
                        previousIntent.getExtras().getString("current location json string"));


                startActivityForResult(mainTasksByLocationIntent, PRESSED_LOCATION_ACTIVITY_REQUEST_CODE);
            } else {
                Toast.makeText(getApplicationContext(),
                        "Location does not exist", Toast.LENGTH_SHORT).show();
            }
        }

        // setup map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        // setup geofences
        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceHelper = new GeofenceHelper(this);

        setupGeofences();

        locationListView = (ListView) findViewById(R.id.locationsListView);
        locationAdapter =
                new LocationAdapter(this, 0, 0, locationsList);
        locationListView.setAdapter(locationAdapter);

        locationListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            // Go to the tasks activity of a location upon clicking that location in the list view
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentLocationClass = locationAdapter.getItem(position);
                Intent intent = new Intent(MainLocationsActivity.this, MainTasksByLocationActivity.class);

                intent.putExtra("location name", currentLocationClass.getName());
                intent.putExtra("location tasks list", currentLocationClass.getTasksList());

                Gson gson = new Gson();
                intent.putExtra("current location json string", gson.toJson(currentLocationClass));

                intent.putExtra("entire location object", currentLocationClass);

                startActivityForResult(intent, PRESSED_LOCATION_ACTIVITY_REQUEST_CODE);

            }
        });

        locationListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            // Call the function to show the location deletion dialog upon long clicking a location
            // in the list view
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {

                locationClassToDelete = locationAdapter.getItem(position);
                showLocationDeleteConfirmationDialog();
                return true;
            }
        });

    }

    // Initialize the map with the locations
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // add the circles
        for (LocationClass location : locationsList) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            float radius = location.getRadius();

            addCircle(new LatLng(latitude, longitude), radius);
        }

        // Enable the option for the user to move the map to his current location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

    }

    // Create the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_tasks_screen_menu, menu);
        return true;
    }

    // Handle menu items click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle menu item clicks here
        switch (item.getItemId()) {
            case (R.id.actionAddLocation):

                Intent addLocationActivity = new Intent(
                        MainLocationsActivity.this, AddLocationActivity.class);
                addLocationActivity.putExtra("username", connectedUsername);
                startActivityForResult(addLocationActivity, ADD_LOCATION_ACTIVITY_REQUEST_CODE);
                return true;

            case (R.id.actionProfile):
                Intent accountActivity = new Intent(
                        MainLocationsActivity.this, AccountActivity.class);
                accountActivity.putExtra("username", connectedUsername);
                startActivityForResult(accountActivity, ACCOUNT_ACTIVITY_REQUEST_CODE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Function to show the location deletion dialog
    private void showLocationDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete location");
        builder.setMessage("Are you sure you want to delete the location: " + locationClassToDelete.getName());

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Gson gson = new Gson();
                String deletedLocationJsonString = gson.toJson(locationClassToDelete);

                String dataStrToSend = "requesting location deletion|" + deletedLocationJsonString;

                // send data to server
                String resultStr = UtilityClass.sendAndReceive(UtilityClass.constructString(activityName, dataStrToSend));

                if (resultStr.equals("location deleted successfully")) {
                    Toast.makeText(getApplicationContext(),
                            "Location deleted.", Toast.LENGTH_SHORT).show();

                    // updating the adapter, and the listView.
                    locationsList.remove(locationClassToDelete);
                    locationAdapter =
                            new LocationAdapter(MainLocationsActivity.this, 0, 0, locationsList);
                    locationListView.setAdapter(locationAdapter);

                    updateAfterLocationsChanged();

                    dialog.dismiss();
                }

                else
                {
                    Toast.makeText(getApplicationContext(),
                            "Something went wrong with deleting the location. Try refreshing the app.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Add a geofence for a certain location, give an id and a location object
    private void addGeofence(String currentGeofenceId, LocationClass locationObject) {
        Geofence currentGeofence = geofenceHelper.getGeofence(
                currentGeofenceId,
                locationObject.getLatitude(),
                locationObject.getLongitude(),
                locationObject.getRadius(),
                Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_DWELL |
                        Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(currentGeofence);

        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();

        // requesting permissions

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //pass
        }

        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        currentGeofences.put(currentGeofenceId, currentGeofence);
                        Log.d(activityName, "onSuccess triggered. Geofence added: " + locationObject.getName());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(activityName, "onFailure triggered.");
                        Log.e(activityName, e.toString());
                    }
                });
    }

    // Add a circle to the map
    private void addCircle(LatLng latLng, float radius) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255, 255, 0,0));
        circleOptions.fillColor(Color.argb(64, 255, 0,0));
        circleOptions.strokeWidth(4);

        mMap.addCircle(circleOptions);
    }

    // Update data and UI upon returning from other activities
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        super.onActivityResult(requestCode, resultCode, dataIntent);

        // if returned from the activity the comes from pressing location (MainTasksByLocationActivity)
        if (requestCode == PRESSED_LOCATION_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK)
            {
                updateAfterLocationsChanged();
            }
        }

        // if returned from add location
        if (requestCode == ADD_LOCATION_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK)
            {
                updateAfterLocationsChanged();
            }
        }

        if (requestCode == ACCOUNT_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // if log out button was pressed
                if (dataIntent.getExtras().getBoolean("logging out"))
                {
                    // removing all present geofences
                    List<String> geofenceIds = new ArrayList<>(currentGeofences.keySet());
                    geofencingClient.removeGeofences(geofenceIds);

                    Intent previousIntent = getIntent();
                    setResult(RESULT_OK, previousIntent);
                    finish();
                    return;
                }

                // change connected username to new username
                connectedUsername = dataIntent.getExtras().getString("new username");
            }
        }
    }

    // Update data and UI after locations have been changed in some way
    private void updateAfterLocationsChanged()
    {
        locationsList = UtilityClass.getLocationListForUser(activityName, connectedUsername);

        if (locationsList == null)
        {
            Toast.makeText(this, "Server did not respond", Toast.LENGTH_SHORT).show();
            return;
        }

        locationAdapter =
                new LocationAdapter(this, 0, 0, locationsList);
        locationListView.setAdapter(locationAdapter);

        // refresh circles
        mMap.clear();
        for (LocationClass location : locationsList)
        {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            float radius = location.getRadius();
            addCircle(new LatLng(latitude, longitude), radius);
        }

        // refresh geofences
        refreshGeofences();
    }

    // Initial setup the geofences for all the locations
    private void setupGeofences()
    {
        for (LocationClass location : locationsList)
        {
            Log.d(activityName, "Adding geofence: " + location.getName());
            String uniqueId = String.valueOf(location.getId());

            addGeofence(uniqueId, location);
        }
    }

    // Re-setup the geofences after location have been changed in some way
    private void refreshGeofences()
    {
        List<String> geofenceIds = new ArrayList<>(currentGeofences.keySet());

        // remove all geofences and re-setup them
        geofencingClient.removeGeofences(geofenceIds);
        currentGeofences.clear();

        setupGeofences();
    }

}
