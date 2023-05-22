package com.example.yearprojectfinal;

import android.app.ActivityManager;
import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

// This class is used to check if the app is running in the foreground, to decide whether to send
// a notification or not
class ForegroundCheckTask extends AsyncTask<Context, Void, Boolean> {

    // The inherited function of AsyncTask
    @Override
    protected Boolean doInBackground(Context... params) {
        final Context context = params[0].getApplicationContext();
        return isAppOnForeground(context);
    }

    // The actual function that checks if the app is in foreground
    private boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null)
        {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && appProcess.processName.equals(packageName))
            {
                // found app in process list. It's running in foreground.
                return true;
            }
        }
        return false;
    }
}
