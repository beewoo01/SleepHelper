package com.android.sleephelper;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
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

    private final String TAG = "MainActivity";

    private String ve = "2020-11-03";
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

    private Messenger mServiceMessenger = null;

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


        startService(new Intent(MainActivity.this, AudioService.class));
        bindService(new Intent(this, AudioService.class), mConnection, Context.BIND_AUTO_CREATE);

        button_alpha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessageToService("ALPHA");
            }
        });

        button_beta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessageToService("BETA");
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

        ButtonUpdate();

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

    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d("test","onServiceConnected");
            mServiceMessenger = new Messenger(iBinder);
            try {
                Message msg = Message.obtain(null, AudioService.MSG_REGISTER_CLIENT, "connection");
                msg.replyTo = mMessenger;
                mServiceMessenger.send(msg);
            }
            catch (RemoteException e) {
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    /** Service 로 부터 message를 받음 */
    private final Messenger mMessenger = new Messenger(new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.i(TAG,"act : what "+msg.what);
            switch (msg.what) {
                case AudioService.MSG_SEND_TO_ACTIVITY:
                    int value1 = msg.getData().getInt("fromService");
                    if(value1 == 1){
                        ButtonUpdate();
                    }
                    Log.i("test","act : value1 "+value1);
                    break;
            }
            return false;
        }
    }));

    /** Service 로 메시지를 보냄 */
    private void sendMessageToService(String str) {
        if (App.audioServiceBound) {
            if (mServiceMessenger != null) {
                try {
                    Message msg = Message.obtain(null, AudioService.MSG_SEND_TO_SERVICE, str);
                    msg.replyTo = mMessenger;
                    mServiceMessenger.send(msg);
                } catch (RemoteException e) {
                }
            }
        }
    }

    private void ButtonUpdate(){
        if(App.isPlaying_alpha)
            button_alpha.setIcon(ic_stop);
        else
            button_alpha.setIcon(ic_play);
        if(App.isPlaying_beta)
            button_beta.setIcon(ic_stop);
        else
            button_beta.setIcon(ic_play);

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