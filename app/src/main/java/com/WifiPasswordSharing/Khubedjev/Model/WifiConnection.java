package com.WifiPasswordSharing.Khubedjev.Model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


/**
 * Created by stanislavtyrsa on 04.10.17.
 */
public class WifiConnection {

    private static WifiConnection connection;

    private WifiManager wifiManager;

    private WifiConnection(){}

    public static WifiConnection getInstance() {
        if(connection == null)
            connection = new WifiConnection();
        return connection;
    }

    public  boolean connectToNetworkWPA( String networkSSID, String passwordMD5 )
    {
        try {
            String password = DigestUtils.md5Hex(passwordMD5);
            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + networkSSID + "\"";   // Please note the quotes. String should contain SSID in quotes

            conf.preSharedKey = "\"" + password + "\"";

            conf.status = WifiConfiguration.Status.ENABLED;
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);

            Log.d("connecting", conf.SSID + " " + conf.preSharedKey);

            wifiManager.addNetwork(conf);

            Log.d("after connecting", conf.SSID + " " + conf.preSharedKey);



            List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
            for( WifiConfiguration i : list ) {
                if(i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                    wifiManager.disconnect();
                    wifiManager.enableNetwork(i.networkId, true);
                    wifiManager.reconnect();
                    Log.d("re connecting", i.SSID + " " + conf.preSharedKey);
                    break;
                }
            }


            //WiFi Connection success, return true
            return true;
        } catch (Exception ex) {
            System.out.println(Arrays.toString(ex.getStackTrace()));
            return false;
        }
    }
    public boolean connectToNetworkWEP( String networkSSID, String passwordMD5 )
    {
        try {
            String password = DigestUtils.md5Hex(passwordMD5);
            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + networkSSID + "\"";   // Please note the quotes. String should contain SSID in quotes
            conf.wepKeys[0] = "\"" + password + "\""; //Try it with quotes first

            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            conf.allowedGroupCiphers.set(WifiConfiguration.AuthAlgorithm.OPEN);
            conf.allowedGroupCiphers.set(WifiConfiguration.AuthAlgorithm.SHARED);

            int networkId = wifiManager.addNetwork(conf);

            if (networkId == -1){
                //Try it again with no quotes in case of hex password
                conf.wepKeys[0] = password;
                networkId = wifiManager.addNetwork(conf);
            }

            List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
            for( WifiConfiguration i : list ) {
                if(i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                    wifiManager.disconnect();
                    wifiManager.enableNetwork(i.networkId, true);
                    wifiManager.reconnect();
                    break;
                }
            }

            //WiFi Connection success, return true
            return true;
        } catch (Exception ex) {
            System.out.println(Arrays.toString(ex.getStackTrace()));
            return false;
        }
    }
    public JSONArray lookupWifi(){
        JSONArray array = new JSONArray();
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        for(WifiConfiguration wifi : configuredNetworks){
            JSONObject object = new JSONObject();
            try {
                object.put("SSID", wifi.BSSID);
                object.put("PASSWORD", wifi.wepKeys[0]);
                array.put(object);
            } catch (JSONException e) {
                System.err.println("Error in WifiConnection.lookupWifi");
                e.printStackTrace();
            }
        }
        return array;
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
