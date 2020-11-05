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
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.github.nikartm.button.FitButton;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


public class AudioService extends Service  {
    private static String TAG = "AudioService";
    private Context context;


    final int ALPHA = 1001;
    final int BETA = 1002;
    final int STOP = 1000;
    private MediaPlayer media_player;
    private AudioManager audioManager;
    private Boolean play = false;
    private Timer timer;

    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_SEND_TO_SERVICE = 3;
    public static final int MSG_SEND_TO_ACTIVITY = 4;
    static int num;

    private Messenger mClient = null;
    private Handler mHandler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        Log.e(TAG, TAG + " onCreate");
        App.audioServiceBound = true;
        audioManager = (AudioManager) getSystemService(context.AUDIO_SERVICE);
        initializeNotification("서비스 동작중", "");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, TAG + " onStartCommand");

        mHandler = new Handler();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, TAG + " onDestroy");
        App.audioServiceBound = false;
        try {
            playSound(STOP);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void initializeNotification(String str1, String str2) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
        style.bigText(str2);
        style.setBigContentTitle(null);
        style.setSummaryText(str1);
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
        play = false;
        if (type == ALPHA) {
            if(App.isPlaying_beta){
                media_player.stop();
                App.isPlaying_alpha = true;
                App.isPlaying_beta = false;
                play = true;
                media_player = MediaPlayer.create(this, R.raw.alpha);
            }
            else if(App.isPlaying_alpha) {
                type = STOP;
            }else{
                media_player = MediaPlayer.create(this, R.raw.alpha);
                App.isPlaying_alpha = true;
                play = true;
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
                App.isPlaying_beta = true;
                play = true;
                media_player = MediaPlayer.create(this, R.raw.beta);
            }
            else if(App.isPlaying_beta) {
                type = STOP;
            }else{
                media_player = MediaPlayer.create(this, R.raw.beta);
                App.isPlaying_beta = true;
                play = true;
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

        if(type == STOP){
            App.isPlaying_alpha = false;
            App.isPlaying_beta = false;
            play = false;
            media_player.stop();
            timer.cancel();
            sendMsgToActivity(1);
        }

        if(play){
            String str = null;
            double vol = 0;
            if(App.isPlaying_alpha){
                vol = 0.8;
                str = "알파파 재생중";
            }
            else if(App.isPlaying_beta) {
                vol = 0.4;
                str = "베타파 재생중";
            }
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    (int) (audioManager.getStreamMaxVolume(audioManager.STREAM_MUSIC) * vol),
                    audioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            media_player.setLooping(true);
            media_player.start();

            final String finalStr = str;
            num = 0;
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    initializeNotification(finalStr, time(num));
                    num++;
                    if(num == 7200) {
                        try {
                            playSound(STOP);
                            mHandler.post(new ToastRunnable("재생시간 2시간경과, 재생을 자동으로 중지합니다."));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            if(play){
                if(timer == null){
                    timer = new Timer();
                }
                timer.cancel();
                timer = new Timer();
                timer.schedule(timerTask, 0, 1000);
            }
        }else{
            initializeNotification("서비스 동작중", "");
        }

    }


    /** activity로부터 binding 된 Messenger */
    private final Messenger mMessenger = new Messenger(new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.w(TAG,"AudioService - message what : "+msg.what +" , msg.obj "+ msg.obj);
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClient = msg.replyTo;
                    Log.e(TAG, "connection : " + mClient.toString());
                case MSG_SEND_TO_SERVICE:
                    switch (msg.obj.toString()){
                        case "ALPHA" :
                            try {
                                playSound(ALPHA);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        case "BETA":
                            try {
                                playSound(BETA);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            Log.e(TAG, "AudioService : No TYPE (msg : "+msg.replyTo+")");
                            break;
                    }
                    sendMsgToActivity(1);
                    break;
            }
            return false;
        }
    }));

    private void sendMsgToActivity(int sendValue) {
        try {
            Bundle bundle = new Bundle();
            bundle.putInt("fromService", sendValue);
            Message msg = Message.obtain(null, MSG_SEND_TO_ACTIVITY);
            msg.setData(bundle);
            mClient.send(msg);      // msg 보내기
        } catch (RemoteException e) {
        }
    }

    private String time(int num){
        int se = num % 60;
        int m = num%(60*60)/(60);
        int h = num%(60*60*24)/(60*60);
        if(num < 60)
            return "0시간 0분 " + num + "초";
        else {
            return h + "시간 " + m + "분 " + se + "초";
        }

    }


    private class ToastRunnable implements Runnable {
        String mText;
        public ToastRunnable(String text) {
            mText = text;
        }
        @Override
        public void run(){
            Toast.makeText(getApplicationContext(), mText, Toast.LENGTH_SHORT).show();
        }
    }


}
