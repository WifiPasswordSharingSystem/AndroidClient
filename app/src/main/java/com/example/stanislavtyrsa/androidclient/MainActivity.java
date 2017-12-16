package com.example.stanislavtyrsa.androidclient;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.WifiPasswordSharing.Khubedjev.Model.WifiConnection;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Serebrennikov132 on 12.10.17.
 * Classes by Khubedjev
 */

public class MainActivity extends AppCompatActivity {
    private WifiConnection connection = WifiConnection.getInstance();

    private Timer mTimer;

    private ListView list;

    private OutputStream os;

    private class AsyncClient implements Runnable {
        
        private Socket socket;

        private String address;

        private int port;

        private InputStream is;


        public void setAddress(String address) {
            this.address = address;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public AsyncClient() {
            port = 2222;
            address = "192.168.0.62";
            try {
                connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                                    connection.connectToNetworkWPA(ssid, DigestUtils.md5Hex(password));
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
        list = (ListView) findViewById(R.id.mainListView);

        JSONArray jsonArray = connection.lookupWifi();
        List<String> wifi_list = new ArrayList<String>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject tmp = (JSONObject) jsonArray.get(i);
                String name = (String) tmp.get("SSID");
                wifi_list.add(name);
            } catch (JSONException e) {}
        }

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, wifi_list);

        list.setAdapter(arrayAdapter);

        //arrayAdapter.notifyDataSetChanged();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) list.getItemAtPosition(position);
                String password = null,message = "Неизвестная ошибка =)";
                boolean found = false;
                JSONArray jsonArray = connection.lookupWifi();
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        JSONObject send = new JSONObject();
                        send.put("Command", "ADD");
                        JSONObject jsonItem = (JSONObject) jsonArray.get(i);
                        send.put("SSID", jsonItem.get("SSID"));
                        send.put("PASSWORD",jsonItem.get("PASSWORD"));
                        os.write(send.toString().getBytes());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                List<String> wifi_list = new ArrayList<String>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        JSONObject tmp = (JSONObject) jsonArray.get(i);
                        String name = (String) tmp.get("SSID");
                        if(name.equals(item)){
                            found = true;
                            password = (String) tmp.get("PASSWORD");
                        }
                    } catch (JSONException e) {}
                }
                if(found){
                    boolean b = connection.connectToNetworkWPA(item, DigestUtils.md5Hex(password));
                    if(b)
                        message = "Подключено к " + item;
                    else
                        message = "Ошибка подключения к " + item;
                }
                else{
                    JSONObject query = new JSONObject();
                    try {
                        query.put("Command", "GET");
                        query.put("SSID", item);
                        os.write(query.toString().getBytes());
                        os.flush();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("WIFI")
                        .setMessage(message)
                        .setCancelable(false)
                        .setNegativeButton("ОК",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
        startAsyncTask();
        startWaitWindow();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                    stopWaitWindow();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
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
