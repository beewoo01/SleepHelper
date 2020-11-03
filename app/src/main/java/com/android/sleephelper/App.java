package com.android.sleephelper;


import android.net.Network;

public class App extends android.app.Application {

    public static Network net_wifi, net_mobile;
    public static boolean isPlaying_alpha = false, isPlaying_beta = false;
    public static boolean audioServiceBound = false;

    @Override
    public void onCreate(){
        super.onCreate();
    }




}
