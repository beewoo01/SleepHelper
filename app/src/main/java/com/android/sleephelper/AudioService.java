package com.android.sleephelper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.github.nikartm.button.FitButton;

import java.io.IOException;


public class AudioService extends Service  {
    private static String TAG = "AudioService";
    private Context context;


    final int ALPHA = 1000;
    final int BETA = 1001;
    MediaPlayer media_player;
    FitButton button_alpha, button_beta;
    private AudioManager audioManager;
    Boolean play = false;
    Drawable ic_play, ic_stop;

    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_SEND_TO_SERVICE = 3;
    public static final int MSG_SEND_TO_ACTIVITY = 4;

    private Messenger mClient = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        Log.e(TAG, "BeaconScanService onCreate");

        initializeNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "BeaconScanService onStartCommand");


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "BeaconScanService onDestroy");

    }

    public void initializeNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
        style.bigText("???");
        style.setBigContentTitle(null);
        style.setSummaryText("서비스 동작중");
        builder.setContentText(null);
        builder.setContentTitle(null);
        builder.setOngoing(true);
        builder.setStyle(style);
        builder.setWhen(0);
        builder.setShowWhen(false);

        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        builder.setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(new NotificationChannel("1", "undead_service", NotificationManager.IMPORTANCE_NONE));
        }
        Notification notification = builder.build();
        startForeground(1, notification);
    }



    private void playSound(int type) throws IOException {
        // Log.e("status", "play : " + isPlaying + "  " + type + " : start");
        play = false;

        if (type == ALPHA) {
            if(App.isPlaying_beta){
                media_player.stop();
                App.isPlaying_beta = false;
                play = true;
            }
            else if(App.isPlaying_alpha) {
                media_player.stop();
                App.isPlaying_alpha = false;
            }else{
                media_player = MediaPlayer.create(this, R.raw.alpha);
                media_player.setLooping(true);
                App.isPlaying_alpha = true;
                media_player.start();
                double vol = 0.8;
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        (int) (audioManager.getStreamMaxVolume(audioManager.STREAM_MUSIC) * vol),
                        audioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                /*
                new Thread(new Runnable(){
                    public void run(){
                        play = true;
                        double vol = 0.1;
                        while (play){
                            if(play) {
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                        (int) (audioManager.getStreamMaxVolume(audioManager.STREAM_MUSIC) * vol),
                                        audioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                                vol = vol + 0.01;
                            }
                            if(vol > 0.6)
                                play = false;
                            try {
                                Log.e("vol", "vol : " + vol);
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
                 */
            }
        } else if (type == BETA) {
            if(App.isPlaying_alpha){
                media_player.stop();
                App.isPlaying_alpha = false;
                button_beta.setIcon(ic_play);
                button_alpha.setIcon(ic_play);
            }
            else if(App.isPlaying_beta) {
                media_player.stop();
                App.isPlaying_beta = false;
                button_beta.setIcon(ic_play);
            }else{
                media_player = MediaPlayer.create(this, R.raw.beta);
                media_player.setLooping(true);
                App.isPlaying_beta = true;
                media_player.start();
                button_beta.setIcon(ic_stop);
                double vol = 0.6;
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        (int) (audioManager.getStreamMaxVolume(audioManager.STREAM_MUSIC) * vol),
                        audioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                /*
                new Thread(new Runnable(){
                    public void run(){
                        play = true;
                        double vol = 0.1;
                        while (play){
                            if(play) {
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                        (int) (audioManager.getStreamMaxVolume(audioManager.STREAM_MUSIC) * vol),
                                        audioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                                vol = vol + 0.01;
                            }
                            if(vol > 1)
                                play = false;
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
                 */
            }
        }
        button_alpha.setEnabled(true);
    }

    /** activity로부터 binding 된 Messenger */
    private final Messenger mMessenger = new Messenger(new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.w("test","ControlService - message what : "+msg.what +" , msg.obj "+ msg.obj);
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClient = msg.replyTo;  // activity로부터 가져온
                    break;
            }
            return false;
        }
    }));

    private void sendMsgToActivity(int sendValue) {
        try {
            Bundle bundle = new Bundle();
            bundle.putInt("fromService", sendValue);
            bundle.putString("test","abcdefg");
            Message msg = Message.obtain(null, MSG_SEND_TO_ACTIVITY);
            msg.setData(bundle);
            mClient.send(msg);      // msg 보내기
        } catch (RemoteException e) {
        }
    }

}
