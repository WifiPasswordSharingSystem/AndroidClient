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

import in.ishankhanna.tinglingsquares.TinglingSquaresView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        final TinglingSquaresView tsv = (TinglingSquaresView) findViewById(R.id.tinglingSquaresView);
        tsv.runAnimation(0);
        
    }


}
