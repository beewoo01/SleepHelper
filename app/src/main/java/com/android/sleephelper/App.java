package com.android.sleephelper;


import android.app.ActivityManager;
import android.content.Context;
import android.net.Network;

public class App extends android.app.Application {

    public static Network net_wifi, net_mobile;
    public static boolean isPlaying_alpha = false, isPlaying_beta = false;
    public static boolean audioServiceBound = false;

    @Override
    public void onCreate(){
        super.onCreate();
    }


    public static boolean isServiceRunning(Context context) {
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo rsi : am.getRunningServices(Integer.MAX_VALUE)) {
            if (AudioService.class.getName().equals(rsi.service.getClassName()))
                return true;
        }
        return false;
    }



}
