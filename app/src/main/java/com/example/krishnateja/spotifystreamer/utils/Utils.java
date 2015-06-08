package com.example.krishnateja.spotifystreamer.utils;

import android.app.ActivityManager;
import android.content.Context;

/**
 * Created by krishnateja on 6/7/2015.
 */
public class Utils {

    public static boolean isMyServiceRunning(Class<?> serviceClass,
                                             Context context) {
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        boolean running = false;
        for (ActivityManager.RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                running = true;
            }
        }
        return running;
    }
}
