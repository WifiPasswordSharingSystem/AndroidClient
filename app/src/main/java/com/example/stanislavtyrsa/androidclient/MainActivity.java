package com.example.stanislavtyrsa.androidclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Serebrennikov132 on 12.10.17.
 * Classes by Khubedjev
 */

public class MainActivity extends AppCompatActivity {

    private class AsyncClient implements Runnable {
        
        private Socket socket;

        private String address;

        private int port;

        private InputStream is;

        private OutputStream os;

        public void setAddress(String address) {
            this.address = address;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public void connect() throws IOException {
            socket = new Socket(address,port);
            os = socket.getOutputStream();
            is = socket.getInputStream();
        }

        @Override
        public void run() {
            try{
                while (true){
                    byte [] data = new byte[512];
                    is.read(data);
                    JSONObject parser = new JSONObject(new String(data).trim());
                    String message = parser.getString("Command");
                    switch (message){
                        case "ERROR":
                            String textError = parser.getString("TEXT");
                            break;
                        case "ACCEPT":
                            String status = parser.getString("STATUS");
                            switch (status){
                                case "DONE":{
                                    String ssid = parser.getString("SSID");
                                    String password = parser.getString("PASSWORD");
                                }
                                break;
                            }
                            break;
                    }
                }
            }catch (Exception ex){
                System.err.println(ex.getMessage());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        startAsyncTask();
        startWaitWindow();
    }

    private void startAsyncTask() {
        Thread thead = new Thread(new AsyncClient());
        thead.start();
    }

    void startWaitWindow(){
        Intent intent = new Intent(this, WaitActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        this.startActivity(intent);
    }
    void stopWaitWindow(){
        Intent intent = new Intent(this, WaitActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("finish",true);
        this.startActivity(intent);
    }
}
