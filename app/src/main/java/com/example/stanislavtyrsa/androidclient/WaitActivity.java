package com.example.stanislavtyrsa.androidclient;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import in.ishankhanna.tinglingsquares.TinglingSquaresView;

public class WaitActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        final TinglingSquaresView tsv = (TinglingSquaresView) findViewById(R.id.tinglingSquaresView);
        tsv.runAnimation(0);
    }
    @Override
    protected void onNewIntent (Intent i){
        if( i.getBooleanExtra("finish",false) ){
            finish();
        }
    }
    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}
