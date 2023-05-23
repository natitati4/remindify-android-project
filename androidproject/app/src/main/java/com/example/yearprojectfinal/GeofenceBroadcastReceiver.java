package com.example.yearprojectfinal;

import static android.content.Context.MODE_PRIVATE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

// This class is used to catch the broadcasts sent by the android OS geofences that were added earlier,
// indicating that the user has entered one of his locations

public class GeofenceBroadcastReceiver extends BroadcastReceiver
{
    private static final String className = "GeofenceBroadcastReceiver";

    // Receiver function
    @Override
    public void onReceive(Context context, Intent intent)
    {

        Log.d(className, "Broadcast received: geofence triggered");

        try
        {
            boolean isAppInForeground = new ForegroundCheckTask().execute(context).get();
            if (isAppInForeground)
            {
                Log.d(className, "But app is not closed. Skip and do not send notification.");
                return;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        NotificationHelper notificationHelper = new NotificationHelper(context);
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError())
        {
            Log.e(className, "Geofencing event has error: " + geofencingEvent.getErrorCode());
            return;
        }

        int transitionType = geofencingEvent.getGeofenceTransition();

        switch (transitionType)
        {
            case Geofence.GEOFENCE_TRANSITION_ENTER:

                Toast.makeText(context, "Entered geofence", Toast.LENGTH_SHORT).show();

                // find location by the geofence id
                SharedPreferences sharedPref = context.getSharedPreferences("remindify_sp", MODE_PRIVATE);
                String connectedUsername = sharedPref.getString("connected username", null);

                List<Geofence> triggeredGeofences = geofencingEvent.getTriggeringGeofences();
                ArrayList<LocationClass> locationsList = UtilityClass.getLocationListForUser(className, connectedUsername);

                if (locationsList == null)
                    Toast.makeText(context, "Remindify: Server did not respond Server did" +
                                                " not respond or problem with database", Toast.LENGTH_SHORT).show();

                LocationClass notifiedLocation = findNotifiedLocationByGeofenceId(triggeredGeofences, locationsList);

                // send notification!
                notificationHelper.sendHighPriorityNotification(
                        "Entered location: " + notifiedLocation.getName(),
                        "Click here to view it's task list!",
                        LoginActivity.class, notifiedLocation);
                break;

            case Geofence.GEOFENCE_TRANSITION_DWELL:
                // Toast.makeText(context, "Dwelled geofence", Toast.LENGTH_SHORT).show();
                break;

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                // Toast.makeText(context, "Exited geofence", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    // Find the location by the geofence id (the geofence id is the location's id)
    private LocationClass findNotifiedLocationByGeofenceId(List<Geofence> triggeredGeofences,
                                                           ArrayList<LocationClass> locationsList)
    {
        Geofence geofence = triggeredGeofences.get(0);
        String geofenceId = geofence.getRequestId();

        for (LocationClass location : locationsList)
        {
            if (geofenceId.equals(String.valueOf(location.getId())))
            {
                // found location with corresponding geofence id
                return location;
            }
        }
        return null;
    }
}