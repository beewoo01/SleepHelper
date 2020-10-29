package com.android.sleephelper;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nikartm.button.FitButton;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {
    Context activity;

    private String ve = "2020-10-29";
    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> devices;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket = null;
    int pariedDeviceCount;

    Thread Sound;

    final int ALPHA = 1000;
    final int BETA = 1001;
    MediaPlayer media_player;
    FitButton button_alpha, button_beta;
    TextView textView_version;
    private boolean isPlaying_alpha = false, isPlaying_beta = false;
    private AudioManager audioManager;
    Boolean play = false;
    Drawable ic_play, ic_stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //25 -> 60
        //11 -> 80
        activity = this;

        button_alpha = findViewById(R.id.main_BT_alpha);
        button_beta = findViewById(R.id.main_BT_beta);
        ic_play = getDrawable(R.drawable.ic_play_white);
        ic_stop = getDrawable(R.drawable.ic_stop_white);
        textView_version = findViewById(R.id.main_TV_version);
        textView_version.setText(ve);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        audioManager = (AudioManager) getSystemService(activity.AUDIO_SERVICE);


        button_alpha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    playSound(ALPHA);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        button_beta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    playSound(BETA);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        //selectBluetoothDevice();

        Sound = new Thread(){
            public void run(){

            }
        };
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (bluetoothAdapter == null) {
            Toast.makeText(activity, "블루투스 미지원 기기!",Toast.LENGTH_LONG);
            finish();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Toast.makeText(activity, "블루투스를 켜주세요!",Toast.LENGTH_LONG);
                Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intent);
            }
        }
    }


    @Override
    protected void onStop(){
        super.onStop();
        try {
            if(isPlaying_alpha){
                playSound(ALPHA);
                isPlaying_alpha = false;
            }
            if(isPlaying_beta) {
                playSound(BETA);
                isPlaying_beta = false;
            }
            play = false;
        }catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void playSound(int type) throws IOException {
       // Log.e("status", "play : " + isPlaying + "  " + type + " : start");
        play = false;
        button_alpha.setEnabled(false);

        if (type == ALPHA) {
            if(isPlaying_beta){
                media_player.stop();
                isPlaying_beta = false;
                button_alpha.setIcon(ic_play);
                button_beta.setIcon(ic_play);
            }
            else if(isPlaying_alpha) {
                media_player.stop();
                isPlaying_alpha = false;
                button_alpha.setIcon(ic_play);
            }else{
                media_player = MediaPlayer.create(this, R.raw.alpha);
                media_player.setLooping(true);
                isPlaying_alpha = true;
                media_player.start();
                button_alpha.setIcon(ic_stop);
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
            if(isPlaying_alpha){
                media_player.stop();
                isPlaying_alpha = false;
                button_beta.setIcon(ic_play);
                button_alpha.setIcon(ic_play);
            }
            else if(isPlaying_beta) {
                media_player.stop();
                isPlaying_beta = false;
                button_beta.setIcon(ic_play);
            }else{
                media_player = MediaPlayer.create(this, R.raw.beta);
                media_player.setLooping(true);
                isPlaying_beta = true;
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

    public void selectBluetoothDevice() {
        devices = bluetoothAdapter.getBondedDevices();
        pariedDeviceCount = devices.size();
        if(pariedDeviceCount == 0) {
            // 페어링을 하기위한 함수 호출
        }

        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("페어링 되어있는 블루투스 디바이스 목록");
            List<String> list = new ArrayList<>();
            for(BluetoothDevice bluetoothDevice : devices) {
                list.add(bluetoothDevice.getName());
            }
            list.add("취소");


            final CharSequence[] charSequences = list.toArray(new CharSequence[list.size()]);
            list.toArray(new CharSequence[list.size()]);

            builder.setItems(charSequences, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    connectDevice(charSequences[which].toString());
                }
            });

            builder.setCancelable(false);

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void connectDevice(final String deviceName) {

        new Thread(new Runnable(){

            public void run(){
                for(BluetoothDevice tempDevice : devices) {
                    if(deviceName.equals(tempDevice.getName())) {
                        bluetoothDevice = tempDevice;
                        break;
                    }
                }

                //bluetoothAdapter.enable();
                Log.e("name : " + bluetoothDevice.getName(),"" + bluetoothDevice.getUuids()[1] + "   " + bluetoothDevice.getUuids().length);
                UUID uuid = UUID.fromString(String.valueOf(bluetoothDevice.getUuids()[1]));
                try {
                    //bluetoothDevice.createBond();

                    //bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                    //bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);
                    bluetoothAdapter.cancelDiscovery();
                    bluetoothSocket = createBluetoothSocket(bluetoothDevice, bluetoothDevice.getUuids()[0].getUuid());

                    bluetoothSocket.connect();
                    bluetoothSocket.getOutputStream();
                    Log.e("isconnected",""+bluetoothSocket.isConnected());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }).start();
        new Thread(new Runnable(){
            public void run(){
            }
        }).start();
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device, UUID mMyUuid) throws IOException {
        if(Build.VERSION.SDK_INT >= 10){
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
                return (BluetoothSocket) m.invoke(device, mMyUuid);
            } catch (Exception e) {
                Log.e("createBluetoothSocket", "Could not create Insecure RFComm Connection",e);
            }
        } return device.createRfcommSocketToServiceRecord(mMyUuid);
    }

}