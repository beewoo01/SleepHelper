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
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.github.nikartm.button.FitButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {
    Context activity;

    private BluetoothAdapter bluetoothAdapter; // 블루투스 어댑터
    private Set<BluetoothDevice> devices; // 블루투스 디바이스 데이터 셋
    private BluetoothDevice bluetoothDevice; // 블루투스 디바이스
    private BluetoothSocket bluetoothSocket = null; // 블루투스 소켓
    int pariedDeviceCount;




    final int ALPHA = 1000;
    final int BETA = 1001;
    MediaPlayer media_player;
    FitButton button_alpha, button_beta;
    private boolean isPlaying_alpha = false, isPlaying_beta = false;
    private AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;

        button_alpha = findViewById(R.id.main_BT_alpha);
        button_beta = findViewById(R.id.main_BT_beta);

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
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "블루투스 미지원 기기!",Toast.LENGTH_LONG);
            finish();
        } else {
            if (bluetoothAdapter.isEnabled()) {

            } else {
                Toast.makeText(this, "블루투스를 켜주세요!",Toast.LENGTH_LONG);
                Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intent);
            }
        }
    }


    private void playSound(int type) throws IOException {

       // Log.e("status", "play : " + isPlaying + "  " + type + " : start");
        if (type == ALPHA) {
            if(isPlaying_beta){
                media_player.stop();
                isPlaying_beta = false;
            }
            if(isPlaying_alpha) {
                media_player.stop();
                isPlaying_alpha = false;
            }else{
                media_player = MediaPlayer.create(this, R.raw.alpha);
                media_player.setLooping(true);
                media_player.prepare();
                isPlaying_alpha = true;
                media_player.start();
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        (int) (audioManager.getStreamMaxVolume(audioManager.STREAM_MUSIC) * 1),
                        audioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            }

        } else if (type == BETA) {
            if(isPlaying_alpha){
                media_player.stop();
                isPlaying_alpha = false;
            }
            if(isPlaying_beta) {
                media_player.stop();
                isPlaying_beta = false;
            }else{
                media_player = MediaPlayer.create(this, R.raw.beta);
                media_player.setLooping(true);
                media_player.prepare();
                isPlaying_beta = true;
                media_player.start();
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        (int) (audioManager.getStreamMaxVolume(audioManager.STREAM_MUSIC) * 0.5),
                        audioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            }
        }
    }

    public void selectBluetoothDevice() {
        devices = bluetoothAdapter.getBondedDevices();
        // 페어링 된 디바이스의 크기를 저장
        pariedDeviceCount = devices.size();
        // 페어링 되어있는 장치가 없는 경우
        if(pariedDeviceCount == 0) {
            // 페어링을 하기위한 함수 호출
        }
        // 페어링 되어있는 장치가 있는 경우
        else {
            // 디바이스를 선택하기 위한 다이얼로그 생성
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("페어링 되어있는 블루투스 디바이스 목록");
            // 페어링 된 각각의 디바이스의 이름과 주소를 저장
            List<String> list = new ArrayList<>();
            // 모든 디바이스의 이름을 리스트에 추가
            for(BluetoothDevice bluetoothDevice : devices) {
                list.add(bluetoothDevice.getName());
            }
            list.add("취소");


            // List를 CharSequence 배열로 변경
            final CharSequence[] charSequences = list.toArray(new CharSequence[list.size()]);
            list.toArray(new CharSequence[list.size()]);

            // 해당 아이템을 눌렀을 때 호출 되는 이벤트 리스너
            builder.setItems(charSequences, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 해당 디바이스와 연결하는 함수 호출
                    connectDevice(charSequences[which].toString());
                }
            });

            // 뒤로가기 버튼 누를 때 창이 안닫히도록 설정
            builder.setCancelable(false);
            // 다이얼로그 생성
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void connectDevice(String deviceName) {
        // 페어링 된 디바이스들을 모두 탐색
        for(BluetoothDevice tempDevice : devices) {
            // 사용자가 선택한 이름과 같은 디바이스로 설정하고 반복문 종료
            if(deviceName.equals(tempDevice.getName())) {
                bluetoothDevice = tempDevice;
                break;
            }
        }
        bluetoothDevice.createBond();
        Log.e("gg","" + bluetoothDevice.getUuids()[1] + "   " + bluetoothDevice.getUuids().length);
        // UUID 생성
        UUID uuid = java.util.UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        // Rfcomm 채널을 통해 블루투스 디바이스와 통신하는 소켓 생성
        try {

            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}