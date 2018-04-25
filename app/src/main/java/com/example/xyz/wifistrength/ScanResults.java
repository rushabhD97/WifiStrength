package com.example.xyz.wifistrength;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ScanResults extends AppCompatActivity {
    WifiManager wifiManager;
    ArrayList<String> displayList;
    ListView listView;
    ArrayAdapter arrayAdapter;
    File contentFile, file;
    OutputStream outputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        setContentView(R.layout.activity_scan_results);
        file = new File(getExternalFilesDir(null), "WifiStrength");
        contentFile = new File(file, "info.txt");
        checkFileExists();
        getSupportActionBar().setTitle("Scan Results");


        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        setContentView(R.layout.activity_scan_results);
        displayList = new ArrayList<String>();
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, displayList);
        ((Button) findViewById(R.id.refreshButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanWifi();
            }
        });

        listView = findViewById(R.id.listView);
        listView.setAdapter(arrayAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        scanWifi();
    }

    void scanWifi() {
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            Toast.makeText(this, "Wifi Enabled", Toast.LENGTH_SHORT).show();
        }

        displayList.clear();
        registerReceiver(new BroadcastReceiver() {
            List<ScanResult> scanResults;

            @Override
            public void onReceive(Context context, Intent intent) {
                scanResults = wifiManager.getScanResults();
                int size = scanResults.size();
                String text = "*******Scan Results********";
                checkFileExists();
                try {
                    while (--size >= 0) {
                        String val = "SSID: " + scanResults.get(size).SSID + " Strength: " + scanResults.get(size).level;
                        displayList.add(val);
                        text = text + "\n" + val;
                        arrayAdapter.notifyDataSetChanged();
                    }
                    text += "\n";
                    outputStream.write(text.getBytes());
                    unregisterReceiver(this);
                    Toast.makeText(context, "Scan Completed & Written in File", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        Toast.makeText(this, "Refreshing Scan", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        }
    }

    private void checkFileExists() {
        if (!file.exists()) {
            file.mkdir();
            try {
                contentFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!contentFile.exists()) {
            try {
                contentFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            outputStream = new FileOutputStream(contentFile, true);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void onClickManageFile(View view) {
        switch (view.getId()) {
            case R.id.openFileButton:
                Intent openFileIntent = new Intent(Intent.ACTION_VIEW);
                openFileIntent.setDataAndType(Uri.fromFile(contentFile), "text/plain");
                startActivity(Intent.createChooser(openFileIntent, "Open File Using...:p"));
                break;
            case R.id.deleteFileButton:
                new AlertDialog.Builder(this).setTitle("Delete File").setMessage("Are you sure you want to delete file")
                        .setCancelable(false)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (contentFile.exists()) {
                                    contentFile.delete();
                                    Toast.makeText(ScanResults.this, "File deleted", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ScanResults.this, "File Does not exist", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        }).create().show();
                break;

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
