package com.example.stanislavtyrsa.androidclient;

import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.WifiPasswordSharing.Khubedjev.Model.WifiConnection;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        readPasswords();
    }
    public  List<HashMap<String, String>> readPasswords() {
        Process p2= null;
        HashMap<String, String> map;
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        try {
            //obtain root rights
            p2 = Runtime.getRuntime().exec("su");
            DataOutputStream d=new DataOutputStream(p2.getOutputStream());
            //Hack to read the contents of wpa_supplicant.conf
            d.writeBytes("cat /data/misc/wifi/wpa_supplicant.conf\n");
            d.writeBytes("exit\n");
            d.flush();

            //parse the wpa_supplicant.conf
            DataInputStream is = new DataInputStream(p2.getInputStream());
            String line = is.readLine();
            while(line != null) {
                if(line.contains("network")){
                    line = is.readLine();
                    map = new HashMap<String, String>();
                    while(line != null && !line.contains("}")){
                        line = line.replace("\n", "").replace("\r", "").replace("\t", "").replace("\"", "");
                        if(line.matches(".+=.+")){
                            map.put(line.substring(0, line.indexOf("=")), line.substring(line.indexOf("=") + 1, line.length()));
                        }
                        line = is.readLine();
                    }
                    list.add(map);
                }
                line = is.readLine();
            } // end parse
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

}
