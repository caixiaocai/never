package com.anker.autotest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(ActivityCompat.checkSelfPermission(this,"android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }
    }

    public void click_btn(View view) throws IOException {
        String cmd = "nohup am instrument -w -r -e debug false -e class 'com.anker.autotest.soundcore.Flare2OTATest' com.anker.autotest.test/androidx.test.runner.AndroidJUnitRunner >/sdcard/nohup.out";
        Process p = Runtime.getRuntime().exec(cmd);

        String data = "";
        BufferedReader ie = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String error;

        while ((error = ie.readLine()) != null
                && !error.equals("null")) {
            data += error + "\n";
        }

        String line;
        while ((line = in.readLine()) != null
                && !line.equals("null")) {
            data += line + "\n";
        }

    }
}
