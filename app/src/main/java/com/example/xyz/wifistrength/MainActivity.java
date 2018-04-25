package com.example.xyz.wifistrength;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    WifiManager wifiManager;
    WifiInfo wifiInfo;
    boolean navigate = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

    }
    public void onStart(){
        super.onStart();
        onClickSetValue(findViewById(R.id.cardView));

    }
    public void onClickNavigate(View view){
        startActivity(new Intent(this,GraphActivity.class));
    }
    public void onClickSetValue(View view){
        TextView status=findViewById(R.id.wifiStatus);
        TextView name=findViewById(R.id.wifiName);
        ImageView iview=findViewById(R.id.navigateImage);
        wifiInfo = wifiManager.getConnectionInfo();
        if(wifiInfo.getSupplicantState()==SupplicantState.COMPLETED){
            status.setText("You Are Connected");
            status.setVisibility(View.VISIBLE);
            name.setText(wifiInfo.getSSID());
            iview.setVisibility(View.VISIBLE);
        }else{
            status.setVisibility(View.INVISIBLE);
            name.setText("Wifi Not Connected! Click To Enable and Scan Results ");
            iview.setVisibility(View.INVISIBLE);

        }


    }

    public void onClickGotoScan(View view) {
        if (navigate) {
            navigate = false;
            startActivity(new Intent(this, ScanResults.class));
        } else {
            navigate = true;
        }

    }
}
