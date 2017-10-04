package com.WifiPasswordSharing.Khubedjev.Model;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
/**
 * Created by Khubedjev on 04.10.17.
 */
public class AsyncClient implements Runnable {

    private AsyncClient instance;

    private Socket socket;

    private String address;

    private int port;

    private InputStream is;

    private OutputStream os;

    private AsyncClient(){

    }

    public AsyncClient getInstance(){
        if(instance == null){
            instance = new AsyncClient();
        }
        return instance;
    }

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
