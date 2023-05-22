package com.example.yearprojectfinal;

import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;

// This class contains functions used to initialize the geofences
public class GeofenceHelper extends ContextWrapper
{
    private static final int REQUEST_CODE = 50005;
    PendingIntent pendingIntent;

    public GeofenceHelper(Context base) {
        super(base);
    }

    // Creates and returns a new GeofencingRequest object.
    public GeofencingRequest getGeofencingRequest(Geofence geofence)
    {
        return new GeofencingRequest.Builder()
                .addGeofence(geofence)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();
    }

    // Creates and returns a new Geofence object.
    public Geofence getGeofence(String id, double latitude, double longitude, float radius, int transitionTypes)
    {
        return new Geofence.Builder()
                .setCircularRegion(latitude, longitude, radius)
                .setRequestId(id)
                .setTransitionTypes(transitionTypes)
                .setLoiteringDelay(5000)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }

    // creates and returns a new PendingIntent object with a GeofenceBroadcastReceiver
    public PendingIntent getPendingIntent()
    {
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);

        pendingIntent = PendingIntent.getBroadcast(this, REQUEST_CODE , intent,
                PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);


        return pendingIntent;
    }
}
