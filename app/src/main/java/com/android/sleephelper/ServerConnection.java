package com.android.sleephelper;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ServerConnection extends AsyncTask<String, Void, String> {
    public static URL url;

    String sendMsg, receiveMsg = "error";
    final String TAG = "ServerConnection";
    @Override
    protected String doInBackground(String...  strings) {
        try {
            String str;
            URL url = new URL(strings[1]);
            HttpURLConnection conn = null;
            if(strings[0].equals("mobile")){
                conn = (HttpURLConnection) App.net_mobile.openConnection(url);
            }else{
                conn = (HttpURLConnection) App.net_wifi.openConnection(url);
            }
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestMethod("POST");
            OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());


            sendMsg = "data=" + strings[2];
            Log.e(TAG,"Send : " + sendMsg);
            osw.write(sendMsg);
            osw.flush();
            if(conn.getResponseCode() == conn.HTTP_OK) {
                InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                BufferedReader reader = new BufferedReader(tmp);
                StringBuffer buffer = new StringBuffer();
                while ((str = reader.readLine()) != null) {
                    buffer.append(str);
                }
                receiveMsg = buffer.toString();
                Log.e(TAG,"Receive : " + receiveMsg);
            }else {
                Log.e("통신 결과", conn.getResponseCode()+"에러");
            }
        } catch (MalformedURLException e) { e.printStackTrace(); }
        catch (IOException e) { e.printStackTrace(); }
        return receiveMsg;
    }

    public void setUrl(String url){
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            Log.e(TAG, "URL error");
            e.printStackTrace();
        }
    }
    public String getUrl(){
        return url.toString();
    }




}
