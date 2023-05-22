package com.example.yearprojectfinal;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.gson.Gson;

// This class contains functions to build and send notifications
public class NotificationHelper extends ContextWrapper
{
    // Initializes the superclass with the given Context object and creates a notification channel
    public NotificationHelper(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            // need to do it through notification channels
            createChannels();
        }
    }

    private String CHANNEL_NAME = "High priority channel";
    private String CHANNEL_ID = "com.example.notifications" + CHANNEL_NAME;

    // Creates a notification channel for the app.
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannels() {
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setDescription("this is the description of the channel.");
        notificationChannel.setLightColor(Color.BLUE);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(notificationChannel);
    }

    // Sends a high priority notification with the given title and body to the user, with an
    // associated activity and a location
    public void sendHighPriorityNotification(String title, String body, Class activity, LocationClass... locations) {

        Intent intent = new Intent(this, activity);

        if (locations == null)
            return;

        // adding the needed things to the intent, so the app will start with the task list
        // of the location.
        LocationClass notifiedLocation = locations[0];

        intent.putExtra("From location notification", true);

        intent.putExtra("location name",
                notifiedLocation.getName());

        intent.putExtra("location tasks list",
                notifiedLocation.getTasksList());

        Gson gson = new Gson();
        String notifiedLocationJsonString = gson.toJson(notifiedLocation);
        intent.putExtra("current location json string",
                notifiedLocationJsonString);

        intent.putExtra("entire location object",
                notifiedLocation);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                50002,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                // .setContentTitle(title)
                // .setContentText(body)
                .setSmallIcon(R.drawable.ic_baseline_gps_fixed_24)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(title).bigText(body))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        // need a unique id so the notifications can stack up and not replace each other.
        NotificationManagerCompat.from(this).notify(notifiedLocation.getId(), notification);
    }

}

