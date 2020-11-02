package com.android.sleephelper;


import android.net.Network;

public class App extends android.app.Application {

    public static Network net_wifi, net_mobile;
    public static boolean isPlaying_alpha = false, isPlaying_beta = false;

    @Override
    public void onCreate(){
        super.onCreate();
    }




}
